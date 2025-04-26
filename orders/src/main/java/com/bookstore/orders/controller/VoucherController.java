package com.bookstore.orders.controller;

import com.bookstore.orders.dto.*;
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

    // Voucher management for admin
    @GetMapping("")
    public ResponseEntity<Page<VoucherDto>> getAllVoucher(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size){
        Page<VoucherDto> voucherDtos = iVoucherService.getAllVoucher(page, size);
        return ResponseEntity.ok(voucherDtos);
    }


    // Obtainable voucher management for admin
    @GetMapping("/obtainable")
    public ResponseEntity<Page<ObtainableVoucherDto>> getAllObtainableVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getAllObtainableVoucher(page, size);
        return ResponseEntity.ok(obtainableVoucherDtos);
    }

    // User personal vouchers
    @GetMapping("/personal-voucher")
    public ResponseEntity<List<ObtainableVoucherDto>> getPersonalVoucher(@RequestParam String userId){
        List<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getAllPersonalVoucher(userId);
        return ResponseEntity.ok(obtainableVoucherDtos);
    }

    // Public vouchers
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

    @GetMapping("/personal")
    public ResponseEntity<ObtainableVoucherDto> getPersonalVoucherByCode(@RequestParam String code) {
        ObtainableVoucherDto obtainableVoucherDto = iVoucherService.getPersonalVoucherByCode(code);
        return ResponseEntity.ok(obtainableVoucherDto);
    }

    @PostMapping("/apply-voucher")
    public ResponseEntity<OrderVoucherDto> applyVoucher(@RequestBody OrderVoucherDto orderVoucherDto){
        OrderVoucherDto orderVoucherDto1 = iVoucherService.applyVoucher(orderVoucherDto);
        return ResponseEntity.ok(orderVoucherDto1);
    }

    @PostMapping("/claim")
    public ResponseEntity<ResponseDto> claimVoucher(@RequestBody ClaimVoucherRequestDto claimVoucherRequest) {
        iVoucherService.claimVoucher(claimVoucherRequest.getUserId(), claimVoucherRequest.getObtainableVoucherDto());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "Voucher claimed successfully"));
    }

    @PostMapping("")
    public ResponseEntity<VoucherDto> createVoucher(@RequestBody VoucherDto voucherDto){
        VoucherDto createdVoucher = iVoucherService.createVoucher(voucherDto);
        return ResponseEntity.ok(createdVoucher);
    }

    @PostMapping("/obtainable")
    public ResponseEntity<ObtainableVoucherDto> createObtainableVoucher(
            @RequestBody ObtainableVoucherDto obtainableVoucherDto) {
        ObtainableVoucherDto createdVoucher = iVoucherService.createObtainableVoucher(obtainableVoucherDto);
        return ResponseEntity.ok(createdVoucher);
    }

    @PutMapping("{id}")
    public ResponseEntity<VoucherDto> updateVoucher(@PathVariable String id, @RequestBody VoucherDto voucherDto){
        VoucherDto updatedVoucher = iVoucherService.updateVoucher(id, voucherDto);
        return ResponseEntity.ok(updatedVoucher);
    }

    @PutMapping("/obtainable/{id}")
    public ResponseEntity<ObtainableVoucherDto> updateObtainableVoucher(
            @PathVariable String id,
            @RequestBody ObtainableVoucherDto obtainableVoucherDto) {
        ObtainableVoucherDto updatedVoucher = iVoucherService.updateObtainableVoucher(id, obtainableVoucherDto);
        return ResponseEntity.ok(updatedVoucher);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ResponseDto> deleteVoucher(@PathVariable String id){
        iVoucherService.delete(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "Delete successfully"));
    }

    @DeleteMapping("/obtainable/{id}")
    public ResponseEntity<ResponseDto> deleteObtainableVoucher(@PathVariable String id) {
        iVoucherService.deleteObtainableVoucher(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "Delete successfully"));
    }
}
