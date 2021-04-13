package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrCheck;
import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrMeasure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;

import oracle.binding.OperationBinding;

public class CntrMeasureCheckDetailExportExcelBean {
    public CntrMeasureCheckDetailExportExcelBean() {
    }

    public void exportExcelActionListener(FacesContext facesContext,
                                          OutputStream outputStream) {
        ExportExcel<CntrCheck> export = new ExportExcel<CntrCheck>();
        //定义表格的头信息
        String[] headers =
        { "序号", "核量单号", "业主确认日期从", "业主确认日期至","累计业主确认产值", "实际产值合计", "其中含“甲指专业分包产值”",
          "业主确认产值", "其中含“甲指专业分包及甲供材产值”", "其中含“已确认总包签证量”", "合同约定业主确认日期",
          "合同约定累计付款金额", "其中含劳保费", "其中含安措费", "合同约定付款日期", "财务已确认金额" , "财务未确认金额" , "状态" };
        //获取AM中方法代码省略
        DCBindingContainer bindings =
            (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();

        OperationBinding getExportCheckData =
            bindings.getOperationBinding("getExportCheckData");

        List<CntrCheck> exampleList = new ArrayList<CntrCheck>();
        List<CntrCheck> list = (List<CntrCheck>)getExportCheckData.execute();
        try {
            if (list != null && list.size() > 0) {
                export.exportExcel(headers, list, outputStream);
            } else {
                export.exportExcel(headers, exampleList, outputStream);
            }
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
