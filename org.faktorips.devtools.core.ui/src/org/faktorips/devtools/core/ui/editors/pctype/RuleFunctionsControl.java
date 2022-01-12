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

import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectCompletionProcessor;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.runtime.MessageList;

/**
 * Control used by the <code>RuleEditDialog</code> to display and edit the business functions a rule
 * is applied in.
 */
public class RuleFunctionsControl extends EditTableControl {

    private static final String IMAGE_COLUMN_VIEWER_PROPERTY = "imageColumn"; //$NON-NLS-1$
    private static final String VALUE_COLUMN_VIEWER_PROPERTY = "valueColumn"; //$NON-NLS-1$

    private IValidationRule rule;

    public RuleFunctionsControl(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    public void initialize(Object modelObject, String label) {
        if (label == null) {
            super.initialize(modelObject, Messages.RuleFunctionsControl_title);
        } else {
            super.initialize(modelObject, label);
        }
        new MessageService(getTableViewer());
    }

    @Override
    protected void initModelObject(Object modelObject) {
        rule = (IValidationRule)modelObject;
    }

    public IValidationRule getRule() {
        return rule;
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
        new TableColumn(table, SWT.NONE).setResizable(false);
        new TableColumn(table, SWT.NONE).setResizable(false);
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { Messages.RuleFunctionsControl_titleColum1, Messages.RuleFunctionsControl_titleColumn2 };
    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        // message image
        layouter.addColumnData(new ColumnPixelData(20, false));
        layouter.addColumnData(new ColumnWeightData(100, true));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected UnfocusableTextCellEditor[] createCellEditors() {
        UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
        // no editor for the message image column
        editors[0] = null;
        editors[1] = new UnfocusableTextCellEditor(getTable());
        Text text = (Text)editors[1].getControl();
        IpsObjectCompletionProcessor processor = new IpsObjectCompletionProcessor(getRule().getIpsProject(),
                IpsObjectType.BUSINESS_FUNCTION);
        processor.setComputeProposalForEmptyPrefix(true);
        // TODO FIPS-7874 Replace SubjectControlContentAssistant with Platform UI's field assist
        // support
        org.eclipse.jface.contentassist.SubjectControlContentAssistant contentAssistant = CompletionUtil
                .createContentAssistant(processor);
        CompletionUtil.createHandlerForText(text, contentAssistant);
        editors[1].setContentAssistant(contentAssistant, 1);
        return editors;
    }

    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    @Override
    public Object addElement() {
        String newFct = ""; //$NON-NLS-1$
        getRule().addBusinessFunction(newFct);
        return newFct;
    }

    @Override
    public void removeElement(int index) {
        getRule().removeBusinessFunction(index);
        getTableViewer().refresh();
    }

    @Override
    protected void swapElements(int index1, int index2) {
        String name1 = rule.getBusinessFunction(index1);
        String name2 = rule.getBusinessFunction(index2);
        rule.setBusinessFunctions(index1, name2);
        rule.setBusinessFunctions(index2, name1);
    }

    private MessageList validate(Object element) throws CoreRuntimeException {
        IndexFctNameWrapper wrapper = (IndexFctNameWrapper)element;
        return rule.validate(rule.getIpsProject()).getMessagesFor(rule, IValidationRule.PROPERTY_BUSINESS_FUNCTIONS,
                wrapper.index);
    }

    /**
     * Updates the first column of the table. The first column shows images according to the
     * validation state of the entry in the second column of the same row.
     */
    public void updateValidationStatus() {
        ContentProvider contentProvider = (ContentProvider)getTableViewer().getContentProvider();
        Object[] elements = contentProvider.getElements(rule);
        getTableViewer().update(elements, new String[] { IMAGE_COLUMN_VIEWER_PROPERTY });
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 0) {
                return null;
            }
            try {
                MessageList list = validate(element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                        false);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return ""; //$NON-NLS-1$
            }
            return element.toString();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            if (IMAGE_COLUMN_VIEWER_PROPERTY.equals(property)) {
                return true;
            }
            if (VALUE_COLUMN_VIEWER_PROPERTY.equals(property)) {
                return true;
            }
            return false;
        }

    }

    private class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            String[] bfNames = getRule().getBusinessFunctions();
            IndexFctNameWrapper[] wrappers = new IndexFctNameWrapper[bfNames.length];
            for (int i = 0; i < bfNames.length; i++) {
                wrappers[i] = new IndexFctNameWrapper(i);
            }
            return wrappers;
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

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return isDataChangeable();
        }

        @Override
        public Object getValue(Object element, String property) {
            IndexFctNameWrapper wrapper = (IndexFctNameWrapper)element;
            return wrapper.getFctName();
        }

        @Override
        public void modify(Object element, String property, Object value) {
            if (element instanceof Item) {
                element = ((Item)element).getData();
            }
            IndexFctNameWrapper wrapper = (IndexFctNameWrapper)element;
            wrapper.setFctName((String)value);
            getTableViewer().update(element, null);
        }
    }

    private class IndexFctNameWrapper {

        private int index;

        IndexFctNameWrapper(int index) {
            this.index = index;
        }

        String getFctName() {
            return rule.getBusinessFunction(index);
        }

        void setFctName(String newName) {
            rule.setBusinessFunctions(index, newName);
        }

        @Override
        public String toString() {
            return getFctName();
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IndexFctNameWrapper)) {
                return false;
            }
            return index == ((IndexFctNameWrapper)o).index;
        }

    }

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected MessageList getMessagesFor(Object element) throws CoreRuntimeException {
            return validate(element);
        }

    }

}
