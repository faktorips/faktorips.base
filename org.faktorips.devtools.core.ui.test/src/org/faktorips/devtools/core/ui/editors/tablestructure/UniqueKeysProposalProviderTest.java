/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UniqueKeysProposalProviderTest {

    @Mock
    private UniqueKeysProposalProvider proposalProvider;

    private List<IIndex> uniqueKeylist;

    @Mock
    private IIndex index1;

    @Mock
    private IIndex index2;

    private IContentProposal[] proposals;

    @Before
    public void setUp() {

        uniqueKeylist = new ArrayList<IIndex>();
        uniqueKeylist.add(index1);
        uniqueKeylist.add(index2);
        IIndex[] uniqueKeysStructure = uniqueKeylist.toArray(new IIndex[0]);
        proposalProvider = new UniqueKeysProposalProvider();
        proposalProvider.setUniqueKeys(uniqueKeysStructure);

        when(index1.getName()).thenReturn("firstResult");
        when(index2.getName()).thenReturn("secondResult");
    }

    @Test
    public void test_getProposal_uniqueKeysAreNull() {
        UniqueKeysProposalProvider proposalProviderEmpty = new UniqueKeysProposalProvider();
        proposals = proposalProviderEmpty.getProposals("", 0);
        assertTrue(proposals.length == 0);
    }

    @Test
    public void test_getProposal_emptyInput() {
        proposals = proposalProvider.getProposals("", 0);
        IContentProposal proposal1 = proposals[0];

        assertEquals(index1.getName(), proposal1.getLabel());

        IContentProposal proposal2 = proposals[1];

        assertEquals(index2.getName(), proposal2.getLabel());
        assertTrue(proposals.length == 2);
    }

    @Test
    public void test_getProposal_falseInput() {
        proposals = proposalProvider.getProposals("NotFIRST", 8);

        assertTrue(proposals.length == 0);
    }

    @Test
    public void test_getProposal_UpperLowerCaseInput() {
        proposals = proposalProvider.getProposals("FIRST", 5);
        IContentProposal proposal1 = proposals[0];

        assertEquals(index1.getName(), proposal1.getLabel());
        assertTrue(proposals.length == 1);
    }
}
