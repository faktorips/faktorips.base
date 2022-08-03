/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;

public class CopyLinkEditingSupport extends EditingSupport {

    private final ComboBoxViewerCellEditor comboBoxCellEditor;

    private final DeepCopyTreeStatus treeStatus;

    public CopyLinkEditingSupport(CheckboxTreeViewer viewer, DeepCopyTreeStatus treeStatus) {
        super(viewer);
        comboBoxCellEditor = new ComboBoxViewerCellEditor(viewer.getTree(), SWT.READ_ONLY);
        comboBoxCellEditor.setContentProvider(new ArrayContentProvider());
        comboBoxCellEditor.setInput(new CopyOrLink[] { CopyOrLink.COPY, CopyOrLink.LINK });
        this.treeStatus = treeStatus;
    }

    @Override
    protected boolean canEdit(Object element) {
        if (element instanceof IProductCmptReference || element instanceof IProductCmptStructureTblUsageReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            if (reference.isRoot()) {
                return false;
            }
            return treeStatus.isEnabled(reference);
        }

        return false;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return comboBoxCellEditor;
    }

    @Override
    protected Object getValue(Object element) {
        return treeStatus.getCopyOrLink((IProductCmptStructureReference)element);
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof IProductCmptStructureReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            treeStatus.setCopyOrLink(reference, (CopyOrLink)value);
        }
    }

    @Override
    public CheckboxTreeViewer getViewer() {
        return (CheckboxTreeViewer)super.getViewer();
    }

}
