package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrMeasure;

import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.binding.OperationBinding;

import oracle.jbo.ViewObject;

public class cntrMeasureApplyDetailExportExcelBean extends CustomManagedBean {
    private RichCommandToolbarButton cntrMeasureApplyDetailExportExcelBtn;

    public cntrMeasureApplyDetailExportExcelBean() {
    }

    public void setCntrMeasureApplyDetailExportExcelBtn(RichCommandToolbarButton cntrMeasureApplyDetailExportExcelBtn) {
        this.cntrMeasureApplyDetailExportExcelBtn =
                cntrMeasureApplyDetailExportExcelBtn;
    }

    public RichCommandToolbarButton getCntrMeasureApplyDetailExportExcelBtn() {
        return cntrMeasureApplyDetailExportExcelBtn;
    }

    public void cntrMeasureApplyDetailExportExcel(FacesContext facesContext,
                                                  OutputStream outputStream) {

        ExportExcel<CntrMeasure> export = new ExportExcel<CntrMeasure>();
        //定义表格的头信息
        String[] headers =
        { "序号", "清单名称", "*合同清单项","合同清单编码", "计量单位", "施工图预算总量", "累计完成量", "完成百分比", "*工程量",
          "单价", "产值", "*核算对象" };
        //获取AM中方法代码省略
        DCBindingContainer bindings =
            (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();

        OperationBinding getExportData =
            bindings.getOperationBinding("getExportData");

        List<CntrMeasure> exampleList = new ArrayList<CntrMeasure>();
        List<CntrMeasure> list = (List<CntrMeasure>)getExportData.execute();
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
