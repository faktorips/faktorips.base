/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

/**
 * Methods this class test, whether the target type of the selected element is present and valid.
 */

public class AbstractAddAndNewProductCmptCommandTest {

    private TestCommand testCommand;

    private Object evaluationContext;

    private SingletonMockHelper singletonMockHelper;

    private IWorkbenchWindow activeWorkbenchWindow;

    @Before
    public void setUp() throws Exception {

        testCommand = new TestCommand();
        evaluationContext = mock(Object.class);

    }

    @Test
    public void testSetEnebledString() {

        IWorkbenchPage workbenchPage = testInit();

        ProductCmptEditor productCmptEditor = mock(ProductCmptEditor.class);
        when(workbenchPage.getActiveEditor()).thenReturn(productCmptEditor);

        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmptEditor.getProductCmpt()).thenReturn(productCmpt);

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(productCmpt.getIpsProject()).thenReturn(ipsProject);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(productCmptType);

        IProductCmptTypeAssociation typeAssociation = mock(IProductCmptTypeAssociation.class);
        when(productCmptType.findAssociation("test", ipsProject)).thenReturn(typeAssociation);

        IProductCmptType targetProductCmptType = mock(IProductCmptType.class);
        when(typeAssociation.findTargetProductCmptType(ipsProject)).thenReturn(targetProductCmptType);

        testCommand.setEnabled(evaluationContext);

        assertTrue(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testSetEnebledProductCmptReference() {

        ISelectionService selectionService = testGetSelectionService();

        ISelection selection = mock(IStructuredSelection.class);
        when(selectionService.getSelection()).thenReturn(selection);

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;

        IProductCmptReference productCmptReference = mock(IProductCmptReference.class);
        when(structuredSelection.getFirstElement()).thenReturn(productCmptReference);
        when(productCmptReference.hasAssociationChildren()).thenReturn(true);

        testCommand.setEnabled(evaluationContext);

        assertTrue(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testSetEnebledSelectionNull() {

        ISelectionService selectionService = testGetSelectionService();

        ISelection selection = null;
        when(selectionService.getSelection()).thenReturn(selection);

        testCommand.setEnabled(evaluationContext);

        assertTrue(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testIsValidAssociationNameProductCmptEditorNull() {

        IWorkbenchPage workbenchPage = testInit();
        ProductCmptEditor productCmptEditor = null;
        when(workbenchPage.getActiveEditor()).thenReturn(productCmptEditor);

        testCommand.setEnabled(evaluationContext);

        assertFalse(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testIsValidAssociationNameIProductCmptTypeNull() {

        IWorkbenchPage workbenchPage = testInit();
        ProductCmptEditor productCmptEditor = mock(ProductCmptEditor.class);
        when(workbenchPage.getActiveEditor()).thenReturn(productCmptEditor);

        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmptEditor.getProductCmpt()).thenReturn(productCmpt);

        IProductCmptType productCmptType = null;
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(productCmpt.getIpsProject()).thenReturn(ipsProject);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(productCmptType);

        testCommand.setEnabled(evaluationContext);

        assertFalse(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testIsValidAssociationNameProductCmptTypeAssociationNull() {

        IWorkbenchPage workbenchPage = testInit();
        ProductCmptEditor productCmptEditor = mock(ProductCmptEditor.class);
        when(workbenchPage.getActiveEditor()).thenReturn(productCmptEditor);

        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmptEditor.getProductCmpt()).thenReturn(productCmpt);

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(productCmpt.getIpsProject()).thenReturn(ipsProject);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(productCmptType);

        IProductCmptTypeAssociation typeAssociation = null;
        when(productCmptType.findAssociation("test", ipsProject)).thenReturn(typeAssociation);

        testCommand.setEnabled(evaluationContext);

        assertFalse(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    @Test
    public void testIsValidAssociationNameTargetProductCmptTypeNull() {

        IWorkbenchPage workbenchPage = testInit();
        ProductCmptEditor productCmptEditor = mock(ProductCmptEditor.class);
        when(workbenchPage.getActiveEditor()).thenReturn(productCmptEditor);

        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(productCmptEditor.getProductCmpt()).thenReturn(productCmpt);

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(productCmpt.getIpsProject()).thenReturn(ipsProject);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(productCmptType);

        IProductCmptTypeAssociation typeAssociation = mock(IProductCmptTypeAssociation.class);
        when(productCmptType.findAssociation("test", ipsProject)).thenReturn(typeAssociation);

        IProductCmptType targetProductCmptType = null;
        when(typeAssociation.findTargetProductCmptType(ipsProject)).thenReturn(targetProductCmptType);

        testCommand.setEnabled(evaluationContext);

        assertFalse(testCommand.isEnabled());

        singletonMockHelper.reset();
    }

    private IWorkbenchPage testInit() {

        ISelectionService selectionService = testGetSelectionService();

        ISelection selection = mock(IStructuredSelection.class);
        when(selectionService.getSelection()).thenReturn(selection);

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;

        when(structuredSelection.getFirstElement()).thenReturn("test");

        IWorkbenchPage workbenchPage = mock(IWorkbenchPage.class);
        when(activeWorkbenchWindow.getActivePage()).thenReturn(workbenchPage);

        return workbenchPage;
    }

    private ISelectionService testGetSelectionService() {

        singletonMockHelper = new SingletonMockHelper();

        IpsUIPlugin uiPlugin = mock(IpsUIPlugin.class, RETURNS_DEEP_STUBS);
        singletonMockHelper.setSingletonInstance(IpsUIPlugin.class, uiPlugin);

        activeWorkbenchWindow = mock(IWorkbenchWindow.class);
        when(uiPlugin.getWorkbench().getActiveWorkbenchWindow()).thenReturn(activeWorkbenchWindow);

        ISelectionService selectionService = mock(ISelectionService.class);
        when(activeWorkbenchWindow.getSelectionService()).thenReturn(selectionService);

        return selectionService;
    }

    private static class TestCommand extends AbstractAddAndNewProductCmptCommand {

        @Override
        public Object execute(ExecutionEvent event) throws ExecutionException {

            return null;
        }

    }

}
