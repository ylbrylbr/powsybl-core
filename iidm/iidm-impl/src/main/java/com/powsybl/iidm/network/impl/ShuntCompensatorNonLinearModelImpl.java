/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorNonLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorNonLinearModel {

    static class SectionImpl implements Section {

        private final double b;

        private final double g;

        SectionImpl(double b, double g) {
            this.b = b;
            this.g = g;
        }

        @Override
        public double getB() {
            return b;
        }

        @Override
        public double getG() {
            return g;
        }
    }

    private final TreeMap<Integer, SectionImpl> sections;

    ShuntCompensatorNonLinearModelImpl(TreeMap<Integer, SectionImpl> sections) {
        this.sections = sections;
    }

    @Override
    public double getMaximumB() {
        if (sections.values().stream().allMatch(section -> section.getB() >= 0)) {
            return sections.values().stream().mapToDouble(SectionImpl::getB).sum();
        } else if (sections.values().stream().allMatch(section -> section.getB() <= 0)) {
            return 0;
        }
        return sections.keySet().stream().mapToDouble(this::getCurrentB).max().orElseThrow(() -> new PowsyblException("a shunt compensator must have at least one section"));
    }

    @Override
    public double getMaximumG() {
        double maxG = sections.keySet().stream()
                .mapToDouble(this::getCurrentG)
                .filter(g -> !Double.isNaN(g))
                .max()
                .orElse(Double.NaN);
        if (!Double.isNaN(maxG)) {
            return Math.max(0, maxG);
        }
        return maxG;
    }

    @Override
    public double getMinimumB() {
        return sections.values().stream()
                .mapToDouble(SectionImpl::getB)
                .filter(b -> b <= 0)
                .sum();
    }

    @Override
    public double getMinimumG() {
        double minG = sections.keySet().stream()
                .mapToDouble(this::getCurrentG)
                .filter(g -> !Double.isNaN(g))
                .min()
                .orElse(Double.NaN);
        if (!Double.isNaN(minG)) {
            return Math.min(0, minG);
        }
        return minG;
    }

    @Override
    public Optional<Section> getSection(int sectionNum) {
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionNum);
        return Optional.ofNullable(sections.get(sectionNum));
    }

    @Override
    public Map<Integer, Section> getSections() {
        return Collections.unmodifiableMap(sections);
    }

    @Override
    public ShuntCompensatorNonLinearModel addOrReplaceSection(int sectionNum, double b, double g) {
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionNum);
        ValidationUtil.checkSectionB(shuntCompensator, b);
        SectionImpl oldValue = sections.put(sectionNum, new SectionImpl(b, g));
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionNum, "b"), Optional.ofNullable(oldValue).map(SectionImpl::getB).orElse(Double.NaN), b);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionNum, "g"), Optional.ofNullable(oldValue).map(SectionImpl::getG).orElse(Double.NaN), g);
        return this;
    }

    @Override
    public ShuntCompensatorNonLinearModel removeSection(int sectionNum) {
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionNum);
        if (shuntCompensator.getCurrentSectionCount() == sectionNum) {
            throw new ValidationException(shuntCompensator, "the number of section to remove (" + sectionNum + ") is the current section count");
        }
        if (!sections.containsKey(sectionNum)) {
            throw new ValidationException(shuntCompensator, invalidSectionNumberMessage(sectionNum, "susceptance nor conductance"));
        }
        SectionImpl oldValue = sections.remove(sectionNum);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionNum, "b"), oldValue.getB(), Double.NaN);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionNum, "g"), oldValue.getG(), Double.NaN);
        return this;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return ShuntCompensatorModelType.NON_LINEAR;
    }

    @Override
    public boolean containsSection(int sectionNumber) {
        return sections.containsKey(sectionNumber);
    }

    @Override
    public int getMaximumSectionCount() {
        return sections.lastKey();
    }

    @Override
    public double getCurrentB(int currentSectionIndex) {
        return sections.entrySet().stream()
                .filter(e -> e.getKey() <= currentSectionIndex)
                .mapToDouble(e -> e.getValue().getB())
                .sum();
    }

    @Override
    public double getCurrentG(int currentSectionIndex) {
        return sections.entrySet().stream()
                .filter(e -> e.getKey() <= currentSectionIndex)
                .mapToDouble(e -> e.getValue().getG())
                .sum();
    }

    @Override
    public double getB(int sectionNum) {
        return Optional.ofNullable(sections.get(sectionNum))
                .map(SectionImpl::getB)
                .orElseThrow(() -> new PowsyblException(invalidSectionNumberMessage(sectionNum, "susceptance")));
    }

    @Override
    public double getG(int sectionNum) {
        return Optional.ofNullable(sections.get(sectionNum))
                .map(SectionImpl::getG)
                .orElseThrow(() -> new PowsyblException(invalidSectionNumberMessage(sectionNum, "conductance")));
    }

    private static String invalidSectionNumberMessage(int sectionNum, String attributes) {
        return "the given number of section (" + sectionNum + ") is not associated with any " + attributes;
    }

    private static String notifyUpdateSection(int sectionNum, String attribute) {
        return "section" + sectionNum + "." + attribute;
    }
}
