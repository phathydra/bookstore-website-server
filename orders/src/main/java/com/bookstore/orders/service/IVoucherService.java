package com.bookstore.orders.service;

import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;

public interface IVoucherService {
    public Page<VoucherDto> getAllVoucher(int page, int size);

    public VoucherDto getVoucherByCode(String code);

    public VoucherDto createVoucher(VoucherDto voucherDto);

    public VoucherDto updateVoucher(String id, VoucherDto voucherDto);

    public void delete(String voucherId);
}
