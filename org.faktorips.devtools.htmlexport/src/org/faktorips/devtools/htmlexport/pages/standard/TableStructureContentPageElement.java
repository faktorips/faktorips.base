package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

public class TableStructureContentPageElement extends AbstractIpsObjectContentPageElement<ITableStructure> {

    /**
     * a table for foreignKeys of the tableStructure
     * 
     * @author dicker
     * 
     */
    private class ForeignKeysTablePageElement extends AbstractSpecificTablePageElement {
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
            cells.add(new TextPageElement(StringUtils.join(foreignKey.getKeyItemNames(), ", "))); //$NON-NLS-1$
            cells.add(link);
            cells.add(new TextPageElement(foreignKey.getReferencedUniqueKey()));
            cells.add(new TextPageElement(foreignKey.getDescription()));

            addSubElement(new TableRowPageElement(cells.toArray(new PageElement[cells.size()])));

        }

        private PageElement getLinkToReferencedTableStructure(IForeignKey foreignKey) {
            PageElement link = null;
            try {
                ITableStructure findReferencedTableStructure = foreignKey.findReferencedTableStructure(getConfig()
                        .getIpsProject());
                link = PageElementUtils.createLinkPageElement(getConfig(), findReferencedTableStructure,
                        "content", foreignKey //$NON-NLS-1$
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

            headline.add(Messages.TableStructureContentPageElement_name);
            headline.add(Messages.TableStructureContentPageElement_keyItems);
            headline.add(Messages.TableStructureContentPageElement_referenced
                    + IpsObjectType.TABLE_STRUCTURE.getDisplayName());
            headline.add(Messages.TableStructureContentPageElement_referencedUniqueKey);
            headline.add(Messages.TableStructureContentPageElement_description);

            return headline;
        }

        @Override
        public boolean isEmpty() {
            return ArrayUtils.isEmpty(tableStructure.getForeignKeys());
        }
    }

    /**
     * a table for ColumnRanges of the tableStructure
     * 
     * @author dicker
     * 
     */
    private class ColumnsRangesTablePageElement extends AbstractSpecificTablePageElement {
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

            headline.add(Messages.TableStructureContentPageElement_name);
            headline.add(Messages.TableStructureContentPageElement_parameterName);
            headline.add(Messages.TableStructureContentPageElement_columnRangeName);
            headline.add(Messages.TableStructureContentPageElement_fromColumn);
            headline.add(Messages.TableStructureContentPageElement_toColumn);
            headline.add(Messages.TableStructureContentPageElement_description);

            return headline;
        }

        @Override
        public boolean isEmpty() {
            return ArrayUtils.isEmpty(tableStructure.getRanges());
        }

    }

    /**
     * a table for columns of the tableStructure
     * 
     * @author dicker
     * 
     */
    private class ColumnsTablePageElement extends AbstractSpecificTablePageElement {
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

            headline.add(Messages.TableStructureContentPageElement_name);
            headline.add(Messages.TableStructureContentPageElement_datatype);
            headline.add(Messages.TableStructureContentPageElement_description);

            return headline;
        }

        @Override
        public boolean isEmpty() {
            return ArrayUtils.isEmpty(tableStructure.getColumns());
        }

    }

    /**
     * a table for uniqueKey of the tableStructure
     * 
     * @author dicker
     * 
     */
    private class UniqueKeysTablePageElement extends AbstractSpecificTablePageElement {
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

            headline.add(Messages.TableStructureContentPageElement_name);
            headline.add(Messages.TableStructureContentPageElement_description);

            return headline;
        }

        @Override
        public boolean isEmpty() {
            return ArrayUtils.isEmpty(tableStructure.getUniqueKeys());
        }

    }

    /**
     * creates a page for the given {@link ITableStructure} with the config
     * 
     * @param object
     * @param config
     */
    protected TableStructureContentPageElement(ITableStructure object, DocumentorConfiguration config) {
        super(object, config);
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

        addPageElements(new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayName(), TextType.HEADING_2));
        addPageElements(new TextPageElement(getDocumentedIpsObject().getTableStructureType().getName(), TextType.BLOCK));

        addColumnTable();

        addUniqueKeysTable();

        addColumnRangesTable();

        addForeignKeyTable();

        addTableContentList();

    }

    /**
     * adds a table for the columns
     */
    private void addColumnTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.TableStructureContentPageElement_columns,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new ColumnsTablePageElement(getDocumentedIpsObject()),
                Messages.TableStructureContentPageElement_noColumns));
        addPageElements(wrapper);
    }

    /**
     * adds a table for the unique keys
     */
    private void addUniqueKeysTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.TableStructureContentPageElement_uniqueKeys,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new UniqueKeysTablePageElement(getDocumentedIpsObject()),
                Messages.TableStructureContentPageElement_noUniqueKeys));
        addPageElements(wrapper);
    }

    /**
     * adds a table for columns ranges
     */
    private void addColumnRangesTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.TableStructureContentPageElement_columnRanges,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new ColumnsRangesTablePageElement(getDocumentedIpsObject()),
                Messages.TableStructureContentPageElement_noColumnRanges));
        addPageElements(wrapper);
    }

    /**
     * adds a table for foreign keys
     */
    private void addForeignKeyTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.TableStructureContentPageElement_foreignKeys,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new ForeignKeysTablePageElement(getDocumentedIpsObject()),
                Messages.TableStructureContentPageElement_noForeignKeys));
        addPageElements(wrapper);
    }

    /**
     * adds a list with the table contents of this table structure
     */
    private void addTableContentList() {
        List<IIpsSrcFile> tableContentsSrcFiles;
        try {
            tableContentsSrcFiles = Arrays.asList(getDocumentedIpsObject().searchMetaObjectSrcFiles(true));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        tableContentsSrcFiles.retainAll(getConfig().getDocumentedSourceFiles());

        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(IpsObjectType.TABLE_CONTENTS.getDisplayNamePlural(),
                TextType.HEADING_2));

        if (tableContentsSrcFiles.size() == 0) {
            wrapper.addPageElements(new TextPageElement("No " + IpsObjectType.TABLE_CONTENTS.getDisplayNamePlural())); //$NON-NLS-1$
            addPageElements(wrapper);
            return;
        }

        List<PageElement> linkPageElements = PageElementUtils.createLinkPageElements(tableContentsSrcFiles,
                "content", new LinkedHashSet<Style>(), getConfig()); //$NON-NLS-1$
        ListPageElement liste = new ListPageElement(linkPageElements);

        wrapper.addPageElements(liste);
        PageElement createTableContentList = wrapper;
        addPageElements(createTableContentList);
    }
}
