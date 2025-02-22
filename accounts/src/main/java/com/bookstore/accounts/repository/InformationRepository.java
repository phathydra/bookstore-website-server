package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {

    Optional<Information> findByAccountId(Long accountId);


    @Transactional
    @Modifying
    void deleteByAccountId(Long accountId);
}
