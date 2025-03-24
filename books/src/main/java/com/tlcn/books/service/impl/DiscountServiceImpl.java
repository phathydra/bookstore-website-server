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
public class DiscountServiceImpl implements IDiscountService {

    public final DiscountRepository discountRepository;
    public final BookDiscountRepository bookDiscountRepository;

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
    public void createDiscount(DiscountDto discountDto) {
        Discount discount = DiscountMapper.mapToDiscount(discountDto, new Discount());
        discountRepository.save(discount);
    }

    @Override
    public void addDiscountToBook(String bookId, String disCountId) {
        Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookIdAndDiscountId(bookId, disCountId);
        if(!bookDiscount.isPresent()){
            BookDiscount newBookDiscount= new BookDiscount();
            newBookDiscount.setBookId(bookId);
            newBookDiscount.setDiscountId(disCountId);
            bookDiscountRepository.save(newBookDiscount);
        }
    }

    @Override///////////////////////
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
            Discount newDiscount = DiscountMapper.mapToDiscount(discountDto, oldDiscount.get());
            discountRepository.save(newDiscount);
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
        discountRepository.deleteById(id);
    }

    @Override

    public void createBookDiscount(BookDiscountDto bookDiscountDto) {

        BookDiscount bookDiscount = new BookDiscount();

        bookDiscount.setBookId(bookDiscountDto.getBookId());

        bookDiscount.setDiscountId(bookDiscountDto.getDiscountId());

        bookDiscountRepository.save(bookDiscount);

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

}
