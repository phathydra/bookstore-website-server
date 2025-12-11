package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.fileIO.ApplyDiscountExcelImporter;
import com.tlcn.books.mapper.BookMapper;
import com.tlcn.books.mapper.DiscountMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.repository.DiscountRepository;
import com.tlcn.books.service.IDiscountService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
@Service
@AllArgsConstructor
public class DiscountServiceImpl implements IDiscountService {

    public final DiscountRepository discountRepository;
    public final BookDiscountRepository bookDiscountRepository;
    public final BookRepository bookRepository;

    @Override
    public BookDiscountDto getDiscountByBookIdAndDiscountId(String bookId, String discountId) {
        Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookIdAndDiscountId(bookId, discountId);
        if(bookDiscount.isPresent()){
            BookDiscountDto bookDiscountDto = new BookDiscountDto();
            bookDiscountDto.setBookId(bookDiscount.get().getBookId());
            bookDiscountDto.setDiscountId(bookDiscount.get().getDiscountId());
            return bookDiscountDto;
        } else {
            throw new RuntimeException("BookDiscount not found");
        }
    }

    @Override
    public Page<DiscountDto> getAllDiscount(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Discount> discounts = discountRepository.findAllBy(pageable);
        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }

    @Override
    public Page<DiscountDto> getExpiredDiscount(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Discount> discounts = discountRepository.findByEndDateBefore(now, pageable);
        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }

