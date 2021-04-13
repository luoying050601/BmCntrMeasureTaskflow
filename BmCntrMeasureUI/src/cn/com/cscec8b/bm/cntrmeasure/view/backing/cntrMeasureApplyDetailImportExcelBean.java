package cn.com.cscec8b.bm.cntrmeasure.view.backing;


import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.RichPopup.PopupHints;

import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import cn.com.cscec8b.utils.db.DBUtil;

import java.sql.SQLException;

import java.text.NumberFormat;

import java.util.List;

import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.VariableValueManager;
import oracle.jbo.ViewCriteria;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

import org.apache.myfaces.trinidad.model.UploadedFile;


public class cntrMeasureApplyDetailImportExcelBean extends CustomManagedBean {

    private RichInputFile excelFile;
    private RichCommandButton cntrMeasureApplyDetailImportExcelBtn;
    private RichPopup importPop;

    public cntrMeasureApplyDetailImportExcelBean() {
    }


    public void setExcelFile(RichInputFile excelFile) {
        this.excelFile = excelFile;
    }

    public RichInputFile getExcelFile() {
        return excelFile;
    }

    public void setCntrMeasureApplyDetailImportExcelBtn(RichCommandButton cntrMeasureApplyDetailImportExcelBtn) {
        this.cntrMeasureApplyDetailImportExcelBtn =
                cntrMeasureApplyDetailImportExcelBtn;
    }

    public RichCommandButton getCntrMeasureApplyDetailImportExcelBtn() {
        return cntrMeasureApplyDetailImportExcelBtn;
    }

    public void cntrMeasureApplyDetailImportExcelActionListener(ActionEvent actionEvent) {
        //    this.removeCntrMeasureApplyDetailVO();
        UploadedFile uploadFile = (UploadedFile)excelFile.getValue();
        if (uploadFile == null) {
            this.addErrorMessage("提示", "请选择文件再导入");
        } else {
            String fileFlag = ImportExcel.checkUpLoadFile(uploadFile);
            if (!(fileFlag.equals("xls") || fileFlag.equals("xlsx"))) {
                this.addErrorMessage("提示", "请选择excel文件再导入");
            } else {
                try{
                    ImportExcel.importExcelFile(uploadFile, fileFlag);
                }catch(IllegalStateException e){
                    this.addErrorMessage("错误", "导入表格格式不对，请使用下载的模板重新导入！");
                    }
                List<List> list =
                    ImportExcel.importExcelFile(uploadFile, fileFlag);
                String errorMsg = isValiadateList(list);
                if ("".equals(errorMsg.trim())) {
                    insertMeasureVo(list);
                }
                this.excelFile.setValue(null);
               this.refreshComponent(this.excelFile);
            }
        }

    }
    //    //全量导入
    //
    //    private void removeCntrMeasureApplyDetailVO() {
    //        ViewObjectImpl vo =
    //            (ViewObjectImpl)this.findIterator("BmCntrMeasureLineT1Iterator").getViewObject();
    //        vo.setRangeSize(-1);
    //        Row[] rows = vo.getAllRowsInRange();
    //        for (Row row : rows) {
    //            row.remove();
    //        }
    //
    //    }

