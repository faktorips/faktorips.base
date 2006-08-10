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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.spreadsheet.ColumnInfo;
import org.faktorips.devtools.core.ui.controls.spreadsheet.SpreadsheetControl;
import org.faktorips.devtools.core.ui.controls.spreadsheet.TableContentProvider;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 */
public class ContentPage extends IpsObjectEditorPage {

	final static String PAGE_ID = "Contents"; //$NON-NLS-1$

	public ContentPage(IpsObjectEditor editor) {
		super(editor, PAGE_ID, Messages.ContentPage_title);
	}

	TableContentsEditor getTableEditor() {
		return (TableContentsEditor) getEditor();
	}

	ITableContents getTableContents() {
		return getTableEditor().getTableContents();
	}

	ITableStructure getTableStructure() throws CoreException {
		return getTableContents().findTableStructure();
	}

	ITableContentsGeneration getActiveGeneration() {
		return (ITableContentsGeneration) getTableEditor()
				.getPreferredGeneration();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(1, false);
		formBody.setLayout(layout);
		try {
			ITableStructure structure = getTableStructure();

			if (structure == null) {
				String msg = NLS.bind(Messages.ContentPage_msgMissingStructure,
						getTableContents().getTableStructure());
				SetStructureDialog dialog = new SetStructureDialog(
						getTableContents(), getSite().getShell(), msg);
				int button = dialog.open();
				if (button != SetStructureDialog.OK) {
					msg = NLS.bind(Messages.ContentPage_msgNoStructureFound,
							getTableContents().getTableStructure());
					toolkit.createLabel(formBody, msg);
					return;
				} else {
					structure = getTableStructure();
				}
			}

			int difference = structure.getColumns().length
					- getTableContents().getNumOfColumns();

			if (difference != 0) {
				IInputValidator validator = new Validator(difference);

				String msg = null;
				String title = null;
				if (difference > 1) {
					title = Messages.ContentPage_titleMissingColumns;
					msg = NLS.bind(Messages.ContentPage_msgAddMany, String
							.valueOf(difference), String
							.valueOf(getTableContents().getNumOfColumns()));

				} else if (difference == 1) {
					title = Messages.ContentPage_titleMissingColumn;
					msg = NLS.bind(Messages.ContentPage_msgAddOne, String
							.valueOf(getTableContents().getNumOfColumns()));
				} else if (difference == -1) {
					title = Messages.ContentPage_titleTooMany;
					msg = NLS.bind(Messages.ContentPage_msgRemoveOne, String
							.valueOf(getTableContents().getNumOfColumns()));
				} else if (difference < -1) {
					title = Messages.ContentPage_titleTooMany;
					msg = NLS.bind(Messages.ContentPage_msgRemoveMany, String
							.valueOf(Math.abs(difference)), String
							.valueOf(getTableContents().getNumOfColumns()));
				}

				InputDialog dialog = new InputDialog(getSite().getShell(),
						title, msg, "", validator); //$NON-NLS-1$
				int state = dialog.open();
				if (state == InputDialog.OK) {
					if (difference > 0) {
						insertColumnsAt(dialog.getValue());
					} else {
						removeColumns(dialog.getValue());
					}
				} else {
					toolkit.createLabel(formBody,
							Messages.ContentPage_msgCantShowContent);
					return;
				}
			}
			SpreadsheetControl tableControl = new SpreadsheetControl(formBody,
					SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
							| SWT.FULL_SELECTION,
					new ContentProvider(structure));
			tableControl.setLayoutData(new GridData(GridData.FILL_BOTH));
			if (getActiveGeneration().getNumOfRows() == 0) {
				getActiveGeneration().newRow();
				tableControl.refresh();
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void insertColumnsAt(String insertIndices) {
		int[] indices = getIndices(insertIndices);
		for (int i = 0; i < indices.length; i++) {
			((TableContents) getTableContents())
					.newColumnAt(indices[i] + i, ""); //$NON-NLS-1$
		}
	}

	private void removeColumns(String removeIndices) {
		int[] indices = getIndices(removeIndices);
		for (int i = 0; i < indices.length; i++) {
			((TableContents) getTableContents()).deleteColumn(indices[i] - i);
		}
	}

	private int[] getIndices(String indices) {
		StringTokenizer tokenizer = getTokenizer(indices);
		int[] result = new int[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			result[i] = Integer.valueOf(tokenizer.nextToken()).intValue();
		}

		Arrays.sort(result);
		return result;
	}

	private StringTokenizer getTokenizer(String tokens) {
		return new StringTokenizer(tokens, ",", false); //$NON-NLS-1$
	}

	private class Validator implements IInputValidator {
		private int indexCount = 0;

		public Validator(int requiredIndexCount) {
			this.indexCount = requiredIndexCount;
		}

		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			StringTokenizer tokenizer = getTokenizer(newText);
			int tokenizerItemCount = tokenizer.countTokens();

			ArrayList values = new ArrayList(tokenizerItemCount);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				try {
					Integer value = Integer.valueOf(token);
					if (values.contains(value) && indexCount < 0) {
						return Messages.ContentPage_errorNoDuplicateIndices;
					}
					if (indexCount < 0
							&& (value.intValue() >= getTableContents()
									.getNumOfColumns() || value.intValue() < 0)) {
						return NLS.bind(
								Messages.ContentPage_errorIndexOutOfRange,
								value);
					}
					values.add(value);
				} catch (NumberFormatException e) {
					if (indexCount == 1) {
						return NLS.bind(
								Messages.ContentPage_errorInvalidValueOne,
								token);
					} else {
						return NLS.bind(
								Messages.ContentPage_errorInvalidValueMany,
								token);
					}
				}
			}

			int difference = Math.abs(indexCount) - tokenizerItemCount;
			if (difference < 0) {
				if (indexCount == 1 || indexCount == -1) {
					return Messages.ContentPage_errorTooManyOne;
				} else {
					return NLS.bind(Messages.ContentPage_errorTooManyMany,
							String.valueOf(Math.abs(indexCount)));
				}
			} else if (difference == 1) {
				return Messages.ContentPage_errorOneMore;
			} else if (difference > 1) {
				return NLS.bind(Messages.ContentPage_errorManyMore, String
						.valueOf(difference));
			}

			return null;
		}

	}

	private class TableContentsColumnInfo extends ColumnInfo {

		private int columnIndex;

		private TableContentsColumnInfo(int index, IColumn column) {
			super(column.getName(), SWT.LEFT, 100, true);
			columnIndex = index;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValue(Object rowElement) {
			IRow row = (IRow) rowElement;
			return row.getValue(columnIndex);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getText(Object rowElement) {
			IRow row = (IRow) rowElement;
			return row.getValue(columnIndex);
		}

		/**
		 * {@inheritDoc}
		 */
		public Image getImage(Object rowElement) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(Object rowElement, Object newValue) {
			IRow row = (IRow) rowElement;
			row.setValue(columnIndex, (String) newValue);
			return;
		}

		/**
		 * {@inheritDoc}
		 */
		public EditField createEditField(Table table) {
			Text text = new Text(table, SWT.NONE);
			return new TextField(text);
		}

	}

	private class ContentProvider implements TableContentProvider {

		private TableContentsColumnInfo[] columnInfos;

		private ContentProvider(ITableStructure structure) throws CoreException {
			ArgumentCheck.notNull(structure);
			IColumn[] columns = structure.getColumns();
			columnInfos = new TableContentsColumnInfo[structure
					.getNumOfColumns()];
			for (int i = 0; i < columnInfos.length; i++) {
				columnInfos[i] = new TableContentsColumnInfo(i, columns[i]);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public ColumnInfo[] getColumnInfos() {
			return columnInfos;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object newRow() {
			return getActiveGeneration().newRow();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean deletePendingRow(Object row) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean deleteRow(Object rowObject) {
			IRow row = (IRow) rowObject;
			row.delete();
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getElements(Object inputElement) {
			return getActiveGeneration().getRows();
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
		}

		/**
		 * {@inheritDoc}
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
