package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.tablestructure.UniqueKey;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.DocumentorUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

public class TableStructureContentPageElement extends AbstractObjectContentPageElement<ITableStructure> {

	public class ForeignKeysTablePageElement extends AbstractSpecificTablePageElement {
		private ITableStructure tableStructure;

		public ForeignKeysTablePageElement(ITableStructure tableStructure) {
			super();
			this.tableStructure = tableStructure;
		}

		@Override
		protected void addDataRows() {
			IForeignKey[] foreignKeys = tableStructure.getForeignKeys();
			for (IForeignKey foreignKey : foreignKeys) {
				addForeignKeyRow(foreignKey);
			}

		}

		private void addForeignKeyRow(IForeignKey foreignKey) {
			List<PageElement> cells = new ArrayList<PageElement>();

			PageElement link = getLinkToReferencedTableStructure(foreignKey);

			cells.add(new TextPageElement(foreignKey.getName()));
			cells.add(new TextPageElement(StringUtils.join(foreignKey.getKeyItemNames(), ", ")));
			cells.add(link);
			cells.add(new TextPageElement(foreignKey.getReferencedUniqueKey()));
			cells.add(new TextPageElement(foreignKey.getDescription()));

			addSubElement(new TableRowPageElement(cells.toArray(new PageElement[cells.size()])));

		}

		private PageElement getLinkToReferencedTableStructure(IForeignKey foreignKey) {
			PageElement link = null;
			try {
				ITableStructure findReferencedTableStructure = foreignKey.findReferencedTableStructure(tableStructure
						.getIpsProject());
				link = new LinkPageElement(findReferencedTableStructure, "content", foreignKey
						.getReferencedTableStructure(), true);
			} catch (CoreException e) {
			} finally {
				if (link == null) {
					link = new TextPageElement(foreignKey.getReferencedTableStructure());
				}
			}
			return link;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(IForeignKey.PROPERTY_NAME);
			headline.add(IForeignKey.PROPERTY_KEY_ITEMS);
			headline.add(IForeignKey.PROPERTY_REF_TABLE_STRUCTURE);
			headline.add(IForeignKey.PROPERTY_REF_UNIQUE_KEY);
			headline.add(IForeignKey.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(tableStructure.getForeignKeys());
		}
	}

	public class ColumnsRangesTablePageElement extends AbstractSpecificTablePageElement {
		private ITableStructure tableStructure;

		public ColumnsRangesTablePageElement(ITableStructure tableStructure) {
			super();
			this.tableStructure = tableStructure;
		}

		@Override
		protected void addDataRows() {
			IColumnRange[] ranges = tableStructure.getRanges();
			for (IColumnRange columnRange : ranges) {
				addColumnRangeRow(columnRange);
			}

		}

		private void addColumnRangeRow(IColumnRange columnRange) {
			addSubElement(new TableRowPageElement(PageElementUtils
					.createTextPageElements(getColumnRangeData(columnRange))));
		}

		protected List<String> getColumnRangeData(IColumnRange columnRange) {
			List<String> columnData = new ArrayList<String>();

			columnData.add(columnRange.getName());
			columnData.add(columnRange.getParameterName());
			columnData.add(columnRange.getColumnRangeType().getName());
			columnData.add(columnRange.getFromColumn());
			columnData.add(columnRange.getToColumn());
			columnData.add(columnRange.getDescription());

			return columnData;

		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(IColumnRange.PROPERTY_NAME);
			headline.add(IColumnRange.PROPERTY_PARAMETER_NAME);
			headline.add(IColumnRange.PROPERTY_RANGE_TYPE);
			headline.add(IColumnRange.PROPERTY_FROM_COLUMN);
			headline.add(IColumnRange.PROPERTY_TO_COLUMN);
			headline.add(IColumnRange.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(tableStructure.getRanges());
		}

	}

	public class ColumnsTablePageElement extends AbstractSpecificTablePageElement {
		private ITableStructure tableStructure;

