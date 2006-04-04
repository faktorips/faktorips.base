/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
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
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectCompletionProcessor;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.util.message.MessageList;


/**
 * Control used by the <code>RuleEditDialog</code> to display and edit
 * the business functions a rule is applied in.
 */
public class RuleFunctionsControl extends EditTableControl {
    
    private IValidationRule rule;

    public RuleFunctionsControl(
            IValidationRule rule,
            Composite parent) {
        super(rule, parent, SWT.NONE, Messages.RuleFunctionsControl_title);
        new MessageService(getTableViewer());
    }
    
    protected void initModelObject(Object modelObject) {
        rule = (IValidationRule)modelObject;
    }
        
    public IValidationRule getRule() {
        return rule;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createContentProvider()
     */
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createLabelProvider()
     */
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createTableColumns(org.eclipse.swt.widgets.TableContentsGeneration)
     */
    protected void createTableColumns(Table table) {
		new TableColumn(table, SWT.NONE).setResizable(false);
		new TableColumn(table, SWT.NONE).setResizable(false);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#getColumnPropertyNames()
     */
    protected String[] getColumnPropertyNames() {
        return new String[]{Messages.RuleFunctionsControl_titleColum1, Messages.RuleFunctionsControl_titleColumn2};
    }
    
    /**
     * {@inheritDoc}
     */ 
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
		layouter.addColumnData(new ColumnPixelData(20, false)); // message image
		layouter.addColumnData(new ColumnWeightData(100, true));
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createCellEditors()
     */
    protected UnfocusableTextCellEditor[] createCellEditors() {
        UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
        editors[0] = null; // no editor for the message image column
        editors[1] = new UnfocusableTextCellEditor(getTable());
		Text text= (Text)editors[1].getControl();
		IpsObjectCompletionProcessor processor= new IpsObjectCompletionProcessor(IpsObjectType.BUSINESS_FUNCTION);
		processor.setComputeProposalForEmptyPrefix(true);
		processor.setPdProject(getRule().getIpsProject());
		SubjectControlContentAssistant contentAssistant= CompletionUtil.createContentAssistant(processor);
		ContentAssistHandler.createHandlerForText(text, contentAssistant);
        editors[1].setContentAssistant(contentAssistant, 1);
        return editors;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createCellModifier()
     */
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#addElement()
     */
    public Object addElement() {
        String newFct = ""; //$NON-NLS-1$
        getRule().addBusinessFunction(newFct);
        return newFct;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#removeElement(int)
     */
    public void removeElement(int index) {
        getRule().removeBusinessFunction(index);
        getTableViewer().refresh();
    }

    protected void swapElements(int index1, int index2) {
        String name1 = rule.getBusinessFunction(index1);
        String name2 = rule.getBusinessFunction(index2);
        rule.setBusinessFunctions(index1, name2);
        rule.setBusinessFunctions(index2, name1);
    }
    
    private MessageList validate(Object element) throws CoreException {
        IndexFctNameWrapper wrapper = (IndexFctNameWrapper)element;
        return rule.validate().getMessagesFor(wrapper.getFctName());
    }

	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex!=0) {
			    return null;
			}
			try {
				MessageList list = validate(element);
				return ValidationUtils.getSeverityImage(list.getSeverity());
			} catch (CoreException e) {
			    IpsPlugin.log(e);
			    return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
		    if (columnIndex==0) {
		        return ""; //$NON-NLS-1$
		    }
		    return element.toString();
		}
	}

    
    private class ContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            String[] bfNames = getRule().getBusinessFunctions();
            IndexFctNameWrapper[] wrappers = new IndexFctNameWrapper[bfNames.length];
            for (int i=0; i<bfNames.length; i++) {
                wrappers[i] = new IndexFctNameWrapper(i);
            }
            return wrappers;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
    }
    
    private class CellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            IndexFctNameWrapper wrapper = (IndexFctNameWrapper)element;
            return wrapper.getFctName();
        }

        public void modify(Object element, String property, Object value) {
            if (element instanceof Item) {
                element = ((Item) element).getData();   
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
        
        public String toString() {
            return getFctName();
        }
        
        public int hashCode() {
            return index;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof IndexFctNameWrapper)) {
                return false;
            }
            return index==((IndexFctNameWrapper)o).index;
        }
    }

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer viewer) {
            super(viewer);
        }

        protected MessageList getMessagesFor(Object element) throws CoreException {
            return validate(element);
        }
        
    }
}
