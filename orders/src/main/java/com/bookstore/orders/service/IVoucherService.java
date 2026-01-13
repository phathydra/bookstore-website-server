package com.bookstore.orders.service;

import com.bookstore.orders.dto.*;
import com.bookstore.orders.entity.ObtainableVoucher;
import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;
import java.util.List;

public interface IVoucherService {
    Page<VoucherDto> getAllVoucher(int page, int size, String code);

    Page<VoucherDto> getExpiredVoucher(int page, int size);

    Page<VoucherDto> getActiveVoucher(int page, int size);

    Page<VoucherDto> getUpcomingVoucher(int page, int size);

    Page<ObtainableVoucherDto> getAllObtainableVoucher(int page, int size);

    List<VoucherDto> getAllPublishVoucher(String userId);

    List<BaseVoucherEntityDto> getAllPersonalVoucher(String userId);

    List<ObtainableVoucherDto> getAllPublicClaimableVoucher(String userId);

    List<ObtainableVoucherDto> automaticallyObtainVoucher(String orderId);
    VoucherDto getVoucherByCode(String code);

    ObtainableVoucherDto getPersonalVoucherByCode(String code);

    VoucherDto createVoucher(VoucherDto voucherDto);

    ObtainableVoucherDto createObtainableVoucher(ObtainableVoucherDto obtainableVoucherDto);

    OrderVoucherDto applyVoucher(OrderVoucherDto orderVoucherDto);

    VoucherDto updateVoucher(String id, VoucherDto voucherDto);

    ObtainableVoucherDto updateObtainableVoucher(String id, ObtainableVoucherDto obtainableVoucherDto);

    Page<ObtainableVoucherDto> getExpiredObtainableVoucher(int page, int size);

    Page<ObtainableVoucherDto> getActiveObtainableVoucher(int page, int size);

    Page<ObtainableVoucherDto> getUpcomingObtainableVoucher(int page, int size);

    void claimVoucher(String userId, ObtainableVoucherDto obtainableVoucherDto);

    void delete(String voucherId);

    void deleteObtainableVoucher(String id);

    Page<RankVoucherDto> getAllRankVoucher(int page, int size, String code);

    Page<RankVoucherDto> getExpiredRankVoucher(int page, int size);

    Page<RankVoucherDto> getActiveRankVoucher(int page, int size);

    Page<RankVoucherDto> getUpcomingRankVoucher(int page, int size);

    RankVoucherDto getRankVoucherByCode(String code);

    RankVoucherDto createRankVoucher(RankVoucherDto rankVoucherDto);

    RankVoucherDto updateRankVoucher(String id, RankVoucherDto rankVoucherDto);

    void deleteRankVoucher(String id);
}
