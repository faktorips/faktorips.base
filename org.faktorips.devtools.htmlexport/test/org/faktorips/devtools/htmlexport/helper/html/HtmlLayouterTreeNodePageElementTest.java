/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;

public class HtmlLayouterTreeNodePageElementTest extends AbstractTestHtmlLayouter {

    public void testTreeEinfach() {
        String rootName = "root";
        String childBaseName = "child";
        int countChildren = 5;

        TreeNodePageElement pageElement = new TreeNodePageElement(new TextPageElement(rootName));

        for (int i = 1; i <= countChildren; i++) {
            pageElement.addPageElements(new TextPageElement(childBaseName + i));
        }

        String layout = layout(pageElement).replaceAll("\\s+<", "<");

        StringBuilder expectedResult = new StringBuilder();
        expectedResult.append("<div>").append(rootName).append("<div class=\"INDENTION\">");
        for (int i = 1; i <= countChildren; i++) {
            expectedResult.append("<div>").append(childBaseName).append(i).append("</div>");
        }
        expectedResult.append("</div>");
        expectedResult.append("</div>");

        assertEquals(expectedResult.toString(), layout);
    }
}
