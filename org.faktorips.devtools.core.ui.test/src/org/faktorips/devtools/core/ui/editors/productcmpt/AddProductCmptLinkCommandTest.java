/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
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
        when(gen.isContainerFor(association)).thenReturn(true);

        command.getExistingLinks(editor, association);

        verify(gen).getLinksAsList(anyString());
        verify(prodCmpt, never()).getLinksAsList(anyString());
    }

    @Test
    public void testGetExistingLinksFromProdCmpt() {
        when(prodCmpt.isContainerFor(association)).thenReturn(true);

        command.getExistingLinks(editor, staticAssociation);

        verify(prodCmpt).getLinksAsList(anyString());
        verify(gen, never()).getLinksAsList(anyString());
    }

}
