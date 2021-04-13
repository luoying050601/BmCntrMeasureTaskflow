package cn.com.cscec8b.bm.cntrmeasure.model.services.client;

import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrCheck;
import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrMeasure;
import cn.com.cscec8b.bm.cntrmeasure.model.services.common.CntrMeasureAM;

import java.util.List;

import oracle.jbo.client.remote.ApplicationModuleImpl;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Aug 24 19:11:20 CST 2016
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class CntrMeasureAMClient extends ApplicationModuleImpl implements CntrMeasureAM {
    /**
     * This is the default constructor (do not remove).
     */
    public CntrMeasureAMClient() {
    }


    public List<CntrMeasure> getExportData(String ProjectId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"getExportData",new String [] {"java.lang.String"},new Object[] {ProjectId});
        return (List<CntrMeasure>)_ret;
    }

    public Number countAllMeasureAmount() {
        Object _ret =
            this.riInvokeExportedMethod(this,"countAllMeasureAmount",null,null);
        return (Number)_ret;
    }


    public List<CntrMeasure> getExportData() {
        Object _ret = this.riInvokeExportedMethod(this,"getExportData",null,null);
        return (List<CntrMeasure>)_ret;
    }


    public List<CntrCheck> getExportCheckData() {
        Object _ret =
            this.riInvokeExportedMethod(this,"getExportCheckData",null,null);
        return (List<CntrCheck>)_ret;
    }


    //    public List getExportData() {
//        Object _ret = this.riInvokeExportedMethod(this,"getExportData",null,null);
//        return (List)_ret;
//    }


    //    public List getExportCheckData() {
//        Object _ret =
//            this.riInvokeExportedMethod(this,"getExportCheckData",null,null);
//        return (List)_ret;
//    }


    public void queryAllCntrListLine(int cntrListId, int headerId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"queryAllCntrListLine",new String [] {"int","int"},new Object[] {new Integer(cntrListId), new Integer(headerId)});
        return;
    }

    public boolean checkValidateMeasureHeader(int cntrMeasureHeaderId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"checkValidateMeasureHeader",new String [] {"int"},new Object[] {new Integer(cntrMeasureHeaderId)});
        return ((Boolean)_ret).booleanValue();
    }

    public boolean updateSelectedRowAccountFlag(List measureHeaderIds) {
        Object _ret =
            this.riInvokeExportedMethod(this,"updateSelectedRowAccountFlag",new String [] {"java.util.List"},new Object[] {measureHeaderIds});
        return ((Boolean)_ret).booleanValue();
    }

    public String generateDocumentNumber(String documentType, String cntrId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"generateDocumentNumber",new String [] {"java.lang.String","java.lang.String"},new Object[] {documentType, cntrId});
        return (String)_ret;
    }

    public Number countAllMeasureAmount(int cntrId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"countAllMeasureAmount",new String [] {"int"},new Object[] {new Integer(cntrId)});
        return (Number)_ret;
    }

    public Number countTrasnferingRecord(int cntrId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"countTrasnferingRecord",new String [] {"int"},new Object[] {new Integer(cntrId)});
        return (Number)_ret;
    }

    public Number getCumulativeProductionAmount(int cntrId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"getCumulativeProductionAmount",new String [] {"int"},new Object[] {new Integer(cntrId)});
        return (Number)_ret;
    }

    public Number getCumulativeActualOutput(int cntrId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"getCumulativeActualOutput",new String [] {"int"},new Object[] {new Integer(cntrId)});
        return (Number)_ret;
    }

    public String isValidateTransferDate(Date transferDate, int orgId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"isValidateTransferDate",new String [] {"oracle.jbo.domain.Date","int"},new Object[] {transferDate, new Integer(orgId)});
        return (String)_ret;
    }

    public String queryBmCntrMeasureByHeaderId(Number headerId) {
        Object _ret =
            this.riInvokeExportedMethod(this,"queryBmCntrMeasureByHeaderId",new String [] {"oracle.jbo.domain.Number"},new Object[] {headerId});
        return (String)_ret;
    }
}
