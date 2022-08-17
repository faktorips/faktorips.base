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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.model.testcasetype.TestParameterType;

/**
 * Wizard page to define a new test attribute - this attribute is not based on a policy cmpt type
 * attribute.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeDefinitionWizardPage extends WizardPage {

    private static final String PAGE_ID = "TestAttributeDefinitionWizardPage"; //$NON-NLS-1$
    private NewTestAttributeWizard wizard;
    private Text testAttributeNameText;
    private DatatypeRefControl datatypeRefControl;
    private Combo testAttributeType;
    private TestParameterType[] valuesWithoutCombined;

    private BindingContext bindingContext = new BindingContext();
    private PmoTestAttribute testAttribute;

    protected TestAttributeDefinitionWizardPage(NewTestAttributeWizard wizard) {
        super(PAGE_ID, Messages.TestAttributeDefinitionWizardPage_wizardPageTitle, null);
        setDescription(Messages.TestAttributeDefinitionWizardPage_wizardPageDescription);
        this.wizard = wizard;
        testAttribute = new PmoTestAttribute();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();
        Composite c = uiToolkit.createLabelEditColumnComposite(parent);

        // name of test attribute
        uiToolkit.createFormLabel(c, Messages.TestAttributeDefinitionWizardPage_testLabelTestAttrName);
        testAttributeNameText = uiToolkit.createText(c);
        bindingContext.bindContent(testAttributeNameText, testAttribute, PmoTestAttribute.PROPERTY_NAME);

        // type of attribute
        TestParameterType[] values = TestParameterType.values();
        valuesWithoutCombined = new TestParameterType[values.length - 1];
        int idx = 0;
        for (TestParameterType value : values) {
            if (value != TestParameterType.COMBINED) {
                valuesWithoutCombined[idx++] = value;
            }
        }
        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        testAttributeType = uiToolkit.createCombo(c);
        testAttributeType.select(0);
        bindingContext.bindContent(testAttributeType, testAttribute, PmoTestAttribute.PROPERTY_TEST_PARAMETER_TYPE,
                valuesWithoutCombined);

        // datatype of test attribute
        uiToolkit.createFormLabel(c, Messages.TestAttributeDefinitionWizardPage_testLabelDatatype);
        datatypeRefControl = uiToolkit.createDatatypeRefEdit(wizard.getIpsProjekt(), c);
        datatypeRefControl.setOnlyValueDatatypesAllowed(true);

        bindingContext.bindContent(datatypeRefControl, testAttribute, PmoTestAttribute.PROPERTY_DATATYPE);

        setControl(c);
    }

    /**
     * Returns the name of the test attribute
     */
    public String getTestAttributeName() {
        return testAttribute.getName();
    }

    /**
     * Returns the selected datatype wich will be used for the test attribute
     */
    public String getDatatype() {
        return testAttribute.getDatatype();
    }

    /**
     * Returns type (input or expected) of the test attribute
     */
    public TestParameterType getTestParameterType() {
        return testAttribute.getTestParameterType();
    }

    /**
     * Check the valid state of the page. Validation errors will be displayed as error inside the
     * wizards message ara.
     */
    public boolean isValid() {
        setErrorMessage(null);
        if (StringUtils.isEmpty(testAttributeNameText.getText())) {
            setErrorMessage(Messages.TestAttributeDefinitionWizardPage_errorMessageEmptyName);
            return false;
        }
        if (StringUtils.isEmpty(datatypeRefControl.getText())) {
            setErrorMessage(
                    Messages.TestAttributeDefinitionWizardPage_TestAttributeDefinitionWizardPage_errorMessageEmptyDatatype);
            return false;
        }
        if (wizard.getIpsProjekt().findDatatype(datatypeRefControl.getText()) == null) {
            setErrorMessage(NLS.bind(Messages.TestAttributeDefinitionWizardPage_errorMessageDatatypeNotFound,
                    testAttribute.getDatatype()));
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }

    /**
     * Presentation model object for the new test attribute.
     * 
     * @author Joerg Ortmann
     */
    public class PmoTestAttribute {

        public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
        public static final String PROPERTY_TEST_PARAMETER_TYPE = "testParameterType"; //$NON-NLS-1$
        public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

        private String name;
        private TestParameterType testParameterType;
        private String datatype;

        public String getDatatype() {
            return datatype;
        }

        public void setDatatype(String datatype) {
            this.datatype = datatype;
            valueChanged();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            valueChanged();
        }

        public TestParameterType getTestParameterType() {
            return testParameterType;
        }

        public void setTestParameterType(TestParameterType testParameterType) {
            this.testParameterType = testParameterType;
            valueChanged();
        }

        private void valueChanged() {
            // if the value has changed then the button state must be updated
            // to enable or disable the finish button
            getContainer().updateButtons();
        }
    }
}
