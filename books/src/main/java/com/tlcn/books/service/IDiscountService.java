package com.tlcn.books.service;

import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.DiscountDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDiscountService {

    BookDiscountDto getDiscountByBookIdAndDiscountId(String bookId, String discountId);

    Page<DiscountDto> getAllDiscount(int page, int size);

    void createDiscount(DiscountDto discountDto);

    void addDiscountToBook(String bookId, String disCountId);

    void updateBookDiscount(String id, String newDiscountId);

    void updateDiscount(String id, DiscountDto discountDto);

    void deleteDiscountByBookIdAndDiscountId(String bookId, String discountId);

    void deleteDiscount(String id);

    void createBookDiscount(BookDiscountDto bookDiscountDto);

    List<BookDiscountDto> getBookDiscountsByDiscountId(String discountId);

    List<BookDiscountDto> getAllBookDiscounts();
}
