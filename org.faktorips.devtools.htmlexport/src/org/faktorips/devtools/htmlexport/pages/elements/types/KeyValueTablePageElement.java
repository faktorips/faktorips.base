/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RegexTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class KeyValueTablePageElement extends TablePageElement {

    private final DocumentationContext context;

    public KeyValueTablePageElement(DocumentationContext context) {
        this(context, context.getMessage("KeyValueTablePageElement_headlineProperty"), context //$NON-NLS-1$
                .getMessage("KeyValueTablePageElement_headlineValue")); //$NON-NLS-1$
    }

    /**
     * Creates an KeyValueTablePageElement with a headline
     * 
     */
    public KeyValueTablePageElement(DocumentationContext context, String keyHeadline, String valueHeadline) {
        super(true);
        this.context = context;
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
        return addKeyValueRow(new TextPageElement(key), new TextPageElement(value));
    }

    /**
     * adds a row to the table with a key and a {@link IPageElement} as value and returns this
     * 
     */
    public KeyValueTablePageElement addKeyValueRow(String key, IPageElement valuePageElement) {
        return addKeyValueRow(new TextPageElement(key), valuePageElement);
    }

    /**
     * adds a row to the table with a {@link IPageElement} as key and a {@link IPageElement} as value
     * and returns this
     * 
     */
    public KeyValueTablePageElement addKeyValueRow(IPageElement keyPageElement, IPageElement valuePageElement) {
        addSubElement(new TableRowPageElement(new IPageElement[] { keyPageElement, valuePageElement }));
        return this;
    }

    public DocumentationContext getContext() {
        return context;
    }
}
