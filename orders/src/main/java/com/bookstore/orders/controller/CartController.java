package com.bookstore.orders.controller;

import com.bookstore.orders.dto.CartDto;
import com.bookstore.orders.dto.CartItemDto;
import com.bookstore.orders.entity.Cart;
import com.bookstore.orders.entity.CartItem;
import com.bookstore.orders.repository.CartRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // Tạo giỏ hàng mặc định cho account nếu chưa có
    @PostMapping("/create/{accountId}")
    public ResponseEntity<?> createCart(@PathVariable String accountId) {
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

    // Lấy giỏ hàng theo accountId
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getCartByAccountId(@PathVariable String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            List<CartItemDto> cartItemDtos = cart.getCartItems().stream()
                    .map(cartItem -> new CartItemDto(
                            cartItem.getBookId(),
                            cartItem.getBookName(),
                            cartItem.getBookImage(),
                            cartItem.getQuantity(), // Đây có thể bị sai thứ tự
                            cartItem.getPrice()))  // Kiểm tra thứ tự của quantity & price
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new CartDto(cart.getAccountId(), cartItemDtos));
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng!");
    }


    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartDto cartDto) {
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
                newItem.setBookName(cartItemDto.getBookName());
                newItem.setBookImage(cartItemDto.getBookImage()); // Lưu ảnh sách
                newItem.setPrice(cartItemDto.getPrice());
                newItem.setQuantity(cartItemDto.getQuantity());
                cart.getCartItems().add(newItem);
            }
        }

        cartRepository.save(cart);
        return ResponseEntity.ok("Giỏ hàng đã được cập nhật!");
    }


    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/update/{accountId}/{bookId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable String accountId,
            @PathVariable String bookId,
            @RequestParam int quantity) {

        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            List<CartItem> cartItems = cart.getCartItems();

            Optional<CartItem> cartItemOptional = cartItems.stream()
                    .filter(item -> item.getBookId().equals(bookId))
                    .findFirst();

            if (cartItemOptional.isPresent()) {
                CartItem cartItem = cartItemOptional.get();
                if (quantity > 0) {
                    cartItem.setQuantity(quantity);
                } else {
                    cartItems.remove(cartItem);
                }
                cartRepository.save(cart);
                return ResponseEntity.ok("Số lượng sản phẩm đã được cập nhật!");
            }
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng hoặc sản phẩm!");
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{accountId}/{bookId}")
    public ResponseEntity<?> removeItem(@PathVariable String accountId, @PathVariable String bookId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            List<CartItem> cartItems = cart.getCartItems();
            cartItems.removeIf(item -> item.getBookId().equals(bookId));
            cartRepository.save(cart);
            return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng!");
    }
}
