package cn.com.cscec8b.bm.cntrmeasure.view.backing;

import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.context.FacesContext;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.event.QueryEvent;

import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.domain.Number;

public class CntrMeasureApplyDetailQueryBean extends CustomManagedBean {
    private RichTable measureTable;
    private RichPanelStretchLayout measureLayout;

    public CntrMeasureApplyDetailQueryBean() {
    }

    public void queryDetailListener(QueryEvent queryEvent) {
        this.invokeQueryEventMethodExpression("#{bindings.BmCntrMeasureQueryVOCriteriaQuery.processQuery}",
                                              queryEvent);
        Row currentRow =
            this.findIterator("BmCntrMeasureQuery1Iterator").getCurrentRow();
        if (currentRow != null) {
            Number cntrId = (Number)currentRow.getAttribute("CntrId");
            if (cntrId != null) {
                            OperationBinding queryBinding =
                                this.findOperation("countAllMeasureAmount");
                            queryBinding.getParamsMap().put("cntrId", cntrId.intValue());
                          Number allAmount = (Number)queryBinding.execute();
                this.setExpressionValue("#{pageFlowScope.allMeasureAmount}",
                                                allAmount);

                            this.refreshComponent(this.getMeasureTable());
            }
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

    public void setMeasureTable(RichTable measureTable) {
        this.measureTable = measureTable;
    }

    public RichTable getMeasureTable() {
        return measureTable;
    }

    public void setMeasureLayout(RichPanelStretchLayout measureLayout) {
        this.measureLayout = measureLayout;
    }

    public RichPanelStretchLayout getMeasureLayout() {
        return measureLayout;
    }
}
