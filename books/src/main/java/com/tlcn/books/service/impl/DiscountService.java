package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.mapper.DiscountMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.DiscountRepository;
import com.tlcn.books.service.IDiscountService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class DiscountService implements IDiscountService {

    public final DiscountRepository discountRepository;
    public final BookDiscountRepository bookDiscountRepository;

    @Override
    public BookDiscountDto getDiscountByBookId(int page, int size, String bookId) {
        Pageable pageable = PageRequest.of(page, size);

        return null;
    }

    @Override
    public BookDiscountDto getDiscountByDiscountId(int page, int size, String discountId) {
        Pageable pageable = PageRequest.of(page, size);
        return null;
    }

    @Override
    public Page<DiscountDto> getAllDiscount(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Discount> discounts = discountRepository.findAllBy(pageable);
        return discounts.map(discount -> DiscountMapper.mapToDiscountDto(discount, new DiscountDto()));
    }

    @Override
    public void createDiscount(DiscountDto discountDto) {
        Discount discount = DiscountMapper.mapToDiscount(discountDto, new Discount());
        discountRepository.save(discount);
    }

    @Override
    public void addDiscountToBook(String bookId, String disCountId) {
        BookDiscount bookDiscount= new BookDiscount();
        bookDiscount.setBookId(bookId);
        bookDiscount.setDiscountId(disCountId);
        bookDiscountRepository.save(bookDiscount);
    }

    @Override
    public void updateBookDiscount(String id, String newDiscountId) {
        Optional<BookDiscount> optionalBookDiscount = bookDiscountRepository.findById(id);

        if (optionalBookDiscount.isPresent()) {
            BookDiscount bookDiscount = optionalBookDiscount.get();
            bookDiscount.setDiscountId(newDiscountId);
            bookDiscountRepository.save(bookDiscount);
        } else {
            throw new RuntimeException("BookDiscount not found with id: " + id);
        }
    }

    @Override
    public void updateDiscount(String id, DiscountDto discountDto){
        Optional<Discount> oldDiscount = discountRepository.findById(id);
        if(oldDiscount.isPresent()){
            Discount newDiscount = DiscountMapper.mapToDiscount(discountDto, oldDiscount.get());
            discountRepository.save(newDiscount);
        } else {
            throw new RuntimeException("Discount not found with id: " + id);
        }
    }

    @Override
    public void deleteDiscountByBookId(String bookId) {
        bookDiscountRepository.deleteByBookId(bookId);
    }

    @Override
    public void deleteDiscountByDiscountId(String discountId) {
        bookDiscountRepository.deleteByDiscountId(discountId);
    }

    @Override
    public void deleteDiscount(String id) {
        discountRepository.deleteById(id);
    }
}
