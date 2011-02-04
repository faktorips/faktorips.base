/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;

public class CopyLinkEditingSupport extends EditingSupport {

    private final ComboBoxViewerCellEditor comboBoxCellEditor;

    private final DeepCopyTreeStatus treeStatus;

    private Map<IProductCmptStructureReference, CopyOrLinkField> fieldMap = new HashMap<IProductCmptStructureReference, CopyLinkEditingSupport.CopyOrLinkField>();

    public CopyLinkEditingSupport(CheckboxTreeViewer viewer, DeepCopyTreeStatus treeStatus) {
        super(viewer);
        this.comboBoxCellEditor = new ComboBoxViewerCellEditor(viewer.getTree(), SWT.READ_ONLY);
        comboBoxCellEditor.setContenProvider(new ArrayContentProvider());
        comboBoxCellEditor.setInput(new CopyOrLink[] { CopyOrLink.COPY, CopyOrLink.LINK });
        this.treeStatus = treeStatus;
    }

    public void createAndBindFields(IProductCmptTreeStructure structure, BindingContext bindingContext) {
        for (final IProductCmptStructureReference element : structure.toSet(false)) {
            if (element instanceof IProductCmptTypeAssociationReference) {
                // need no copyOrLink field for associations
            } else {
                CopyOrLinkField copyOrLinkField = new CopyOrLinkField();
                LinkStatus status = treeStatus.getStatus((IIpsObjectPart)element.getWrapped());
                copyOrLinkField.value = status.getCopyOrLink();
                fieldMap.put(element, copyOrLinkField);
                bindingContext.bindContent(copyOrLinkField, status, LinkStatus.COPY_OR_LINK);
            }
        }
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
        return fieldMap.get(element).value; // treeStatus.getCopyOrLink((IProductCmptStructureReference)element);
    }

    @Override
    protected void setValue(Object element, Object value) {
        CopyOrLinkField field = fieldMap.get(element);
        field.setValue(value);

        expandOrCollapse((IProductCmptStructureReference)element, value == CopyOrLink.COPY);

        field.notifyChangeListeners();
    }

    private void expandOrCollapse(IProductCmptStructureReference element, boolean isCopy) {
        if (isCopy && treeStatus.isChecked(element)) {
            getViewer().expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
            updateAfterExpansion(element, true);
        } else {
            getViewer().collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
        }
    }

    /**
     * For any reason, the check state of collapsed elements is not updated. So we have to update
     * the correct state when expanding the tree
     */
    private void updateAfterExpansion(IProductCmptStructureReference reference, boolean recursive) {
        for (IProductCmptStructureReference child : reference.getChildren()) {
            boolean checked = treeStatus.isChecked(child);
            getViewer().setChecked(child, checked);
            getViewer().setGrayed(child, !treeStatus.isEnabled(child));
            if (recursive) {
                updateAfterExpansion(child, recursive);
            }
        }
    }

    @Override
    public CheckboxTreeViewer getViewer() {
        return (CheckboxTreeViewer)super.getViewer();
    }

    private class CopyOrLinkField extends DefaultEditField {

        private CopyOrLink value;

        @Override
        public Control getControl() {
            return comboBoxCellEditor.getControl();
        }

        @Override
        public void setValue(Object newValue) {
            value = (CopyOrLink)newValue;
        }

        @Override
        public String getText() {
            return value.toString();
        }

        @Override
        public void setText(String newText) {
            value = CopyOrLink.valueOf(newText);
        }

        @Override
        public void insertText(String text) {
            // do nothing
        }

        @Override
        public void selectAll() {
            // do nothing
        }

        @Override
        protected Object parseContent() throws Exception {
            return value;
        }

        @Override
        protected void addListenerToControl() {
            // listener is controlled by edit field
        }

        public void notifyChangeListeners() {
            super.notifyChangeListeners(new FieldValueChangedEvent(this), false);
        }

    }

}
