/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;

/**
 * Class to represent the root element for the test case type tree.
 * 
 * @author Joerg Ortmann
 */
class TestCaseTypeTreeRootElement {
    private ITestCaseType testCaseType;
    
    public TestCaseTypeTreeRootElement(ITestCaseType testCaseType){
        this.testCaseType = testCaseType;
    }
    
    public String getText(){
        return Messages.TestCaseTypeTreeRootElement_RootElement_Text;
    }
   
    public Image getImgage(){
        return IpsObjectType.TEST_CASE_TYPE.getEnabledImage();
    }

    /**
     * Returns the test case type the root element belongs to.
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }
}
