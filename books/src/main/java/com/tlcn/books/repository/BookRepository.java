package com.tlcn.books.repository;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
    Optional<Book> findByBookId(String bookId);

    Page<Book> findAllBy(Pageable pageable);

    Page<Book> findByBookNameContainingIgnoreCaseOrBookAuthorContainingIgnoreCase(Pageable pageable, String bookName, String bookAuthor);

    List<Book> findTop5ByBookCategoryAndBookIdNot(String bookCategory, String bookId);

    List<Book> findTop5ByBookAuthorAndBookIdNot(String bookAuthor, String bookId);

    Collection<? extends Book> findTop3ByOrderByBookNameAsc();
}
