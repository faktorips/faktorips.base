/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;

/**
 * A page representing {@link ITableContents}
 * 
 * @author dicker
 * 
 */
public class TableContentsContentPageElement extends AbstractIpsObjectContentPageElement<ITableContents> {

    /**
     * a table for the content of the given tableContentsGeneration
     * 
     * @author dicker
     * 
     */
    public class ContentTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IRow> {
        private ITableStructure tableStructure;
        private ValueDatatype[] datatypes;

        public ContentTablePageElement(ITableContentsGeneration tableContentsGeneration) {
            super(Arrays.asList(tableContentsGeneration.getRows()), config);
            this.tableStructure = findTableStructure();
            initDatatypes(tableContentsGeneration);
        }

        private void initDatatypes(ITableContentsGeneration tableContentsGeneration) {
            try {
                datatypes = new ValueDatatype[tableStructure.getNumOfColumns()];
                for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                    datatypes[i] = tableStructure.getColumn(i).findValueDatatype(
                            tableContentsGeneration.getIpsProject());
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        @Override
        protected List<? extends PageElement> createRowWithIpsObjectPart(IRow rowData) {
            return Arrays.asList(PageElementUtils.createTextPageElements(getRowData(rowData)));
        }

        private List<String> getRowData(IRow row) {
            List<String> rowData = new ArrayList<String>();

            for (int i = 0; i < tableStructure.getNumOfColumns(); i++) {
                String value = row.getValue(i);
                rowData.add(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(datatypes[i], value));
            }

            return rowData;
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            IColumn[] columns = tableStructure.getColumns();

            List<String> headline = new ArrayList<String>();
            for (IColumn column : columns) {
                headline.add(column.getName());
            }
            return headline;
        }

    }

    /**
     * creates a page for the given {@link ITableContents} with the config
     * 
     */
    protected TableContentsContentPageElement(ITableContents object, DocumentorConfiguration config) {
        super(object, config);
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        ITableStructure tableStructure = findTableStructure();
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
                new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayName() + ": "), //$NON-NLS-1$
                PageElementUtils.createLinkPageElement(getConfig(), tableStructure,
                        "content", tableStructure.getName(), true) })); //$NON-NLS-1$

    }

    @Override
    public void build() {
        super.build();

        addContentTable();
    }

    /**
     * adds the content of the table
     */
    private void addContentTable() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_content,
                TextType.HEADING_2));

        if (getTableContent().getNumOfGenerations() == 0) {
            wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_noGenerations));
            addPageElements(wrapper);
            return;
        }

        IIpsObjectGeneration[] objectGenerations = getTableContent().getGenerationsOrderedByValidDate();

        for (IIpsObjectGeneration ipsObjectGeneration : objectGenerations) {
            ITableContentsGeneration tableContentsGeneration = (ITableContentsGeneration)ipsObjectGeneration;

            wrapper.addPageElements(new TextPageElement(Messages.TableContentsContentPageElement_generation
                    + " " + tableContentsGeneration.getName(), //$NON-NLS-1$
                    TextType.HEADING_3));
            wrapper.addPageElements(getTableOrAlternativeText(new ContentTablePageElement(tableContentsGeneration),
                    Messages.TableContentsContentPageElement_noContent));
        }
        PageElement createContentTable = wrapper;
        addPageElements(createContentTable);
    }

    /**
     * returns the {@link ITableStructure} for the tableContent
     * 
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
     * 
     */
    private ITableContents getTableContent() {
        return getDocumentedIpsObject();
    }
}