    /***
     *
     * 校验数据库导入的List是否符合规则
     * 1、必输性校验
     * 2、类型匹配校验
     * 3、单独数据校验
     *
     * ***/
    private String isValiadateList(List<List> list) {
        DBUtil dbUtil = new DBUtil();

        String errorMsg = "";
            String projectId = this.getCurrentProjectId().toString();
        Number cntrId = this.getCurrentCntrId();
       String listId ="";
        for (int i = 0;i<list.size();++i) {
            List li = list.get(i);
            //校验清单名称在下拉框的值内
            if (li.get(1) != null && !"".equals(li.get(1))) {
             listId =
                    dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.stm_bm_subcntr_measure_pkg.get_list_id(?,?)",
                                                  new String[] { (String)li.get(1),
                                                                 cntrId.toString() });
                if (listId == null||"0".equals(listId)||"".equals(listId)) {
                    this.addErrorMessage("错误",
                                         "第"+(i+2)+"行导入清单名称" + li.get(1).toString() +
                                         "有误，导入失败");
                    errorMsg =
                            errorMsg + "导入失败";
                } else {
                    li.set(1, listId);
                }
            } else {
                errorMsg = errorMsg + "导入失败";
                this.addErrorMessage("错误","第"+(i+2)+"行清单名称不能为空，导入失败");
              continue;
            }

            //校验合同清单项（主清单）是否合理
            Number cntrListLineId = null;
            if (li.get(2)!=null && !"".equals(li.get(2))) {
                try {
                    cntrListLineId =
                            new Number(dbUtil.quickGetFunctionResult(DBUtil.STM_DS,
                                                                     "stm.stm_bm_cntr_list_pkg.get_list_line_id_BY_NUMBER(?,?,?)",
                                                                     new String[] {cntrId.toString(), listId, li.get(2).toString() }));
                   

                        

                    if (cntrListLineId == null || "".equals(cntrListLineId)||cntrListLineId.intValue()==0) {
                        errorMsg =
                                errorMsg + "导入失败";
                        this.addErrorMessage("错误",
                                             "第"+(i+2)+"行导入的合同清单编码" + li.get(2).toString() +
                                             "不存在，导入失败");
                      //  return errorMsg;
                    }
                } catch (Exception se) {
                    errorMsg = errorMsg +"导入失败";
                    this.addErrorMessage("错误","第"+(i+2)+"行导入的合同清单编码" + li.get(2).toString() +
                                "不存在，导入失败");
                 //   se.printStackTrace();
                    continue;
                }

            } else {
                errorMsg = errorMsg + "导入失败";
                this.addErrorMessage("错误", "第"+(i+2)+"行合同清单编码不能为空，导入失败");
            }

            //校验工程量：数字类型+必输
            Number engineeringQuantity = null;
            try {
                if (li.get(6) != null && !"".equals(li.get(6))) {
                    engineeringQuantity = new Number(li.get(6));
                    queryCntrListLovByLineId(cntrListLineId);
                    Row listLineRow =
                        this.findIterator("BmCntrMeasureListLov1Iterator").getCurrentRow();
                    if(listLineRow!=null){
                    Number cumulativeQuantity =
                        (Number)listLineRow.getAttribute("CumulativeQuantities");
                    Number budgetQuantity =
                        (Number)listLineRow.getAttribute("BudgetQuantity");
                    if(budgetQuantity!=null&&budgetQuantity.doubleValue()==0){
                        errorMsg +="error";
                        this.addErrorMessage("错误","第"+(i+2)+"行施工图预算量为0，请检查清单后操作");
                        }
                    //校验完成百分比在0-100的范围内
                    Number TotalQuantity = cumulativeQuantity.add(engineeringQuantity);
                    NumberFormat NF = NumberFormat.getPercentInstance();
                    NF.setMaximumIntegerDigits(10);
                    NF.setMaximumFractionDigits(3);
                    Number completionTemp = null;
                    try {
                        completionTemp = TotalQuantity.divide(budgetQuantity);
                    } catch (NumberFormatException se) {
                        errorMsg = errorMsg + "导入失败";
                        this.addErrorMessage("错误","第"+(i+2)+"行导入数据类型错误，导入失败");
                    } catch (Exception se) {
                        se.printStackTrace();
                    }
                    Number completionPercentage = completionTemp.multiply(100);
                    if (completionPercentage.doubleValue() < 0 ||
                        completionPercentage.doubleValue() > 100) {
                        errorMsg = errorMsg + "导入失败";
                        this.addErrorMessage("错误", "第"+(i+2)+"行完成百分比应该在0-100之间，导入失败");
                    }
                    }
                    
                    
                } else {
                    errorMsg = errorMsg + "导入失败";
                    this.addErrorMessage("错误","第"+(i+2)+"行工程量不能为空，导入失败");
                }
            } catch (NumberFormatException se) {
                errorMsg = errorMsg + "导入失败";
                this.addErrorMessage("错误", "第"+(i+2)+"行工程量非数字类型，导入失败");
            } catch (Exception se) {
                errorMsg = errorMsg + "入失败";
            }
            
            // 11核算对象：存在性校验
           if (li.get(9) != null&& !"".equals(li.get(9).toString().trim())) {
              
                            String taskId =
                                dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.STM_BM_SUBCNTR_MEASURE_PKG.get_task_id(?,?)",
                                                              new String[] { (String)li.get(9),
                                                                             projectId });
                            if (taskId != null&&!"0".equals(taskId.trim())) {
                                li.set(9, taskId);
                            }else{
                                    errorMsg = errorMsg + "导入失败";
                                    this.addErrorMessage("错误", "第"+(i+2)+"行导入的核算对象编码不存在，导入失败");
                                
                                }
                        }
       
        }
        return errorMsg;

    }

    /***
     *
     * 插入到Vo
     *
     * **/
    private void insertMeasureVo(List<List> list) {
        DBUtil dbUtil = new DBUtil();
        Number cntrId = this.getCurrentCntrId();
        ViewObjectImpl vo =
            (ViewObjectImpl)this.findIterator("BmCntrMeasureLineT1Iterator").getViewObject();
        int count = 0;
        for (List li : list) {
            ++count;
            Row newRow = vo.createRow();
            try {
             Number   cntrListLineId =
                        new Number(dbUtil.quickGetFunctionResult(DBUtil.STM_DS,
                                                                 "stm.stm_bm_cntr_list_pkg.get_list_line_id_BY_NUMBER(?,?,?)",
                                                                 new String[] {cntrId.toString(), li.get(1).toString(), li.get(2).toString() }));;
                queryCntrListLovByLineId(cntrListLineId);
                Row listLineRow =
                    this.findIterator("BmCntrMeasureListLov1Iterator").getCurrentRow();
                if(listLineRow!=null){
                Number cumulativeQuantity = (Number)((Number)listLineRow.getAttribute("CumulativeQuantities")).round(5);
                //
                //                Number transferRate =
                //                    (Number)listLineRow.getAttribute("TransferRate");
                //
                Number engineeringQuantity = (Number)new Number(li.get(6)).round(5);

                Number totalQuantity =
                    cumulativeQuantity.add(engineeringQuantity);
                Number budgetQuantity = (Number)((Number)listLineRow.getAttribute("BudgetQuantity")).round(5);
                Number completionTemp = (Number)totalQuantity.divide(budgetQuantity).round(4);
                Number completionPercentage = completionTemp.multiply(100);
                Number unitPrice =
                    (Number)listLineRow.getAttribute("CntrPrice");
                Number MeasureAmount = (Number)(unitPrice.multiply(engineeringQuantity)).round(2);
                newRow.setAttribute("CntrListHeaderId",
                                    listLineRow.getAttribute("ListId"));
                newRow.setAttribute("CntrListName",
                                    listLineRow.getAttribute("CntrListName"));
                newRow.setAttribute("CntrListLineId",
                                    listLineRow.getAttribute("CntrListLineId"));
                newRow.setAttribute("CntrListNumber",
                                    listLineRow.getAttribute("CntrListNumber"));
                newRow.setAttribute("CntrListUomCode",
                                    listLineRow.getAttribute("Unit"));
                newRow.setAttribute("BudgetQuantity",
                                    listLineRow.getAttribute("BudgetQuantity"));
                newRow.setAttribute("UnitPrice",
                                    listLineRow.getAttribute("CntrPrice"));
                newRow.setAttribute("EngineeringQuantity",
                                    engineeringQuantity);
                newRow.setAttribute("MeasureAmount", MeasureAmount);
                newRow.setAttribute("LineNum", count);
                
                newRow.setAttribute("CompletionPercentage",
                                    completionPercentage);
                if(li.get(9)==null){
                    newRow.setAttribute("TaskId",
                                    listLineRow.getAttribute("TaskId"));
                    System.out.println(listLineRow.getAttribute("TaskId"));
                        
                        
                    String taskName=
                        dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.STM_BM_SUBCNTR_MEASURE_PKG.get_task_name(?)",
                                                      new String[] { (String)listLineRow.getAttribute("TaskId") });
                    newRow.setAttribute("TaskName",taskName);
                }else{
                    
                        newRow.setAttribute("TaskId",
                                          li.get(9));
                        String taskName=
                            dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.STM_BM_SUBCNTR_MEASURE_PKG.get_task_name(?)",
                                                          new String[] { (String)li.get(9) });
                        newRow.setAttribute("TaskName",taskName);
                          
                    }
                vo.insertRow(newRow);
                }else{
                    this.addErrorMessage("错误", "查不到对应的主清单");
                    }

            } catch (NumberFormatException se) {
                this.addErrorMessage("错误", "导入数据类型错误，导入失败");
                se.printStackTrace();
            } catch (Exception se) {
                se.printStackTrace();
            }

        }
    }

    /***
     * 根据listlineid查找相关row
     *
     *
     * */
    private void queryCntrListLovByLineId(Number cntrListLineId) {
        ViewObjectImpl vo =
            (ViewObjectImpl)this.findIterator("BmCntrMeasureListLov1Iterator").getViewObject();
    
        ViewCriteria vc = vo.getViewCriteria("BmCntrMeasureListLovVOCriteriaById");      
        vc.resetCriteria();      
        VariableValueManager vvm = vc.ensureVariableManager();  //为子查询中绑定的变量赋值
        vvm.setVariableValue("bvListLineId", cntrListLineId);      
        vo.applyViewCriteria(vc,true);      
        vo.executeQuery();

//        vo.executeQuery();
//        Row[] byKey =
//            vo.findByKey(new Key(new Object[] { cntrListLineId.toString() }),
//                         1);
//        if (byKey != null && byKey.length > 0) {
//            Row row = byKey[0];
//            vo.setCurrentRow(row);
//        }
    }


    /**
     *
     * 获取当前所选合同id
     *
     * */
    private Number getCurrentCntrId() {
        ViewObjectImpl headerVO =
            (ViewObjectImpl)this.findIterator("BmCntrMeasureHeaderT1Iterator").getViewObject();
        Number cntrId = null;
        if (headerVO.getCurrentRow() != null) {
            cntrId = (Number)headerVO.getCurrentRow().getAttribute("CntrId");
            if (cntrId == null) {
                this.addErrorMessage("错误", "合同编号或名称为空，请检查");
            }
        }
        return cntrId;
    }

    public void setImportPop(RichPopup importPop) {
        this.importPop = importPop;
    }

    public RichPopup getImportPop() {
        return importPop;
    }

    public void importAction() {
        Number cntrId = getCurrentCntrId();
        if(cntrId==null){
            this.addInfoMessage("错误", "请选择合同");
            return;
            }
        PopupHints hint = new PopupHints();
        this.getImportPop().show(hint);
    }

    public void popupShowOff() {
        this.excelFile.setValue(null);
        //        
         this.refreshComponent(this.excelFile);
    }
}
