/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.controls.contentproposal.ContentProposals;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.runtime.MessageList;

/**
 * A GUI control to edit the validationAttributes property of a IValidationRuleDef object.
 */
public class ValidatedAttributesControl extends EditTableControl {

    private static final String MESSAGE_COLUMN_PROPERTY = "message"; //$NON-NLS-1$

    private static final String ATTRIBUTENAME_COLUMN_PROPERTY = "attributeName"; //$NON-NLS-1$

    private static String[] columnProperties = { MESSAGE_COLUMN_PROPERTY, ATTRIBUTENAME_COLUMN_PROPERTY };

    private IValidationRule rule;

    public ValidatedAttributesControl(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    public void initialize(Object modelObject, String label) {
        String validatedAttributesLabel = label;
        if (validatedAttributesLabel == null) {
            validatedAttributesLabel = Messages.ValidatedAttributesControl_description;
        }
        super.initialize(modelObject, validatedAttributesLabel);
        new MessageService(getTableViewer());
    }

    @Override
    protected void initModelObject(Object modelObject) {
        rule = (IValidationRule)modelObject;
    }

    @Override
    protected UnfocusableTextCellEditor[] createCellEditors() {
        UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
        editors[0] = null;
        editors[1] = new UnfocusableTextCellEditor(getTable());
        ContentProposals.forText((Text)editors[1].getControl(), new ValidatedAttributesContentProposalProvider(rule));
        return editors;
    }

    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return columnProperties;
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    @Override
    protected void createTableColumns(Table table) {
        TableColumn messageColumn = new TableColumn(table, SWT.NONE);
        messageColumn.setResizable(false);
        TableColumn attributeNameColumn = new TableColumn(table, SWT.NONE);
        attributeNameColumn.setResizable(false);
        attributeNameColumn.setText(Messages.ValidatedAttributesControl_label);

    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        // message image
        layouter.addColumnData(new ColumnPixelData(20, false));
        layouter.addColumnData(new ColumnWeightData(100, true));
    }

    @Override
    protected Object addElement() {
        return rule.addValidatedAttribute(""); //$NON-NLS-1$
    }

    @Override
    protected void removeElement(int index) {
        rule.removeValidatedAttribute(index);
    }

    @Override
    protected void swapElements(int index1, int index2) {
        // do nothing
    }

    private IndexedValidatedAttributeWrapper[] getWrappersForAttributes() {
        String[] validatedAttributes = rule.getValidatedAttributes();
        IndexedValidatedAttributeWrapper[] indexedWrappers = new IndexedValidatedAttributeWrapper[validatedAttributes.length];
        for (int i = 0; i < validatedAttributes.length; i++) {
            indexedWrappers[i] = new IndexedValidatedAttributeWrapper(i);
        }
        return indexedWrappers;

    }

    private MessageList validate(Object element) {
        try {
            IndexedValidatedAttributeWrapper wrapper = (IndexedValidatedAttributeWrapper)element;
            return rule.validate(rule.getIpsProject()).getMessagesFor(rule,
                    IValidationRule.PROPERTY_VALIDATED_ATTRIBUTES, wrapper.index);
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return new MessageList();
        }
    }

    private class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return getWrappersForAttributes();
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 0) {
                return null;
            }
            MessageList list = validate(element);
            return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()), false);
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 1) {
                return ((IndexedValidatedAttributeWrapper)element).getAttributeName();
            }
            return ""; //$NON-NLS-1$
        }
    }

    private class IndexedValidatedAttributeWrapper {

        private int index;

        public IndexedValidatedAttributeWrapper(int index) {
            this.index = index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IndexedValidatedAttributeWrapper) {
                return ((IndexedValidatedAttributeWrapper)obj).index == index;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return index;
        }

        public String getAttributeName() {
            return rule.getValidatedAttributeAt(index);
        }

        public void setAttributeName(String attributeName) {
            rule.setValidatedAttributeAt(index, attributeName);
        }
    }

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            if (!isDataChangeable()) {
                return false;
            }

            if (ATTRIBUTENAME_COLUMN_PROPERTY.equals(property)) {
                return true;
            }
            return false;
        }

        @Override
        public Object getValue(Object element, String property) {
            return ((IndexedValidatedAttributeWrapper)element).getAttributeName();
        }

        @Override
        public void modify(Object element, String property, Object value) {
            Object elem = element;
            if (elem instanceof Item) {
                elem = ((Item)elem).getData();
            }
            IndexedValidatedAttributeWrapper validatedAttribute = (IndexedValidatedAttributeWrapper)elem;
            validatedAttribute.setAttributeName((String)value);
            getTableViewer().update(getWrappersForAttributes(), null);
        }
    }

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected MessageList getMessagesFor(Object element) {
            return validate(element);
        }
    }
}
