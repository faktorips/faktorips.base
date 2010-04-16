package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

/**
 * A page representing {@link ITableContents}
 * 
 * @author dicker
 * 
 */
public class TableContentsContentPageElement extends AbstractObjectContentPageElement<ITableContents> {

	/**
	 * a table for the content of the given tableContentsGeneration
	 * 
	 * @author dicker
	 * 
	 */
	public class ContentTablePageElement extends AbstractSpecificTablePageElement {
		private ITableContentsGeneration tableContentsGeneration;
		private ITableStructure tableStructure;

		public ContentTablePageElement(ITableContentsGeneration tableContentsGeneration) {
			super();
			this.tableContentsGeneration = tableContentsGeneration;
			this.tableStructure = findTableStructure();
		}

		@Override
		protected void addDataRows() {
			IRow[] rows = tableContentsGeneration.getRows();
			for (IRow row : rows) {
				addRow(row);
			}

		}

		private void addRow(IRow row) {
			addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getRowData((Row) row))));
		}

		private List<String> getRowData(Row row) {
			List<String> rowData = new ArrayList<String>();

			for (int i = 0; i < row.getNoOfColumns(); i++) {
				rowData.add(row.getValue(i));
			}

			return rowData;
		}

		@Override
		protected List<String> getHeadline() {
			IColumn[] columns = tableStructure.getColumns();

			List<String> headline = new ArrayList<String>();
			for (IColumn column : columns) {
				headline.add(column.getName());
			}
			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(tableContentsGeneration.getRows());
		}

	}

	/**
	 * creates a page for the given {@link ITableContents} with the config
	 * 
	 * @param object
	 * @param config
	 */
	protected TableContentsContentPageElement(ITableContents object, DocumentorConfiguration config) {
		super(object, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractObjectContentPageElement#addStructureData()
	 */
	@Override
	protected void addStructureData() {
		super.addStructureData();

		ITableStructure tableStructure = findTableStructure();
		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
				new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayName() + ": "), //$NON-NLS-1$
				new LinkPageElement(tableStructure, "content", tableStructure.getName(), true) })); //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractObjectContentPageElement#build()
	 */
	@Override
	public void build() {
		super.build();

		addContentTable();

	}

	/**
	 * adds the content of the table
	 */
	private void addContentTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_content, TextType.HEADING_2));

		if (getTableContent().getNumOfGenerations() == 0) {
			wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_noGenerations));
			addPageElements(wrapper);
			return;
		}

		IIpsObjectGeneration[] objectGenerations = getTableContent().getGenerationsOrderedByValidDate();

		for (IIpsObjectGeneration ipsObjectGeneration : objectGenerations) {
			ITableContentsGeneration tableContentsGeneration = (ITableContentsGeneration) ipsObjectGeneration;

			wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_generation + " " + tableContentsGeneration.getName(),  //$NON-NLS-1$
					TextType.HEADING_3));
			wrapper
					.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_description + ": "  //$NON-NLS-1$
							+ tableContentsGeneration.getDescription()));

			wrapper.addPageElements(getTableOrAlternativeText(new ContentTablePageElement(tableContentsGeneration),
					Messages.TableContentsContentPageElement_noContent));
		}
		PageElement createContentTable = wrapper;
		addPageElements(createContentTable);
	}

	/**
	 * returns the {@link ITableStructure} for the tableContent
	 * @return
	 */
	private ITableStructure findTableStructure() {
		ITableStructure tableStructure;
		try {
			tableStructure = getDocumentedIpsObject().findTableStructure(getConfig().getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return tableStructure;
	}

	/**
	 * returns the {@link ITableContents}
	 * @return
	 */
	private ITableContents getTableContent() {
		return getDocumentedIpsObject();
	}

}
