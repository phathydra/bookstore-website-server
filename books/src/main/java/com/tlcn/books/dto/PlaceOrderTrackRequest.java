package com.tlcn.books.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // <-- (1) IMPORT THÊM
import lombok.Data;
import java.util.List; // <-- (2) IMPORT THÊM

@Data
public class PlaceOrderTrackRequest {


    private String accountId;

    @JsonProperty("totalPrice")
    private Double totalAmount; // Giữ nguyên tên biến totalAmount của bạn

    private String paymentMethod;
    private String voucherCode; // Có thể là null

    private List<OrderItemTrackDto> items;
}