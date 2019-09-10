package com.powsybl.cgmes.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesModel;

public abstract class AbstractIidmToCgmes {
    public AbstractIidmToCgmes(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
    }

    public AbstractIidmToCgmes(IidmChange change) {
        this.change = change;
        this.cgmes = null;
    }

    protected List<CgmesPredicateDetails> convert() throws Exception {

        if (change instanceof IidmChangeOnUpdate) {
            allCgmesDetails = new ArrayList<CgmesPredicateDetails>();
            allCgmesDetails.add(switcher().get(change.getAttribute()));

        } else if (change instanceof IidmChangeOnCreate) {
            // for onCreate all fields are inside the Identifiable object.
            allCgmesDetails = new ArrayList<CgmesPredicateDetails>(switcher().values());

        } else if (change instanceof IidmChangeOnRemove) {
            // onRemove is pending

        } else {

        }

        return allCgmesDetails;
    }

    public String getIidmInstanceName() {
        return change.getIdentifiable().getClass().getSimpleName();
    }

    protected abstract Map<String, CgmesPredicateDetails> switcher();

    protected IidmChange change;
    protected CgmesModel cgmes;
    protected Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates;
    protected List<CgmesPredicateDetails> allCgmesDetails;

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";
    public static final String SHUNTCOMPENSATOR_IMPL = "ShuntCompensatorImpl";

}