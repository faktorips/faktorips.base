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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Wizard page to display the details of a test policy cmpt type parameter.<br>
 * The following fields will be handled: Min instances, max instances and requires product
 * 
 * @author Joerg Ortmann
 */
public class NewTestParamDetailWizardPage extends WizardPage implements ValueChangeListener {

    private static final String PAGE_ID = "RootParamDetailWizardPage"; //$NON-NLS-1$

    private IBlockedValidationWizard wizard;

    private EditField<Integer> editFieldMin;
    private EditField<Integer> editFieldMax;
    private EditField<Boolean> editFieldReqProd;

    private UIToolkit uiToolkit;

    private int pageNumber = 3;

    public NewTestParamDetailWizardPage(IBlockedValidationWizard wizard, UIToolkit uiToolkit, int pageNumber) {
        super(PAGE_ID, Messages.NewTestParamDetailWizardPage_Title, null);
        this.setDescription(Messages.NewTestParamDetailWizardPage_Description);
        this.wizard = wizard;
        this.uiToolkit = uiToolkit;
        this.pageNumber = pageNumber;
    }

    @Override
    public void createControl(Composite parent) {
        Composite c = uiToolkit.createLabelEditColumnComposite(parent);

        uiToolkit.createFormLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_MinInstances);
        editFieldMin = new CardinalityField(uiToolkit.createText(c));
        editFieldMin.addChangeListener(this);

        uiToolkit.createFormLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_MaxInstances);
        editFieldMax = new CardinalityField(uiToolkit.createText(c));
        editFieldMax.addChangeListener(this);

        uiToolkit.createFormLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_RequiresProduct);
        editFieldReqProd = new CheckboxField(uiToolkit.createCheckbox(c));
        editFieldReqProd.addChangeListener(this);

        setControl(c);
    }

    /**
     * Connects the edit fields with the given controller to the given test parameter
     */
    void connectToModel(BindingContext bindingContext, ITestParameter testParameter) {
        bindingContext.bindContent(editFieldMin, testParameter, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
        bindingContext.bindContent(editFieldMax, testParameter, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
        bindingContext.bindContent(editFieldReqProd, testParameter,
                ITestPolicyCmptTypeParameter.PROPERTY_REQUIRES_PRODUCTCMT);

        editFieldMin.getControl().setEnabled(true);
        editFieldMax.getControl().setEnabled(true);
        editFieldReqProd.getControl().setEnabled(true);

        // min and max are not editable for root parameters
        if (testParameter.isRoot()) {
            editFieldMin.getControl().setEnabled(false);
            editFieldMax.getControl().setEnabled(false);
            return;
        }

        // req product cmpt is not editable for associations
        if (testParameter instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)testParameter;
            IPolicyCmptTypeAssociation association;
            try {
                association = testPolicyCmptTypeParameter.findAssociation(testPolicyCmptTypeParameter.getIpsProject());
                if (association != null && association.isAssoziation()) {
                    editFieldReqProd.getControl().setEnabled(false);
                }
            } catch (IpsException e) {
                // ignore exception while searching for the model association,
                // will be reported in previous wizard pages!
            }
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        wizard.postAsyncRunnable(this::updateSetPageComplete);
    }

    private boolean validatePage() {
        setErrorMessage(null);
        return wizard.isPageValid(pageNumber);
    }

    /**
     * Updates the page complete status.
     */
    private void updateSetPageComplete() {
        boolean completeAllowed = false;
        completeAllowed = validatePage();
        super.setPageComplete(completeAllowed);
    }

    /**
     * Informs the wizard that this page was displayed.
     */
    @Override
    public IWizardPage getNextPage() {
        wizard.setMaxPageShown(pageNumber);
        return super.getNextPage();
    }

    public void resetPage() {
        if (wizard.getBindingContext() != null) {
            wizard.getBindingContext().clear();
        }
        editFieldMin.setText(""); //$NON-NLS-1$
        editFieldMax.setText(""); //$NON-NLS-1$
        ((Checkbox)editFieldReqProd.getControl()).setChecked(false);
        editFieldMin.getControl().setEnabled(true);
        editFieldMax.getControl().setEnabled(true);
        editFieldReqProd.getControl().setEnabled(true);
    }
}
