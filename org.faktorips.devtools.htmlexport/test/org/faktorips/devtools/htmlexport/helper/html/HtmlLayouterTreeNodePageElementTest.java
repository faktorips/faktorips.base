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

package org.faktorips.devtools.htmlexport.helper.html;

import java.io.UnsupportedEncodingException;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;

public class HtmlLayouterTreeNodePageElementTest extends AbstractTestHtmlLayouter {

    public void testTreeEinfach() throws UnsupportedEncodingException {
        String rootName = "root"; //$NON-NLS-1$
        String childBaseName = "child"; //$NON-NLS-1$
        int countChildren = 5;

        TreeNodePageElement pageElement = new TreeNodePageElement(new TextPageElement(rootName));

        for (int i = 1; i <= countChildren; i++) {
            pageElement.addPageElements(new TextPageElement(childBaseName + i));
        }

        String layout = layout(pageElement).replaceAll("\\s+<", "<"); //$NON-NLS-1$ //$NON-NLS-2$

        StringBuilder expectedResult = new StringBuilder();
        expectedResult.append("<div>").append(rootName).append("<div class=\"INDENTION\">"); //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 1; i <= countChildren; i++) {
            expectedResult.append("<div>").append(childBaseName).append(i).append("</div>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        expectedResult.append("</div>"); //$NON-NLS-1$
        expectedResult.append("</div>"); //$NON-NLS-1$

        assertEquals(expectedResult.toString(), layout);
    }
}
