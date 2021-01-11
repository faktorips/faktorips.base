/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.junit.Assert;
import org.junit.Test;

public class HtmlLayouterTreeNodePageElementTest extends AbstractTestHtmlLayouter {

    @Test
    public void testTreeEinfach() {
        String rootName = "root"; //$NON-NLS-1$
        String childBaseName = "child"; //$NON-NLS-1$
        int countChildren = 5;

        TreeNodePageElement pageElement = new TreeNodePageElement(new TextPageElement(rootName, getContext()),
                getContext());

        for (int i = 1; i <= countChildren; i++) {
            pageElement.addPageElements(new TextPageElement(childBaseName + i, getContext()));
        }

        String layout = layout(pageElement).replaceAll("\\s+<", "<"); //$NON-NLS-1$ //$NON-NLS-2$

        StringBuilder expectedResult = new StringBuilder();
        expectedResult.append("<div>").append(rootName).append("<div class=\"INDENTION\">"); //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 1; i <= countChildren; i++) {
            expectedResult.append("<div>").append(childBaseName).append(i).append("</div>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        expectedResult.append("</div>"); //$NON-NLS-1$
        expectedResult.append("</div>"); //$NON-NLS-1$

        Assert.assertEquals(expectedResult.toString(), layout);
    }
}
