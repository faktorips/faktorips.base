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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;

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