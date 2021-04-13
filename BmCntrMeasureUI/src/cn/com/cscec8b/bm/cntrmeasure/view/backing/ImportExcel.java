package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.myfaces.trinidad.model.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ImportExcel {
    static Short[] yyyyMMdd =
    { 14, 15, 16, 17, 22, 27, 28, 30, 31,57, 58, 176, 177, 178, 179, 180, 181,
      182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195,
      196, 197, 198, 199, 200, 201, 202, 203, 205, 206, 207 };
    static List yyyyMMddList = Arrays.asList(yyyyMMdd);
    public static String checkUpLoadFile(UploadedFile uploadfile) {
        String fileType = null;
        String fileName = null;
        String result = null;
        if (uploadfile != null) {
            fileName = uploadfile.getFilename();
            fileType = getFileExtension(fileName);
            if ((fileType == null) || (fileName == null)) {
                result = "Only Support xls or xlsx File.";
                return result;
            }
            if (fileType.equals("xls"))
                result = "xls";
            else if (fileType.equals("xlsx"))
                result = "xlsx";
            else
                result = "Only Support xls or xlsx File.";
        } else {
            result = "Please choose a file to import.";
        }
        return result;
    }

    public static List importExcelFile(UploadedFile uploadFile,
                                       String fileFlag) {
        List list = new ArrayList();
        try {
            InputStream is = uploadFile.getInputStream();
            if ((null != uploadFile) && (null != is))
                list = ReadExl(is, fileFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static String getFileExtension(String fileName) {
        if (fileName != null) {
            int id = fileName.lastIndexOf('.');
            if ((id > 0) && (id < fileName.length() - 1)) {
                return fileName.substring(id + 1).toLowerCase();
            }
        }
        return null;
    }

    public static String getMyStringCellValue(Cell cell) {
        String strCell = "";
        short format = cell.getCellStyle().getDataFormat();
        SimpleDateFormat sdf = null;
        switch (cell.getCellType()) {
        case 1:
            strCell = cell.getStringCellValue();
            break;
        case 0:
            if (yyyyMMddList.contains(format)) {
              //  System.out.println(1);
                    
            //          if (format == 14 || format == 31 || format == 57 ||
            //                                format == 58) {
                sdf = new SimpleDateFormat("yyyy年MM月");
             //   System.out.println(sdf);
                    
                double value = cell.getNumericCellValue();
                Date date =
                    org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                strCell = sdf.format(date);
            //       System.out.println(strCell);

            } else if (format == 20 || format == 32) {
            //    System.out.println(2);

                sdf = new SimpleDateFormat("HH:mm");
                double value = cell.getNumericCellValue();
                Date date =
                    org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                strCell = sdf.format(date);
            } else {
             //   System.out.println(3);

                if (cell.getNumericCellValue() % 1.0 == 0) {
                    strCell = String.valueOf((long)cell.getNumericCellValue());
                } else {
                    strCell =
                            new Double(cell.getNumericCellValue()).toString();
                }
            }
            break;
        case 4:
            strCell = String.valueOf(cell.getBooleanCellValue());
            break;
        case 3:
            strCell = "";
            break;
        case 2:
            strCell = cell.getStringCellValue();
            break;
        case 5:
            strCell = "CELL_TYPE_ERROR";
            break;
        default:
            strCell = "";
        }

        if ((strCell.equals("")) || (strCell == null)) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }

    public static List ReadExl(InputStream inputStream, String fileFlag) {
        Workbook workbook = null;
        Sheet sheet1 = null;
        List list = new ArrayList();
        try {
            if (fileFlag.equals("xls"))
                workbook = new HSSFWorkbook(inputStream);
            else if (fileFlag.equals("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            }
            sheet1 = workbook.getSheetAt(0);
            Row header = sheet1.getRow(0); //Get the header object
            if (null != header) {
                int colNum =
                    header.getPhysicalNumberOfCells(); // get the number of columns
                int rowNum = sheet1.getLastRowNum();
                for (int i = 1; i <= rowNum; i++) {
                    Row rowCell = sheet1.getRow(i);
                    List listRow = new ArrayList();
                    int j = 0;
                    boolean flag = false;
                    while (j < colNum && rowCell != null) {
                        Cell cell = rowCell.getCell(j);
                        if (cell != null) {
                            String cellValue = getMyStringCellValue(cell);
                            if ("CELL_TYPE_ERROR".equals(cellValue)) {
                                CellReference cellRef =
                                    new CellReference(rowCell.getRowNum(),
                                                      cell.getColumnIndex());
                                listRow.add(null);
                            } else {
                                if (!"".equals(cellValue))
                                    flag = true;
                                listRow.add(cellValue);
                            }
                        } else {
                            listRow.add(null);
                        }
                        j++;
                    }
                    if (flag)
                        list.add(listRow);
                }
            }
        } catch (IOException e) {
            System.err.println("IO error occurred while reading excel file.");
        }
        return list;
    }
}
