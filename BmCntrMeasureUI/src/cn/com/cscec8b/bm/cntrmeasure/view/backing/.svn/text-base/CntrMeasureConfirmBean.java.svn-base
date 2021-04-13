package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.framework.view.common.CustomManagedBean;


import cn.com.cscec8b.proxy.bm.cntrtrans.types.QueryProcRequestType;

import cn.com.cscec8b.proxy.bm.cntrtrans.types.QueryProcResponseType;

import cn.com.cscec8b.proxy.bm.cntrtrans.ws.QueryCntrTrans;

import cn.com.cscec8b.proxy.bm.cntrtrans.ws.Querycntrtrans_client_ep;


import java.sql.SQLException;

import java.text.SimpleDateFormat;


import javax.el.ELContext;

import javax.el.ExpressionFactory;

import javax.el.MethodExpression;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;
import oracle.adf.view.rich.event.QueryEvent;

import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;


public class CntrMeasureConfirmBean extends CustomManagedBean {
    @SuppressWarnings("compatibility:-6328124141536112112")
    private static final long serialVersionUID = 2L;
    private RichInputDate confirmDate;
    private RichTable verifyTable;
    private RichPopup confirmDatePopup;
    private RichTable historyTable;
    private RichPanelStretchLayout historyLayout;
    // private ADFLogger adfLogger;

    public CntrMeasureConfirmBean() {
    }

    //弹窗事件

