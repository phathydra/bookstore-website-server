package com.tlcn.books.controller;

import com.tlcn.books.Constants.BookConstants;
import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.dto.ResponseDto;
import com.tlcn.books.service.IDiscountService;
import com.tlcn.books.service.impl.DiscountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController()
@RequestMapping("/api/discounts")
public class DiscountController {
    @Autowired
    private IDiscountService iDiscountService;

    @GetMapping("")
    public ResponseEntity<Page<DiscountDto>> getAllDiscount(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size){
        try {
            Page<DiscountDto> discountDtos = iDiscountService.getAllDiscount(page, size);
            return ResponseEntity.ok(discountDtos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @PostMapping("")
    public ResponseEntity<ResponseDto> createDiscount(@Valid @RequestBody DiscountDto discountDto){
        try {
            iDiscountService.createDiscount(discountDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto(BookConstants.STATUS_201, BookConstants.MESSAGE_201));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    @PostMapping("{id}")
    public ResponseEntity<ResponseDto> updateDiscount(@PathVariable String id, @Valid @RequestBody DiscountDto discountDto){
        try {
            iDiscountService.updateDiscount(id, discountDto);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Cập nhật giảm giá thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi cập nhật giảm giá: " + e.getMessage()));
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ResponseDto> deleteDiscount(@PathVariable String id){
        try {
            iDiscountService.deleteDiscount(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Xóa giảm giá thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi xóa giảm giá: " + e.getMessage()));
        }
    }
}
