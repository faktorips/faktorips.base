/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateNewGenerationActionTest extends AbstractIpsPluginTest {

    @Mock
    private Shell shell;

    @Mock
    private ISelectionProvider selectionProvider;

    private CreateNewGenerationAction action;

    private AutoCloseable openMocks;

    @Override
    @Before
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        action = new CreateNewGenerationAction(shell, selectionProvider);
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // nothing to tear down
    }

    @Test
    public void testComputeEnabledProperty_DisabledIfSelectionIsEmpty() {
        assertFalse(action.computeEnabledProperty(new StructuredSelection()));
    }

    @Test
    public void testComputeEnabledProperty_DisabledIfElementOtherThanProductCmptOrProductCmptReferenceOrIpsSrcFileIncluded() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(true);
        when(productCmpt.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        when(ipsSrcFile.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(productCmpt,
                mock(IProductCmptReference.class), ipsSrcFile, mock(IEnumType.class)));

        assertFalse(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_EnabledIfOnlyProductCmptOrProductCmptReferenceOrProductCmptIpsSrcFileIncluded() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(true);
        when(productCmpt.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        when(ipsSrcFile.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(productCmpt, ipsSrcFile));

        assertTrue(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_DisabledIfIpsSrcFileContainsOtherThanProductCmpt() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getAdapter(IIpsObject.class)).thenReturn(mock(IEnumContent.class));
        IStructuredSelection selection = new StructuredSelection(ipsSrcFile);

        assertFalse(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_DisabledIfProductCmptDoesNotAllowGenerations() {
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(false);
        when(productCmpt.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(productCmpt));

        assertFalse(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_EnabledIfProductCmptAllowsGenerations() {
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(true);
        when(productCmpt.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(productCmpt));

        assertTrue(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_DisabledIfIpsSrcFileContainsProductCmptAndDoesNotAllowGenerations() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(false);
        when(ipsSrcFile.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(ipsSrcFile));

        assertFalse(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_EnabledIfIpsSrcFileContainsProductCmptAndAllowsGenerations() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmpt.allowGenerations()).thenReturn(true);
        when(ipsSrcFile.getAdapter(IIpsObject.class)).thenReturn(productCmpt);
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(ipsSrcFile));

        assertTrue(action.computeEnabledProperty(selection));
    }
}
