package cn.com.cscec8b.bm.cntrmeasure.model.services.common;

import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrCheck;
import cn.com.cscec8b.bm.cntrmeasure.model.object.CntrMeasure;

import java.util.List;

import oracle.jbo.ApplicationModule;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Aug 24 19:11:20 CST 2016
// ---------------------------------------------------------------------
public interface CntrMeasureAM extends ApplicationModule {


    void queryAllCntrListLine(int cntrListId, int headerId);


    boolean checkValidateMeasureHeader(int cntrMeasureHeaderId);

    boolean updateSelectedRowAccountFlag(List measureHeaderIds);

    String generateDocumentNumber(String documentType, String cntrId);


    Number countAllMeasureAmount(int cntrId);


    Number countTrasnferingRecord(int cntrId);

    Number getCumulativeProductionAmount(int cntrId);

    Number getCumulativeActualOutput(int cntrId);

    String isValidateTransferDate(Date transferDate, int orgId);

    String queryBmCntrMeasureByHeaderId(Number headerId);
}