/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RegexTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class KeyValueTablePageElement extends TablePageElement {

    public KeyValueTablePageElement(DocumentationContext context) {
        this(context, context.getMessage(HtmlExportMessages.KeyValueTablePageElement_headlineProperty), context
                .getMessage(HtmlExportMessages.KeyValueTablePageElement_headlineValue));
    }

    /**
     * Creates an KeyValueTablePageElement with a headline
     * 
     */
    public KeyValueTablePageElement(DocumentationContext context, String keyHeadline, String valueHeadline) {
        super(true, context);
        addLayouts(RowTablePageElementLayout.HEADLINE);
        addLayouts(new AlternateRowTablePageElementLayout(true));
        addLayouts(new RegexTablePageElementLayout(".{1,3}", Style.CENTER)); //$NON-NLS-1$

        addKeyValueRow(keyHeadline, valueHeadline);
    }

    /**
     * Adds Rows to the {@link KeyValueTablePageElement}. One Row consists of two
     * {@link IPageElement}s.
     * 
     * @param pageElements an array of {@link IPageElement}
     * @throws IllegalArgumentException if the number of elements is uneven
     * @return a reference of this
     */
    @Override
    public KeyValueTablePageElement addPageElements(IPageElement... pageElements) {
        if (pageElements.length % 2 == 1) {
            throw new IllegalArgumentException(getContext().getMessage(
                    "KeyValueTablePageElement_justEvenNumberOfPageElementsAllowed")); //$NON-NLS-1$
        }
        if (pageElements.length == 0) {
            return this;
        }

        for (int i = 0; i < pageElements.length; i += 2) {
            addKeyValueRow(pageElements[i], pageElements[i + 1]);
        }

        return this;
    }

    /**
     * adds a row to the table with a key and a value and returns this
     * 
     */
    public KeyValueTablePageElement addKeyValueRow(String key, String value) {
        return addKeyValueRow(new TextPageElement(key, getContext()), new TextPageElement(value, getContext()));
    }

    /**
     * adds a row to the table with a key and a {@link IPageElement} as value and returns this
     * 
     */
    public KeyValueTablePageElement addKeyValueRow(String key, IPageElement valuePageElement) {
        return addKeyValueRow(new TextPageElement(key, getContext()), valuePageElement);
    }

    /**
     * adds a row to the table with a {@link IPageElement} as key and a {@link IPageElement} as
     * value and returns this
     * 
     */
    public KeyValueTablePageElement addKeyValueRow(IPageElement keyPageElement, IPageElement valuePageElement) {
        addSubElement(new TableRowPageElement(new IPageElement[] { keyPageElement, valuePageElement }, getContext()));
        return this;
    }

}
