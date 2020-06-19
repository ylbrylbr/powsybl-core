/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.BusbarSectionTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BusbarSectionContingencyTest {

    @Test
    public void test() {
        Contingency contingency = Contingency.busbarSection("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());

        BusbarSectionContingency bbsContingency = new BusbarSectionContingency("id");
        assertEquals("id", bbsContingency.getId());
        assertEquals(ContingencyElementType.BUSBAR_SECTION, bbsContingency.getType());

        assertNotNull(bbsContingency.toTask());
        assertTrue(bbsContingency.toTask() instanceof BusbarSectionTripping);

        new EqualsTester()
                .addEqualityGroup(new BusbarSectionContingency("bbs1"), new BusbarSectionContingency("bbs1"))
                .addEqualityGroup(new BusbarSectionContingency("bbs2"), new BusbarSectionContingency("bbs2"))
                .testEquals();

    }
}
