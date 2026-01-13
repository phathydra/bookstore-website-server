package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.*;
import com.bookstore.orders.entity.*;
import com.bookstore.orders.mapper.VoucherMapper;
import com.bookstore.orders.repository.*;
import com.bookstore.orders.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements IVoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ObtainableVoucherRepository obtainableVoucherRepository;

    @Autowired
    private OrderVoucherRepository orderVoucherRepository;

    @Autowired
    private UsedVoucherRepository usedVoucherRepository;

    @Autowired
    private ObtainedVoucherRepository obtainedVoucherRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RankVoucherRepository rankVoucherRepository;

    @Override
    public Page<VoucherDto> getAllVoucher(int page, int size, String code) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = voucherRepository.findAllByCodeContainingIgnoreCaseOrderByEndDateDesc(pageable, code);
        return vouchers.map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto()));
    }

    @Override
    public Page<VoucherDto> getExpiredVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Voucher> vouchers = voucherRepository.findByEndDateBefore(now, pageable);
        return vouchers.map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto()));
    }

    @Override
    public Page<VoucherDto> getActiveVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Voucher> vouchers = voucherRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable);
        return vouchers.map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto()));
    }

    @Override
    public Page<VoucherDto> getUpcomingVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<Voucher> vouchers = voucherRepository.findByStartDateAfter(now, pageable);
        return vouchers.map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto()));
    }

    @Override
    public Page<ObtainableVoucherDto> getAllObtainableVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ObtainableVoucher> vouchers = obtainableVoucherRepository.findAllByOrderByEndDateDesc(pageable);
        return vouchers.map(voucher -> VoucherMapper.toObtainableVoucherDto(voucher, new ObtainableVoucherDto()));
    }

    @Override
    public List<VoucherDto> getAllPublishVoucher(String accountId){
        List<Voucher> vouchers = voucherRepository.getAllByPublish(true);
        Optional<UsedVoucher> usedVoucher = usedVoucherRepository.findByAccountId(accountId);
        List<VoucherDto> voucherDtos = vouchers.stream().map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto())).toList();
        if(usedVoucher.isPresent()){
            List<String> usedVoucherCodes = usedVoucher.get().getUserVoucherCodes();
            for(VoucherDto voucher : voucherDtos){
                if(usedVoucherCodes.contains(voucher.getCode())){
                    long count = usedVoucherCodes.stream().filter(code -> code.equals(voucher.getCode())).count();
                    int usagesLeft = voucher.getUserUsageLimit() - (int) count;
                    voucher.setUserUsageLimit(Math.max(usagesLeft, 0));
                }
            }
        }
        else{
            UsedVoucher usedVoucher1 = new UsedVoucher();
            List<String> usedCode = new ArrayList<>();
            usedVoucher1.setAccountId(accountId);
            usedVoucher1.setUserVoucherCodes(usedCode);
            usedVoucherRepository.save(usedVoucher1);
            getAllPublishVoucher(accountId);
        }
        return voucherDtos;
    }

    @Override
    public List<BaseVoucherEntityDto> getAllPersonalVoucher(String accountId){

        List<ObtainedVoucher> obtainedVouchers = obtainedVoucherRepository.getObtainedVoucherByAccountId(accountId);
        if(obtainedVouchers.isEmpty()){
            return Collections.emptyList();
        }
        Date now = new Date();
        List<BaseVoucherEntityDto> voucherDtos = new ArrayList<>();
        for(ObtainedVoucher voucher : obtainedVouchers){
            Optional<ObtainableVoucher> tempV = obtainableVoucherRepository.findByCodeAndEndDateGreaterThanEqual(voucher.getCode(), now);
            if(tempV.isPresent()){
                ObtainableVoucherDto availableVoucher = VoucherMapper.toObtainableVoucherDto(tempV.get(), new ObtainableVoucherDto());
                voucherDtos.add(availableVoucher);
            }
            else{
                Optional<RankVoucher> tempR = rankVoucherRepository.findByCodeAndEndDateGreaterThanEqual(voucher.getCode(), now);
                if(tempR.isPresent()){
                    RankVoucherDto availableVoucher = VoucherMapper.toRankVoucherDto(tempR.get(), new RankVoucherDto());
                    voucherDtos.add(availableVoucher);
                }
                else{
                    obtainedVoucherRepository.deleteByAccountIdAndCode(accountId, voucher.getCode());
                }
            }
        }
        return voucherDtos;
    }

    @Override
    public List<ObtainableVoucherDto> getAllPublicClaimableVoucher(String accountId) {
        List<ObtainedVoucher> obtainedVouchers = obtainedVoucherRepository.getObtainedVoucherByAccountId(accountId);
        List<ObtainableVoucher> obtainableVouchers = obtainableVoucherRepository.getObtainableVoucherByPublicClaimable(true);

        if (!obtainedVouchers.isEmpty()) {
            Set<String> ownedVoucherCodes = obtainedVouchers.stream()
                    .map(ObtainedVoucher::getCode)
                    .collect(Collectors.toSet());
            obtainableVouchers.removeIf(v -> ownedVoucherCodes.contains(v.getCode()));
        }

        // 5. Map remaining vouchers to DTO
        return obtainableVouchers.stream()
                .map(v -> VoucherMapper.toObtainableVoucherDto(v, new ObtainableVoucherDto()))
                .toList();
    }


    @Override
    public List<ObtainableVoucherDto> automaticallyObtainVoucher(String orderId){
        List<ObtainableVoucher> obtainableVouchers = obtainableVoucherRepository.getObtainableVoucherByPublicClaimable(false);
        Optional<Order> placedOrder = orderRepository.findById(orderId);
        Double totalPrice = placedOrder.get().getTotalPrice();

        List<ObtainableVoucherDto> claimableVouchers = new ArrayList<>();
        for (ObtainableVoucher voucher: obtainableVouchers){
            if(totalPrice >= voucher.getValueRequirement()){
                claimableVouchers.add(VoucherMapper.toObtainableVoucherDto(voucher, new ObtainableVoucherDto()));
            }
        }
        if(!claimableVouchers.isEmpty()){
            List<ObtainedVoucher> existed = obtainedVoucherRepository.getObtainedVoucherByAccountId(placedOrder.get().getAccountId());
            for(ObtainableVoucherDto curr : claimableVouchers){
                Optional<ObtainedVoucher> obtainedVoucher = obtainedVoucherRepository.findByCode(curr.getCode());
                if(obtainedVoucher.isPresent()){
                    obtainedVoucher.get().setCount(obtainedVoucher.get().getCount() + 1);
                    obtainedVoucherRepository.save(obtainedVoucher.get());
                }
                else{
                    ObtainedVoucher newVoucher = new ObtainedVoucher();
                    newVoucher.setAccountId(placedOrder.get().getAccountId());
                    newVoucher.setCode(curr.getCode());
                    newVoucher.setCount(1);
                    obtainedVoucherRepository.save(newVoucher);
                }
            }
        }
        return claimableVouchers;
    }

    @Override
    public VoucherDto getVoucherByCode(String code) {
        Optional<Voucher> voucher = voucherRepository.getVoucherByCode(code);
        if(voucher.isPresent()){
            return VoucherMapper.toVoucherDto(voucher.get(), new VoucherDto());
        } else{
            throw new RuntimeException("Voucher not found with code: " + code);
        }
    }

    @Override
    public ObtainableVoucherDto getPersonalVoucherByCode(String code) {
        Optional<ObtainableVoucher> voucher = obtainableVoucherRepository.getObtainableVoucherByCode(code);
        if(voucher.isPresent()){
            return VoucherMapper.toObtainableVoucherDto(voucher.get(), new ObtainableVoucherDto());
        } else{
            throw new RuntimeException("Voucher not found with code: " + code);
        }
    }

    @Override
    public VoucherDto createVoucher(VoucherDto voucherDto) {
        Voucher voucher = VoucherMapper.toVoucher(voucherDto, new Voucher());
        return VoucherMapper.toVoucherDto(voucherRepository.save(voucher), new VoucherDto());
    }

    @Override
    public ObtainableVoucherDto createObtainableVoucher(ObtainableVoucherDto obtainableVoucherDto) {
        ObtainableVoucher voucher = VoucherMapper.toObtainableVoucher(obtainableVoucherDto, new ObtainableVoucher());
        return VoucherMapper.toObtainableVoucherDto(obtainableVoucherRepository.save(voucher), new ObtainableVoucherDto());
    }

    @Override
    @Transactional
    public OrderVoucherDto applyVoucher(OrderVoucherDto orderVoucherDto) {
        OrderVoucher orderVoucher = VoucherMapper.toOrderVoucher(orderVoucherDto, new OrderVoucher());
        orderVoucherRepository.save(orderVoucher);
        Optional<Order> placedOrder = orderRepository.findById(orderVoucherDto.getOrderId());
        Optional<Voucher> voucher = voucherRepository.getVoucherByCode(orderVoucherDto.getVoucherCode());
        Optional<UsedVoucher> usedVoucher = usedVoucherRepository.findByAccountId(placedOrder.get().getAccountId());
        if(voucher.isEmpty()){
            Optional<ObtainableVoucher> obtainableVoucher = obtainableVoucherRepository.getObtainableVoucherByCode(orderVoucherDto.getVoucherCode());
            usedVoucher.get().getUserVoucherCodes().add(obtainableVoucher.get().getCode());
            usedVoucherRepository.save(usedVoucher.get());
            obtainableVoucher.get().setUsageLimit(obtainableVoucher.get().getUsageLimit() - 1);
            obtainableVoucherRepository.save(obtainableVoucher.get());
        }
        else {
            usedVoucher.get().getUserVoucherCodes().add(voucher.get().getCode());
            usedVoucherRepository.save(usedVoucher.get());
            voucher.get().setUsageLimit(voucher.get().getUsageLimit() - 1);
            voucherRepository.save(voucher.get());
        }
        return orderVoucherDto;
    }


    @Override
    public VoucherDto updateVoucher(String id, VoucherDto voucherDto) {
        Optional<Voucher> voucher = voucherRepository.findById(id);
        if(voucher.isPresent()){
            Voucher updatedVoucher = VoucherMapper.toVoucher(voucherDto, voucher.get());
            return VoucherMapper.toVoucherDto(voucherRepository.save(updatedVoucher), new VoucherDto());
        }
        else{
            throw new RuntimeException("Voucher not found with id: " + id);
        }
    }

    @Override
    public ObtainableVoucherDto updateObtainableVoucher(String id, ObtainableVoucherDto obtainableVoucherDto) {
        Optional<ObtainableVoucher> voucher = obtainableVoucherRepository.findById(id);
        if(voucher.isPresent()){
            ObtainableVoucher updatedVoucher = VoucherMapper.toObtainableVoucher(obtainableVoucherDto, voucher.get());
            return VoucherMapper.toObtainableVoucherDto(obtainableVoucherRepository.save(updatedVoucher), new ObtainableVoucherDto());
        }
        else{
            throw new RuntimeException("Voucher not found with id: " + id);
        }
    }

    @Override
    public Page<ObtainableVoucherDto> getExpiredObtainableVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<ObtainableVoucher> rankVouchers = obtainableVoucherRepository.findByEndDateBefore(now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toObtainableVoucherDto(v, new ObtainableVoucherDto()));
    }

    @Override
    public Page<ObtainableVoucherDto> getActiveObtainableVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<ObtainableVoucher> rankVouchers = obtainableVoucherRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toObtainableVoucherDto(v, new ObtainableVoucherDto()));
    }

    @Override
    public Page<ObtainableVoucherDto> getUpcomingObtainableVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<ObtainableVoucher> rankVouchers = obtainableVoucherRepository.findByStartDateAfter(now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toObtainableVoucherDto(v, new ObtainableVoucherDto()));
    }

    @Override
    public void claimVoucher(String accountId, ObtainableVoucherDto obtainableVoucherDto){
        Optional<ObtainedVoucher> oldVouchers = obtainedVoucherRepository.findByCode(obtainableVoucherDto.getCode());
        if(oldVouchers.isPresent()){
            oldVouchers.get().setCount(oldVouchers.get().getCount() + 1);
            obtainedVoucherRepository.save(oldVouchers.get());
        }
        else {
            ObtainedVoucher newVoucher = new ObtainedVoucher();
            newVoucher.setAccountId(accountId);
            newVoucher.setCode(obtainableVoucherDto.getCode());
            newVoucher.setCount(1);
            obtainedVoucherRepository.save(newVoucher);
        }
    }

    @Override
    public void delete(String voucherId) {
        voucherRepository.deleteById(voucherId);
    }

    @Override
    public void deleteObtainableVoucher(String id) {
        obtainableVoucherRepository.deleteById(id);
    }

    @Override
    public Page<RankVoucherDto> getAllRankVoucher(int page, int size, String code) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RankVoucher> rankVouchers = rankVoucherRepository.findAllByCodeContainingIgnoreCaseOrderByEndDateDesc(pageable, code);
        return rankVouchers.map(v -> VoucherMapper.toRankVoucherDto(v, new RankVoucherDto()));
    }

    @Override
    public Page<RankVoucherDto> getExpiredRankVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<RankVoucher> rankVouchers = rankVoucherRepository.findByEndDateBefore(now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toRankVoucherDto(v, new RankVoucherDto()));
    }

    @Override
    public Page<RankVoucherDto> getActiveRankVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<RankVoucher> rankVouchers = rankVoucherRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toRankVoucherDto(v, new RankVoucherDto()));
    }

    @Override
    public Page<RankVoucherDto> getUpcomingRankVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Date now = new Date();
        Page<RankVoucher> rankVouchers = rankVoucherRepository.findByStartDateAfter(now, pageable);
        return rankVouchers.map(v -> VoucherMapper.toRankVoucherDto(v, new RankVoucherDto()));
    }

    @Override
    public RankVoucherDto getRankVoucherByCode(String code) {
        Optional<RankVoucher> voucher = rankVoucherRepository.getRankVoucherByCode(code);
        return voucher.map(v -> VoucherMapper.toRankVoucherDto(v, new RankVoucherDto()))
                .orElseThrow(() -> new RuntimeException("RankVoucher not found with code: " + code));
    }

    @Override
    public RankVoucherDto createRankVoucher(RankVoucherDto rankVoucherDto) {
        RankVoucher entity = VoucherMapper.toRankVoucher(rankVoucherDto, new RankVoucher());
        RankVoucher saved = rankVoucherRepository.save(entity);
        return VoucherMapper.toRankVoucherDto(saved, new RankVoucherDto());
    }

    @Override
    public RankVoucherDto updateRankVoucher(String id, RankVoucherDto rankVoucherDto) {
        Optional<RankVoucher> optional = rankVoucherRepository.findById(id);
        if (optional.isPresent()) {
            RankVoucher updated = VoucherMapper.toRankVoucher(rankVoucherDto, optional.get());
            RankVoucher saved = rankVoucherRepository.save(updated);
            return VoucherMapper.toRankVoucherDto(saved, new RankVoucherDto());
        } else {
            throw new RuntimeException("RankVoucher not found with id: " + id);
        }
    }

    @Override
    public void deleteRankVoucher(String id) {
        rankVoucherRepository.deleteById(id);
    }
}
