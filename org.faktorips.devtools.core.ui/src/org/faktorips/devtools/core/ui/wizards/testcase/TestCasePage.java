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

package org.faktorips.devtools.core.ui.wizards.testcase;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 * @author Joerg Ortmann
 */
public class TestCasePage extends IpsObjectPage implements ValueChangeListener {
    
    private TestCaseTypeRefControl superTypeControl;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TestCasePage(IStructuredSelection selection) throws JavaModelException {
        super(IpsObjectType.TEST_CASE, selection, Messages.TestCasePage_title);
    }
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TestCasePage_labelSuperclass);
        superTypeControl = toolkit.createTestCaseTypeRefControl(null, nameComposite);
        
        TextButtonField supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
        super.fillNameComposite(nameComposite, toolkit);

        superTypeControl.setFocus();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root!=null) {
            superTypeControl.setIpsProject(root.getIpsProject());
        } else {
            superTypeControl.setIpsProject(null);
        }
    }
    
    String getSuperType() {
        return superTypeControl.getText();
    }
    
    protected void setDefaults(IResource selectedResource) throws CoreException {
        super.setDefaults(selectedResource);
        IIpsObject obj = getSelectedIpsObject();
        if (obj instanceof ITestCaseType) {
            superTypeControl.setText(obj.getQualifiedName());
        }
        else if (obj instanceof ITestCase) {
            superTypeControl.setText(((ITestCase)obj).getTestCaseType());
        }
        return;
    }

    /**
     * {@inheritDoc}
     */
    protected void validatePageExtension() throws CoreException {
        if (getErrorMessage()!=null) {
            return;
        }
	    if (superTypeControl.findTestCaseType()==null) {
	        setErrorMessage(NLS.bind(Messages.TestCasePage_msgTestCaseTypeDoesNotExist, superTypeControl.getText()));
	    }
    }
    

    /**
     * {@inheritDoc}
     */
    protected void finishIpsObjects(IIpsObject newIpsObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        // fill the default content of the test case bases on the test case type
        ITestCase testCase = (ITestCase)newIpsObject;
        testCase.setTestCaseType(getSuperType());
        ITestCaseType testCaseType = testCase.findTestCaseType(newIpsObject.getIpsProject());
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
                testValue.setDefaultValue();
            } else if (parameter[i] instanceof ITestPolicyCmptTypeParameter) {
                ITestPolicyCmptTypeParameter testCaseTypeParam = (ITestPolicyCmptTypeParameter)parameter[i];
                ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
                testPolicyCmpt.setTestPolicyCmptTypeParameter(parameter[i].getName());
                testPolicyCmpt.setName(testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, testPolicyCmpt
                        .getTestPolicyCmptTypeParameter()));
                testPolicyCmpt.setPolicyCmptType(testCaseTypeParam.getPolicyCmptType());
                // add the attributes which are defined in the test case type parameter
                ITestAttribute attributes[] = testCaseTypeParam.getTestAttributes();
                for (int j = 0; j < attributes.length; j++) {
                    ITestAttribute attribute = attributes[j];
                    ITestAttributeValue attrValue = testPolicyCmpt.newTestAttributeValue();
                    attrValue.setTestAttribute(attribute.getName());
                    attrValue.setDefaultValue();
                }
            }
        }
    }

}
