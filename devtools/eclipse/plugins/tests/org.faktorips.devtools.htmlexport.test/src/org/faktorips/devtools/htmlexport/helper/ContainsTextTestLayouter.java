/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class ContainsTextTestLayouter extends AbstractTestLayouter {
    private List<String> textList;

    public ContainsTextTestLayouter(String... texts) {
        super();
        textList = new ArrayList<>(Arrays.asList(texts));
    }

    @Override
    public void layoutTextPageElement(TextPageElement pageElement) {
        String text = pageElement.getText();

        if (textList.contains(text)) {
            textList.remove(text);
        }
    }

    @Override
    public void assertTest() {
        assertTrue("Not all texts found:\n" + textList, textList.isEmpty());
    }

}