    public void transferListener(DialogEvent dialogEvent) {
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            if (null != confirmDate) {
                this.transferAllVerifyData();
            }
        }
    }
    //获取确认日期值

    public void getDateValueListener(ValueChangeEvent valueChangeEvent) {
        this.setConfirmDate((RichInputDate)valueChangeEvent.getNewValue());
    }

    public void setConfirmDate(RichInputDate confirmDate) {
        this.confirmDate = confirmDate;
    }

    public RichInputDate getConfirmDate() {
        return confirmDate;
    }

    /***
     *
     * 通过遍历table获取所有值，
     * 1、传入ERP
     * 2、获取相关字段和ERP返回值新增到确认表
     *
     * **/
    private void transferAllVerifyData() {

        //1、校验日期是否符合规则
        String validateFlag = isValidateTransferDate();
        //    System.out.println(validateFlag.intValue());
        //"传送日期不在已打开的项目期间、应收期间或者总账期间内，请确认！"
        if (validateFlag != "") {
            this.addErrorMessage("错误", validateFlag);
            return;
        }

        //   cntrId
        OperationBinding countBinding =
            this.findOperation("countTrasnferingRecord");
        Number cntrId = getCntrId();
        if (cntrId == null) {
            this.addErrorMessage("错误", "请输入合同查询条件！");
            return;
        }
        countBinding.getParamsMap().put("cntrId", cntrId);
        Number count = (Number)countBinding.execute();
        if (count != null && count.intValue() > 0) {
            this.addErrorMessage("警告", "当前合同下的数据正在传送中，请勿重复传送");
            return;
        }

        String transferStatus = "PROCESSING";
        String transferErrorMsg = " ";
        // String invoiceNumber = "";
        DCIteratorBinding confirm1Iterator =
            this.findIterator("BmCntrConfirm1Iterator");
        RowSetIterator rsi = confirm1Iterator.getRowSetIterator();
        Row[] allRow = rsi.getAllRowsInRange();
        boolean validate = true;
        Number projectId = new Number(0);
        for (Row row : allRow) {
            if (row.getAttribute("CurrentTransferAmount") != null) {
                Number currentTransferAmount =
                    (Number)row.getAttribute("CurrentTransferAmount");

                projectId = (Number)row.getAttribute("ProjectId");
                if (currentTransferAmount.doubleValue() != 0) {
                    //判断输入值是否符合规则
                    //已传送金额
                    Number al =
                        (Number)row.getAttribute("AlreadyConfirmAmount");
                    //未传送金额
                    Number left =
                        (Number)row.getAttribute("LeftConfirmAmount");
                    Number minTransAmount;
                    Number maxTransAmount;
                    /*
                     * 总包财务可传送量的范围是最多 未传送量，最少（已传送量的负数）
                       比如：
                       业主确认产值（1000） 财务已确认产值（400）财务未确认产值（600）
                       则可传送量= -400 ~ 600
                       业主确认产值（-1000） 财务已确认产值（-400）财务未确认产值（-600）
                       则可传送量= -600 ~ -400
                     * 
                     */
                    if(left.doubleValue()>-al.doubleValue()){
                        maxTransAmount = left;
                        try {
                            minTransAmount = new Number(-al.doubleValue());
                        } catch (SQLException e) {
                            minTransAmount = new Number(0);
                        }
                    } else {
                        try {
                            maxTransAmount = new Number(-al.doubleValue());
                        } catch (SQLException e) {
                            maxTransAmount = new Number(0);
                        }
                        minTransAmount = left;
                    }
                    if (currentTransferAmount.doubleValue() > maxTransAmount.doubleValue() ||
                        currentTransferAmount.doubleValue() < minTransAmount.doubleValue()) {
                        this.addErrorMessage("错误",
                                             "核量单号为" + row.getAttribute("DocumentNumber") +
                                             "本次传送金额应不小于" + minTransAmount.doubleValue() +
                                             "且不大于" + maxTransAmount.doubleValue());
                        validate = false;
                    }
                    /*
                    if (al.doubleValue() >= 0 && left.doubleValue() >= 0 &&
                        (currentTransferAmount.doubleValue() <
                         -al.doubleValue() ||
                         currentTransferAmount.doubleValue() >
                         left.doubleValue())) {
                        this.addErrorMessage("错误",
                                             "核量单号为" + row.getAttribute("DocumentNumber") +
                                             "本次传送金额应不小于" + -al.doubleValue() +
                                             "且不大于" + left.doubleValue());
                        validate = false;
                    }
                    if (al.doubleValue() >= 0 && left.doubleValue() < 0 &&
                        (currentTransferAmount.doubleValue() <
                         (left.doubleValue() - al.doubleValue()) ||
                         currentTransferAmount.doubleValue() >
                         left.doubleValue())) {
                        this.addErrorMessage("错误",
                                             "核量单号为" + row.getAttribute("DocumentNumber") +
                                             "本次传送金额应不小于" +
                                             (left.doubleValue() -
                                              al.doubleValue()) + "且不大于" +
                                             left.doubleValue());
                        validate = false;
                    }
                    if (al.doubleValue() < 0 && left.doubleValue() >= 0 &&
                        (currentTransferAmount.doubleValue() < 0 ||
                         currentTransferAmount.doubleValue() >
                         left.doubleValue())) {
                        this.addErrorMessage("错误",
                                             "核量单号为" + row.getAttribute("DocumentNumber") +
                                             "本次传送金额应不小于" + 0 + "且不大于" +
                                             left.doubleValue());
                        validate = false;
                    }*/
                }
            }
        }

        if (validate) {
            //生成单号
            String documentNumber = generateDocumentNumber();
            //传送ERP

            if (documentNumber != null && !"".equals(documentNumber.trim())) {
                QueryProcResponseType response =
                    transferERP(documentNumber, projectId);
                response.getERPDocumentNum();
                transferStatus = response.getStatus();
                //invoiceNumber = response.getERPDocumentNum();
                transferErrorMsg = response.getMsg();
                //新增传送
                for (Row row : allRow) {
                    if (row.getAttribute("CurrentTransferAmount") != null) {
                        Number currentTransferAmount =
                            (Number)row.getAttribute("CurrentTransferAmount");
                        if (currentTransferAmount.doubleValue() != 0) {
                            //判断输入值是否符合规则
                            //已传送金额
                            //                            Number al =
                            //                                (Number)row.getAttribute("AlreadyConfirmAmount");
                            //                            //未传送金额
                            //                            Number left =
                            //                                (Number)row.getAttribute("LeftConfirmAmount");


                            this.findOperation("CreateInsert").execute();
                            DCIteratorBinding confirmT1Iterator =
                                this.findIterator("BmCntrFinConfirmT1Iterator");
                            Row newRow = confirmT1Iterator.getCurrentRow();
                            newRow.setAttribute("ConfirmAmount",
                                                currentTransferAmount);
                            newRow.setAttribute("CntrVerifyHeaderId",
                                                row.getAttribute("CntrVerifyHeaderId"));
                            newRow.setAttribute("CumulativeConfirmAmount",
                                                row.getAttribute("AlreadyConfirmAmount"));
                            newRow.setAttribute("ConfirmDate",
                                                new Date(new java.sql.Date(((java.util.Date)this.getConfirmDate().getValue()).getTime())));

                            if (!"S".equals(transferStatus)) {
                                newRow.setAttribute("TransferErrorMsg",
                                                    transferErrorMsg);
                                newRow.setAttribute("TransferStatus",
                                                    "FAILURE");
                                row.setAttribute("TransferStatus", "FAILURE");
                            } else {
                                //  newRow.setAttribute("InvoiceNumber", invoiceNumber);
                                newRow.setAttribute("TransferStatus",
                                                    "TRANSFERING");
                                row.setAttribute("TransferStatus",
                                                 "TRANSFERING");


                            }
                            newRow.setAttribute("DocumentNumber",
                                                documentNumber);

                            row.setAttribute("FinConfirmDocumentNumber",
                                             documentNumber);

                            row.setAttribute("CurrentTransferAmount",
                                             new Number(0));
                        }
                    }
                }
                this.findOperation("Commit").execute();
                if ("S".equals(transferStatus)) {
                    this.getConfirmDatePopup().hide();
                    this.addInfoMessage("提示",
                                        "数据传送中……,稍后请在《历史记录查询界面》查看数据传送状态。");
                } else {
                    this.getConfirmDatePopup().hide();
                    this.addErrorMessage("警告", "传入失败");
                }
            } else {

                this.addErrorMessage("警告", "未传送成功，请重试！");
                return;
            }
            this.refreshComponent(this.getVerifyTable());

        }

    }

    public void setVerifyTable(RichTable verifyTable) {
        this.verifyTable = verifyTable;
    }

    public RichTable getVerifyTable() {
        return verifyTable;
    }

    //    private void print(Row row) {
    //        for(String s:row.getAttributeNames()){
    //            System.out.println(s+":"+row.getAttribute(s));
    //            }
    //

    public void isTableEmplyListener(ActionEvent actionEvent) {
        DCIteratorBinding confirm1Iterator =
            this.findIterator("BmCntrConfirm1Iterator");
        if (null == confirm1Iterator.getCurrentRow()) {
            this.addErrorMessage("警告", "请查询需要传入的核量单");
        } else {
            //  this.showPopup(confirmDatePopup);
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            this.getConfirmDatePopup().show(hints);

        }
    }

    public void setConfirmDatePopup(RichPopup confirmDatePopup) {
        this.confirmDatePopup = confirmDatePopup;
    }

    public RichPopup getConfirmDatePopup() {
        return confirmDatePopup;
    }

    /**
     *
     * 校验传送日期
     *
     * **/
    private String isValidateTransferDate() {
        OperationBinding validateBinding =
            this.findOperation("isValidateTransferDate");
        validateBinding.getParamsMap().put("transferDate",
                                           new Date(new java.sql.Date(((java.util.Date)this.getConfirmDate().getValue()).getTime())));
        validateBinding.getParamsMap().put("orgId", this.getCurrentOrgId());
        String validateFlag = (String)validateBinding.execute();
        return validateFlag;
    }

    /**
     *
     * 传送ERP
     *
     * **/
    private QueryProcResponseType transferERP(String documentNumber,
                                              Number projectId) {
        QueryProcRequestType type = new QueryProcRequestType();
        type.setBillAmount(((Number)this.resolveExpression("#{bindings.CurrentAllTransferAmount.inputValue}")).toString());
        type.setDescription(documentNumber + "对业主的工程完工报量");
        type.setProjectId(projectId.toString());
        type.setDocumentNum(documentNumber);
        type.setOrgId(this.getCurrentOrgId().toString());
        type.setPreparedPersonId(this.getCurrentEmployeeId().toString());

        //        System.out.println("BillAmount:" +
        //                           this.resolveExpression("#{bindings.CurrentAllTransferAmount.inputValue}").toString());
        //        System.out.println("Description:" + documentNumber + "对业主的工程完工报量");
        //        System.out.println("ProjectId:" +
        //                           this.getCurrentProjectId().toString());
        //        System.out.println("DocumentNum:" + documentNumber);
        //        System.out.println("OrgId:" + this.getCurrentOrgId().toString());
        //        System.out.println("PreparedPersonId:" +
        //                           this.getCurrentEmployeeId().toString());
        //        System.out.println("**********************************************************");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        java.util.Date transferDate =
            (java.util.Date)this.getConfirmDate().getValue();
        type.setTransactionDate(sdf.format(transferDate));

        Querycntrtrans_client_ep querycntrtrans_client_ep =
            new Querycntrtrans_client_ep();
        QueryCntrTrans queryCntrTrans =
            querycntrtrans_client_ep.getQueryCntrTrans_pt();
        QueryProcResponseType response = queryCntrTrans.process(type);

        return response;
    }

    public Number getCntrId() {
        Number cntrId =
            (Number)this.resolveExpression("#{pageFlowScope.cntrId}");
        if (cntrId == null) {
            DCIteratorBinding confirmT1Iterator =
                this.findIterator("BmCntrFinConfirmT1Iterator");
            Row newRow = confirmT1Iterator.getCurrentRow();
            if (newRow != null) {
                cntrId = (Number)newRow.getAttribute("CntrId");
            }
        }
        return cntrId;
    }

    /***
     *
     * 生成单号
     *
     * */
    private String generateDocumentNumber() {
        String documentNumber = "";
        OperationBinding generateBinding =
            this.findOperation("generateDocumentNumber");
        generateBinding.getParamsMap().put("documentType", "CNTR_FIN_CONFIRM");
        Number cntrId = getCntrId();
        if (cntrId == null) {
            this.addErrorMessage("错误", "请输入合同查询条件！");
        } else {
            generateBinding.getParamsMap().put("cntrId", cntrId);
            documentNumber = (String)generateBinding.execute();
        }

        return documentNumber;
    }

    public void processQuery(QueryEvent queryEvent) {
        invokeQueryEventMethodExpression("#{bindings.BmCntrConfirmVOCriteriaQuery.processQuery}",
                                         queryEvent);
        DCIteratorBinding confirm1Iterator =
            this.findIterator("BmCntrConfirm1Iterator");
        Row[] allRow = confirm1Iterator.getAllRowsInRange();
        if (allRow.length > 0) {
            Row currentRow = allRow[0];
            this.setExpressionValue("#{pageFlowScope.cntrId}",
                                    currentRow.getAttribute("CntrId"));
            System.out.println("currentRow.getAttribute(CntrId)=" +
                               currentRow.getAttribute("CntrId"));
        }

    }


    private void invokeQueryEventMethodExpression(String expression,
                                                  QueryEvent queryEvent) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ELContext elctx = fctx.getELContext();
        ExpressionFactory efactory =
            fctx.getApplication().getExpressionFactory();

        MethodExpression me =
            efactory.createMethodExpression(elctx, expression, Object.class,
                                            new Class[] { QueryEvent.class });
        me.invoke(elctx, new Object[] { queryEvent });

    }

    public void cleanTransferDateListener(PopupFetchEvent popupFetchEvent) {

        this.setConfirmDate(null);

    }

    public void returnConfirmActionListener(ActionEvent actionEvent) {
        ViewObjectImpl exVO =
            (ViewObjectImpl)this.findIterator("BmCntrConfirm1Iterator").getViewObject();

        exVO.executeQuery();
        this.executeAction("confirm");
        // Add event code here...
    }

    public void goHistory(ActionEvent actionEvent) {
        //        ViewObjectImpl exVO =
        //            (ViewObjectImpl)this.findIterator("BmCntrFinConfirmT1Iterator").getViewObject();
        //        exVO.executeQuery();
        //        System.out.println(1111111);

        //   this.refreshComponent(this.historyTable);
        this.executeAction("goQuery");
        //BmCntrFinConfirmT1Iterator
    }

    public void setHistoryTable(RichTable historyTable) {
        this.historyTable = historyTable;
    }

    public RichTable getHistoryTable() {
        return historyTable;
    }

    public void setHistoryLayout(RichPanelStretchLayout historyLayout) {
        this.historyLayout = historyLayout;
    }

    public RichPanelStretchLayout getHistoryLayout() {
        return historyLayout;
    }
}
