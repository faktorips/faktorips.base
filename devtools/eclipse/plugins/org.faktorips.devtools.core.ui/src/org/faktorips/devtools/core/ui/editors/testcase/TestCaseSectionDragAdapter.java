/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;

/**
 * Provides drag support for {@linkplain TestCaseSection}.
 * <p>
 * Uses {@linkplain LocalSelectionTransfer} to transfer the current selection, only if
 * <ul>
 * <li>{@link TestCaseSection#isDataChangeable()} returns {@code true},
 * <li>a {@linkplain ITestPolicyCmpt test policy component} is selected,
 * <li>the selected test policy component has a parent test policy component
 * </ul>
 */
class TestCaseSectionDragAdapter extends DragSourceAdapter {

    private final ISelectionProvider selectionProvider;

    private final TestCaseSection testCaseSection;

    TestCaseSectionDragAdapter(ISelectionProvider selectionProvider, TestCaseSection testCaseSection) {
        this.selectionProvider = selectionProvider;
        this.testCaseSection = testCaseSection;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
        Object selected = selection.getFirstElement();

        if (selected instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt parentTestPolicyCmpt = ((ITestPolicyCmpt)selected).getParentTestPolicyCmpt();
            event.doit = testCaseSection.isDataChangeable() && parentTestPolicyCmpt != null;
            if (event.doit) {
                testCaseSection.setLocalDragAndDrop(true);
                LocalSelectionTransfer.getTransfer().setSelection(selectionProvider.getSelection());
            }
        }
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        testCaseSection.setLocalDragAndDrop(false);
    }

}
