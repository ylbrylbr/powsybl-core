/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModelAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseSwitchedShunt;
import com.powsybl.psse.model.PsseSwitchedShunt35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SwitchedShuntCompensatorConverter extends AbstractConverter {

    public SwitchedShuntCompensatorConverter(PsseSwitchedShunt psseSwitchedShunt, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseSwitchedShunt = psseSwitchedShunt;
    }

    public void create() {
        List<ShuntBlock> shuntBlocks = defineShuntBlocks(psseSwitchedShunt);
        if (shuntBlocks.isEmpty()) {
            return;
        }

        String busId = getBusId(psseSwitchedShunt.getI());
        String id = defineShuntId(psseSwitchedShunt);
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseSwitchedShunt.getI()));

        ShuntCompensatorAdder adder = voltageLevel.newShuntCompensator()
            .setId(getShuntId(busId, id))
            .setConnectableBus(busId)
            .setSectionCount(defineSectionCount(psseSwitchedShunt.getBinit(), shuntBlocks));
        ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
        shuntBlocks.forEach(shuntBlock -> {
            for (int i = 0; i < shuntBlock.getN(); i++) {
                modelAdder.beginSection()
                    .setG(0.0)
                    .setB(shuntBlock.getB())
                    .endSection();
            }
        });
        modelAdder.add();
        ShuntCompensator shunt = adder.add();

        if (psseSwitchedShunt.getStat() == 1) {
            shunt.getTerminal().connect();
        }
    }

    public void addControl() {
        String busId = getBusId(psseSwitchedShunt.getI());
        String id = defineShuntId(psseSwitchedShunt);
        ShuntCompensator shunt = getNetwork().getShuntCompensator(getShuntId(busId, id));

        // Add control only if shunt has been created
        if (shunt == null) {
            return;
        }

        double vnom = shunt.getTerminal().getVoltageLevel().getNominalV();
        double vLow = psseSwitchedShunt.getVswlo() * vnom;
        double vHigh = psseSwitchedShunt.getVswhi() * vnom;
        double targetV = 0.5 * (vLow + vHigh);
        boolean psseVoltageRegulatorOn = defineVoltageRegulatorOn(psseSwitchedShunt);
        Terminal regulatingTerminal = defineRegulatingTerminal(psseSwitchedShunt, getNetwork());
        boolean voltageRegulatorOn = false;
        double targetDeadband = 0.0;
        if (targetV != 0.0) {
            targetDeadband = vHigh - vLow;
            voltageRegulatorOn = psseVoltageRegulatorOn;
        }

        shunt.setTargetV(targetV)
            .setTargetDeadband(targetDeadband)
            .setVoltageRegulatorOn(voltageRegulatorOn)
            .setRegulatingTerminal(regulatingTerminal);
    }

    // TODO complete all cases
    private static boolean defineVoltageRegulatorOn(PsseSwitchedShunt psseSwitchedShunt) {
        if (psseSwitchedShunt.getModsw() == 0) {
            return false;
        }
        return true;
    }

    // TODO complete all cases. Consider Nreg (version 35)
    private static Terminal defineRegulatingTerminal(PsseSwitchedShunt psseSwitchedShunt, Network network) {
        String defaultRegulatingBusId = getBusId(psseSwitchedShunt.getI());
        Terminal regulatingTerminal = null;
        if (psseSwitchedShunt.getSwrem() == 0) {
            Bus bus = network.getBusBreakerView().getBus(defaultRegulatingBusId);
            regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
        } else {
            String regulatingBusId = getBusId(psseSwitchedShunt.getSwrem());
            Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
            if (bus != null) {
                regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
            }
        }
        if (regulatingTerminal == null) {
            throw new PsseException("PSSE. SwitchedShunt " + defaultRegulatingBusId + "-"
                + defineShuntId(psseSwitchedShunt) + ". Unexpected regulatingTerminal.");
        }
        return regulatingTerminal;
    }

    private static int defineSectionCount(double binit, List<ShuntBlock> shuntBlocks) {
        double maxDistance = Double.MAX_VALUE;
        int sectionCount = 0;
        for (int i = 0; i < shuntBlocks.size(); i++) {
            double d = Math.abs(binit - shuntBlocks.get(i).getB());
            if (d < maxDistance) {
                maxDistance = d;
                sectionCount = i + 1; // index + 1 (count) is expected as sectionCount
            }
        }
        return sectionCount;
    }

    // IIDM only considers consecutive sections
    private static List<ShuntBlock> defineShuntBlocks(PsseSwitchedShunt psseSwitchedShunt) {
        List<ShuntBlock> psseBlocks = collectShuntBlocks(psseSwitchedShunt);
        List<ShuntBlock> psseReactorBlocks = psseBlocks.stream().filter(sb -> sb.getB() < 0.0).collect(Collectors.toList());
        List<ShuntBlock> psseCapacitorBlocks = psseBlocks.stream().filter(sb -> sb.getB() > 0.0).collect(Collectors.toList());

        // In that case we do not consider any switched combination
        // blocks are sorted and switched on in input order
        // When Adjm is zero the input order is considered
        if (psseSwitchedShunt.getAdjm() == 1) {
            psseReactorBlocks.sort(Comparator.comparing(ShuntBlock::getB).reversed());
            psseCapacitorBlocks.sort(Comparator.comparing(ShuntBlock::getB));

            LOGGER.warn("Switched combination not exactly supported ({})",
                getShuntId(getBusId(psseSwitchedShunt.getI()), defineShuntId(psseSwitchedShunt)));
        }

        List<ShuntBlock> shuntBlocks = new ArrayList<>();
        if (!psseReactorBlocks.isEmpty()) {
            double bAdd = 0.0;
            for (int i = 0; i < psseReactorBlocks.size(); i++) {
                for (int j = 0; j < psseReactorBlocks.get(i).getN(); j++) {
                    bAdd = bAdd + psseReactorBlocks.get(i).getB();
                    shuntBlocks.add(new ShuntBlock(1, 1, bAdd));
                }
            }
        }

        shuntBlocks.add(new ShuntBlock(1, 1, 0.0));

        if (!psseCapacitorBlocks.isEmpty()) {
            double bAdd = 0.0;
            for (int i = 0; i < psseCapacitorBlocks.size(); i++) {
                for (int j = 0; j < psseCapacitorBlocks.get(i).getN(); j++) {
                    bAdd = bAdd + psseCapacitorBlocks.get(i).getB();
                    shuntBlocks.add(new ShuntBlock(1, 1, bAdd));
                }
            }
        }

        return shuntBlocks;
    }

    // defined blocks can be reactors (< 0) or / and capacitors ( > 0)
    private static List<ShuntBlock> collectShuntBlocks(PsseSwitchedShunt psseSwitchedShunt) {
        List<ShuntBlock> shuntBlocks = new ArrayList<>();
        if (psseSwitchedShunt instanceof PsseSwitchedShunt35) {
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS1(), psseSwitchedShunt.getN1(), psseSwitchedShunt.getB1());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS2(), psseSwitchedShunt.getN2(), psseSwitchedShunt.getB2());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS3(), psseSwitchedShunt.getN3(), psseSwitchedShunt.getB3());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS4(), psseSwitchedShunt.getN4(), psseSwitchedShunt.getB4());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS5(), psseSwitchedShunt.getN5(), psseSwitchedShunt.getB5());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS6(), psseSwitchedShunt.getN6(), psseSwitchedShunt.getB6());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS7(), psseSwitchedShunt.getN7(), psseSwitchedShunt.getB7());
            addShuntBlock(shuntBlocks, ((PsseSwitchedShunt35) psseSwitchedShunt).getS8(), psseSwitchedShunt.getN8(), psseSwitchedShunt.getB8());
        } else {
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN1(), psseSwitchedShunt.getB1());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN2(), psseSwitchedShunt.getB2());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN3(), psseSwitchedShunt.getB3());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN4(), psseSwitchedShunt.getB4());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN5(), psseSwitchedShunt.getB5());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN6(), psseSwitchedShunt.getB6());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN7(), psseSwitchedShunt.getB7());
            addShuntBlock(shuntBlocks, 1, psseSwitchedShunt.getN8(), psseSwitchedShunt.getB8());
        }
        return shuntBlocks;
    }

    // Only in-service blocks are included (in-service s = 1 and out-of-service s = 0)
    private static void addShuntBlock(List<ShuntBlock> shuntBlocks, int s, int n, double b) {
        if (s == 0 || n == 0 || b == 0.0) {
            return;
        }
        shuntBlocks.add(new ShuntBlock(s, n, b));
    }

    static class ShuntBlock {
        int s;
        int n;
        double b;

        ShuntBlock(int s, int n, double b) {
            this.s = s;
            this.n = n;
            this.b = b;
        }

        int getS() {
            return s;
        }

        int getN() {
            return n;
        }

        double getB() {
            return b;
        }
    }

    private static String defineShuntId(PsseSwitchedShunt psseSwitchedShunt) {
        if (psseSwitchedShunt instanceof PsseSwitchedShunt35) {
            return ((PsseSwitchedShunt35) psseSwitchedShunt).getId();
        } else {
            return "1";
        }
    }

    private static String getShuntId(String busId, String id) {
        return busId + "-SwSH-" + id;
    }

    private final PsseSwitchedShunt psseSwitchedShunt;

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchedShuntCompensatorConverter.class);
}