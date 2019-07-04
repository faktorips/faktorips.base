/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcase;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * @author Joerg Ortmann
 */
public class TestCasePage extends IpsObjectPage {

    private TestCaseTypeRefControl superTypeControl;
    private Text nameField;

    public TestCasePage(IStructuredSelection selection) {
        super(IpsObjectType.TEST_CASE, selection, Messages.TestCasePage_title);
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TestCasePage_labelSuperclass);
        superTypeControl = toolkit.createTestCaseTypeRefControl(null, nameComposite);

        TextButtonField supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);

        nameField = addNameLabelField(toolkit, nameComposite);

        superTypeControl.setFocus();
    }

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            superTypeControl.setIpsProject(root.getIpsProject());
        } else {
            superTypeControl.setIpsProject(null);
        }
    }

    String getSuperType() {
        return superTypeControl.getText();
    }

    @Override
    protected void setDefaults(IResource selectedResource) throws CoreException {
        super.setDefaults(selectedResource);
        IIpsObject obj = getSelectedIpsObject();
        if (obj instanceof ITestCaseType) {
            superTypeControl.setText(obj.getQualifiedName());
        } else if (obj instanceof ITestCase) {
            superTypeControl.setText(((ITestCase)obj).getTestCaseType());
        }
        return;
    }

    @Override
    protected void validatePageExtension() throws CoreException {
        if (getErrorMessage() != null) {
            return;
        }
        if (superTypeControl.findTestCaseType() == null) {
            setErrorMessage(NLS.bind(Messages.TestCasePage_msgTestCaseTypeDoesNotExist, superTypeControl.getText()));
        }
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        // fill the default content of the test case bases on the test case type
        ITestCase testCase = (ITestCase)newIpsObject;
        testCase.setTestCaseType(getSuperType());
        ITestCaseType testCaseType = testCase.findTestCaseType(newIpsObject.getIpsProject());
        generateDefaultContent(testCaseType.getTestParameters(), testCase);
    }

    /**
     * Generate the default content for the given test case. All test value parameter will be
     * created, because for this kind of parameter there is no add functionality.
     */
    private void generateDefaultContent(ITestParameter[] parameter, ITestCase testCase) throws CoreException {
        for (ITestParameter element : parameter) {
            if (element instanceof ITestValueParameter) {
                ITestValue testValue = testCase.newTestValue();
                testValue.setTestValueParameter(element.getName());
                testValue.setDefaultValue();
            }
        }
    }

    /**
     * Sets the focus to the super type control if empty, if not to the name control.
     */
    @Override
    protected void setDefaultFocus() {
        super.setDefaultFocus();
        if (StringUtils.isEmpty(superTypeControl.getText())) {
            superTypeControl.setFocus();
            return;
        }
        nameField.setFocus();
    }
}
