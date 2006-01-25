package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jdt.internal.ui.util.TableLayoutComposite;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRuleDef;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.ui.controls.EditTableControl;

public class ValidatedAttributesControl extends EditTableControl {

	private static String MESSAGE_COLUMN_PROPERTY = "message";
	private static String ATTRIBUTENAME_COLUMN_PROPERTY = "attributeName";
	
	private static String[] columnProperties = new String[]{MESSAGE_COLUMN_PROPERTY, ATTRIBUTENAME_COLUMN_PROPERTY};

	private IValidationRuleDef rule;
	 
	public ValidatedAttributesControl(Object modelObject, Composite parent) {
		super(modelObject, parent, SWT.NONE, "Specify the attributes that are validated within this rule:");
	}

	protected void initModelObject(Object modelObject) {
		 rule = (IValidationRuleDef)modelObject;
	}

	protected UnfocusableTextCellEditor[] createCellEditors() {
        UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
        editors[0] = null; // no editor for the message image column
        editors[1] = new UnfocusableTextCellEditor(getTable());
		return editors;
	}

	protected ICellModifier createCellModifier() {
		return new CellModifier();
	}

	protected String[] getColumnPropertyNames() {
		return columnProperties;
	}

	protected IStructuredContentProvider createContentProvider() {
		return new ContentProvider();
	}

	protected ILabelProvider createLabelProvider() {
		return new TableLabelProvider();
	}

	protected void createTableColumns(Table table) {
		TableColumn messageColumn = new TableColumn(table, SWT.NONE);
		messageColumn.setResizable(false);
		TableColumn attributeNameColumn = new TableColumn(table, SWT.NONE);
		attributeNameColumn.setResizable(false);
		attributeNameColumn.setText("Attribute name");

	}

	protected void addColumnLayoutData(TableLayoutComposite layouter) {
		layouter.addColumnData(new ColumnPixelData(10, false)); // message image
		layouter.addColumnData(new ColumnWeightData(100, true));
	}

	protected Object addElement() {
		return rule.addValidatedAttribute("");
	}

	protected void removeElement(int index) {
		rule.removeValidatedAttribute(index);
	}

	protected void swapElements(int index1, int index2) {
		// TODO Auto-generated method stub

	}

	private class ContentProvider implements IStructuredContentProvider{

		public Object[] getElements(Object inputElement) {
			ValidationRuleDef rule = (ValidationRuleDef)inputElement;
			String[] validatedAttributes = rule.getValidatedAttributes();
			IndexedValidatedAttributeWrapper[] indexedWrappers = new IndexedValidatedAttributeWrapper[validatedAttributes.length];
			for (int i = 0; i < validatedAttributes.length; i++) {
				indexedWrappers[i] = new IndexedValidatedAttributeWrapper(i);
			}
			return indexedWrappers;
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			
			if(columnIndex == 1){
				return ((IndexedValidatedAttributeWrapper)element).getAttributeName();
			}
			return "";
		}
	}
	
	private class IndexedValidatedAttributeWrapper{
		
		private int index;

		public IndexedValidatedAttributeWrapper(int index) {
			this.index = index;
		}

		public boolean equals(Object obj) {
			if(obj instanceof IndexedValidatedAttributeWrapper){
				return ((IndexedValidatedAttributeWrapper)obj).index == index;
			}
			return false;
		}

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
	
	private class CellModifier implements ICellModifier{

		public boolean canModify(Object element, String property) {
			if(ATTRIBUTENAME_COLUMN_PROPERTY.equals(property)){
				return true;
			}
			return false;
		}

		public Object getValue(Object element, String property) {
			return ((IndexedValidatedAttributeWrapper)element).getAttributeName();
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
                element = ((Item) element).getData();   
            }
			IndexedValidatedAttributeWrapper validatedAttribute = (IndexedValidatedAttributeWrapper)element;
			validatedAttribute.setAttributeName((String)value);
			getTableViewer().update(element, null);
		}
	}
}
