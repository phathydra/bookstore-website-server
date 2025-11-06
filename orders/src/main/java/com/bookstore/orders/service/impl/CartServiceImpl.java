package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.*;
import com.bookstore.orders.entity.Cart;
import com.bookstore.orders.entity.CartItem;
import com.bookstore.orders.entity.Combo;
import com.bookstore.orders.mapper.CartMapper;
import com.bookstore.orders.repository.CartRepository;
import com.bookstore.orders.repository.ComboRepository;
import com.bookstore.orders.service.ICartService;
// import com.bookstore.orders.service.client.BookDataClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ComboRepository comboRepository;
    private final BookDataClientService bookDataClientService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           CartMapper cartMapper,
                           ComboRepository comboRepository,
                           BookDataClientService bookDataClientService) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.comboRepository = comboRepository;
        this.bookDataClientService = bookDataClientService;
    }

    //
    // CÁC HÀM CŨ GIỮ NGUYÊN (createCart, addToCart, updateQuantity, removeItem)
    //
    @Override
    public ResponseEntity<?> createCart(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);
        if (cartOptional.isEmpty()) {
            Cart cart = new Cart();
            cart.setAccountId(accountId);
            cart.setCartItems(new ArrayList<>());
            cartRepository.save(cart);
            return ResponseEntity.ok("Giỏ hàng mặc định đã được tạo.");
        }
        return ResponseEntity.ok("Giỏ hàng đã tồn tại.");
    }

    @Override
    public ResponseEntity<?> addToCart(CartDto cartDto) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(cartDto.getAccountId());
        Cart cart = cartOptional.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setAccountId(cartDto.getAccountId());
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        for (CartItemDto cartItemDto : cartDto.getCartItems()) {
            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getBookId().equals(cartItemDto.getBookId()))
                    .findFirst();
            if (existingItem.isPresent()) {
                CartItem cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setBookId(cartItemDto.getBookId());
                newItem.setQuantity(cartItemDto.getQuantity());
                cart.getCartItems().add(newItem);
            }
        }
        cartRepository.save(cart);
        return ResponseEntity.ok("Giỏ hàng đã được cập nhật!");
    }

    @Override
    public ResponseEntity<?> updateQuantity(String accountId, String bookId, int quantity) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
                    .filter(item -> item.getBookId().equals(bookId))
                    .findFirst();
            if (cartItemOptional.isPresent()) {
                CartItem cartItem = cartItemOptional.get();
                if (quantity > 0) {
                    cartItem.setQuantity(quantity);
                } else {
                    cart.getCartItems().remove(cartItem);
                }
                cartRepository.save(cart);
                return ResponseEntity.ok("Số lượng sản phẩm đã được cập nhật!");
            }
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng hoặc sản phẩm!");
    }

    @Override
    public ResponseEntity<?> removeItem(String accountId, String bookId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getCartItems().removeIf(item -> item.getBookId().equals(bookId));
            cartRepository.save(cart);
            return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng!");
    }

    //
    // ===== HÀM getCartByAccountId (LOGIC MỚI ĐÃ ĐƯỢC HỢP NHẤT) =====
    //
    @Override
    public ResponseEntity<?> getCartByAccountId(String accountId) {
        // 1. Lấy giỏ hàng thô
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);
        if (cartOptional.isEmpty()) {
            Cart newCart = new Cart();
            newCart.setAccountId(accountId);
            newCart.setCartItems(new ArrayList<>());
            cartRepository.save(newCart);
            return ResponseEntity.ok(createEmptyCalculatedCart(newCart.getCartId(), accountId));
        }

        Cart cart = cartOptional.get();
        List<String> bookIdsInCart = cart.getCartItems().stream()
                .map(CartItem::getBookId)
                .toList();

        // 2. Gọi 'books' service để lấy dữ liệu sách MỚI NHẤT (bao gồm tags, author, category)
        Map<String, BookDataForCartDto> bookDataMap =
                bookDataClientService.getUpToDateBookData(bookIdsInCart);

        // 3. Bắt đầu tính toán
        CartResponseDto responseDto = new CartResponseDto();
        responseDto.setCartId(cart.getCartId());
        responseDto.setAccountId(cart.getAccountId());
        List<DiscountApplicationDto> appliedDiscounts = new ArrayList<>();
        double subtotal = 0.0;

        // 4. Tính toán Tạm tính
        List<CartItemResponseDto> responseItems = new ArrayList<>();
        for (CartItem savedItem : cart.getCartItems()) {
            BookDataForCartDto freshBookData = bookDataMap.get(savedItem.getBookId());
            if (freshBookData != null) {
                CartItemResponseDto resItem = new CartItemResponseDto();
                resItem.setBookId(savedItem.getBookId());
                resItem.setQuantity(savedItem.getQuantity());
                resItem.setBookName(freshBookData.getBookName());
                resItem.setBookImages(freshBookData.getBookImages());
                resItem.setOriginalPrice(freshBookData.getPrice());
                double lineTotal = freshBookData.getPrice() * savedItem.getQuantity();
                resItem.setLineItemTotal(lineTotal);
                responseItems.add(resItem);
                subtotal += lineTotal;
            }
        }

        // 5. ===== (LOGIC MỚI) TÍNH TOÁN COMBO (Cứng + Động) =====

        // 5.1. Lấy tất cả combo "Cứng" đang hoạt động
        LocalDateTime now = LocalDateTime.now();
        List<Combo> activeCombos = comboRepository
                .findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);

        List<CalculatedCombo> allValidCombos = new ArrayList<>();

        // 5.2. Tính toán các combo "Cứng" MÀ GIỎ HÀNG THỎA MÃN
        for (Combo combo : activeCombos) {
            boolean hasAllBooksForCombo = combo.getBookIds().stream()
                    .allMatch(bookIdsInCart::contains);

            if (hasAllBooksForCombo) {
                double discountAmount = 0.0;
                if (combo.getDiscountType() == Combo.DiscountType.FIXED_AMOUNT) {
                    discountAmount = combo.getDiscountValue();
                } else if (combo.getDiscountType() == Combo.DiscountType.PERCENT) {
                    double comboBooksPrice = responseItems.stream()
                            .filter(item -> combo.getBookIds().contains(item.getBookId()))
                            .mapToDouble(CartItemResponseDto::getLineItemTotal)
                            .sum();
                    discountAmount = (comboBooksPrice * combo.getDiscountValue()) / 100.0;
                }

                if (discountAmount > 0) {
                    allValidCombos.add(new CalculatedCombo(
                            combo.getName(),
                            discountAmount,
                            new HashSet<>(combo.getBookIds())
                    ));
                }
            }
        }

        // 5.3. (MỚI) Tự động tạo "Combo Động" (Theo Tác giả, Thể loại)
        // và thêm vào danh sách `allValidCombos` để thuật toán tự so sánh

        // A. Gom theo Tác giả (ví dụ: Mua >= 3 cuốn cùng tác giả, giảm 10%)
        Map<String, List<CartItemResponseDto>> itemsByAuthor = responseItems.stream()
                .filter(item -> bookDataMap.get(item.getBookId()) != null && bookDataMap.get(item.getBookId()).getBookAuthor() != null)
                .collect(Collectors.groupingBy(item -> bookDataMap.get(item.getBookId()).getBookAuthor()));

        for (Map.Entry<String, List<CartItemResponseDto>> entry : itemsByAuthor.entrySet()) {
            if (entry.getValue().size() >= 3) { // Điều kiện: 3 sách khác nhau
                Set<String> bookIds = entry.getValue().stream().map(CartItemResponseDto::getBookId).collect(Collectors.toSet());
                double comboBooksPrice = entry.getValue().stream().mapToDouble(CartItemResponseDto::getLineItemTotal).sum();
                double discountAmount = comboBooksPrice * 0.10; // Giảm 10%

                allValidCombos.add(new CalculatedCombo(
                        "Combo Tác giả: " + entry.getKey(),
                        discountAmount,
                        bookIds
                ));
            }
        }

        // B. Gom theo Thể loại chính (ví dụ: Mua >= 2 cuốn "Văn Học", giảm 15k)
        Map<String, List<CartItemResponseDto>> itemsByCategory = responseItems.stream()
                .filter(item -> bookDataMap.get(item.getBookId()) != null && bookDataMap.get(item.getBookId()).getMainCategory() != null)
                .collect(Collectors.groupingBy(item -> bookDataMap.get(item.getBookId()).getMainCategory()));

        for (Map.Entry<String, List<CartItemResponseDto>> entry : itemsByCategory.entrySet()) {
            // Ví dụ chỉ áp dụng cho "Văn Học"
            if (entry.getKey().equals("Văn Học") && entry.getValue().size() >= 2) {
                Set<String> bookIds = entry.getValue().stream().map(CartItemResponseDto::getBookId).collect(Collectors.toSet());
                double discountAmount = 15000.0; // Giảm 15k

                allValidCombos.add(new CalculatedCombo(
                        "Combo Thể loại: " + entry.getKey(),
                        discountAmount,
                        bookIds
                ));
            }
        }

        // 5.4. Tìm tổ hợp combo TỐT NHẤT (cả cứng và động)
        Map<String, DiscountApplicationDto> bestComboCombination =
                findOptimalCombination(allValidCombos);

        // 5.5. Áp dụng kết quả tốt nhất
        appliedDiscounts.addAll(bestComboCombination.values());
        double comboDiscountAmount = bestComboCombination.values().stream()
                .mapToDouble(DiscountApplicationDto::getAmount).sum();


        // 6. (MỚI) Áp dụng logic "Combo Mua Kèm" (dựa trên Tags)
        // (SỬA LỖI: Logic này được CỘNG DỒN, không cần kiểm tra sách đã dùng)
        double dynamicTagDiscountAmount = 0.0;

        // Lấy ra các sách "Hot" và "Tồn kho" (COLD_SELLER)
        List<CartItemResponseDto> hotItems = new ArrayList<>();
        List<CartItemResponseDto> coldItems = new ArrayList<>();

        for (CartItemResponseDto item : responseItems) {
            BookDataForCartDto bookData = bookDataMap.get(item.getBookId());
            if (bookData != null && bookData.getTags() != null) {
                if (bookData.getTags().contains("HOT_SELLER")) {
                    hotItems.add(item);
                }
                if (bookData.getTags().contains("COLD_SELLER")) {
                    coldItems.add(item);
                }
            }
        }

        // Ví dụ: Mua 1 sách HOT, được giảm 50% cho 1 sách COLD rẻ nhất
        if (!hotItems.isEmpty() && !coldItems.isEmpty()) {
            // Tìm cuốn sách "tồn kho" rẻ nhất để giảm giá
            CartItemResponseDto itemToDiscount = coldItems.stream()
                    .min(Comparator.comparing(CartItemResponseDto::getOriginalPrice))
                    .orElse(null);

            if (itemToDiscount != null) {
                // Giảm 50% cho 1 cuốn
                double discountPerItem = itemToDiscount.getOriginalPrice() * 0.5;

                // Số lượng sách HOT (tổng số lượng)
                int hotItemQuantity = hotItems.stream().mapToInt(CartItemResponseDto::getQuantity).sum();
                // Số lượng sách COLD (của cuốn rẻ nhất)
                int coldItemQuantity = itemToDiscount.getQuantity();

                // Số lượng được giảm tối đa là min(sl sách hot, sl sách cold)
                int discountQuantity = Math.min(hotItemQuantity, coldItemQuantity);

                double totalDiscountForThis = discountPerItem * discountQuantity;

                appliedDiscounts.add(new DiscountApplicationDto(
                        "Ưu đãi mua kèm (" + itemToDiscount.getBookName() + ")",
                        totalDiscountForThis
                ));
                dynamicTagDiscountAmount += totalDiscountForThis;
            }
        }

        // 7. Hoàn thành Response
        // (Xóa logic "expensiveDiscountAmount" cũ)
        double totalDiscountAmount = comboDiscountAmount + dynamicTagDiscountAmount;
        responseDto.setItems(responseItems);
        responseDto.setSubtotal(subtotal);
        responseDto.setAppliedDiscounts(appliedDiscounts);
        responseDto.setTotalDiscountAmount(totalDiscountAmount);

        double finalTotal = subtotal - totalDiscountAmount;
        responseDto.setTotal(finalTotal < 0 ? 0 : finalTotal);

        return ResponseEntity.ok(responseDto);
    }

    // Hàm helper để trả về giỏ hàng rỗng
    private CartResponseDto createEmptyCalculatedCart(String cartId, String accountId) {
        CartResponseDto dto = new CartResponseDto();
        dto.setCartId(cartId);
        dto.setAccountId(accountId);
        dto.setItems(new ArrayList<>());
        dto.setAppliedDiscounts(new ArrayList<>());
        dto.setSubtotal(0.0);
        dto.setTotalDiscountAmount(0.0);
        dto.setTotal(0.0);
        return dto;
    }

    //
    // ===== BỘ 3 HÀM LOGIC TỐI ƯU HÓA COMBO (MỚI) =====
    //

    /**
     * Lớp helper private để lưu trữ combo đã tính toán
     */
    private static class CalculatedCombo {
        private final String name;
        private final double amount;
        private final Set<String> bookIds;

        public CalculatedCombo(String name, double amount, Set<String> bookIds) {
            this.name = name;
            this.amount = amount;
            this.bookIds = bookIds;
        }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public Set<String> getBookIds() { return bookIds; }
    }

    /**
     * Hàm "wrapper" để bắt đầu thuật toán đệ quy.
     * Sắp xếp các combo (giảm dần theo số tiền) để ưu tiên
     * xét các combo "ngon" trước, giúp tăng hiệu quả.
     */
    private Map<String, DiscountApplicationDto> findOptimalCombination(List<CalculatedCombo> allValidCombos) {
        // Sắp xếp giảm dần theo số tiền discount
        allValidCombos.sort((c1, c2) -> Double.compare(c2.getAmount(), c1.getAmount()));

        // Bắt đầu đệ quy từ combo đầu tiên (index 0) với set item rỗng
        return solveRecursively(allValidCombos, 0, new HashSet<>());
    }

    /**
     * Hàm đệ quy (Backtracking)
     * Thử 2 lựa chọn cho mỗi combo: "Lấy" hoặc "Bỏ qua"
     * và trả về lựa chọn có tổng discount cao nhất.
     */
    private Map<String, DiscountApplicationDto> solveRecursively(
            List<CalculatedCombo> allValidCombos,
            int comboIndex,
            Set<String> itemsUsed) {

        // 1. Base Case: Đã xét hết tất cả combo
        if (comboIndex == allValidCombos.size()) {
            return new HashMap<>(); // Trả về bộ rỗng
        }

        CalculatedCombo currentCombo = allValidCombos.get(comboIndex);

        // 2. Lựa chọn 1: BỎ QUA (skip) combo này
        // Ta luôn có thể bỏ qua.
        Map<String, DiscountApplicationDto> skippedResult =
                solveRecursively(allValidCombos, comboIndex + 1, itemsUsed);

        double skippedDiscount = skippedResult.values().stream()
                .mapToDouble(DiscountApplicationDto::getAmount).sum();

        // 3. Lựa chọn 2: LẤY (take) combo này
        // Kiểm tra xem combo này có bị trùng item với các combo đã "lấy" trước đó không
        boolean canTake = true;
        for (String bookId : currentCombo.getBookIds()) {
            if (itemsUsed.contains(bookId)) {
                canTake = false;
                break;
            }
        }

        // 4. So sánh
        if (canTake) {
            // Nếu có thể "lấy", tạo set item mới
            Set<String> newItemsUsed = new HashSet<>(itemsUsed);
            newItemsUsed.addAll(currentCombo.getBookIds());

            // Gọi đệ quy cho các combo còn lại, với set item đã cập nhật
            Map<String, DiscountApplicationDto> takenResult =
                    solveRecursively(allValidCombos, comboIndex + 1, newItemsUsed);

            // Thêm combo hiện tại vào kết quả "lấy"
            takenResult.put(currentCombo.getName(),
                    new DiscountApplicationDto(currentCombo.getName(), currentCombo.getAmount()));

            double takenDiscount = takenResult.values().stream()
                    .mapToDouble(DiscountApplicationDto::getAmount).sum();

            // Trả về kết quả tốt hơn (tổng discount cao hơn)
            if (takenDiscount > skippedDiscount) {
                return takenResult;
            } else {
                return skippedResult;
            }

        } else {
            // 5. Không thể "lấy" (vì trùng), BẮT BUỘC "bỏ qua"
            return skippedResult;
        }
    }
}