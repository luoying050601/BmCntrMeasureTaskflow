package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.dbpm.DBpmApprovalChainService;
import cn.com.cscec8b.dbpm.types.DocumentOrderType;
import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import cn.com.cscec8b.utils.db.DBUtil;

import java.math.BigDecimal;

import java.sql.SQLException;

import oracle.adf.view.rich.component.rich.RichPopup.PopupHints;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichColumn;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.fragment.RichRegion;
import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import oracle.adf.view.rich.event.DialogEvent;

import oracle.adf.view.rich.event.ReturnPopupEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.JboException;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.domain.Number;

import oracle.jbo.uicli.binding.JUCtrlListBinding;


public class CntrMeasureApplyDetailBean extends CustomManagedBean {

    @SuppressWarnings("compatibility:7496049541458934054")
    private static final long serialVersionUID = 1L;
    private RichTable measureLinesTable;
    private RichPanelStretchLayout measurePanelStretchLayout;
    private RichInputListOfValues cntrName;
    private RichPopup importPop;
    private RichColumn completage;
    private RichPopup historPopup;


    public CntrMeasureApplyDetailBean() {
    }

    /**
     *
     * 计算MeasureAmount行汇总
     * **/
    public Number getProductionTotal() {
        Number productionTotalValue = new Number(0);

        DCIteratorBinding measureLineT1Iterator =
            this.findIterator("BmCntrMeasureLineT1Iterator");
        RowSetIterator rsi = measureLineT1Iterator.getRowSetIterator();
        rsi.setRangeSize(-1);
        for (Row row : rsi.getAllRowsInRange()) {
            productionTotalValue =
                    (Number)productionTotalValue.add(((Number)row.getAttribute("MeasureAmount")).round(2));
        }
        return productionTotalValue;
    }


    /**
     *
     * 计算cumulativeProductionAmount行汇总
     * **/
    public Number getCumulativeProductionAmount() {
        Number cumulativeProductionAmount = new Number(0);
        //计算累积值
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        if (currentRow != null) {
            Number cntrId = (Number)currentRow.getAttribute("CntrId");
            if (cntrId != null) {
                OperationBinding generateBinding =
                    this.findOperation("getCumulativeProductionAmount");
                generateBinding.getParamsMap().put("cntrId", cntrId);
                Number cumu = (Number)generateBinding.execute();
                if (cumu != null)
                    cumulativeProductionAmount =
                            (Number)(cumu.add(getProductionTotal())).round(2);
            }
        }
        return cumulativeProductionAmount;
    }

