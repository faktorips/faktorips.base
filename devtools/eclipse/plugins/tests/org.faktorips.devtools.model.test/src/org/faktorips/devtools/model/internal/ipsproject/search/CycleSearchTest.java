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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CycleSearchTest {

    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IIpsProject unrelatedProject;
    private CycleSearch cycleSearch;

    @Mock
    private IpsProjectRefEntry ipsProjectRefEntry;

    @BeforeEach
    public void setUp() {
        cycleSearch = new CycleSearch(ipsProject);

        when(ipsProjectRefEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
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
