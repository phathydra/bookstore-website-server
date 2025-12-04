package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.RankDto;
import com.bookstore.orders.entity.MonthlyPoints;
import com.bookstore.orders.entity.ObtainedVoucher;
import com.bookstore.orders.entity.Rank;
import com.bookstore.orders.entity.RankVoucher;
import com.bookstore.orders.mapper.RankMapper;
import com.bookstore.orders.repository.MonthlyPointsRepository;
import com.bookstore.orders.repository.ObtainedVoucherRepository;
import com.bookstore.orders.repository.RankRepository;
import com.bookstore.orders.repository.RankVoucherRepository;
import com.bookstore.orders.service.IRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RankServiceImpl implements IRankService {
    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private MonthlyPointsRepository monthlyPointsRepository;

    @Autowired
    private RankVoucherRepository rankVoucherRepository;

    @Autowired
    private ObtainedVoucherRepository obtainedVoucherRepository;

    @Override
    public RankDto getRankById(String accountId){
        Optional<Rank> rank = rankRepository.findByAccountId(accountId);
        if(rank.isPresent()){
            return RankMapper.toRankDto(rank.get(), new RankDto());
        }
        else{
            RankDto newRank = new RankDto();
            newRank.setAccountId(accountId);
            newRank.setRank(1);
            newRank.setOldRank(1);
            newRank.setPoints(0.0);
            newRank.setLastRankEvaluatedMonth(YearMonth.now());
            newRank.setRankAchievedDate(LocalDate.now());
            Rank created = rankRepository.save(RankMapper.toRank(newRank, new Rank()));
            return RankMapper.toRankDto(created, new RankDto());
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void evaluateRanks() {
        YearMonth current = YearMonth.now();
        List<Rank> ranks = rankRepository.findAll();
        for(Rank rank : ranks){
            if(rank.getLastRankEvaluatedMonth() != null &&
                    !YearMonth.parse(rank.getLastRankEvaluatedMonth()).isBefore(current)) continue;

            List<MonthlyPoints> lastedMonthlyPoints = monthlyPointsRepository.findTop12ByAccountIdOrderByYearDescMonthDesc(rank.getAccountId());
            Double lasted12MonthPoints = 0.0;
            for(MonthlyPoints monthlyPoints : lastedMonthlyPoints){
                lasted12MonthPoints += monthlyPoints.getPoint();
            }
            if(getRankByPoints(lasted12MonthPoints) > rank.getRank()){
                rank.setOldRank(rank.getRank());
                rank.setRank(rank.getRank() + 1);
                rank.setRankAchievedDate(LocalDate.now());
            }

            if(rank.getRankAchievedDate() != null &&
                    ChronoUnit.MONTHS.between(rank.getRankAchievedDate(), LocalDate.now()) > 12){
                if(getRankByPoints(lasted12MonthPoints) < rank.getRank()){
                    rank.setOldRank(rank.getRank());
                    rank.setRank(rank.getRank() - 1);
                    rank.setRankAchievedDate(LocalDate.now());
                }
            }

            rankRepository.save(rank);
        }
    }

    private int getRankByPoints(Double points){
        if(points >= 3000){
            return 4;
        } else if (points >= 1500) {
            return 3;
        } else if (points >= 5) {
            return 2;
        } else return 1;
    }

    @Override
    @Scheduled(cron = "0 30 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void distributeMonthlyVouchers() {
        List<Rank> ranks = rankRepository.findAll();

        // Chuẩn bị map rank -> voucher list
        Map<Integer, List<RankVoucher>> vouchersByRank = Map.of(
                1, rankVoucherRepository.findAllByRank(1),
                2, rankVoucherRepository.findAllByRank(2),
                3, rankVoucherRepository.findAllByRank(3),
                4, rankVoucherRepository.findAllByRank(4)
        );

        LocalDate today = LocalDate.now();

        for (Rank rank : ranks) {
            int currentRank = rank.getRank();
            int oldRank = rank.getOldRank();
            String accountId = rank.getAccountId();

            if (rank.getRankAchievedDate().isEqual(today)) {
                List<RankVoucher> oldVouchers = vouchersByRank.get(oldRank);
                if (oldVouchers != null) {
                    oldVouchers.forEach(v ->
                            obtainedVoucherRepository.deleteByAccountIdAndCode(accountId, v.getCode()));
                }
            }

            List<RankVoucher> currentVouchers = vouchersByRank.get(currentRank);
            if (currentVouchers != null) {
                currentVouchers.forEach(voucher -> grantOrResetVoucher(accountId, voucher));
            }
        }
    }
    private void grantOrResetVoucher(String accountId, RankVoucher voucher) {
        Optional<ObtainedVoucher> opt = obtainedVoucherRepository.findByAccountIdAndCode(accountId, voucher.getCode());

        ObtainedVoucher ov = opt.orElseGet(ObtainedVoucher::new);
        ov.setAccountId(accountId);
        ov.setCode(voucher.getCode());
        ov.setCount(voucher.getUsageLimit());

        obtainedVoucherRepository.save(ov);
    }

    @Override
    public RankDto createRank(RankDto rankDto){
        Rank newRank = rankRepository.save(RankMapper.toRank(rankDto, new Rank()));
        return RankMapper.toRankDto(newRank, new RankDto());
    }
}
