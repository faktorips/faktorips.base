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
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * A page representing {@link ITableContents}
 * 
 * @author dicker
 * 
 */
public class TableContentsContentPageElement extends AbstractIpsObjectContentPageElement<ITableContents> {

    /**
     * creates a page for the given {@link ITableContents} with the context
     * 
     */
    protected TableContentsContentPageElement(ITableContents object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        ITableStructure tableStructure;
        try {
            tableStructure = getDocumentedIpsObject().findTableStructure(getContext().getIpsProject());
        } catch (CoreRuntimeException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR,
                            "Could not find TableStructure of " + getDocumentedIpsObject().getName(), e)); //$NON-NLS-1$
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(),
                new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayName() + ": ", getContext()), //$NON-NLS-1$
                new PageElementUtils(getContext()).createLinkPageElement(getContext(), tableStructure,
                        TargetType.CONTENT, getContext().getLabel(tableStructure), true)));
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addContentTable();
    }

    /**
     * adds the content of the table
     */
    private void addContentTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.TableContentsContentPageElement_content), TextType.HEADING_2, getContext()));

        ITableRows tableRows = getTableContent().getTableRows();
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "TableContentsContentPageElement_generation") //$NON-NLS-1$
                + " " + getContext().getLabel(tableRows), //$NON-NLS-1$
                TextType.HEADING_3, getContext()));
        ContentTablePageElement contentTablePageElement = null;
        try {
            contentTablePageElement = new ContentTablePageElement(tableRows);
        } catch (CoreRuntimeException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.WARNING, "Could not create ContentTable of " + tableRows.getName(), e)); //$NON-NLS-1$
        }

        wrapper.addPageElements(getTableOrAlternativeText(contentTablePageElement,
                getContext().getMessage(HtmlExportMessages.TableContentsContentPageElement_noContent)));

        IPageElement createContentTable = wrapper;
        addPageElements(createContentTable);
    }

    /**
     * returns the {@link ITableContents}
     * 
     */
    private ITableContents getTableContent() {
        return getDocumentedIpsObject();
    }

    /**
     * a table for the content of the given tableContentsGeneration
     * 
     * @author dicker
     * 
     */
    public class ContentTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IRow> {
        private ITableStructure tableStructure;
        private ValueDatatype[] datatypes;

        public ContentTablePageElement(ITableRows tableContentsGeneration) throws CoreRuntimeException {
            super(Arrays.asList(tableContentsGeneration.getRows()), TableContentsContentPageElement.this.getContext());
            this.tableStructure = getDocumentedIpsObject().findTableStructure(getContext().getIpsProject());
            initDatatypes(tableContentsGeneration);
        }

        private void initDatatypes(ITableRows tableContentsGeneration) throws CoreRuntimeException {
            datatypes = new ValueDatatype[tableStructure.getNumOfColumns()];
            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                datatypes[i] = tableStructure.getColumn(i).findValueDatatype(tableContentsGeneration.getIpsProject());
            }
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IRow rowData) {
            return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(getRowData(rowData)));
        }

        private List<String> getRowData(IRow row) {
            List<String> rowData = new ArrayList<>();

            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                String value = row.getValue(i);
                rowData.add(getContext().getDatatypeFormatter().formatValue(datatypes[i], value));
            }

            return rowData;
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            IColumn[] columns = tableStructure.getColumns();

            List<String> headline = new ArrayList<>();
            for (IColumn column : columns) {
                headline.add(getContext().getLabel(column));
            }
            return headline;
        }
    }
}
