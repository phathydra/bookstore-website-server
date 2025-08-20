package com.bookstore.accounts.fileIO;

import com.bookstore.accounts.dto.AccountDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountExcelImporter {

    public static List<AccountDto> importAccounts(InputStream inputStream) throws IOException {
        List<AccountDto> accounts = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();

            // Bỏ qua header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            AccountDto account = new AccountDto();

            // Đọc từng cột (theo thứ tự trong file Excel)
            Cell emailCell = currentRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell passwordCell = currentRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell roleCell = currentRow.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell statusCell = currentRow.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            account.setEmail(getCellValueAsString(emailCell));
            account.setPassword(getCellValueAsString(passwordCell));
            account.setRole(getCellValueAsString(roleCell));
            account.setStatus(getCellValueAsString(statusCell));

            accounts.add(account);
        }

        workbook.close();
        return accounts;
    }

    // Hàm phụ: convert Cell -> String
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}