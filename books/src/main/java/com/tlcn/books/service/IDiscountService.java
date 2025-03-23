package com.tlcn.books.service;

import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.DiscountDto;
import org.springframework.data.domain.Page;

public interface IDiscountService {

    BookDiscountDto getDiscountByBookId(int page, int size, String bookId);

    BookDiscountDto getDiscountByDiscountId(int page, int size, String discountId);

    Page<DiscountDto> getAllDiscount(int page, int size);

    void createDiscount(DiscountDto discountDto);

    void addDiscountToBook(String bookId, String disCountId);

    void updateBookDiscount(String id, String newDiscountId);

    void updateDiscount(String id, DiscountDto discountDto);

    void deleteDiscountByBookId(String bookId);

    void deleteDiscountByDiscountId(String discountId);

    void deleteDiscount(String id);
}
