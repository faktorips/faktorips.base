/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IForeignKey;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class TableStructureContentPageElement extends AbstractIpsObjectContentPageElement<ITableStructure> {

    /**
     * creates a page for the given {@link ITableStructure} with the context
     * 
     */
    protected TableStructureContentPageElement(ITableStructure object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addPageElements(new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayName(), TextType.HEADING_2,
                getContext()));
        addPageElements(new TextPageElement(getDocumentedIpsObject().getTableStructureType().getName(), TextType.BLOCK,
                getContext()));

        addColumnTable();

        addIndexTable();

        addColumnRangesTable();

        addForeignKeyTable();

        addTableContentList();

    }

    /**
     * adds a table for the columns
     */
    private void addColumnTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.TableStructureContentPageElement_columns), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(new ColumnsTablePageElement(getDocumentedIpsObject(),
                getContext()), getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_noColumns)));
        addPageElements(wrapper);
    }

    /**
     * adds a table for the indices
     */
    private void addIndexTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "TableStructureContentPageElement_uniqueKeys"), //$NON-NLS-1$
                TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(new IndexTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext()
                        .getMessage(HtmlExportMessages.TableStructureContentPageElement_noUniqueKeys)));
        addPageElements(wrapper);
    }

    /**
     * adds a table for columns ranges
     */
    private void addColumnRangesTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "TableStructureContentPageElement_columnRanges"), //$NON-NLS-1$
                TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(new ColumnsRangesTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_noColumnRanges)));
        addPageElements(wrapper);
    }

    /**
     * adds a table for foreign keys
     */
    private void addForeignKeyTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "TableStructureContentPageElement_foreignKeys"), //$NON-NLS-1$
                TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(new ForeignKeysTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_noForeignKeys)));
        addPageElements(wrapper);
    }

    /**
     * adds a list with the table contents of this table structure
     */
    private void addTableContentList() {
        Collection<IIpsSrcFile> tableContentsSrcFiles;
        try {
            tableContentsSrcFiles = getDocumentedIpsObject().searchMetaObjectSrcFiles(true);
        } catch (CoreRuntimeException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.WARNING,
                            "Could not find TableContents for " + getDocumentedIpsObject().getName(), e)); //$NON-NLS-1$
            return;
        }

        tableContentsSrcFiles.retainAll(getContext().getDocumentedSourceFiles());

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(IpsObjectType.TABLE_CONTENTS.getDisplayNamePlural(),
                TextType.HEADING_2, getContext()));

        if (tableContentsSrcFiles.size() == 0) {
            wrapper.addPageElements(new TextPageElement(
                    "No " + IpsObjectType.TABLE_CONTENTS.getDisplayNamePlural(), getContext())); //$NON-NLS-1$
            addPageElements(wrapper);
            return;
        }

        List<IPageElement> linkPageElements = new PageElementUtils(getContext()).createLinkPageElements(
                new ArrayList<>(tableContentsSrcFiles), TargetType.CONTENT, new LinkedHashSet<Style>(),
                getContext());
        ListPageElement liste = new ListPageElement(linkPageElements, getContext());

        wrapper.addPageElements(liste);
        IPageElement createTableContentList = wrapper;
        addPageElements(createTableContentList);
    }

    /**
     * a table for foreignKeys of the tableStructure
     * 
     * @author dicker
     * 
     */
    private static class ForeignKeysTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<IForeignKey> {

        public ForeignKeysTablePageElement(ITableStructure tableStructure, DocumentationContext context) {
            super(Arrays.asList(tableStructure.getForeignKeys()), context);
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IForeignKey foreignKey) {
            List<IPageElement> cells = new ArrayList<>();

            IPageElement link = getLinkToReferencedTableStructure(foreignKey);

            cells.add(new TextPageElement(getContext().getLabel(foreignKey), getContext()));
            cells.add(new TextPageElement(StringUtils.join(foreignKey.getKeyItemNames(), ", "), getContext())); //$NON-NLS-1$
            cells.add(link);
            cells.add(new TextPageElement(foreignKey.getReferencedUniqueKey(), getContext()));
            cells.add(new TextPageElement(getContext().getDescription(foreignKey), getContext()));

            return cells;
        }

        private IPageElement getLinkToReferencedTableStructure(IForeignKey foreignKey) {
            ITableStructure findReferencedTableStructure;
            try {
                findReferencedTableStructure = foreignKey.findReferencedTableStructure(getContext().getIpsProject());
            } catch (CoreRuntimeException e) {
                getContext().addStatus(
                        new IpsStatus(IStatus.WARNING,
                                "Could not find referenced TableStructure for foreignKey" + foreignKey.getName())); //$NON-NLS-1$

                return new TextPageElement(foreignKey.getReferencedTableStructure(), getContext());
            }

            return new PageElementUtils(getContext()).createLinkPageElement(getContext(), findReferencedTableStructure,
                    TargetType.CONTENT, foreignKey.getReferencedTableStructure(), true);
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_name));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_keyItems));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_referenced)
                    + IpsObjectType.TABLE_STRUCTURE.getDisplayName());
            headline.add(getContext().getMessage(
                    HtmlExportMessages.TableStructureContentPageElement_referencedUniqueKey));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_description));

            return headline;
        }
    }

    /**
     * a table for ColumnRanges of the tableStructure
     * 
     * @author dicker
     * 
     */
    private static class ColumnsRangesTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<IColumnRange> {

        public ColumnsRangesTablePageElement(ITableStructure tableStructure, DocumentationContext context) {
            super(Arrays.asList(tableStructure.getRanges()), context);
        }

        protected List<String> getColumnRangeData(IColumnRange columnRange) {
            List<String> columnData = new ArrayList<>();

            columnData.add(getContext().getLabel(columnRange));
            columnData.add(columnRange.getParameterName());
            columnData.add(columnRange.getColumnRangeType().getName());
            columnData.add(columnRange.getFromColumn());
            columnData.add(columnRange.getToColumn());
            columnData.add(getContext().getDescription(columnRange));

            return columnData;

        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IColumnRange columnRange) {
            return Arrays.asList(new PageElementUtils(getContext())
                    .createTextPageElements(getColumnRangeData(columnRange)));
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_name));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_parameterName));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_columnRangeName));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_fromColumn));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_toColumn));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_description));

            return headline;
        }
    }

    /**
     * a table for columns of the tableStructure
     * 
     * @author dicker
     * 
     */
    private static class ColumnsTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IColumn> {

        public ColumnsTablePageElement(ITableStructure tableStructure, DocumentationContext context) {
            super(Arrays.asList(tableStructure.getColumns()), context);
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IColumn column) {
            return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(getColumnData(column)));
        }

        protected List<String> getColumnData(IColumn column) {
            List<String> columnData = new ArrayList<>();

            columnData.add(getContext().getLabel(column));
            columnData.add(column.getDatatype());
            columnData.add(getContext().getDescription(column));

            return columnData;
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_name));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_datatype));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_description));

            return headline;
        }
    }

    /**
     * a table for uniqueKey of the tableStructure
     * 
     * @author dicker
     * 
     */
    private static class IndexTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IIndex> {

        public IndexTablePageElement(ITableStructure tableStructure, DocumentationContext context) {
            super(tableStructure.getIndices(), context);
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IIndex index) {
            return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(getIndexData(index)));
        }

        protected List<String> getIndexData(IIndex uniqueKey) {
            List<String> columnData = new ArrayList<>();

            columnData.add(getContext().getLabel(uniqueKey));
            columnData.add(getContext().getDescription(uniqueKey));

            return columnData;
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_name));
            headline.add(getContext().getMessage(HtmlExportMessages.TableStructureContentPageElement_description));

            return headline;
        }

    }
}
