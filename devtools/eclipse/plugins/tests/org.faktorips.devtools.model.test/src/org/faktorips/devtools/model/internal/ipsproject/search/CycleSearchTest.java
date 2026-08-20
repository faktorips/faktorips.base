/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class CycleSearchTest {


    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IIpsProject unrelatedProject;
    private CycleSearch cycleSearch;

    @Mock
    private IpsProjectRefEntry ipsProjectRefEntry;

    private MockitoSession mockito;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        cycleSearch = new CycleSearch(ipsProject);

        lenient().when(ipsProjectRefEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testProcessEntry_noCycle() {
        when(ipsProjectRefEntry.getReferencedIpsProject()).thenReturn(unrelatedProject);
        cycleSearch.processEntry(ipsProjectRefEntry);

        assertFalse(cycleSearch.isCycleDetected());
    }

    @Test
    public void testProcessEntry_cycle() {
        when(ipsProjectRefEntry.getReferencedIpsProject()).thenReturn(ipsProject);
        cycleSearch.processEntry(ipsProjectRefEntry);

        assertTrue(cycleSearch.isCycleDetected());
    }

    @Test
    public void testCycleSearch_InitialProjectIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            CycleSearch cycleSearchWithNullProject = new CycleSearch(null);
            cycleSearchWithNullProject.processEntry(ipsProjectRefEntry);
        });
    }

}
