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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddProductCmptLinkCommandTest {

    @Mock
    ProductCmptEditor editor;
    @Mock
    ProductCmptGeneration gen;
    @Mock
    ProductCmpt prodCmpt;
    @Mock
    IProductCmptTypeAssociation association;
    @Mock
    IProductCmptTypeAssociation staticAssociation;
    private AddProductCmptLinkCommand command;

    @Before
    public void setUp() {
        command = mock(AddProductCmptLinkCommand.class, CALLS_REAL_METHODS);

        when(editor.getActiveGeneration()).thenReturn(gen);
        when(gen.getProductCmpt()).thenReturn(prodCmpt);
        when(association.isChangingOverTime()).thenReturn(true);
        when(staticAssociation.isChangingOverTime()).thenReturn(false);
    }

    @Test
    public void testGetExistingLinksFromGeneration() {
        command.getExistingLinks(editor, association);
        verify(gen).getLinksAsList(anyString());
        verify(prodCmpt, never()).getLinksAsList(anyString());
    }

    @Test
    public void testGetExistingLinksFromProdCmpt() {
        command.getExistingLinks(editor, staticAssociation);
        verify(prodCmpt).getLinksAsList(anyString());
        verify(gen, never()).getLinksAsList(anyString());
    }

}
