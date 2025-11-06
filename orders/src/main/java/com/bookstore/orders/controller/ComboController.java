package com.bookstore.orders.controller;

import com.bookstore.orders.dto.ComboDto;
import com.bookstore.orders.dto.ResponseDto;
import com.bookstore.orders.entity.Combo;
import com.bookstore.orders.service.IComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/combos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ComboController {

    private final IComboService comboService;

    @Autowired
    public ComboController(IComboService comboService) {
        this.comboService = comboService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCombo(@RequestBody ComboDto comboDto) {
        try {
            comboService.createCombo(comboDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto("201", "Tạo combo thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Lỗi khi tạo combo: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Combo>> getAllCombos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Combo> combos = comboService.getAllCombos(pageable);
        return ResponseEntity.ok(combos);
    }

    @GetMapping("/{comboId}")
    public ResponseEntity<?> getComboById(@PathVariable String comboId) {
        try {
            Combo combo = comboService.getComboById(comboId);
            return ResponseEntity.ok(combo);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto("404", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{comboId}")
    public ResponseEntity<?> updateCombo(@PathVariable String comboId, @RequestBody ComboDto comboDto) {
        try {
            Combo updatedCombo = comboService.updateCombo(comboId, comboDto);
            return ResponseEntity.ok(updatedCombo);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto("404", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Lỗi khi cập nhật combo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{comboId}")
    public ResponseEntity<ResponseDto> deleteCombo(@PathVariable String comboId) {
        try {
            comboService.deleteCombo(comboId);
            return ResponseEntity.ok(new ResponseDto("200", "Xóa combo thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Lỗi khi xóa combo: " + e.getMessage()));
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<Combo>> getComboSuggestions(@RequestParam String bookId) {
        try {
            List<Combo> suggestions = comboService.findActiveCombosContainingBook(bookId);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Combo>> getActiveCombos() {
        try {
            List<Combo> combos = comboService.findActiveCombos();
            return ResponseEntity.ok(combos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
}
