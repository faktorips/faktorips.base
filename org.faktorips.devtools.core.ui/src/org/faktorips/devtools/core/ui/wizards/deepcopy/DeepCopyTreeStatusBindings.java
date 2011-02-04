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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;

public class DeepCopyTreeStatusBindings {

    private final CheckboxTreeViewer viewer;

    private final Map<IProductCmptStructureReference, CellCheckField> cellFieldMap = new HashMap<IProductCmptStructureReference, CellCheckField>();

    private final DeepCopyTreeStatus treeStatus;

    public DeepCopyTreeStatusBindings(final CheckboxTreeViewer viewer, final DeepCopyTreeStatus statusMap) {
        this.viewer = viewer;
        this.treeStatus = statusMap;

        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                CellCheckField cellField = cellFieldMap.get(event.getElement());
                if (cellField != null) {
                    cellField.notifyChangeListeners();
                }
            }
        });

        viewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                IProductCmptStructureReference reference = (IProductCmptStructureReference)event.getElement();
                updateAfterExpansion(reference, false);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // do nothing
            }
        });

    }

    public void createAndBindFields(IProductCmptTreeStructure structure, BindingContext bindingContext) {
        for (final IProductCmptStructureReference element : structure.toSet(false)) {
            if (element instanceof IProductCmptTypeAssociationReference) {
                AssociationReferenceField field = new AssociationReferenceField(
                        (IProductCmptTypeAssociationReference)element);
                cellFieldMap.put(element, field);
                for (IProductCmptReference child : structure.getChildProductCmptReferences(element)) {
                    bindLinkStatus(bindingContext, child, field);
                }
            } else {
                CellCheckField field = new CellCheckField(element);
                cellFieldMap.put(element, field);
                bindLinkStatus(bindingContext, element, field);
            }
        }
    }

    private LinkStatus bindLinkStatus(BindingContext bindingContext,
            IProductCmptStructureReference element,
            CellCheckField field) {
        LinkStatus linkStatus = treeStatus.getStatus((IIpsObjectPart)element.getWrapped());
        bindingContext.bindContent(field, linkStatus, LinkStatus.CHECKED);
        return linkStatus;
    }

    /**
     * For any reason, the check state of collapsed elements is not updated. So we have to update
     * the correct state when expanding the tree
     */
    private void updateAfterExpansion(IProductCmptStructureReference reference, boolean recursive) {
        for (IProductCmptStructureReference child : reference.getChildren()) {
            boolean checked = treeStatus.isChecked(child);
            viewer.setChecked(child, checked);
            viewer.setGrayed(child, !treeStatus.isEnabled(child));
            if (recursive) {
                updateAfterExpansion(child, recursive);
            }
        }
    }

    /**
     * An Element is grayed if not all children or itself is checked. That means, an element is also
     * gray if none of its children is checked or itself is unchecked.
     * 
     */
    public void updateGrayedStatus(IProductCmptTreeStructure structure) {
        getAndUpdateGrayStatus(structure.getRoot());
    }

    private boolean getAndUpdateGrayStatus(IProductCmptStructureReference reference) {
        if (reference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)reference;
            if (associationReference.getChildren().length == 0 || associationReference.getAssociation().isAssoziation()) {
                // these elements are not visible so should never change gray state
                return false;
            }
        }
        boolean atLeastOneChildIsGrayed = false;
        for (IProductCmptStructureReference child : reference.getChildren()) {
            boolean grayedChild = getAndUpdateGrayStatus(child);
            atLeastOneChildIsGrayed = atLeastOneChildIsGrayed || grayedChild;
        }
        boolean thisIsChecked = treeStatus.isChecked(reference);
        boolean grayed = atLeastOneChildIsGrayed || !thisIsChecked;
        viewer.setGrayed(reference, grayed || !treeStatus.isEnabled(reference));
        return grayed;
    }

    public DeepCopyTreeStatus getTreeStatus() {
        return treeStatus;
    }

    private class CellCheckField extends DefaultEditField {

        private final IProductCmptStructureReference reference;

        public CellCheckField(IProductCmptStructureReference reference) {
            this.reference = reference;
        }

        @Override
        public Control getControl() {
            return viewer.getControl();
        }

        @Override
        public void setValue(Object newValue) {
            Boolean checked = (Boolean)newValue;
            viewer.setChecked(reference, checked);
            expandOrCollapse(checked);
        }

        private void expandOrCollapse(boolean checked) {
            if (checked && treeStatus.getCopyOrLink(reference) != CopyOrLink.LINK) {
                viewer.expandToLevel(reference, AbstractTreeViewer.ALL_LEVELS);
                updateAfterExpansion(reference, true);
            } else {
                viewer.collapseToLevel(reference, AbstractTreeViewer.ALL_LEVELS);
            }
        }

        @Override
        public String getText() {
            return String.valueOf(viewer.getChecked(reference));
        }

        @Override
        public void setText(String newText) {
            setValue(Boolean.valueOf(newText));
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
            return viewer.getChecked(reference);
        }

        @Override
        protected void addListenerToControl() {
            // listener is handled in outer class
        }

        public void notifyChangeListeners() {
            expandOrCollapse((Boolean)getValue());
            super.notifyChangeListeners(new FieldValueChangedEvent(this), true);
        }

    }

    private class AssociationReferenceField extends CellCheckField {

        private final IProductCmptTypeAssociationReference reference;

        public AssociationReferenceField(IProductCmptTypeAssociationReference reference) {
            super(reference);
            this.reference = reference;
        }

        @Override
        public void setValue(Object newValue) {
            // set checked when at least one child is checked (gray status is handled separately)
            boolean checked = false; // at least one is checked
            IProductCmptTypeAssociationReference associationReference = reference;
            for (IProductCmptReference cmptReference : associationReference.getStructure()
                    .getChildProductCmptReferences(associationReference)) {
                boolean isChecked = getTreeStatus().isChecked(cmptReference);
                checked = checked || isChecked;
            }
            viewer.setChecked(reference, checked);
        }

    }

}