    @Override
    public Page<DiscountDto> getActiveDiscount(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Discount> discounts = discountRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable);
        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }

    @Override
    public Page<DiscountDto> getUpcomingDiscount(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Discount> discounts = discountRepository.findByStartDateAfter(now, pageable);
        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }
    // ============================================================
    // UPDATED: Logic thêm giảm giá (có check hết hạn)
    // ============================================================

    @Override
    public List<BookDto> addDiscountToBooks(List<String> bookIds, String discountId) {
        // 1. Xóa sách khỏi discountId này nếu user bỏ chọn (Logic cũ giữ nguyên)
        List<BookDiscount> existBookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
        for(BookDiscount bookDiscount : existBookDiscounts){
            if(!bookIds.contains(bookDiscount.getBookId())){
                bookDiscountRepository.deleteByBookIdAndDiscountId(bookDiscount.getBookId(), discountId);
            }
        }

        // 2. Thêm sách vào discountId mới (Logic mới có check hết hạn)
        List<BookDto> failedBooks = new ArrayList<>(); // Danh sách sách KHÔNG thêm được (do kẹt mã còn hạn)

        for(String id : bookIds){
            boolean isSuccess = processAddDiscountToBook(id, discountId);

            if(!isSuccess) {
                // Nếu thất bại (tức là đang có mã KHÁC còn hạn), thêm vào list báo lỗi trả về
                Optional<Book> book = bookRepository.findByBookId(id);
                book.ifPresent(value -> failedBooks.add(BookMapper.mapToBookDto(value, new BookDto())));
            }
        }
        return failedBooks;
    }

    @Override
    public List<BookDto> addDiscountToBooksUsingExcel(MultipartFile fileInput, String discountId) {
        try {
            List<String> bookIds = ApplyDiscountExcelImporter.importAppliedBooks(fileInput.getInputStream());

            // 1. Xóa sách khỏi discountId này nếu không có trong excel
            List<BookDiscount> existBookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
            for (BookDiscount bookDiscount : existBookDiscounts) {
                if (!bookIds.contains(bookDiscount.getBookId())) {
                    bookDiscountRepository.deleteByBookIdAndDiscountId(bookDiscount.getBookId(), discountId);
                }
            }

            // 2. Thêm sách (Logic mới có check hết hạn)
            List<BookDto> addedBook = new ArrayList<>(); // Danh sách sách thêm THÀNH CÔNG

            for (String id : bookIds) {
                boolean isSuccess = processAddDiscountToBook(id, discountId);

                if (isSuccess) {
                    Optional<Book> book = bookRepository.findByBookId(id);
                    book.ifPresent(value -> addedBook.add(BookMapper.mapToBookDto(value, new BookDto())));
                }
            }
            return addedBook;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Helper Method: Xử lý logic kiểm tra và thêm mã giảm giá cho 1 cuốn sách
     * @return true nếu thêm thành công (hoặc đè mã cũ thành công), false nếu sách đang có mã khác còn hạn
     */
    private boolean processAddDiscountToBook(String bookId, String newDiscountId) {
        Optional<BookDiscount> existingBookDiscountOpt = bookDiscountRepository.findByBookId(bookId);

        // TH1: Sách chưa có mã nào -> Thêm mới luôn
        if(existingBookDiscountOpt.isEmpty()){
            saveNewBookDiscount(bookId, newDiscountId);
            return true;
        }

        // TH2: Sách đang có mã -> Cần kiểm tra xem mã đó hết hạn chưa
        BookDiscount existingBD = existingBookDiscountOpt.get();

        // Nếu mã cũ chính là mã đang định add vào -> Coi như thành công (không cần làm gì)
        if (existingBD.getDiscountId().equals(newDiscountId)) {
            return true;
        }

        // Kiểm tra hạn của mã cũ
        Optional<Discount> oldDiscountOpt = discountRepository.findById(existingBD.getDiscountId());

        if (oldDiscountOpt.isPresent()) {
            Discount oldDiscount = oldDiscountOpt.get();
            Date now = new Date();

            // Nếu mã cũ ĐÃ HẾT HẠN (endDate < now)
            if (oldDiscount.getEndDate().before(now)) {
                // Xóa mã cũ -> Thêm mã mới
                bookDiscountRepository.delete(existingBD);
                saveNewBookDiscount(bookId, newDiscountId);
                return true;
            } else {
                // Mã cũ VẪN CÒN HẠN -> Không được ghi đè -> Trả về false
                return false;
            }
        } else {
            // Data rác (có BookDiscount nhưng không tìm thấy Discount gốc) -> Xóa và thêm mới
            bookDiscountRepository.delete(existingBD);
            saveNewBookDiscount(bookId, newDiscountId);
            return true;
        }
    }

    private void saveNewBookDiscount(String bookId, String discountId) {
        BookDiscount newBookDiscount = new BookDiscount();
        newBookDiscount.setBookId(bookId);
        newBookDiscount.setDiscountId(discountId);
        bookDiscountRepository.save(newBookDiscount);
    }

    // ============================================================
    // END UPDATED Logic
    // ============================================================

    @Override
    public void updateBookDiscount(String id, String newDiscountId) {
        Optional<BookDiscount> optionalBookDiscount = bookDiscountRepository.findById(id);

        if (optionalBookDiscount.isPresent()) {
            BookDiscount bookDiscount = optionalBookDiscount.get();
            bookDiscount.setDiscountId(newDiscountId);
            bookDiscountRepository.save(bookDiscount);
        } else {
            throw new RuntimeException("BookDiscount not found");
        }
    }

    @Override
    public void updateDiscount(String id, DiscountDto discountDto){
        Optional<Discount> oldDiscount = discountRepository.findById(id);
        if(oldDiscount.isPresent()){
            Discount updatedDiscount = DiscountMapper.mapToDiscount(discountDto, oldDiscount.get());
            discountRepository.save(updatedDiscount);
        } else {
            throw new RuntimeException("Discount not found with id: " + id);
        }
    }

    @Override
    public void deleteDiscountByBookIdAndDiscountId(String bookId, String discountId) {
        bookDiscountRepository.deleteByBookIdAndDiscountId(bookId, discountId);
    }

    @Override
    public void deleteDiscount(String id) {
        bookDiscountRepository.deleteAllByDiscountId(id);
        discountRepository.deleteById(id);
    }

    @Override
    public void createBookDiscount(BookDiscountDto bookDiscountDto) {
        Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookIdAndDiscountId(bookDiscountDto.getBookId(), bookDiscountDto.getDiscountId());
        if(!bookDiscount.isPresent()){
            BookDiscount newBookDiscount= new BookDiscount();
            newBookDiscount.setBookId(bookDiscountDto.getBookId());
            newBookDiscount.setDiscountId(bookDiscountDto.getDiscountId());
            bookDiscountRepository.save(newBookDiscount);
        }
    }

    @Override
    public List<BookDiscountDto> getAllBookDiscounts() {
        List<BookDiscount> bookDiscounts = bookDiscountRepository.findAll();
        List<BookDiscountDto> bookDiscountDtos = new ArrayList<>();

        for (BookDiscount bookDiscount : bookDiscounts) {
            BookDiscountDto dto = new BookDiscountDto();
            dto.setBookId(bookDiscount.getBookId());
            dto.setDiscountId(bookDiscount.getDiscountId());
            bookDiscountDtos.add(dto);
        }

        return bookDiscountDtos;
    }

    @Override
    public List<BookDiscountDto> getBookDiscountsByDiscountId(String discountId) {
        List<BookDiscount> bookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
        List<BookDiscountDto> bookDiscountDtos = new ArrayList<>();

        for (BookDiscount bookDiscount : bookDiscounts) {
            BookDiscountDto dto = new BookDiscountDto();
            dto.setBookId(bookDiscount.getBookId());
            dto.setDiscountId(bookDiscount.getDiscountId());
            bookDiscountDtos.add(dto);
        }
        return bookDiscountDtos;
    }

    @Override
    public Page<DiscountDto> getActiveFlashSales(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();

        // Chỉ lấy những cái có type là "FLASH_SALE" và đang trong thời gian hiệu lực
        Page<Discount> discounts = discountRepository.findByTypeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                "FLASH_SALE", now, now, pageable);

        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }

    // Lưu ý: Cập nhật hàm createDiscount để map thêm trường type
    @Override
    public void createDiscount(DiscountDto discountDto) {
        Discount discount = DiscountMapper.mapToDiscount(discountDto, new Discount());

        // Nếu user không gửi type, mặc định là NORMAL
        if (discount.getType() == null || discount.getType().isEmpty()) {
            discount.setType("NORMAL");
        }

        discountRepository.save(discount);
    }

    // Trong DiscountServiceImpl.java

    @Override
    public Page<BookDto> getBooksInActiveFlashSales(int page, int size) {
        Date now = new Date();

        // 1. Tìm các đợt Flash Sale đang chạy
        List<Discount> activeFlashSales = discountRepository.findByTypeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                "FLASH_SALE", now, now
        );

        if (activeFlashSales.isEmpty()) {
            return Page.empty();
        }

        // 2. Lấy danh sách Discount ID
        List<String> discountIds = activeFlashSales.stream()
                .map(Discount::getId)
                .toList();

        // 3. Tìm BookDiscount liên quan
        List<BookDiscount> bookDiscounts = bookDiscountRepository.findByDiscountIdIn(discountIds);
        List<String> bookIds = bookDiscounts.stream()
                .map(BookDiscount::getBookId)
                .toList();

        // --- KHẮC PHỤC LỖI Ở ĐÂY ---
        // 4. Tạo đối tượng Pageable từ page và size
        Pageable pageable = PageRequest.of(page, size);

        // 5. Gọi Repository (Lưu ý tên hàm là findByBookIdIn như đã sửa ở bước trước)
        Page<Book> books = bookRepository.findByBookIdIn(bookIds, pageable);

        return books.map(book -> BookMapper.mapToBookDto(book, new BookDto()));
    }
}