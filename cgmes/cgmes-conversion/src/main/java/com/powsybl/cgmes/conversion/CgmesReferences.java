/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class CgmesReferences {

    public void addIdentifiableType(String id, String cgmesType) {
        cgmesTypes.computeIfAbsent(id, k -> cgmesType);
    }

    public String getIdentifiableType(String id) {
        return cgmesTypes.get(id);
    }

    public Map<String, String> cgmesTypes() {
        return cgmesTypes;
    }

    private final Map<String, String> cgmesTypes = new HashMap<>();

}
