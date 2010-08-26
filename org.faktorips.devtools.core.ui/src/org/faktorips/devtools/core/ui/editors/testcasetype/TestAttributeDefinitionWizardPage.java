/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.enums.EnumValue;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;

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
    private EnumValue[] valuesWithoutCombined;

    private BindingContext bindingContext = new BindingContext();
    private PmoTestAttribute testAttribute;

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
            TestAttributeDefinitionWizardPage.this.getContainer().updateButtons();
        }
    }

    protected TestAttributeDefinitionWizardPage(NewTestAttributeWizard wizard) {
        super(PAGE_ID, Messages.TestAttributeDefinitionWizardPage_wizardPageTitle, null);
        setDescription(Messages.TestAttributeDefinitionWizardPage_wizardPageDescription);
        this.wizard = wizard;
        this.testAttribute = new PmoTestAttribute();
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
        EnumValue[] values = TestParameterType.getEnumType().getValues();
        valuesWithoutCombined = new EnumValue[values.length - 1];
        int idx = 0;
        for (EnumValue value : values) {
            if (value != TestParameterType.COMBINED) {
                valuesWithoutCombined[idx++] = value;
            }
        }
        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        testAttributeType = uiToolkit.createCombo(c, valuesWithoutCombined);
        testAttributeType.select(0);
        bindingContext.bindContent(testAttributeType, testAttribute, PmoTestAttribute.PROPERTY_TEST_PARAMETER_TYPE,
                TestParameterType.getEnumType());

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
        try {
            if (StringUtils.isEmpty(testAttributeNameText.getText())) {
                setErrorMessage(Messages.TestAttributeDefinitionWizardPage_errorMessageEmptyName);
                return false;
            }
            if (StringUtils.isEmpty(datatypeRefControl.getText())) {
                setErrorMessage(Messages.TestAttributeDefinitionWizardPage_TestAttributeDefinitionWizardPage_errorMessageEmptyDatatype);
                return false;
            }
            if (wizard.getIpsProjekt().findDatatype(datatypeRefControl.getText()) == null) {
                setErrorMessage(NLS.bind(Messages.TestAttributeDefinitionWizardPage_errorMessageDatatypeNotFound,
                        testAttribute.getDatatype()));
                return false;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }
}
