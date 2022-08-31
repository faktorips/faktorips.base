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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CycleSearchTest {

    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IIpsProject unrelatedProject;
    private CycleSearch cycleSearch;

    @Mock
    private IpsProjectRefEntry ipsProjectRefEntry;

    @Before
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

    @Test(expected = IllegalArgumentException.class)
    public void testCycleSearch_InitialProjectIsNull() {
        CycleSearch cycleSearchWithNullProject = new CycleSearch(null);
        cycleSearchWithNullProject.processEntry(ipsProjectRefEntry);
    }
}
