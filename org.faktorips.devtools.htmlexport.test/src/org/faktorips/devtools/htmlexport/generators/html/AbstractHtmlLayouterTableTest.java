/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.helper.html.AbstractTestHtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public abstract class AbstractHtmlLayouterTableTest extends AbstractTestHtmlLayouter {

    public AbstractHtmlLayouterTableTest() {
        super();
    }

    public AbstractHtmlLayouterTableTest(String name) {
        super(name);
    }

    protected TablePageElement createTable(int rows, int cols) {
        TablePageElement table = new TablePageElement();
    
        for (int row = 0; row < rows; row++) {
            PageElement[] pageElements = new PageElement[cols];
            for (int col = 0; col < cols; col++) {
                pageElements[col] = new TextPageElement(createCellContent(row, col));
            }
            table.addPageElements(new TableRowPageElement(pageElements));
        }
        return table;
    }

    protected String createCellContent(int row, int col) {
        return "item " + row + "-" + col;
    }

}