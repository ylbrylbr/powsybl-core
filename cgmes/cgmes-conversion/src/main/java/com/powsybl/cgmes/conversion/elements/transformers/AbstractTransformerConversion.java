/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
abstract class AbstractTransformerConversion extends AbstractConductingEquipmentConversion {

    AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    protected static void setToIidmRatioTapChanger(TapChanger rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position);

        rtc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            rtca.beginStep()
                .setRho(1 / ratio)
                .setR(r)
                .setX(x)
                .setB(b1)
                .setG(g1)
                .endStep();
        });
        rtca.add();
    }

    protected static void setToIidmPhaseTapChanger(TapChanger ptc, PhaseTapChangerAdder ptca) {
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        ptca.setLowTapPosition(lowStep).setTapPosition(position);

        ptc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            ptca.beginStep()
                .setRho(1 / ratio)
                .setAlpha(-angle)
                .setR(r)
                .setX(x)
                .setB(b1)
                .setG(g1)
                .endStep();
        });
        ptca.add();
    }

    protected CgmesRegulatingControlRatio setContextRegulatingDataRatio(TapChanger tc) {
        CgmesRegulatingControlRatio rcRtc = null;
        if (tc != null) {
            rcRtc = context.regulatingControlMapping().forTransformers().buildRegulatingControlRatio(tc.getId(),
                tc.getRegulatingControlId(), tc.getTculControlMode(), tc.isTapChangerControlEnabled());
        }
        return rcRtc;
    }

    protected CgmesRegulatingControlPhase setContextRegulatingDataPhase(TapChanger tc) {
        CgmesRegulatingControlPhase rcPtc = null;
        if (tc != null) {
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(
                tc.getId(), tc.getRegulatingControlId(), tc.isTapChangerControlEnabled(), tc.isLtcFlag());
        }
        return rcPtc;
    }

    @Override
    protected void addAliases(Identifiable<?> identifiable) {
        super.addAliases(identifiable);
        List<String> ptcs = context.cgmes().phaseTapChangerListForPowerTransformer(identifiable.getId());
        if (ptcs != null) {
            for (int  i = 0; i < ptcs.size(); i++) {
                int index = i + 1;
                Optional.ofNullable(ptcs.get(i)).ifPresent(ptc -> identifiable.addAlias(ptc, Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + index));
            }
        }
        List<String> rtcs = context.cgmes().ratioTapChangerListForPowerTransformer(identifiable.getId());
        if (rtcs != null) {
            for (int i = 0; i < rtcs.size(); i++) {
                int index = i + 1;
                Optional.ofNullable(rtcs.get(i)).ifPresent(rtc -> identifiable.addAlias(rtc, Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + index));
            }
        }
    }

    private static RegulatingControl getRegulatingControl(Context context, String regulatingControlId) {
        Objects.requireNonNull(regulatingControlId);
        return context.regulatingControlMapping().cachedRegulatingControls().get(regulatingControlId);
    }

    protected void addCgmesDataAsProperties(Identifiable<?> transformer, TapChanger rtc, TapChanger ptc, Context context) {
        addRatioTapChangerProperties(transformer, rtc, context);
        addPhaseTapChangerProperties(transformer, ptc, context);
    }

    private static void  addRatioTapChangerProperties(Identifiable<?> transformer, TapChanger rtc, Context context) {
        if (rtc == null || rtc.getId() == null) {
            return;
        }

        if (rtc.getRegulatingControlId() != null) {
            String key = String.format("RatioTapChanger.%s.TapChangerControl", rtc.getId());
            transformer.setProperty(key, rtc.getRegulatingControlId());
        }

        if (rtc.getHiddenCombinedTapChanger() != null) {
            defineHiddenTapChangerProperties(transformer, rtc, rtc.getHiddenCombinedTapChanger(), "RatioTapChanger", context);
        }
    }

    private static void  addPhaseTapChangerProperties(Identifiable<?> transformer, TapChanger ptc, Context context) {
        if (ptc == null || ptc.getId() == null) {
            return;
        }

        if (ptc.getRegulatingControlId() != null) {
            String key = String.format("PhaseTapChanger.%s.TapChangerControl", ptc.getId());
            transformer.setProperty(key, ptc.getRegulatingControlId());
        }
        if (ptc.getType() != null) {
            String key = String.format("PhaseTapChanger.%s.type", ptc.getId());
            transformer.setProperty(key, ptc.getType());
        }

        if (ptc.getHiddenCombinedTapChanger() != null) {
            defineHiddenTapChangerProperties(transformer, ptc, ptc.getHiddenCombinedTapChanger(), "PhaseTapChanger", context);

            String key = String.format("PhaseTapChanger.%s.type", ptc.getHiddenCombinedTapChanger().getId());
            transformer.setProperty(key, ptc.getHiddenCombinedTapChanger().getType());
        }
    }

    private static void defineHiddenTapChangerProperties(Identifiable<?> transformer, TapChanger tc, TapChanger hiddenTc, String propertyTag, Context context) {

        String key = String.format("%s.%s.hiddenTapChangerId", propertyTag, tc.getId());
        transformer.setProperty(key, hiddenTc.getId());

        key = String.format("%s.%s.controlEnabled", propertyTag, hiddenTc.getId());
        transformer.setProperty(key, String.valueOf(hiddenTc.isTapChangerControlEnabled()));

        key = String.format("%s.%s.step", propertyTag, hiddenTc.getId());
        transformer.setProperty(key, String.valueOf(hiddenTc.getTapPosition()));

        if (hiddenTc.getRegulatingControlId() != null) {
            key = String.format("%s.%s.tapChangerControl", propertyTag, hiddenTc.getId());
            transformer.setProperty(key, hiddenTc.getRegulatingControlId());

            RegulatingControl rc = getRegulatingControl(context, hiddenTc.getRegulatingControlId());
            if (rc != null) {
                // isRegulating always false in hidden tapChangers
                key = String.format("%s.%s.isRegulating", propertyTag, hiddenTc.getId());
                transformer.setProperty(key, "false");

                key = String.format("%s.%s.targetValue", propertyTag, hiddenTc.getId());
                transformer.setProperty(key, String.valueOf(rc.getTargetValue()));

                key = String.format("%s.%s.targetDeadBand", propertyTag, hiddenTc.getId());
                transformer.setProperty(key, String.valueOf(rc.getTargetDeadBand()));
            }
        }
    }
}