		public ColumnsTablePageElement(ITableStructure tableStructure) {
			super();
			this.tableStructure = tableStructure;
		}

		@Override
		protected void addDataRows() {
			IColumn[] columns = tableStructure.getColumns();
			for (IColumn column : columns) {
				addColumnRow(column);
			}

		}

		private void addColumnRow(IColumn column) {
			addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getColumnData(column))));
		}

		protected List<String> getColumnData(IColumn column) {
			List<String> columnData = new ArrayList<String>();

			columnData.add(column.getName());
			columnData.add(column.getDatatype());
			columnData.add(column.getDescription());

			return columnData;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(IColumn.PROPERTY_NAME);
			headline.add(IColumn.PROPERTY_DATATYPE);
			headline.add(IColumn.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(tableStructure.getColumns());
		}

	}

	public class UniqueKeysTablePageElement extends AbstractSpecificTablePageElement {
		private ITableStructure tableStructure;

		public UniqueKeysTablePageElement(ITableStructure tableStructure) {
			super();
			this.tableStructure = tableStructure;
		}

		@Override
		protected void addDataRows() {
			IUniqueKey[] uniqueKeys = tableStructure.getUniqueKeys();
			for (IUniqueKey uniqueKey : uniqueKeys) {
				addUniqueKeyRow(uniqueKey);
			}

		}

		private void addUniqueKeyRow(IUniqueKey uniqueKey) {
			addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getUniqueKeyData(uniqueKey))));
		}

		protected List<String> getUniqueKeyData(IUniqueKey uniqueKey) {
			List<String> columnData = new ArrayList<String>();

			columnData.add(uniqueKey.getName());
			columnData.add(uniqueKey.getDescription());

			return columnData;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(UniqueKey.PROPERTY_NAME);
			headline.add(UniqueKey.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(tableStructure.getUniqueKeys());
		}

	}

	protected TableStructureContentPageElement(ITableStructure object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	public void build() {
		super.build();

		// Beschreibung
		addPageElements(new TextPageElement("Tabellentyp", TextType.HEADING_2));
		addPageElements(new TextPageElement(object.getTableStructureType().getName(), TextType.BLOCK));

		// Spalten
		addPageElements(createColumnTable());

		// UniqueKeys
		addPageElements(createUniqueKeysTable());

		// Bereiche
		addPageElements(createColumnRangesTable());

		// Fremdschluessel
		addPageElements(createForeignKeyTable());
		
		// Tabelleninhalte
		addPageElements(createTableContentList());

	}

	protected PageElement createTableContentList() {
		IIpsSrcFile[] allTableContentsSrcFiles;
		List<IProductCmpt> tableContents;
		try {
			allTableContentsSrcFiles = object.searchMetaObjectSrcFiles(true);
			tableContents = DocumentorUtil.getIpsObjects(allTableContentsSrcFiles);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Tabelleninhalte", TextType.HEADING_2));

		if (tableContents.size() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Tabelleninhalte"));
			return wrapper;
		}

		List<LinkPageElement> createLinkPageElements = PageElementUtils.createLinkPageElements(tableContents, "content", new LinkedHashSet<Style>());
		ListPageElement liste = new ListPageElement(createLinkPageElements);

		wrapper.addPageElements(liste);

		return wrapper;

	}
	
	protected PageElement createColumnTable() {

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Spalten", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new ColumnsTablePageElement(object), "keine Spalten"));

		return wrapper;
	}

	protected PageElement createUniqueKeysTable() {

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("UniqueKeys", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new UniqueKeysTablePageElement(object), "keine Unique Keys"));

		return wrapper;
	}

	protected PageElement createColumnRangesTable() {
		
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Ranges", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new ColumnsRangesTablePageElement(object), "keine ranges"));

		return wrapper;
	}

	protected PageElement createForeignKeyTable() {

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("ForeignKeys", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new ForeignKeysTablePageElement(object), "keine Foreign Keys"));

		return wrapper;
	}

}
