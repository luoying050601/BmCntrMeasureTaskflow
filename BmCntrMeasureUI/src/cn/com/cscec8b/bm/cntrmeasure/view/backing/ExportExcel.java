package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExportExcel<T>{
    public void exportExcel(Collection<T> dataset, OutputStream out)
         {
           exportExcel("EXCEL文档", null, dataset, out, "yyyy-MM-dd");
         }

         public void exportExcel(String[] headers, Collection<T> dataset, OutputStream out)
         {
           exportExcel("EXCEL文档", headers, dataset, out, "yyyy-MM-dd");
         }

         public void exportExcel(String[] headers, Collection<T> dataset, OutputStream out, String pattern)
         {
           exportExcel("EXCEL文档", headers, dataset, out, pattern);
         }

         public void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out)
         {
           exportExcel(title, headers, dataset, out, "yyyy-MM-dd");
         }

         public void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out, String pattern)
         {
           HSSFWorkbook workbook = new HSSFWorkbook();

           HSSFSheet sheet = workbook.createSheet(title);

           sheet.setDefaultColumnWidth(15);
           
              
          HSSFDataFormat format = workbook.createDataFormat();  

           HSSFCellStyle style = workbook.createCellStyle();
           style.setDataFormat(format.getFormat("@"));
           style.setFillForegroundColor((short)1);
           style.setFillPattern((short)1);
           style.setBorderBottom((short)1);
           style.setBorderLeft((short)1);
           style.setBorderRight((short)1);
           style.setBorderTop((short)1);
           style.setAlignment((short)1);//2

           HSSFFont font = workbook.createFont();
    //           font.setFontHeightInPoints((short)12);
           font.setBoldweight((short)700);

           style.setFont(font);

           HSSFCellStyle style2 = workbook.createCellStyle();
           style2.setDataFormat(format.getFormat("@"));
           style2.setFillForegroundColor((short)1);
           style2.setFillPattern((short)1);
           style2.setBorderBottom((short)1);
           style2.setBorderLeft((short)1);
           style2.setBorderRight((short)1);
           style2.setBorderTop((short)1);
           style2.setAlignment((short)1);
           style2.setVerticalAlignment((short)1);

           HSSFFont font2 = workbook.createFont();
           font2.setBoldweight((short)400);

           style2.setFont(font2);

           HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

//           HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short)4,
//                           2, (short)6, 5));
//
//       //    comment.setString(new HSSFRichTextString("you can add comment here！"));
//
//           comment.setAuthor("HAND");

           HSSFRow row = sheet.createRow(0);
           for (int i = 0; i < headers.length; i = (i + 1)) {
             HSSFCell cell = row.createCell(i);
             cell.setCellStyle(style);
             HSSFRichTextString text = new HSSFRichTextString(headers[i]);
             cell.setCellValue(text);
           }
           
           Iterator it = dataset.iterator();
           int index = 0;
           while (it.hasNext()) {
             index++;
             row = sheet.createRow(index);
             Object t = it.next();

             Field[] fields = t.getClass().getDeclaredFields();
             for (int i = 0; i < fields.length; i = (i + 1)) {
               HSSFCell cell = row.createCell(i);
               cell.setCellStyle(style2);
               Field field = fields[i];
               String fieldName = field.getName();
               String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
               try
               {
                 Class tCls = t.getClass();
                 Method getMethod = tCls.getMethod(getMethodName, new Class[0]);

                 Object value = getMethod.invoke(t, new Object[0]);

                 String textValue = null;
                 if ((value instanceof Boolean)) {
                   boolean bValue = ((Boolean)value).booleanValue();
                   textValue = "是";
                   if (!bValue)
                     textValue = "否";
                 }
                 else if ((value instanceof Date)) {
                   Date date = (Date)value;
                   SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                   textValue = sdf.format(date);
                 } else if ((value instanceof byte[]))
                 {
                   row.setHeightInPoints(60.0F);

                   sheet.setColumnWidth(i, 2856);

                   byte[] bsValue = (byte[])(byte[])(byte[])value;
                   HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255, (short)6,
                                       index, (short)6, index);
                     
       //                anchor.setAnchorType(2);
                     
                   patriarch.createPicture(anchor, workbook.addPicture(bsValue, 5));
                 }
                 else
                 {
                     textValue = value==null? "": value.toString();
                 }

                 if (textValue != null) {
                   Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                   Matcher matcher = p.matcher(textValue);
                   if (matcher.matches())
                   {
                     cell.setCellValue(Double.parseDouble(textValue));
                   } else {
                     HSSFRichTextString richString = new HSSFRichTextString(textValue);
       //              HSSFFont font3 = workbook.createFont();
       //              font3.setColor((short)12);
       //              richString.applyFont(font3);
                     cell.setCellValue(richString);
                   }
                 }
               }
               catch (SecurityException e) {
                 e.printStackTrace();
               }
               catch (NoSuchMethodException e) {
                 e.printStackTrace();
               }
               catch (IllegalArgumentException e) {
                 e.printStackTrace();
               }
               catch (IllegalAccessException e) {
                 e.printStackTrace();
               }
               catch (InvocationTargetException e) {
                 e.printStackTrace();
               }
               finally
               {
               }
             }
           }
           try {
             workbook.write(out);
           }
           catch (IOException e) {
             e.printStackTrace();
           }
         }
}
