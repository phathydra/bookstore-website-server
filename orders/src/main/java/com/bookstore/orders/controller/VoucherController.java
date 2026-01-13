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
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "") String code){
        Page<VoucherDto> voucherDtos = iVoucherService.getAllVoucher(page, size, code);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("expired")
    public ResponseEntity<Page<VoucherDto>> getExpiredVoucher(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        Page<VoucherDto> voucherDtos = iVoucherService.getExpiredVoucher(page, size);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("active")
    public ResponseEntity<Page<VoucherDto>> getActiveVoucher(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        Page<VoucherDto> voucherDtos = iVoucherService.getActiveVoucher(page, size);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("upcoming")
    public ResponseEntity<Page<VoucherDto>> getUpcomingVoucher(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        Page<VoucherDto> voucherDtos = iVoucherService.getUpcomingVoucher(page, size);
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
    public ResponseEntity<List<BaseVoucherEntityDto>> getPersonalVoucher(@RequestParam String accountId){
        List<BaseVoucherEntityDto> voucherDtos = iVoucherService.getAllPersonalVoucher(accountId);
        return ResponseEntity.ok(voucherDtos);
    }

    // Public vouchers
    @GetMapping("/available-voucher")
    public ResponseEntity<List<VoucherDto>> getAllPublishVoucher(@RequestParam String accountId){
        List<VoucherDto> voucherDtos = iVoucherService.getAllPublishVoucher(accountId);
        return ResponseEntity.ok(voucherDtos);
    }

    @GetMapping("/claimable")
    public ResponseEntity<List<ObtainableVoucherDto>> getAllClaimableVoucher(@RequestParam String accountId){
        List<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getAllPublicClaimableVoucher(accountId);
        return ResponseEntity.ok(obtainableVoucherDtos);
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
        iVoucherService.claimVoucher(claimVoucherRequest.getAccountId(), claimVoucherRequest.getObtainableVoucherDto());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "Voucher claimed successfully"));
    }

    @GetMapping("/obtain")
    public ResponseEntity<List<ObtainableVoucherDto>> automaticallyObtainVoucher(@RequestParam String orderId){
        List<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.automaticallyObtainVoucher(orderId);
        return ResponseEntity.ok(obtainableVoucherDtos);
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

    @GetMapping("/obtainable/expired")
    public ResponseEntity<Page<ObtainableVoucherDto>> getExpiredObtainableVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getExpiredObtainableVoucher(page, size);
        return ResponseEntity.ok(obtainableVoucherDtos);
    }

    @GetMapping("/obtainable/active")
    public ResponseEntity<Page<ObtainableVoucherDto>> getActiveObtainableVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getActiveObtainableVoucher(page, size);
        return ResponseEntity.ok(obtainableVoucherDtos);
    }

    @GetMapping("/obtainable/upcoming")
    public ResponseEntity<Page<ObtainableVoucherDto>> getUpcomingObtainableVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ObtainableVoucherDto> obtainableVoucherDtos = iVoucherService.getUpcomingObtainableVoucher(page, size);
        return ResponseEntity.ok(obtainableVoucherDtos);
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

    @GetMapping("/rank")
    public ResponseEntity<Page<RankVoucherDto>> getAllRankVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String code) {
        Page<RankVoucherDto> rankVoucherDtos = iVoucherService.getAllRankVoucher(page, size, code);
        return ResponseEntity.ok(rankVoucherDtos);
    }

    @GetMapping("/rank/expired")
    public ResponseEntity<Page<RankVoucherDto>> getExpiredRankVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RankVoucherDto> rankVoucherDtos = iVoucherService.getExpiredRankVoucher(page, size);
        return ResponseEntity.ok(rankVoucherDtos);
    }

    @GetMapping("/rank/active")
    public ResponseEntity<Page<RankVoucherDto>> getActiveRankVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RankVoucherDto> rankVoucherDtos = iVoucherService.getActiveRankVoucher(page, size);
        return ResponseEntity.ok(rankVoucherDtos);
    }

    @GetMapping("/rank/upcoming")
    public ResponseEntity<Page<RankVoucherDto>> getUpcomingRankVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RankVoucherDto> rankVoucherDtos = iVoucherService.getUpcomingRankVoucher(page, size);
        return ResponseEntity.ok(rankVoucherDtos);
    }

    @GetMapping("/rank/code")
    public ResponseEntity<RankVoucherDto> getRankVoucherByCode(@RequestParam String code) {
        RankVoucherDto rankVoucherDto = iVoucherService.getRankVoucherByCode(code);
        return ResponseEntity.ok(rankVoucherDto);
    }

    @PostMapping("/rank")
    public ResponseEntity<RankVoucherDto> createRankVoucher(@RequestBody RankVoucherDto rankVoucherDto) {
        RankVoucherDto created = iVoucherService.createRankVoucher(rankVoucherDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/rank/{id}")
    public ResponseEntity<RankVoucherDto> updateRankVoucher(
            @PathVariable String id,
            @RequestBody RankVoucherDto rankVoucherDto) {
        RankVoucherDto updated = iVoucherService.updateRankVoucher(id, rankVoucherDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/rank/{id}")
    public ResponseEntity<ResponseDto> deleteRankVoucher(@PathVariable String id) {
        iVoucherService.deleteRankVoucher(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto("200", "RankVoucher deleted successfully"));
    }
}
