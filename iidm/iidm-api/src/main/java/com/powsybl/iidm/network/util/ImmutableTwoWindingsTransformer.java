/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTwoWindingsTransformer extends AbstractImmutableIdentifiable<TwoWindingsTransformer> implements TwoWindingsTransformer {

    TwoWindingsTransformer twt;

    public ImmutableTwoWindingsTransformer(TwoWindingsTransformer twt) {
        super(twt);
        this.twt = Objects.requireNonNull(twt);
    }

    public static ImmutableTwoWindingsTransformer ofNullable(TwoWindingsTransformer twt) {
        return twt == null ? null : new ImmutableTwoWindingsTransformer(twt);
    }

    @Override
    public Substation getSubstation() {
        return ImmutableSubstation.ofNullable(twt.getSubstation());
    }

    @Override
    public double getR() {
        return twt.getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getX() {
        return twt.getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getG() {
        return twt.getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getB() {
        return twt.getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRatedU1() {
        return twt.getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRatedU2() {
        return twt.getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        return ImmutablePhaseTapChanger.ofNullable(twt.getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return ImmutableRatioTapChanger.ofNullable(twt.getRatioTapChanger());
    }

    // branch interfaces
    @Override
    public Terminal getTerminal1() {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal1());
    }

    @Override
    public Terminal getTerminal2() {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal2());
    }

    @Override
    public Terminal getTerminal(Side side) {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal(side));
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal(voltageLevelId));
    }

    @Override
    public Side getSide(Terminal terminal) {
        if (terminal instanceof ImmutableTerminal) {
            return identifiable.getSide(((ImmutableTerminal) terminal).getTerminal());
        } else {
            return identifiable.getSide(terminal);
        }
    }

    @Override
    public CurrentLimits getCurrentLimits(Side side) {
        return ImmutableCurrentLimits.ofNullable(identifiable.getCurrentLimits(side));
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return ImmutableCurrentLimits.ofNullable(identifiable.getCurrentLimits1());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return ImmutableCurrentLimits.ofNullable(identifiable.getCurrentLimits2());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isOverloaded() {
        return identifiable.isOverloaded();
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
        return identifiable.isOverloaded(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        return identifiable.getOverloadDuration();
    }

    @Override
    public boolean checkPermanentLimit(Side side, float limitReduction) {
        return identifiable.checkPermanentLimit(side, limitReduction);
    }

    @Override
    public boolean checkPermanentLimit(Side side) {
        return identifiable.checkPermanentLimit(side);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction) {
        return identifiable.checkPermanentLimit1(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit1() {
        return identifiable.checkPermanentLimit1();
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction) {
        return identifiable.checkPermanentLimit2(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit2() {
        return identifiable.checkPermanentLimit2();
    }

    @Override
    public Overload checkTemporaryLimits(Side side, float limitReduction) {
        return identifiable.checkTemporaryLimits(side, limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits(Side side) {
        return identifiable.checkTemporaryLimits(side);
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction) {
        return identifiable.checkTemporaryLimits1(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits1() {
        return identifiable.checkTemporaryLimits1();
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction) {
        return identifiable.checkTemporaryLimits2(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits2() {
        return identifiable.checkTemporaryLimits2();
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(i -> ImmutableTerminal.ofNullable(i)).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}