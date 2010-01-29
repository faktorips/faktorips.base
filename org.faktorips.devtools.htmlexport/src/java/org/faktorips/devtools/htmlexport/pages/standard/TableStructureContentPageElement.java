package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

public class TableStructureContentPageElement extends AbstractObjectContentPageElement<ITableStructure> {

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

			columnData.add(Integer.toString(column.getId()));
			columnData.add(column.getName());
			columnData.add(column.getDatatype());
			columnData.add(column.getDescription());

			return columnData;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(IColumn.PROPERTY_ID);
			headline.add(IColumn.PROPERTY_NAME);
			headline.add(IColumn.PROPERTY_DATATYPE);
			headline.add(IColumn.PROPERTY_DESCRIPTION);

			return headline;
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
		
		addPageElements(new TextPageElement("TODO Bereiche", TextType.HEADING_2));
		addPageElements(new TextPageElement("TODO Unique Keys", TextType.HEADING_2));
		addPageElements(new TextPageElement("TODO Fremdschlüssel", TextType.HEADING_2));

		
	}

	protected PageElement createColumnTable() {
		IColumn[] columns = object.getColumns();

		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Spalten", TextType.HEADING_2));

		if (columns.length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Spalten"));
			return wrapper;
		}

		TablePageElement table = getColumnsTablePageElement();

		wrapper.addPageElements(table);

		return wrapper;
	}

	protected ColumnsTablePageElement getColumnsTablePageElement() {
		return new ColumnsTablePageElement(object);
	}

}
