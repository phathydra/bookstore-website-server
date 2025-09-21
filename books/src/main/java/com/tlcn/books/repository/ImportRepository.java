package com.tlcn.books.repository;

import com.tlcn.books.entity.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImportRepository extends MongoRepository<Import, String> {
    Page<Import> findByImportDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<Import> findByImportDateBetween(LocalDateTime startDate, LocalDateTime endDate);

}