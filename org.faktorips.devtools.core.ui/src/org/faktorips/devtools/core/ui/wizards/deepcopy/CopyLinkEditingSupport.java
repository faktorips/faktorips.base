/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;

public class CopyLinkEditingSupport extends EditingSupport {

    private final ComboBoxViewerCellEditor comboBoxCellEditor;

    private final DeepCopyTreeStatus treeStatus;

    public CopyLinkEditingSupport(CheckboxTreeViewer viewer, DeepCopyTreeStatus treeStatus) {
        super(viewer);
        this.comboBoxCellEditor = new ComboBoxViewerCellEditor(viewer.getTree(), SWT.READ_ONLY);
        comboBoxCellEditor.setContenProvider(new ArrayContentProvider());
        comboBoxCellEditor.setInput(new CopyOrLink[] { CopyOrLink.COPY, CopyOrLink.LINK });
        this.treeStatus = treeStatus;
    }

    @Override
    protected boolean canEdit(Object element) {
        if (element instanceof IProductCmptReference || element instanceof IProductCmptStructureTblUsageReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            if (reference.getParent() == null) {
                // cannot edit root node
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
            treeStatus.setCopOrLink(reference, (CopyOrLink)value);
        }
    }

    @Override
    public CheckboxTreeViewer getViewer() {
        return (CheckboxTreeViewer)super.getViewer();
    }

}
