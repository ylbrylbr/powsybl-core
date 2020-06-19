/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.BranchTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchContingencyTest {

    @Test
    public void test() {
        BranchContingency contingency = new BranchContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.BRANCH, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof BranchTripping);

        contingency = new BranchContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new BranchContingency("c1", "vl1"), new BranchContingency("c1", "vl1"))
                .addEqualityGroup(new BranchContingency("c2"), new BranchContingency("c2"))
                .testEquals();

        Contingency contingency2 = Contingency.line("LINE");
        assertEquals("LINE", contingency2.getId());
        assertEquals(1, contingency2.getElements().size());
        assertEquals("LINE", contingency2.getElements().get(0).getId());
        assertEquals(ContingencyElementType.LINE, contingency2.getElements().get(0).getType());

        Contingency contingency3 = Contingency.twoWindingsTransformer("TWT");
        assertEquals("TWT", contingency3.getId());
        assertEquals(1, contingency3.getElements().size());
        assertEquals("TWT", contingency3.getElements().get(0).getId());
        assertEquals(ContingencyElementType.TWO_WINDINGS_TRANSFORMER, contingency3.getElements().get(0).getType());
    }
}
