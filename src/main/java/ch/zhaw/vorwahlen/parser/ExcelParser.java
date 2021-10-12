package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.LookupTable;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract excel parser for type {@code <T>}.<br/>
 *
 * To use this class you need a model class representing {@code <T>} which holds the desired data.<br/>
 * To find out on which cells the desired fields are we use a lookup table {@code <S>}.<br/>
 * This lookup table is an Enum which has to contain the header string and the cell number (default: -1).<br/>
 * With this string the parser can scan the Excel sheet and set the cell number (which column the header was found).<br/>
 *
 * @param <T> type to be parsed from the provided Excel sheet.
 * @param <S> lookup table from type enum.
 */
@RequiredArgsConstructor
public abstract class ExcelParser<T, S extends LookupTable<?>> {
    private final String fileLocation;
    private final String workSheet;
    private final Class<S> clazz;

    /**
     * Parse all modules from the provided Excel sheet.
     * @return list of {@code} <T>}.
     * @throws IOException if file not found or file not an Excel sheet.
     */
    public List<T> parseModulesFromXLSX() throws IOException {
        List<T> moduleList = new ArrayList<>();
        T object;

        try (var fis = new FileInputStream(fileLocation)) {
            var workbook = new XSSFWorkbook(fis);
            var moduleSheet = workbook.getSheet(workSheet);

            var rowIterator = moduleSheet.rowIterator();
            if (rowIterator.hasNext()) {
                setCellIndexes(rowIterator.next());
            }

            while (rowIterator.hasNext()) {
                if ((object = createObjectFromRow(rowIterator.next())) != null) {
                    moduleList.add(object);
                }
            }

        }
        return moduleList;
    }

    /**
     * Set all columns in lookup table where the constant enum value is found.
     * @param row Excel sheet row.
     */
    void setCellIndexes(Row row) {
        for (var cell : row) {
            var cellValue = cell.getStringCellValue().trim().replace("\n", "");

            S stringTable = LookupTable.getConstantByValue(cellValue, clazz);
            if (stringTable != null) {
                stringTable.setCellNumber(cell.getColumnIndex());
            }
        }
    }

    /**
     * Create <T> from the Excel sheet row.
     * @param row Excel sheet row.
     * @return <T>
     */
    abstract T createObjectFromRow(Row row);
}