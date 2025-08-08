package com.tlcn.books.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.AppliedBookDto;
import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.fileIO.ApplyDiscountExcelImporter;
import com.tlcn.books.mapper.DiscountMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.DiscountRepository;
import com.tlcn.books.service.IDiscountService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    public void addDiscountToBooks(List<String> bookIds, String discountId) {
        List<BookDiscount> existBookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
        for(BookDiscount bookDiscount : existBookDiscounts){
            if(!bookIds.contains(bookDiscount.getBookId())){
                bookDiscountRepository.deleteByBookIdAndDiscountId(bookDiscount.getBookId(), discountId);
            }
        }

        for(String id : bookIds){
            Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookId(id);
            if(bookDiscount.isEmpty()){
                BookDiscount newBookDiscount = new BookDiscount();
                newBookDiscount.setBookId(id);
                newBookDiscount.setDiscountId(discountId);
                bookDiscountRepository.save(newBookDiscount);
            }
        }
    }

    @Override
    public void addDiscountToBooksUsingExcel(MultipartFile fileInput, String discountId){
        try{
            List<String> bookIds = ApplyDiscountExcelImporter.importAppliedBooks(fileInput.getInputStream());
            List<BookDiscount> existBookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
            for(BookDiscount bookDiscount : existBookDiscounts){
                if(!bookIds.contains(bookDiscount.getBookId())){
                    bookDiscountRepository.deleteByBookIdAndDiscountId(bookDiscount.getBookId(), discountId);
                }
            }

            for(String id : bookIds){
                Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookId(id);
                if(bookDiscount.isEmpty()){
                    BookDiscount newBookDiscount = new BookDiscount();
                    newBookDiscount.setBookId(id);
                    newBookDiscount.setDiscountId(discountId);
                    bookDiscountRepository.save(newBookDiscount);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
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

}
