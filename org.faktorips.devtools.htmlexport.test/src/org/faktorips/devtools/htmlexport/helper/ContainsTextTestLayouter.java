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

package org.faktorips.devtools.htmlexport.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class ContainsTextTestLayouter extends AbstractTestLayouter {
    private List<String> textList;

    public ContainsTextTestLayouter(String... texts) {
        super();
        textList = new ArrayList<String>(Arrays.asList(texts));
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