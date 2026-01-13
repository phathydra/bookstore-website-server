package com.bookstore.orders.dto;

import com.bookstore.orders.entity.BaseVoucherEntity;
import lombok.Data;

import java.util.Date;

@Data
public class ObtainableVoucherDto extends BaseVoucherEntityDto {

    private String id;

    private boolean publicClaimable;
    private Double valueRequirement;
}
