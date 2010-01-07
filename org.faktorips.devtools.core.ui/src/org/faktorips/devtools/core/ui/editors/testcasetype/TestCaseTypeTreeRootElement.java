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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Class to represent the root element for the test case type tree.
 * 
 * @author Joerg Ortmann
 */
class TestCaseTypeTreeRootElement {
    private ITestCaseType testCaseType;

    public TestCaseTypeTreeRootElement(ITestCaseType testCaseType) {
        this.testCaseType = testCaseType;
    }

    public String getText() {
        return Messages.TestCaseTypeTreeRootElement_RootElement_Text;
    }

    public Image getImgage() {
        return IpsUIPlugin.getImageHandling().getImage(testCaseType);
    }

    /**
     * Returns the test case type the root element belongs to.
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }
}
