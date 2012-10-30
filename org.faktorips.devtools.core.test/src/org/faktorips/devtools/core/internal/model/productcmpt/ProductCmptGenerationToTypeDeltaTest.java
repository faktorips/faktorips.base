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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptGenerationToTypeDeltaTest {

    @Mock
    IProductCmptGeneration gen;
    @Mock
    IIpsProject ipsProject;
    @Mock
    IProductCmptLink link1;
    @Mock
    IProductCmptLink link2;
    @Mock
    IProductCmptLink link3;
    @Mock
    IProductCmptType type;
    @Mock
    IProductCmptTypeAssociation assoc1;
    private ProductCmptGenerationToTypeDelta delta;

    @Before
    public void setUp() throws CoreException {
        delta = spy(new ProductCmptGenerationToTypeDelta(gen, ipsProject));

        List<IProductCmptLink> links = new ArrayList<IProductCmptLink>();
        links.add(link1);
        links.add(link2);
        links.add(link3);

        when(gen.getLinksAsList()).thenReturn(links);

        when(link1.getAssociation()).thenReturn("link1");
        when(link2.getAssociation()).thenReturn("link2");
        when(link3.getAssociation()).thenReturn("link3");

        doReturn(type).when(delta).getProductCmptType();
        doReturn(ipsProject).when(delta).getIpsProject();

        when(type.findAssociation("link1", ipsProject)).thenReturn(assoc1);
        when(type.findAssociation("link2", ipsProject)).thenReturn(assoc1);
        when(type.findAssociation("link3", ipsProject)).thenReturn(assoc1);
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry1() throws CoreException {
        verifyAddEntryForLink(link1, "link1");
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry2() throws CoreException {
        verifyAddEntryForLink(link2, "link2");
    }

    private void verifyAddEntryForLink(IProductCmptLink link, String assocName) throws CoreException {
        when(type.findAssociation(assocName, ipsProject)).thenReturn(null);
        delta.createAdditionalEntriesAndChildren();

        ArgumentCaptor<LinkWithoutAssociationEntry> captor = ArgumentCaptor.forClass(LinkWithoutAssociationEntry.class);

        verify(delta).addEntry(captor.capture());
        assertEquals(link, captor.getValue().getLink());
    }
}
