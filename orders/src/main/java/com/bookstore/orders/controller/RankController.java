package com.bookstore.orders.controller;

import com.bookstore.orders.dto.RankDto;
import com.bookstore.orders.entity.Rank;
import com.bookstore.orders.service.IRankService;
import com.bookstore.orders.service.impl.RankServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3001, http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/ranks")
public class RankController {

    @Autowired
    private IRankService iRankService;

    @GetMapping("")
    public ResponseEntity<RankDto> findRankById(@RequestParam String accountId){
        RankDto rankDto = iRankService.getRankById(accountId);
        return ResponseEntity.ok(rankDto);
    }

    @PostMapping("/test/evaluate")
    public ResponseEntity<String> evaluateTest(){
        iRankService.evaluateRanks();
        return ResponseEntity.ok("Evaluation completed!");
    }

    @PostMapping("/test/distribute")
    public ResponseEntity<String> distributeTest(){
        iRankService.distributeMonthlyVouchers();
        return ResponseEntity.ok("Distribution completed!");
    }

    @PostMapping("")
    public ResponseEntity<RankDto> createRank(RankDto rankDto){
        RankDto created = iRankService.createRank(rankDto);
        return ResponseEntity.ok(created);
    }
}
