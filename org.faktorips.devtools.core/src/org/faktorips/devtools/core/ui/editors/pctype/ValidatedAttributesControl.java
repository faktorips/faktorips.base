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
import org.eclipse.jdt.internal.ui.util.TableLayoutComposite;
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
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.util.message.MessageList;

/**
 * A gui control to edit the validationAttributes property of a IValidationRuleDef object. 
 */
public class ValidatedAttributesControl extends EditTableControl {

	private static String MESSAGE_COLUMN_PROPERTY = "message"; //$NON-NLS-1$

	private static String ATTRIBUTENAME_COLUMN_PROPERTY = "attributeName"; //$NON-NLS-1$

	private static String[] columnProperties = new String[] {
			MESSAGE_COLUMN_PROPERTY, ATTRIBUTENAME_COLUMN_PROPERTY };

	private IValidationRule rule;

	public ValidatedAttributesControl(Object modelObject, Composite parent) {
		super(modelObject, parent, SWT.NONE,
				Messages.ValidatedAttributesControl_description);
		new MessageService(getTableViewer());
	}

	protected void initModelObject(Object modelObject) {
		rule = (IValidationRule) modelObject;
	}

	protected UnfocusableTextCellEditor[] createCellEditors() {
		UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
		editors[0] = null; // no editor for the message image column
		editors[1] = new UnfocusableTextCellEditor(getTable());
		ValidatedAttributesCompletionProcessor completionProcessor = new ValidatedAttributesCompletionProcessor(
				rule);
		completionProcessor.setComputeProposalForEmptyPrefix(true);
		ContentAssistHandler.createHandlerForText((Text) editors[1]
				.getControl(), CompletionUtil
				.createContentAssistant(completionProcessor));
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
		attributeNameColumn.setText(Messages.ValidatedAttributesControl_label);

	}

	protected void addColumnLayoutData(TableLayoutComposite layouter) {
		layouter.addColumnData(new ColumnPixelData(10, false)); // message image
		layouter.addColumnData(new ColumnWeightData(100, true));
	}

	protected Object addElement() {
		return rule.addValidatedAttribute(""); //$NON-NLS-1$
	}

	protected void removeElement(int index) {
		rule.removeValidatedAttribute(index);
	}

	protected void swapElements(int index1, int index2) {
		// TODO Auto-generated method stub

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
			IndexedValidatedAttributeWrapper wrapper = (IndexedValidatedAttributeWrapper) element;
			return rule.validate().getMessagesFor(rule,
					IValidationRule.PROPERTY_VALIDATED_ATTRIBUTES,
					wrapper.index);
		} catch (CoreException e) {
			IpsPlugin.log(e);
			return new MessageList();
		}
	}

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return getWrappersForAttributes();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class TableLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 0) {
				return null;
			}
			MessageList list = validate(element);
			return ValidationUtils.getSeverityImage(list.getSeverity());
		}

		public String getColumnText(Object element, int columnIndex) {

			if (columnIndex == 1) {
				return ((IndexedValidatedAttributeWrapper) element)
						.getAttributeName();
			}
			return ""; //$NON-NLS-1$
		}
	}

	private class IndexedValidatedAttributeWrapper {

		private int index;

		public IndexedValidatedAttributeWrapper(int index) {
			this.index = index;
		}

		public boolean equals(Object obj) {
			if (obj instanceof IndexedValidatedAttributeWrapper) {
				return ((IndexedValidatedAttributeWrapper) obj).index == index;
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

	private class CellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			if (ATTRIBUTENAME_COLUMN_PROPERTY.equals(property)) {
				return true;
			}
			return false;
		}

		public Object getValue(Object element, String property) {
			return ((IndexedValidatedAttributeWrapper) element)
					.getAttributeName();
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			IndexedValidatedAttributeWrapper validatedAttribute = (IndexedValidatedAttributeWrapper) element;
			validatedAttribute.setAttributeName((String) value);
			getTableViewer().update(getWrappersForAttributes(), null);
		}
	}

	private class MessageService extends TableMessageHoverService {

		public MessageService(TableViewer viewer) {
			super(viewer);
		}

		protected MessageList getMessagesFor(Object element)
				throws CoreException {
			return validate(element);
		}
	}
}
