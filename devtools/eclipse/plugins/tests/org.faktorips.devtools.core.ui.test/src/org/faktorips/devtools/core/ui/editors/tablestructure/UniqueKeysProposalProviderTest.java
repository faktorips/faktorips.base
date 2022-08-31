/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class UniqueKeysProposalProviderTest {

    @Mock
    private UniqueKeysProposalProvider proposalProvider;

    private List<IIndex> uniqueKeylist;

    @Mock
    private IIndex index1;

    @Mock
    private IIndex index2;

    @Mock
    private ForeignKeyPMO pmo;

    private IContentProposal[] proposals;

    @Before
    public void setUp() {
        proposalProvider = new UniqueKeysProposalProvider(pmo);
        when(index1.getName()).thenReturn("firstResult");
        when(index2.getName()).thenReturn("secondResult");
    }

    @Test
    public void test_getProposal_uniqueKeysAreNull() {
        UniqueKeysProposalProvider proposalProviderEmpty = new UniqueKeysProposalProvider(pmo);
        proposals = proposalProviderEmpty.getProposals("", 0);
        assertTrue(proposals.length == 0);
    }

    @Test
    public void test_getProposal_emptyInput() {
        setUpKeyList();
        proposals = proposalProvider.getProposals("", 0);
        IContentProposal proposal1 = proposals[0];

        assertEquals(index1.getName(), proposal1.getLabel());

        IContentProposal proposal2 = proposals[1];

        assertEquals(index2.getName(), proposal2.getLabel());
        assertTrue(proposals.length == 2);
    }

    @Test
    public void test_getProposal_falseInput() {
        setUpKeyList();
        proposals = proposalProvider.getProposals("NotFIRST", 8);

        assertTrue(proposals.length == 0);
    }

    @Test
    public void test_getProposal_UpperLowerCaseInput() {
        setUpKeyList();
        proposals = proposalProvider.getProposals("FIRST", 5);
        IContentProposal proposal1 = proposals[0];

        assertEquals(index1.getName(), proposal1.getLabel());
        assertTrue(proposals.length == 1);
    }

    private void setUpKeyList() {
        uniqueKeylist = new ArrayList<>();
        uniqueKeylist.add(index1);
        uniqueKeylist.add(index2);
        IIndex[] uniqueKeysStructure = uniqueKeylist.toArray(new IIndex[0]);
        when(pmo.getAvailableUniqueKeys()).thenReturn(uniqueKeysStructure);
    }

}