    /**
     *
     * 计算VatAmount行汇总
     * **/
    public Number getVatAmount() {
        Number vatAmount = new Number(0);
        DCIteratorBinding measureLineT1Iterator =
            this.findIterator("BmCntrMeasureLineT1Iterator");
        RowSetIterator rsi = measureLineT1Iterator.getRowSetIterator();
        rsi.setRangeSize(-1);
        Row[] allRow = rsi.getAllRowsInRange();
        for (Row row : allRow) {
            if (row.getAttribute("LineTaxRate") != null &&
                row.getAttribute("MeasureAmount") != null){
                    String taxRate = (String)row.getAttribute("LineTaxRate");


                try {
                    vatAmount =
                        (Number)vatAmount.add(((Number)row.getAttribute("MeasureAmount")).multiply(new Number(taxRate)).divide(100).round(2));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            }
        //        Number productionTotal = getProductionTotal();
        //        DCIteratorBinding measureHeaderT1Iterator =
        //            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        //        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        //        if (currentRow != null) {
        //            try {
        //                String taxRate = (String)currentRow.getAttribute("TaxRate");
        //                if (taxRate != null) {
        //                    vatAmount =
        //                            (Number)productionTotal.multiply(new Number(taxRate)).divide(100).round(2);
        //                }
        //            } catch (SQLException e) {
        //                e.printStackTrace();
        //            }
        //        }
        return vatAmount;
    }

    /**
     *
     * 计算 ProductionProSubcntr 行汇总
     * **/
    public Number getProductionProSubcntr() {

        //        DCIteratorBinding measureLineT1Iterator =
        //            this.findIterator("BmCntrMeasureLineT1Iterator");
        //        RowSetIterator rsi = measureLineT1Iterator.getRowSetIterator();
        Number TotalValue = new Number(0);
        //        rsi.setRangeSize(-1);
        //        Row[] allRow = rsi.getAllRowsInRange();
        //        for (Row row : allRow) {
        //            if (row.getAttribute("ProductionProAmount") != null &&
        //                row.getAttribute("EngineeringQuantity") != null)
        //                TotalValue =
        //                        TotalValue.add(((Number)row.getAttribute("ProductionProAmount")).multiply((Number)row.getAttribute("EngineeringQuantity")));
        //        }
        return TotalValue;
    }


    /**
     * 保存方法
     * **/
    public void saveAction() {
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        if (currentRow != null) {
            if (currentRow.getAttribute("DocumentNumber") == null ||
                "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
                OperationBinding generateBinding =
                    this.findOperation("generateDocumentNumber");
                generateBinding.getParamsMap().put("documentType",
                                                   "CNTR_MEASURE_APPLY");
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
            //计算行汇总
            Number productionTotal = getProductionTotal();
            // Number productionProSubcntr = getProductionProSubcntr();
            Number cumulativeProductionAmount =
                getCumulativeProductionAmount();
            currentRow.setAttribute("ProductionTotal",
                                    productionTotal.round(2));
            currentRow.setAttribute("ProductionProSubcntr", new Number(0));
            currentRow.setAttribute("VatAmount", getVatAmount().round(2));
            currentRow.setAttribute("CumulativeProductionAmount",
                                    cumulativeProductionAmount.round(2));
            currentRow.setAttribute("PreparedBy", this.getCurrentEmployeeId());
            if (!"REJECTED".equals(currentRow.getAttribute("Status")))
                currentRow.setAttribute("Status", "EDIT");

            try {
                Object result = this.findOperation("Commit").execute();
                if (null == result) {
                    this.addInfoMessage("提示", "保存成功");
                    this.getCntrName().setDisabled(true);
                }
            } catch (JboException ex) {
                this.findOperation("Rollback").execute();
                throw ex;
            }

        }
        this.refreshComponent(this.getMeasurePanelStretchLayout());

    }


    /**
     *
     * 格式化百分比最小值
     *
     * **/
    public Number getRowCompleteRate() {
        Double com =
            (Double)this.resolveExpression("#{row.bindings.CompletionPercentage.inputValue.value}");
        String com1 = String.format("%.5f", com);
        try {
            Number rs = new Number(com1);
            if (rs.compareTo(100) > 0)
                return new Number(100);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Number(100);
    }

    /**
     *
     * 提交方法
     * 1、修改计量状态
     * **/
    public void submitAction() {
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        Number productionProSubcntr = getProductionProSubcntr();
        DCIteratorBinding measureLineT1Iterator =
            this.findIterator("BmCntrMeasureLineT1Iterator");
        Row[] lineRows = measureLineT1Iterator.getAllRowsInRange();
        for(Row r :lineRows){
            if(r.getAttribute("TaskId")==null){
                this.addErrorMessage("错误", "核算对象不能为空");
                return;
            }
        }
        //修改提交状态
        if (measureLineT1Iterator.getCurrentRow() == null) {
            this.addErrorMessage("错误", "请至少选择一项清单项进行报量");
            return;
        }
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        if (null != currentRow) {
            if (currentRow.getAttribute("DocumentNumber") == null ||
                "".equals(currentRow.getAttribute("DocumentNumber").toString().trim())) {
                OperationBinding generateBinding =
                    this.findOperation("generateDocumentNumber");
                generateBinding.getParamsMap().put("documentType",
                                                   "CNTR_MEASURE_APPLY");
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

            //调用流程
            try {
                //更新字段
                currentRow.setAttribute("ProductionProSubcntr", new Number(0));
                Number productionTotal = getProductionTotal();
                Number cumulativeProductionAmount =
                    getCumulativeProductionAmount();
                currentRow.setAttribute("ProductionTotal",
                                        productionTotal.round(2));
                currentRow.setAttribute("ProductionProSubcntr",
                                        productionProSubcntr.round(2));
                currentRow.setAttribute("VatAmount", getVatAmount().round(2));
                currentRow.setAttribute("CumulativeProductionAmount",
                                        cumulativeProductionAmount.round(2));
                currentRow.setAttribute("PreparedBy",
                                        this.getCurrentEmployeeId());
                currentRow.setAttribute("Status", "EDIT");
                this.findOperation("Commit").execute();

                DBpmApprovalChainService service =
                    new DBpmApprovalChainService();
                DocumentOrderType document = new DocumentOrderType();
                document.setSourceId(currentRow.getAttribute("CntrMeasureHeaderId").toString()); // 单据的头ID
                document.setSourceType("BM_CNTR_MEASURE"); // 单据类型编码
                document.setDocumentNumber(currentRow.getAttribute("DocumentNumber").toString()); // 单据编码
                document.setOrganizationId(new BigDecimal(this.getCurrentOrgId().toString()));
                document.setProjectId(new BigDecimal(this.getCurrentProjectId().toString()));
                document.setUserId(new BigDecimal(this.getCurrentEmployeeId().toString()));
                boolean result = service.start(document);
                if (result) {
                    currentRow.setAttribute("Status", "PROCESSING");
                    try {
                        this.findOperation("Commit").execute();
                    } catch (Exception e) {
                        this.findOperation("Rollback").execute();
                        e.printStackTrace();
                    }
                    this.addInfoMessage("提示", "提交成功");
                    this.setExpressionValue("#{pageFlowScope.disableAction}",
                                            "true");
                } else {
                    this.findOperation("Rollback").execute();
                    this.addErrorMessage("错误", "提交失败,请稍后重试");
                }

            } catch (JboException ex) {
                this.findOperation("Rollback").execute();
                throw ex;
            }
        }
        this.refreshComponent(this.getMeasurePanelStretchLayout());

    }

    /**
     * 删除前确认dialog事件
     *
     * **/
    public void confirmDeleteListener(DialogEvent dialogEvent) {
        // Add event code here...
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            DCIteratorBinding measureHeaderT1Iterator =
                this.findIterator("BmCntrMeasureHeaderT1Iterator");
            String status =
                (String)measureHeaderT1Iterator.getCurrentRow().getAttribute("Status");
            if ("EDIT".equals(status) || "REJECTED".equals(status)) {
                Row[] lineRows =
                    this.getIteratorAllRows("BmCntrMeasureLineT1Iterator");
                if (lineRows != null && lineRows.length > 0) {
                    for (Row row : lineRows) {
                        row.remove();
                    }
                }

                Row headerRow = measureHeaderT1Iterator.getCurrentRow();
                headerRow.remove();

                try {
                    Object result = this.findOperation("Commit").execute();
                    if (result == null) {
                        this.executeAction("back");
                    }
                } catch (JboException ex) {
                    this.findOperation("Rollback").execute();
                    throw ex;
                }
            } else {
                this.addErrorMessage("警告", "该计量单非录入或拒绝状态，不能删除");
            }
        }
    }


    /**
     * 单行删除前确认dialog事件
     *
     * **/
    public void DeleteLineConfirmListener(DialogEvent dialogEvent) {
        // Add event code here...
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            //            Row headerRow =
            //                this.findIterator("BmCntrMeasureHeaderT1Iterator").getCurrentRow();
            Row lineRow =
                this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
            lineRow.remove();
            this.refreshComponent(this.getMeasurePanelStretchLayout());

        }
    }

    /**
     *
     * 删除行表结构数据
     *
     * **/
    public void deleteSeletedLineAction(ActionEvent actionEvent) {
        Row row =
            this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
        if (row == null) {
            this.addErrorMessage("错误", "请先选择一行，再删除");
            return;
        }

        this.showPopupActionListener(actionEvent, "是否确认删除该行？",
                                     "#{backingBeanScope.cntrMeasureApplyDetailBean.DeleteLineConfirmListener}");


    }


    /**
     *
     * 工程量engineQuantities的ValueListener
     *
     * **/
    public void engineQuantitiesValueListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        oracle.adfinternal.view.faces.model.binding.FacesCtrlHierNodeBinding node =
            (oracle.adfinternal.view.faces.model.binding.FacesCtrlHierNodeBinding)this.resolveExpression("#{row}");

        // DCIteratorBinding measureLineT1Iterator =
        //   this.findIterator("BmCntrMeasureLineT1Iterator");
        Row currentRow = node.getRow();
        //measureLineT1Iterator.getCurrentRow();

        //工程量
        Number engineeringQuantities =
            (Number)currentRow.getAttribute("EngineeringQuantity");
        //预算工程总量
        Number budgetQuantities =
            (Number)currentRow.getAttribute("BudgetQuantity");
        //累积完成量
        Number cumulativeQuantities =
            (Number)currentRow.getAttribute("CumulativeQuantity");
        Number tmp =
            (Number)currentRow.getAttribute("CompletionPercentage") == null ?
            new Number(0) :
            (Number)currentRow.getAttribute("CompletionPercentage");
        Number completionPercentage = (Number)tmp.round(4);


        if (engineeringQuantities != null && budgetQuantities != null &&
            !budgetQuantities.equals(0)) {
            //百分比 =（ 工程量+累计完成量）/施工图预算量
            completionPercentage =
                    (Number)(engineeringQuantities.add(cumulativeQuantities)).div(budgetQuantities);
            if (completionPercentage.doubleValue() > 1) {
                this.addErrorMessage("错误", "累计完成百分比超过100%，请重新输入");
                //   currentRow.setAttribute("CompletionPercentage", 0);
                currentRow.setAttribute("EngineeringQuantity", 0);
                this.refreshComponent(this.getMeasureLinesTable());

                return;
            }
        } else if (engineeringQuantities == null) {
            this.addErrorMessage("错误", "请输入当期工程量");
            return;
        } else if (budgetQuantities.equals(0)) {
            this.addErrorMessage("错误", "施工图预算量为0，请重新选择报量");
            return;
        }
        //  currentRow.setAttribute("CompletionPercentage", 0);
        currentRow.setAttribute("CompletionPercentage",
                                new Number((completionPercentage.multiply(100))).round(2));

        //UnitPrice
        Number unitPrice = (Number)currentRow.getAttribute("UnitPrice");
        //MeasureAmount
        //当期产值=工程量*单价
        if (unitPrice != null) {
            Number measureAmount = engineeringQuantities.multiply(unitPrice);
            currentRow.setAttribute("MeasureAmount", measureAmount.round(2));
        }

        this.refreshComponent(this.getMeasureLinesTable());

    }

    /**
     *
     * 完成百分比completedPercentage的ValueListener
     *
     * **/
    public void completedPercentageValueListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        //  DCIteratorBinding measureLineT1Iterator =
        //    this.findIterator("BmCntrMeasureLineT1Iterator");
        //百分比
        //Row currentRow = measureLineT1Iterator.getCurrentRow();
        oracle.adfinternal.view.faces.model.binding.FacesCtrlHierNodeBinding node =
            (oracle.adfinternal.view.faces.model.binding.FacesCtrlHierNodeBinding)this.resolveExpression("#{row}");

        // DCIteratorBinding measureLineT1Iterator =
        //   this.findIterator("BmCntrMeasureLineT1Iterator");
        Row currentRow = node.getRow();
        Number tmp = new Number(0);
        if (currentRow.getAttribute("CompletionPercentage") != null)
            tmp =
new Number(((Number)currentRow.getAttribute("CompletionPercentage")).round(2));
        if (tmp != null) {
            Number completionPercentage = (Number)(tmp).div(100);
            if (completionPercentage.doubleValue() > 1) {
                this.addErrorMessage("错误", "累计完成百分比超过100%，请重新输入");
                currentRow.setAttribute("CompletionPercentage", 0);
                this.refreshComponent(this.getMeasureLinesTable());

                return;
            }
            //累计完成量
            Number cumulativeQuantities =
                (Number)currentRow.getAttribute("CumulativeQuantity");
            //预算工程总量
            Number budgetQuantities =
                (Number)currentRow.getAttribute("BudgetQuantity");
            Number engineeringQuantities =
                (Number)currentRow.getAttribute("EngineeringQuantity");
            //工程量=施工预算总量 multply 百分比-累计完成量
            engineeringQuantities =
                    (Number)budgetQuantities.multiply(completionPercentage).minus(cumulativeQuantities);

            currentRow.setAttribute("EngineeringQuantity", 0);
            currentRow.setAttribute("EngineeringQuantity",
                                    new Number((engineeringQuantities).round(5)));
            //UnitPrice
            Number unitPrice = (Number)currentRow.getAttribute("UnitPrice");
            //MeasureAmount
            //当期产值=工程量*单价
            if (unitPrice != null) {
                Number measureAmount =
                    engineeringQuantities.multiply(unitPrice);
                currentRow.setAttribute("MeasureAmount",
                                        measureAmount.round(2));
            }
            //    AdfFacesContext.getCurrentInstance().addPartialTarget(this.getMeasureLinesTable());
            this.refreshComponent(measureLinesTable);

        } else {
            this.addErrorMessage("错误", "请输入完成百分比");
            return;
        }


    }

    public void setMeasureLinesTable(RichTable measureLinesTable) {
        this.measureLinesTable = measureLinesTable;
    }

    public RichTable getMeasureLinesTable() {
        return measureLinesTable;
    }


    /**
     *
     * 单行复制事件
     *
     * **/
    public void copySelectedLine(ActionEvent actionEvent) {
        //#{bindings.CreateWithParams.execute}
        Row row =
            this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
        if (row == null) {
            this.addErrorMessage("警告", "请选择需要复制的行");
            return;
        } else {
            this.createWithParams(row);
        }
        this.refreshComponent(this.getMeasurePanelStretchLayout());
    }


    /***
     *
     * 根据字段新增清单项
     *
     * **/
    private void createWithParams(Row row) {
        OperationBinding copyBinding = this.findOperation("CreateWithParams");
        copyBinding.execute();
        Row newRow =
            this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
        newRow.setAttribute("CntrListHeaderId",
                            row.getAttribute("CntrListHeaderId"));
        newRow.setAttribute("CntrListLineId",
                            row.getAttribute("CntrListLineId"));
        newRow.setAttribute("CntrListName", row.getAttribute("CntrListName"));
        this.refreshComponent(this.getMeasureLinesTable());
    }


    /**
     * 根据下拉框绑定名称，获取下拉框中的值
     * @param listBindingName binding属性名
     * @return
     */
    private String getSelectOneChioceSelectedRow(String listBindingName,
                                                 String attributeName) {
        BindingContainer bindings =
            BindingContext.getCurrent().getCurrentBindingsEntry();

        JUCtrlListBinding listBinding =
            (JUCtrlListBinding)bindings.get(listBindingName);
        Row selectedValue = (Row)listBinding.getSelectedValue();
        if (selectedValue != null) {
            return (String)selectedValue.getAttribute(attributeName);
        } else {
            return null;
        }

    }


    /***
     *
     * 查询当前所选的清单项下的所有合同清单
     *
     * **/
    public void queryAllListLineListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
//        int cntrListId = 0;
//        if (valueChangeEvent.getNewValue() != null)
//            cntrListId =
//                    new Integer(this.getListBindingValue("CntrListNumber", "CntrListHeaderId",
//                                                         ((Integer)valueChangeEvent.getNewValue()).intValue())).intValue();
//
//        int headerId =
//            ((Number)currentRow.getAttribute("CntrMeasureHeaderId")).intValue();
//        //设置cntrid到全局
//
//        OperationBinding countBinding =
//            this.findOperation("queryAllCntrListLine");
//        countBinding.getParamsMap().put("cntrListId", cntrListId);
//        countBinding.getParamsMap().put("headerId", headerId);
//        countBinding.execute();
        DBUtil dbUtil = new DBUtil();
        if(valueChangeEvent.getNewValue()!=null&&currentRow.getAttribute("CntrMeasureHeaderId")!=null){
            dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm_bm_measure_pkg.func_sync_measure_from_list(?,?)", 
                                          new String[]{this.getListBindingValue("CntrListNumber", "CntrListHeaderId",((Integer)valueChangeEvent.getNewValue()).intValue()).toString(),
                                                       currentRow.getAttribute("CntrMeasureHeaderId").toString()});
        }
        this.findIterator("BmCntrMeasureLineT1Iterator").getViewObject().executeQuery();
        //将结果集返回set进去measureLinesTable
        this.refreshComponent(this.measureLinesTable);
    }


    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    /*****
     *
     * 新增方法
     *
     * ***/
    public void CreateInsertListener(ActionEvent actionEvent) {
        Row currentRow =
            this.findIterator("BmCntrMeasureHeaderT1Iterator").getCurrentRow();
        if (null == currentRow.getAttribute("CntrNumber")) {
            this.addErrorMessage("错误", "请选择合同编号");
            return;
        }
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding =
            bindings.getOperationBinding("CreateInsert");
        operationBinding.execute();
        Row newRow =
            this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
        newRow.setAttribute("LineNum",
                            this.getMeasureLinesTable().getEstimatedRowCount() +
                            "");
        this.refreshComponent(this.getMeasureLinesTable());

    }


    /**
     *
     * 清空该行的值
     *
     * **/
    public void listIdChangeListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        Row currentRow =
            this.findIterator("BmCntrMeasureLineT1Iterator").getCurrentRow();
        if (currentRow != null) {
            currentRow.setAttribute("CntrListName", "");
            currentRow.setAttribute("CntrListNumber", "");
            currentRow.setAttribute("CntrListLineId", new Number(0));

            currentRow.setAttribute("CntrListUomCode", "");
            currentRow.setAttribute("BudgetQuantity", new Number(0));
            currentRow.setAttribute("CumulativeQuantity", new Number(0));
            currentRow.setAttribute("CompletionPercentage", new Number(0));
            currentRow.setAttribute("EngineeringQuantity", new Number(0));
            currentRow.setAttribute("UnitPrice", new Number(0));
            currentRow.setAttribute("MeasureAmount", new Number(0));
            currentRow.setAttribute("TaskId", "");
            currentRow.setAttribute("TaskNumber", "");
            currentRow.setAttribute("TaskName", "");
            currentRow.setAttribute("ProductionProAmount", new Number(0));
            this.refreshComponent(this.getMeasureLinesTable());

        }


    }

    public void setMeasurePanelStretchLayout(RichPanelStretchLayout measurePanelStretchLayout) {
        this.measurePanelStretchLayout = measurePanelStretchLayout;
    }

    public RichPanelStretchLayout getMeasurePanelStretchLayout() {
        return measurePanelStretchLayout;
    }

    public void setCntrName(RichInputListOfValues cntrName) {
        this.cntrName = cntrName;
    }

    public RichInputListOfValues getCntrName() {
        return cntrName;
    }

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
                                         "#{backingBeanScope.cntrMeasureApplyDetailBean.returnConfirmListener}");
        } else {
            this.findOperation("Rollback").execute();
            this.setExpressionValue("#{pageFlowScope.disableAction}", "false");
            this.executeAction("back");
        }
    }


    public String importAction() {
        Object cntrName = this.getCntrName().getValue();
        if (cntrName == null || "".equals(cntrName)) {
            this.addInfoMessage("提示", "请选择合同后再导入");
            return null;
        }
        PopupHints hint = new PopupHints();
        this.getImportPop().show(hint);
        return null;

    }

    public void setImportPop(RichPopup importPop) {
        this.importPop = importPop;
    }

    public RichPopup getImportPop() {
        return importPop;
    }

    //    public void popupShowOff(ActionEvent actionEvent) {
    ////                this.excelFile.setValue(null);
    ////                this.refreshComponent(this.excelFile);
    //        this.getImportPop().hide();
    //    }

    public void setCompletage(RichColumn completage) {
        this.completage = completage;
    }

    public RichColumn getCompletage() {
        return completage;
    }

    public void initCumulativeAttributeListener(ReturnPopupEvent returnPopupEvent) {
        DCIteratorBinding measureHeaderT1Iterator =
            this.findIterator("BmCntrMeasureHeaderT1Iterator");
        Row currentRow = measureHeaderT1Iterator.getCurrentRow();
        if (currentRow != null) {

            Number cumulativeProductionAmount =
                getCumulativeProductionAmount();
            currentRow.setAttribute("CumulativeProductionAmount",
                                    cumulativeProductionAmount.round(2));
            this.refreshComponent(this.getMeasurePanelStretchLayout());
        }
    }

    public void setHistorPopup(RichPopup historPopup) {
        this.historPopup = historPopup;
    }

    public RichPopup getHistorPopup() {
        return historPopup;
    }

    public void showHistoryPopupActionListener(ActionEvent actionEvent) {
        RichRegion region =
            (RichRegion)this.getHistorPopup().getChildren().get(0).getChildren().get(0);
        this.showHistoryPopup(actionEvent, this.getHistorPopup(), region);
    }

    public String popupShowOff() {
        // Add event code here...
        return null;
    }
}
