package com.tlcn.books.fileIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplyDiscountExcelImporter {
    public static List<String> importAppliedBooks(InputStream inputStream) throws IOException {
        List<String> appliedBooksId = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            //Skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();

            String bookId = "";
            int cellIdx = 0;

            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0:
                        bookId = currentCell.getStringCellValue();
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }

            appliedBooksId.add(bookId);

        }

        workbook.close();
        return appliedBooksId;
    }
}
