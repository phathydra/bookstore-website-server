package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.OrderVoucher;
import com.bookstore.orders.entity.Voucher;
import com.bookstore.orders.mapper.VoucherMapper;
import com.bookstore.orders.repository.OrderVoucherRepository;
import com.bookstore.orders.repository.VoucherRepository;
import com.bookstore.orders.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VoucherServiceImpl implements IVoucherService {
    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    OrderVoucherRepository orderVoucherRepository;

    @Override
    public Page<VoucherDto> getAllVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> vouchers = voucherRepository.findAllBy(pageable);
        return vouchers.map(voucher -> VoucherMapper.toVoucherDto(voucher, new VoucherDto()));
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
    public VoucherDto createVoucher(VoucherDto voucherDto) {
        Voucher voucher = VoucherMapper.toVoucher(voucherDto, new Voucher());
        return VoucherMapper.toVoucherDto(voucherRepository.save(voucher), new VoucherDto());
    }

    @Override
    public OrderVoucherDto applyVoucher(OrderVoucherDto orderVoucherDto) {
        OrderVoucher orderVoucher = VoucherMapper.toOrderVoucher(orderVoucherDto, new OrderVoucher());
        orderVoucherRepository.save(orderVoucher);
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
    public void delete(String voucherId) {
        voucherRepository.deleteById(voucherId);
    }
}
