package com.bookstore.orders.service;

import com.bookstore.orders.dto.RankDto;
import org.springframework.stereotype.Service;

public interface IRankService {

    RankDto getRankById(String accountId);

    void evaluateRanks();

    void distributeMonthlyVouchers();

    RankDto createRank(RankDto rankDto);
}
