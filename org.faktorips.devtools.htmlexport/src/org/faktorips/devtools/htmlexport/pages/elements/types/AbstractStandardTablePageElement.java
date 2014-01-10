/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.ColumnTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * <p>
 * Is an abstract {@link TablePageElement} for tables with fixed structure e.g. a table with the
 * methods of an {@link IpsObject}.
 * </p>
 * <p>
 * The table is built internally by the methods addDataRows() and addHeadline(). The normal way with
 * addPageElements() doesn't work with this {@link TablePageElement}s.
 * </p
 * 
 * @author dicker
 * 
 */
public abstract class AbstractStandardTablePageElement extends TablePageElement implements DataPageElement {

    /**
     * Creates an AbstractSpecificTablePageElement
     */
    public AbstractStandardTablePageElement() {
        super();
        addLayouts(RowTablePageElementLayout.HEADLINE);
        addLayouts(new AlternateRowTablePageElementLayout(true));
    }

    /**
     * returns the headlines as List of Strings
     * 
     */
    protected abstract List<String> getHeadline();

    /**
     * adds a row to the table, which fits to the structure of the table
     */
    protected abstract void addDataRows();

    @Override
    public void build() {
        addHeadline();
        addDataRowsWithExtensionPoints();
    }

    private void addDataRowsWithExtensionPoints() {
        addDataRows();

    }

    /**
     * adds the headline to the table and uses the values returned by the method getHeadline
     */
    protected void addHeadline() {
        IPageElement[] pageElements = new PageElementUtils().createTextPageElements(getHeadline(), null, TextType.WITHOUT_TYPE);

        addSubElement(new TableRowPageElement(pageElements));
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public AbstractCompositePageElement addPageElements(IPageElement... pageElements) {
        throw new UnsupportedOperationException();
    }

    /**
     * sets the headline of a column and specifies a layout for this column at the same time.
     * <p>
     * Useful e.g. for center the tablecell for boolean values
     * </p>
     * 
     */
    protected void addHeadlineAndColumnLayout(List<String> headline, String item, Style... styles) {
        addLayouts(new ColumnTablePageElementLayout(new int[] { headline.size() }, styles));
        headline.add(item);
    }
}
