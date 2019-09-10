package com.powsybl.cgmes.update.elements14;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.ShuntCompensator;

public class ShuntCompensatorToShuntCompensator implements ConversionMapper {

    public ShuntCompensatorToShuntCompensator(IidmChange change) {
        this.change = change;
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        ShuntCompensator newShuntCompensator = (ShuntCompensator) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:ShuntCompensator"));

        String name = newShuntCompensator.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        String voltageLevelId = newShuntCompensator.getTerminal().getVoltageLevel().getId();
        if (!voltageLevelId.equals("NaN")) {
            map.put("equipmentContainer", new CgmesPredicateDetails(
                "cim:Equipment.MemberOf_EquipmentContainer", "_EQ", true, voltageLevelId));
        }

        double bPerSection = newShuntCompensator.getbPerSection();
        map.put("bPerSection",
            new CgmesPredicateDetails("cim:ShuntCompensator.bPerSection", "_EQ", false,
                String.valueOf(bPerSection)));

        double maximumSectionCount = newShuntCompensator.getMaximumSectionCount();
        map.put("maximumSectionCount", new CgmesPredicateDetails("cim:ShuntCompensator.maximumSections", "_EQ", false,
            String.valueOf(maximumSectionCount)));

        return map;
    }

    private IidmChange change;
}