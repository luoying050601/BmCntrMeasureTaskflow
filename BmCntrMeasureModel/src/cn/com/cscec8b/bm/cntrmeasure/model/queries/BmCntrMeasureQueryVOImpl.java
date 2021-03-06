package cn.com.cscec8b.bm.cntrmeasure.model.queries;

import cn.com.cscec8b.framework.model.common.CustomViewObjectImpl;

import oracle.jbo.domain.Number;
import oracle.jbo.server.SQLBuilder;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Thu Aug 25 14:49:47 CST 2016
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BmCntrMeasureQueryVOImpl extends CustomViewObjectImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public BmCntrMeasureQueryVOImpl() {
    }

    //    public Number getTotalAdjSalesAmount() {
    //            return getTotal("AdjSaleAmount");
    //        }

    public Number getCurrentMeasureAmount() {
         return getTotal("MeasureAmount");
    }





    /**
     * Returns the variable value for bv_projectId.
     * @return variable value for bv_projectId
     */
    public Number getbv_projectId() {
        return (Number)ensureVariableManager().getVariableValue("bv_projectId");
    }

    /**
     * Sets <code>value</code> for variable bv_projectId.
     * @param value value to bind as bv_projectId
     */
    public void setbv_projectId(Number value) {
        ensureVariableManager().setVariableValue("bv_projectId", value);
    }
}
