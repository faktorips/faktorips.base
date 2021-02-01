/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;

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
