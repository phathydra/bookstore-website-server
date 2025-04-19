package com.bookstore.orders.controller;

import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.ResponseDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3001, http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired
    private IVoucherService iVoucherService;

    @GetMapping("")
    public ResponseEntity<Page<VoucherDto>> getAllVoucher(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size){
        Page<VoucherDto> voucherDtos = iVoucherService.getAllVoucher(page, size);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("/available-voucher")
    public ResponseEntity<List<VoucherDto>> getAllPublishVoucher(@RequestParam String userId){
        List<VoucherDto> voucherDtos = iVoucherService.getAllPublishVoucher(userId);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("/get-voucher")
    public ResponseEntity<VoucherDto> getVoucherByCode(@RequestParam String code){
        VoucherDto voucherDto = iVoucherService.getVoucherByCode(code);
        return ResponseEntity.ok(voucherDto);
    }

    @PostMapping("/apply-voucher")
    public ResponseEntity<OrderVoucherDto> applyVoucher(@RequestBody OrderVoucherDto orderVoucherDto){
        OrderVoucherDto orderVoucherDto1 = iVoucherService.applyVoucher(orderVoucherDto);
        return ResponseEntity.ok(orderVoucherDto1);
    }

    @PostMapping("")
    public ResponseEntity<VoucherDto> createVoucher(@RequestBody VoucherDto voucherDto){
        VoucherDto createdVoucher = iVoucherService.createVoucher(voucherDto);
        return ResponseEntity.ok(createdVoucher);
    }

    @PutMapping("{id}")
    public ResponseEntity<VoucherDto> updateVoucher(@PathVariable String id, @RequestBody VoucherDto voucherDto){
        VoucherDto updatedVoucher = iVoucherService.updateVoucher(id, voucherDto);
        return ResponseEntity.ok(updatedVoucher);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ResponseDto> deleteVoucher(@PathVariable String id){
        iVoucherService.delete(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "Delete successfully"));
    }
}
