package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.ObtainableVoucherDto;
import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Page<VoucherDto> getAllVoucher(int page, int size, String code) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = voucherRepository.findAllByCodeContainingIgnoreCaseOrderByEndDateDesc(pageable, code);
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
    public List<ObtainableVoucherDto> getAllPersonalVoucher(String accountId){
        Optional<ObtainedVoucher> obtainedVoucher = obtainedVoucherRepository.getObtainedVoucherByAccountId(accountId);
        if(obtainedVoucher.isPresent()){
            List<String> codes = obtainedVoucher.get().getObtainedVouchers();
            List<ObtainableVoucherDto> obtainableVoucherDtos = new ArrayList<>();
            codes.forEach(code -> {
                ObtainableVoucher voucher = obtainableVoucherRepository.getObtainableVoucherByCode(code)
                        .orElseThrow(() -> new RuntimeException("Voucher not found: " + code));
                obtainableVoucherDtos.add(VoucherMapper.toObtainableVoucherDto(voucher, new ObtainableVoucherDto()));
            });
            return  obtainableVoucherDtos;
        }
        else {
            throw new RuntimeException("Dont have any vouchers");
        }
    }

    @Override
    public List<ObtainableVoucherDto> getAllPublicClaimableVoucher(String accountId){
        Optional<ObtainedVoucher> obtainedVoucher = obtainedVoucherRepository.getObtainedVoucherByAccountId(accountId);
        List<ObtainableVoucher> obtainableVouchers = obtainableVoucherRepository.getObtainableVoucherByPublicClaimable(true);
        if(obtainedVoucher.isPresent()){
            List<String> ownedVoucherCodes = obtainedVoucher.get().getObtainedVouchers();
            obtainableVouchers.removeIf(voucher -> ownedVoucherCodes.contains(voucher.getCode()));
        }
        return obtainableVouchers.stream().map(voucher -> VoucherMapper.toObtainableVoucherDto(voucher, new ObtainableVoucherDto())).toList();
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
            Optional<ObtainedVoucher> existed = obtainedVoucherRepository.getObtainedVoucherByAccountId(placedOrder.get().getAccountId());
            if(existed.isPresent()){
                existed.get().getObtainedVouchers().addAll(claimableVouchers.stream().map(voucher -> voucher.getCode()).toList());
                obtainedVoucherRepository.save(existed.get());
            }
            else {
                ObtainedVoucher obtainedVoucher = new ObtainedVoucher();
                obtainedVoucher.setAccountId(placedOrder.get().getAccountId());
                obtainedVoucher.setObtainedVouchers(claimableVouchers.stream().map(voucher -> voucher.getCode()).toList());
                obtainedVoucherRepository.save(obtainedVoucher);
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
    public void claimVoucher(String accountId, ObtainableVoucherDto obtainableVoucherDto){
        Optional<ObtainedVoucher> oldVouchers = obtainedVoucherRepository.getObtainedVoucherByAccountId(accountId);
        if(oldVouchers.isPresent()){
            oldVouchers.get().getObtainedVouchers().add(obtainableVoucherDto.getCode());
            obtainedVoucherRepository.save(oldVouchers.get());
        }
        else {
            ObtainedVoucher obtainedVoucher = new ObtainedVoucher();
            obtainedVoucher.setAccountId(accountId);
            List<String> obtainedVoucherCodes = new ArrayList<>();
            obtainedVoucherCodes.add(obtainableVoucherDto.getCode());
            obtainedVoucher.setObtainedVouchers(obtainedVoucherCodes);
            obtainedVoucherRepository.save(obtainedVoucher);
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
}
