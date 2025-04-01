package com.bookstore.orders.service;

import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;

public interface IVoucherService {
    Page<VoucherDto> getAllVoucher(int page, int size);

    VoucherDto getVoucherByCode(String code);

    VoucherDto createVoucher(VoucherDto voucherDto);

    OrderVoucherDto applyVoucher(OrderVoucherDto orderVoucherDto);

    VoucherDto updateVoucher(String id, VoucherDto voucherDto);

    void delete(String voucherId);
}
