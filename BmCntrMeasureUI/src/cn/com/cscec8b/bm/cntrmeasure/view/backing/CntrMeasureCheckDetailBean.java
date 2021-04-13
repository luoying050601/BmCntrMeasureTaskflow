package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;

import oracle.adf.model.binding.DCIteratorBinding;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.layout.RichPanelFormLayout;
import oracle.adf.view.rich.component.rich.layout.RichPanelGroupLayout;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.event.DialogEvent;

import oracle.adf.view.rich.event.ReturnPopupEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.JboException;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;


public class CntrMeasureCheckDetailBean extends CustomManagedBean {
    private RichTable measureTable;
    private RichTable verifyLinesTable;
    private RichPanelFormLayout verifyHeaderForm;
    // private RichPanelStretchLayout verifyPanelLayout;
    private RichPopup p3;
    private RichPopup measurePopup;
    private RichInputText actualOutputIT;
    private RichPanelStretchLayout verifyStretchLayout;
    private Number proSubcntrOutput;
    private Number actualOutput;
    //  private RichPanelGroupLayout measureCheckGroupLayout;

    public CntrMeasureCheckDetailBean() {
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public void setActualOutput(Number value){
        this.actualOutput = value;
    }
    /**
     *
     * 计算实际产值合计
     *
     *
     * **/
    public Number getActualOutput() {
        return getTotal("ProductionTotal");
    }
    
    public void setProSubcntrOutput(Number value){
        this.proSubcntrOutput = value;
    }

    /**
     *
     *
     *计算其中含专业甲指分包合计
     *
     *
     * **/
    public Number getProSubcntrOutput() {
        //    return getTotal("ProductionProSubcntr");

        DCIteratorBinding verifyLineT1Iterator =
            this.findIterator("BmCntrVerifyLineT1Iterator");
        RowSetIterator rsi = verifyLineT1Iterator.getRowSetIterator();
        rsi.setRangeSize(-1);
        Number total = new Number(0);
        for (Row row : rsi.getAllRowsInRange()) {
            if (null == (row.getAttribute("ProductionTotal"))) {
                row.setAttribute("ProductionTotal", new Number(0));
            }
            if ("Y".equals(row.getAttribute("ProSubcntrFlag"))) {
                total =
                        (Number)(total.add((Number)row.getAttribute("ProductionTotal"))).round(2);
            }
        }
        return total;
    }


    /**
     *
     * 计算 cumulativeActualOutput 行汇总
     * **/
    public Number getCumulativeActualOutput() {
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        Number cumulativeActualOutput = new Number(0);
        //计算累积值
        if (currentRow != null) {
            cumulativeActualOutput =
                    (Number)(currentRow.getAttribute("OwerConfirmQuantity") ==
                             null ? new Number(0) :
                             (Number)currentRow.getAttribute("OwerConfirmQuantity")).round(2);
            Number cntrId = (Number)currentRow.getAttribute("CntrId");
            if (cntrId != null) {
                OperationBinding generateBinding =
                    this.findOperation("getCumulativeActualOutput");
                generateBinding.getParamsMap().put("cntrId", cntrId);
                Number cumu = (Number)generateBinding.execute();
                if (cumu != null && cumulativeActualOutput != null)
                    cumulativeActualOutput =
                            (Number)(cumu.add(cumulativeActualOutput)).round(2);
            }
        }
        return cumulativeActualOutput;
    }

    /**
     *
     * 计算行汇总
     * **/
    private Number getTotal(String attributeName) {
        DCIteratorBinding verifyLineT1Iterator =
            this.findIterator("BmCntrVerifyLineT1Iterator");
        RowSetIterator rsi = verifyLineT1Iterator.getRowSetIterator();
        rsi.setRangeSize(-1);
        Number total = new Number(0);
        for (Row row : rsi.getAllRowsInRange()) {
            if (null == (row.getAttribute(attributeName))) {
                row.setAttribute(attributeName, new Number(0));
            }
            total = total.add((Number)row.getAttribute(attributeName));
        }
        return total;
    }

    /**
     *
     * 保存方法
     *
     * **/
    public void saveAction() {
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = verifyHeaderT1Iterator.getCurrentRow();
        if (null != currentRow) {
            if (currentRow.getAttribute("DocumentNumber") == null ||
                "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
                OperationBinding generateBinding =
                    this.findOperation("generateDocumentNumber");
                generateBinding.getParamsMap().put("documentType",
                                                   "CNTR_MEASURE_VERIFY");
                generateBinding.getParamsMap().put("cntrId",
                                                   currentRow.getAttribute("CntrId"));
                String documentNumber = (String)generateBinding.execute();
                currentRow.setAttribute("DocumentNumber", documentNumber);
                if (currentRow.getAttribute("DocumentNumber") == null ||
                    "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
                    this.addErrorMessage("警告", "单号生成操作失败请重试");
                    return;
                }
            }
            currentRow.setAttribute("ActualOutput",
                                    this.getActualOutput().round(2));
            currentRow.setAttribute("CumulativeActualOutput",
                                    this.getCumulativeActualOutput().round(2));

            currentRow.setAttribute("ProSubcntrOutput",
                                    this.getProSubcntrOutput().round(2));
            currentRow.setAttribute("PreparedBy", this.getCurrentEmployeeId());
            currentRow.setAttribute("Status", "EDIT");

            try {
                Object result = this.findOperation("Commit").execute();
                if (result == null) {
                    this.addInfoMessage("提示", "保存成功");
                }
            } catch (JboException ex) {
                this.findOperation("Rollback").execute();
                throw ex;
            }

        }
        this.refreshComponent(this.getVerifyStretchLayout());

    }


    private String queryMeasureByHeaderId(Number cntrMeasureId) {
        ViewObjectImpl measureHeaderVo =
            (ViewObjectImpl)this.findIterator("BmCntrMeasureHeaderT1Iterator").getViewObject();
        ViewCriteria vc = measureHeaderVo.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        vcr.setAttribute("CntrMeasureHeaderId", cntrMeasureId);
        vc.add(vcr);
        measureHeaderVo.applyViewCriteria(vc);
        measureHeaderVo.executeQuery();
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        //  计算产值合计，计算甲指分包合计
        Row measureRow = measureHeaderT1Iterator.getCurrentRow();
        String accountFlag = "";
        if (measureRow != null)
            accountFlag = (String)measureRow.getAttribute("AccountFlag");
        measureHeaderVo.applyViewCriteria(measureHeaderVo.getViewCriteria("BmCntrMeasureHeaderForVerifyCriteria"));
        measureHeaderVo.executeQuery();
        return accountFlag;

    }

    /**
     *
     * 确认方法
     *
     * **/
    public void submitAction() {
        //        BmCntrVerifyHeaderT1Iterator
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = verifyHeaderT1Iterator.getCurrentRow();

        if (currentRow.getAttribute("DocumentNumber") == null ||
            "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
            OperationBinding generateBinding =
                this.findOperation("generateDocumentNumber");
            generateBinding.getParamsMap().put("documentType",
                                               "CNTR_MEASURE_VERIFY");
            generateBinding.getParamsMap().put("cntrId",
                                               currentRow.getAttribute("CntrId"));
            String documentNumber = (String)generateBinding.execute();
            currentRow.setAttribute("DocumentNumber", documentNumber);
            if (currentRow.getAttribute("DocumentNumber") == null ||
                "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
                this.addErrorMessage("警告", "单号生成操作失败请重试");
                return;
            }
        }

        //修改计量单的状态
        DCIteratorBinding verifyLineT1Iterator =
            this.findIterator("BmCntrVerifyLineT1Iterator");
        RowSetIterator rsi = verifyLineT1Iterator.getRowSetIterator();
        List measureHeaderIds = new ArrayList();
        String errorMsg = "";
        for (Row row : rsi.getAllRowsInRange()) {
            //校验是否该行的计量单号为未审核状态：AccountFlag='N'
            String accountFlag =
                this.queryMeasureByHeaderId((Number)row.getAttribute("CntrMeasureHeaderId"));
            if (currentRow != null) {
                if (accountFlag != null && "N".equals(accountFlag.trim())) {
                    measureHeaderIds.add(((Number)row.getAttribute("CntrMeasureHeaderId")).intValue());
                } else {
                    errorMsg +=
                            "    " + row.getAttribute("DocumentNumber") + "    ";
                }
            }
        }
        if (errorMsg.trim() != "") {
            this.addErrorMessage("警告", "计量单号为" + errorMsg + "已被确认,请检查\n");
            return;
        }
        if (((Number)currentRow.getAttribute("OwerConfirmQuantity")).doubleValue() >
            0 && measureHeaderIds.size() == 0) {
            this.addErrorMessage("警告", "没有符合条件的计量单可操作，请重新选择");
        } else if (((Number)currentRow.getAttribute("OwerConfirmQuantity")).doubleValue() >
                   0 && measureHeaderIds.size() > 0) {
            OperationBinding updateBindings =
                this.findOperation("updateSelectedRowAccountFlag");
            updateBindings.getParamsMap().put("measureHeaderIds",
                                              measureHeaderIds);
            Object result = updateBindings.execute();
            if ("true".equals(result.toString())) {
                //修改提交状态
                //  计算产值合计，计算甲指分包合计
                currentRow.setAttribute("ActualOutput",
                                        this.getActualOutput().round(2));
                currentRow.setAttribute("CumulativeActualOutput",
                                        this.getCumulativeActualOutput().round(2));
                currentRow.setAttribute("ProSubcntrOutput",
                                        this.getProSubcntrOutput().round(2));
                currentRow.setAttribute("Status", "CONFIRMED");
                currentRow.setAttribute("PreparedBy",
                                        this.getCurrentEmployeeId());
                try {
                    Object commitResult =
                        this.findOperation("Commit").execute();
                    if (commitResult == null) {
                        this.addInfoMessage("提示", "操作成功");
                        this.setExpressionValue("#{pageFlowScope.disableAction}",
                                                "true");
                    }
                } catch (JboException ex) {
                    this.findOperation("Rollback").execute();
                    throw ex;
                }

            } else {
                this.addErrorMessage("错误", "操作失败，请重试");
            }
            this.refreshComponent(this.getVerifyStretchLayout());
        } else {
            currentRow.setAttribute("ActualOutput",
                                    this.getActualOutput().round(2));
            currentRow.setAttribute("CumulativeActualOutput",
                                    this.getCumulativeActualOutput().round(2));
            currentRow.setAttribute("ProSubcntrOutput",
                                    this.getProSubcntrOutput().round(2));
            currentRow.setAttribute("Status", "CONFIRMED");
            currentRow.setAttribute("PreparedBy", this.getCurrentEmployeeId());
            try {
                Object commitResult = this.findOperation("Commit").execute();
                if (commitResult == null) {
                    this.addInfoMessage("提示", "操作成功");
                    this.setExpressionValue("#{pageFlowScope.disableAction}",
                                            "true");
                }
            } catch (JboException ex) {
                this.findOperation("Rollback").execute();
                throw ex;
            }


        }


    }


    /***
     *
     * 批量新增行
     *
     * **/
    public void createMeasureLineListener(DialogEvent dialogEvent) {
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            DCIteratorBinding verifyLineT1Iterator =
                this.findIterator("BmCntrVerifyLineT1Iterator");
            verifyLineT1Iterator.setRangeSize(-1);
            Row[] allRow = verifyLineT1Iterator.getAllRowsInRange();
            List<Row> checkedRows =
                this.getCheckedRows("BmCntrMeasureHeaderT1Iterator");
            for (Row verifylineRow : allRow) {
                Number vMeasureHeaderId =
                    (Number)verifylineRow.getAttribute("CntrMeasureHeaderId");
                for (Row currentRow : checkedRows) {
                    Number cntrMeasureHeaderId =
                        ((Number)currentRow.getAttribute("CntrMeasureHeaderId"));
                    //判断当前选中行是否已经新增进去
                    if (cntrMeasureHeaderId.equals(vMeasureHeaderId)) {
                        verifylineRow.remove();
                        break;
                    }
                }
            }
            checkedRows = this.getCheckedRows("BmCntrMeasureHeaderT1Iterator");
            for (Row currentRow : checkedRows) {
                this.createNewRow(currentRow);
            }

            this.refreshComponent(this.getVerifyStretchLayout());
        }
    }


