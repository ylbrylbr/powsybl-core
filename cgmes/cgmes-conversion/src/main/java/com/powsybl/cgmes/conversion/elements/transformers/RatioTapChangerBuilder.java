package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.Comparator;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class RatioTapChangerBuilder extends TapChangerBuilder {

    RatioTapChangerBuilder(PropertyBag ratioTapChanger, Context context) {
        super(ratioTapChanger, context);
    }

    public TapChanger build() {
        int lowStep = p.asInt(CgmesNames.LOW_STEP);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        int normalStep = p.asInt(CgmesNames.NORMAL_STEP, neutralStep);
        int position = initialTapPosition(normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = p.asBoolean(CgmesNames.LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addRegulationData();
        addSteps();
        return tapChanger;
    }

    private void addRegulationData() {
        String regulatingControlId = context.regulatingControlMapping().forTransformers().getRegulatingControlId(p);
        tapChanger.setId(p.getId(CgmesNames.RATIO_TAP_CHANGER))
            .setRegulating(context.regulatingControlMapping().forTransformers().getRegulating(regulatingControlId))
            .setRegulatingControlId(regulatingControlId)
            .setTculControlMode(p.get(CgmesNames.TCUL_CONTROL_MODE))
            .setTapChangerControlEnabled(p.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false));
    }

    private void addSteps() {
        String tableId = p.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId != null) {
            addStepsFromTable(tableId);
        } else {
            addStepsFromLowHighIncrement();
        }
    }

    private void addStepsFromTable(String tableId) {
        PropertyBags table = context.ratioTapChangerTable(tableId);
        Comparator<PropertyBag> byStep = Comparator
            .comparingInt((PropertyBag p) -> p.asInt(CgmesNames.STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(CgmesNames.STEP);
            double ratio = fixing(point, CgmesNames.RATIO, 1.0, tableId, step);
            double r = fixing(point, CgmesNames.R, 0, tableId, step);
            double x = fixing(point, CgmesNames.X, 0, tableId, step);
            double g = fixing(point, CgmesNames.G, 0, tableId, step);
            double b = fixing(point, CgmesNames.B, 0, tableId, step);
            tapChanger.beginStep()
                .setRatio(ratio)
                .setR(r)
                .setX(x)
                .setG1(g)
                .setB1(b)
                .endStep();
        }
    }

    private void addStepsFromLowHighIncrement() {
        double stepVoltageIncrement = p.asDouble(CgmesNames.STEP_VOLTAGE_INCREMENT);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        for (int step = tapChanger.getLowTapPosition(); step <= highStep; step++) {
            double ratio = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0);
            tapChanger.beginStep()
                .setRatio(ratio)
                .endStep();
        }
    }
}