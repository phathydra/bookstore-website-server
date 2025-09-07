package com.tlcn.books.repository;

import com.tlcn.books.entity.Book;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    Optional<Book> findByBookId(String bookId);

    Page<Book> findByBookIdIn(List<String> bookIds, Pageable pageable);

    Page<Book> findAllBy(Pageable pageable);

    @Query("{$or: [ " +
            "{ 'bookName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'bookAuthor': { $regex: ?0, $options: 'i' } }, " +
            "]}")
    Page<Book> findBySearchTerms(String regex, Pageable pageable);

    List<Book> findTop5ByMainCategoryAndBookIdNot(String mainCategory, String bookId);

    List<Book> findTop5ByBookAuthorAndBookIdNot(String bookAuthor, String bookId);

    Collection<? extends Book> findTop3ByOrderByBookNameAsc();

    List<Book> findTop3ByBookNameContainingIgnoreCaseAndBookIdNotIn(String bookName, List<String> excludedBooks);

    Page<Book> findByMainCategory(String mainCategory, Pageable pageable);

    Page<Book> findByBookCategory(String bookCategory, Pageable pageable);

    @Query("{" +
            " 'bookAuthor': { $regex: ?0, $options: 'i' }, " +
            " 'mainCategory': { $in: ?1 }, " +
            " 'bookCategory': { $in: ?2 }, " +
            " 'bookPublisher': { $in: ?3 }, " +
            " 'bookSupplier': { $in: ?4 }, " +
            " 'bookPrice': { $gte: ?5, $lte: ?6 } " +
            "}")
    Page<Book> filterBooksByAll(
            String bookAuthorRegex,
            List<Pattern> mainCategories,
            List<Pattern> bookCategories,
            List<Pattern> publishers,
            List<Pattern> suppliers,
            double minPrice,
            double maxPrice,
            Pageable pageable
    );

    @Query("{" +
            " 'bookAuthor': { $regex: ?0, $options: 'i' }, " +
            " 'mainCategory': { $in: ?1 }, " +
            " 'bookPublisher': { $in: ?2 }, " +
            " 'bookSupplier': { $in: ?3 }, " +
            " 'bookPrice': { $gte: ?4, $lte: ?5 } " +
            "}")
    Page<Book> filterBooksByAllNoSubCategory(
            String bookAuthorRegex,
            List<Pattern> mainCategories,
            List<Pattern> publishers,
            List<Pattern> suppliers,
            double minPrice,
            double maxPrice,
            Pageable pageable
    );



    Page<Book> findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookPublisherInAndBookSupplierIn(
            String bookAuthor, List<String> mainCategory, double minPrice, double maxPrice, List<String> bookPublisher, List<String> bookSupplier, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookPublisherIn(
            String bookAuthor, List<String> mainCategory, double minPrice, double maxPrice, List<String> bookPublisher, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookSupplierIn(
            String bookAuthor, List<String> mainCategory, double minPrice, double maxPrice, List<String> bookSupplier, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookPublisherInAndBookSupplierIn(
            String bookAuthor, double minPrice, double maxPrice, List<String> bookPublisher, List<String> bookSupplier, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetween(
            String bookAuthor, List<String> mainCategory, double minPrice, double maxPrice, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookPublisherIn(
            String bookAuthor, double minPrice, double maxPrice, List<String> bookPublisher, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookSupplierIn(
            String bookAuthor, double minPrice, double maxPrice, List<String> bookSupplier, Pageable pageable);

    Page<Book> findByBookAuthorContainingIgnoreCaseAndBookPriceBetween(
            String bookAuthor, double minPrice, double maxPrice, Pageable pageable);

    Page<Book> findByBookStockQuantity(int bookStockQuantity, Pageable pageable);

    Page<Book> findByBookStockQuantityGreaterThan(int bookStockQuantity, Pageable pageable);

    List<Book> findTop5ByOrderByBookNameAsc();
}

