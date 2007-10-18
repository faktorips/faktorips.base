/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.testcasetype.TestValueParameter;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 * Creates a new test case.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseWizard extends NewIpsObjectWizard {
    
    private TestCasePage typePage;
    
    public NewTestCaseWizard() {
        super(IpsObjectType.TEST_CASE);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewTestCaseWizard.png")); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */

    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        typePage = new TestCasePage(selection);
        return typePage;
    }

    /**
     * {@inheritDoc}
     */

    protected void createAdditionalPages() {
    }

    /**
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject pdObject) throws CoreException {
    	// fill the default content of the test case bases on the test case type
    	ITestCase testCase = (ITestCase)pdObject;
    	testCase.setTestCaseType(typePage.getSuperType());
    	ITestCaseType testCaseType = testCase.findTestCaseType();
    	generateDefaultContent(testCaseType.getTestParameters(), testCase);
    	if (testCaseType == null){
    		throw new CoreException(new IpsStatus(
    				NLS.bind(Messages.NewTestCaseWizard_ErrorTestCaseTypeNotExists, testCase.getTestCaseType())));
    	}
    }
    
    /*
     * Generate the default content for the given test case if isInput is <code>true</code> then the 
     * content for the input will be created otherwise for the expected result.
     * All test value parameter and root policy component type parameter (including all attributes)
     * from the given list of test parameter will be created.
     */
    private void generateDefaultContent(ITestParameter[] parameter, ITestCase testCase) throws CoreException{
    	for (int i = 0; i < parameter.length; i++) {
            if (parameter[i] instanceof ITestValueParameter) {
                ITestValue testValue = testCase.newTestValue();
                testValue.setTestValueParameter(parameter[i].getName());
                ValueDatatype valueDatatype = ((TestValueParameter)parameter[i]).findValueDatatype();
                if (valueDatatype != null){
                    testValue.setValue(valueDatatype.getDefaultValue());
                }
            } else if (parameter[i] instanceof ITestPolicyCmptTypeParameter) {
                ITestPolicyCmptTypeParameter testCaseTypeParam = (ITestPolicyCmptTypeParameter)parameter[i];
                ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
                testPolicyCmpt.setTestPolicyCmptTypeParameter(parameter[i].getName());
                testPolicyCmpt.setName(testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, testPolicyCmpt
                        .getTestPolicyCmptTypeParameter()));
                // add the attributes which are defined in the test case type parameter
                ITestAttribute attributes[] = testCaseTypeParam.getTestAttributes();
                for (int j = 0; j < attributes.length; j++) {
                    ITestAttribute attribute = attributes[j];
                    ITestAttributeValue attrValue = testPolicyCmpt.newTestAttributeValue();
                    attrValue.setTestAttribute(attribute.getName());
                    // set the default value if the test attribute is an input test attribute
                    if (attribute.isInputAttribute()) {
                        IPolicyCmptTypeAttribute modelAttribute = attribute.findAttribute();
                        if (modelAttribute != null) {
                            attrValue.setValue(modelAttribute.getDefaultValue());
                        }
                    }
                }
            }
        }
    }
}
