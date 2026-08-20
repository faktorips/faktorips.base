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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;
    private AddProductCmptLinkCommand command;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        command = mock(AddProductCmptLinkCommand.class, CALLS_REAL_METHODS);

        when(editor.getActiveGeneration()).thenReturn(gen);
        lenient().when(gen.getProductCmpt()).thenReturn(prodCmpt);
        lenient().when(association.getName()).thenReturn("foo");
        lenient().when(gen.isContainerFor(association)).thenReturn(true);
        lenient().when(staticAssociation.getName()).thenReturn("bar");
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