    public void setMeasureTable(RichTable measureTable) {
        this.measureTable = measureTable;
    }

    public RichTable getMeasureTable() {
        return measureTable;
    }

    public void setVerifyLinesTable(RichTable verifyLinesTable) {
        this.verifyLinesTable = verifyLinesTable;
    }

    public RichTable getVerifyLinesTable() {
        return verifyLinesTable;
    }

    /***
     * 删除所选行弹窗
     *
     * **/
    public void deleteSeletedLineListener(ActionEvent actionEvent) {
        Row row =
            this.findIterator("BmCntrVerifyLineT1Iterator").getCurrentRow();
        if (row == null) {
            this.addErrorMessage("错误", "请先选择一行，再删除");
            return;
        }
        this.showPopupActionListener(actionEvent, "是否确认删除该行？",
                                     "#{backingBeanScope.cntrMeasureCheckDetailBean.DeleteLineConfirmListener}");


    }


    /***
     *
     * 批量新增行
     *
     * **/
    public void DeleteLineConfirmListener(DialogEvent dialogEvent) {
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            Row headerRow =
                this.findIterator("BmCntrVerifyHeaderT1Iterator").getCurrentRow();
            Row lineRow =
                this.findIterator("BmCntrVerifyLineT1Iterator").getCurrentRow();
            lineRow.remove();
            headerRow.setAttribute("ActualOutput",
                                   this.getActualOutput().round(2));
            headerRow.setAttribute("ProSubcntrOutput",
                                   this.getProSubcntrOutput().round(2));
            this.refreshComponent(this.getVerifyStretchLayout());
        }
    }

    /**
     *
     * 删除头行确认方法
     *
     * **/
    public void deleteConfirmListener(DialogEvent dialogEvent) {
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            this.deleteHeaderAndLines();
        }

    }

    private void deleteHeaderAndLines() {
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        String status =
            (String)verifyHeaderT1Iterator.getCurrentRow().getAttribute("Status");
        if ("EDIT".equals(status)) {
            Row[] lineRows =
                this.getIteratorAllRows("BmCntrVerifyLineT1Iterator");
            if (lineRows != null && lineRows.length > 0) {
                for (Row row : lineRows) {
                    row.remove();
                }
            }
            Row headerRow = verifyHeaderT1Iterator.getCurrentRow();
            headerRow.remove();

            try {
                Object result = this.findOperation("Commit").execute();
                if (null == result) {
                    this.executeAction("back");

                }
            } catch (JboException ex) {
                this.findOperation("Rollback").execute();
                throw ex;
            }

        } else {
            this.addErrorMessage("警告", "该核量单非录入状态，不能删除");
        }
    }

    public void setVerifyHeaderForm(RichPanelFormLayout verifyHeaderForm) {
        this.verifyHeaderForm = verifyHeaderForm;
    }

    public RichPanelFormLayout getVerifyHeaderForm() {
        return verifyHeaderForm;
    }


    //    public void setVerifyPanelLayout(RichPanelStretchLayout verifyPanelLayout) {
    //        this.verifyPanelLayout = verifyPanelLayout;
    //    }
    //
    //    public RichPanelStretchLayout getVerifyPanelLayout() {
    //        return verifyPanelLayout;
    //    }

    /**
     *
     * 返回前确认事件
     *
     * **/
    public void returnConfirmListener(DialogEvent dialogEvent) {
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            this.findOperation("Rollback").execute();
            this.setExpressionValue("#{pageFlowScope.disableAction}", "false");
            this.executeAction("back");
        }

    }

    /***
     *
     * 返回事件
     *
     * **/
    public void returnActionListener(ActionEvent actionEvent) {
        boolean result = this.isTransactionDirty();
        if (true == result) {
            this.showPopupActionListener(actionEvent,
                                         "返回时若存在未保存数据，信息将会丢失。请确认是否返回？",
                                         "#{backingBeanScope.cntrMeasureCheckDetailBean.returnConfirmListener}");
        } else {
            this.findOperation("Rollback").execute();
            this.setExpressionValue("#{pageFlowScope.disableAction}", "false");
            this.executeAction("back");
        }
    }


    public void setP3(RichPopup p3) {
        this.p3 = p3;
    }

    public RichPopup getP3() {
        return p3;
    }

    /**
     * 全选
     * @param valueChangeEvent
     */
    public void checkAllValueChangeListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        this.checkAll(valueChangeEvent, "BmCntrMeasureHeaderT1Iterator");
        this.refreshComponent(this.getMeasureTable());
    }

    /***
     *
     * 新增BmCntrVerifyLineT1Iterator行
     *
     * **/
    private void createNewRow(Row currentRow) {
        OperationBinding copyBinding = this.findOperation("CreateInsert");
        copyBinding.execute();
        Row newRow =
            this.findIterator("BmCntrVerifyLineT1Iterator").getCurrentRow();
        if (null != newRow) {
            newRow.setAttribute("CntrMeasureHeaderId",
                                currentRow.getAttribute("CntrMeasureHeaderId"));
            newRow.setAttribute("DocumentNumber",
                                currentRow.getAttribute("DocumentNumber"));
            newRow.setAttribute("MeasureDate",
                                currentRow.getAttribute("MeasureDate"));

            newRow.setAttribute("ProSubcntrFlag",
                                currentRow.getAttribute("ProSubcntrFlag"));
            newRow.setAttribute("PreparedBy",
                                currentRow.getAttribute("PreparedBy"));
            newRow.setAttribute("PreparedByName",
                                currentRow.getAttribute("PreparedByName"));
            newRow.setAttribute("PreparedDate",
                                currentRow.getAttribute("PreparedDate"));
            newRow.setAttribute("ProductionTotal",
                                currentRow.getAttribute("ProductionTotal"));
        }

    }

    /***
     *
     * 关联计量单弹窗：
     * 1、设置pageFlowScope.cntrId的值
     * 2、showpopup
     *
     * **/
    public void connectToMeasurePopupListener(ActionEvent actionEvent) {
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = verifyHeaderT1Iterator.getCurrentRow();
        if (null != currentRow) {
            if (currentRow.getAttribute("CntrId") != null) {
                this.setExpressionValue("#{pageFlowScope.cntrId}",
                                        currentRow.getAttribute("CntrId"));
                this.showPopup(this.getMeasurePopup().getClientId(this.getFacesContext()));
            } else {
                this.addErrorMessage("错误", "请选择合同编号");
            }
        }

    }

    public void setMeasurePopup(RichPopup measurePopup) {
        this.measurePopup = measurePopup;
    }

    public RichPopup getMeasurePopup() {
        return measurePopup;
    }

    public void setActualOutputIT(RichInputText actualOutputIT) {
        this.actualOutputIT = actualOutputIT;
    }

    public RichInputText getActualOutputIT() {
        return actualOutputIT;
    }

    public void initCumulativeValueListener(ValueChangeEvent valueChangeEvent) {
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = verifyHeaderT1Iterator.getCurrentRow();
        if (null != currentRow) {
            currentRow.setAttribute("CumulativeActualOutput",
                                    this.getCumulativeActualOutput().round(2));
        }
    }

    public void initCumulativeAttributeListener(ReturnPopupEvent returnPopupEvent) {
        DCIteratorBinding verifyHeaderT1Iterator =
            this.findIterator("BmCntrVerifyHeaderT1Iterator");
        Row currentRow = verifyHeaderT1Iterator.getCurrentRow();
        if (null != currentRow) {
            currentRow.setAttribute("CumulativeActualOutput",
                                    this.getCumulativeActualOutput().round(2));
            this.refreshComponent(this.getVerifyStretchLayout());
        }
    }

    public void setVerifyStretchLayout(RichPanelStretchLayout verifyStretchLayout) {
        this.verifyStretchLayout = verifyStretchLayout;
    }

    public RichPanelStretchLayout getVerifyStretchLayout() {
        return verifyStretchLayout;
    }
}
