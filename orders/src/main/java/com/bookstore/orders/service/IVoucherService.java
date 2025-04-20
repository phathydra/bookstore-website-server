package com.bookstore.orders.service;

import com.bookstore.orders.dto.ObtainableVoucherDto;
import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.ObtainableVoucher;
import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;
import java.util.List;

public interface IVoucherService {
    Page<VoucherDto> getAllVoucher(int page, int size);

    Page<ObtainableVoucherDto> getAllObtainableVoucher(int page, int size);

    List<VoucherDto> getAllPublishVoucher(String userId);

    List<ObtainableVoucherDto> getAllPersonalVoucher(String userId);
    VoucherDto getVoucherByCode(String code);

    ObtainableVoucherDto getPersonalVoucherByCode(String code);

    VoucherDto createVoucher(VoucherDto voucherDto);

    ObtainableVoucherDto createObtainableVoucher(ObtainableVoucherDto obtainableVoucherDto);

    OrderVoucherDto applyVoucher(OrderVoucherDto orderVoucherDto);

    VoucherDto updateVoucher(String id, VoucherDto voucherDto);

    ObtainableVoucherDto updateObtainableVoucher(String id, ObtainableVoucherDto obtainableVoucherDto);

    void claimVoucher(String userId, ObtainableVoucherDto obtainableVoucherDto);

    void delete(String voucherId);

    void deleteObtainableVoucher(String id);
}
