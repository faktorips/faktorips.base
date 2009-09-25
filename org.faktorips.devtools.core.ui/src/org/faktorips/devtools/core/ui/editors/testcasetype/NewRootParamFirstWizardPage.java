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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Wizard page to select the kind of the new created root test policy cmpt type parameter.<br>
 * Could be one of: test policy cmpt type param, test value param, or test rule parameter.
 * 
 * @author Joerg Ortmann
 */
public class NewRootParamFirstWizardPage extends WizardPage implements ValueChangeListener {
    private static final String PAGE_ID = "RootParameterFirstPage"; //$NON-NLS-1$
    private static final int PAGE_NUMBER = 1;

    private NewRootParameterWizard wizard;

    /** edit fields */
    private Button testValueParameterBtn;
    private Button testPolicyCmptTypeParameterBtn;
    private Button testRuleParameterBtn;
    private Button prevSelection;

    /**
     * Listener for the radio buttons.
     */
    private class KindOfTestParamSelectionListener implements SelectionListener {
        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            // if no reverse association is selected then disable next wizard page
            // other wise enable next wizard page
            if (prevSelection != e.getSource()) {
                String title = ""; //$NON-NLS-1$
                String description = ""; //$NON-NLS-1$
                prevSelection = (Button)e.getSource();
                if (e.getSource() == testValueParameterBtn) {
                    wizard.setKindOfTestParameter(NewRootParameterWizard.TEST_VALUE_PARAMETER);
                    title = Messages.NewRootParamWizardPage_Title_TestValueParam;
                    description = Messages.NewRootParamWizardPage_Description_TestValueParam;
                } else if (e.getSource() == testPolicyCmptTypeParameterBtn) {
                    wizard.setKindOfTestParameter(NewRootParameterWizard.TEST_POLICY_CMPT_TYPE_PARAMETER);
                    title = Messages.NewRootParamWizardPage_Title_TestPolicyCmptParam;
                    description = Messages.NewRootParamWizardPage_Description_TestPolicyCmptParam;
                } else if (e.getSource() == testRuleParameterBtn) {
                    wizard.setKindOfTestParameter(NewRootParameterWizard.TEST_RULE_PARAMETER);
                    title = Messages.NewRootParamWizardPage_Title_TestRuleParam;
                    description = Messages.NewRootParamWizardPage_Description_TestRuleParam;
                }
                wizard.setTitleAndDescriptionOfSecondPage(title, description);
                wizard.resetWizard();
            }
        }

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }

    public NewRootParamFirstWizardPage(NewRootParameterWizard wizard) {
        super(PAGE_ID, Messages.NewRootParamFirstWizardPage_Title, null);
        setDescription(Messages.NewRootParamFirstWizardPage_Decription);
        this.wizard = wizard;
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite group = uiToolkit.createGroup(parent, Messages.NewRootParamFirstWizardPage_GroupLabel);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        group.setLayout(layout);

        Composite c = uiToolkit.createLabelEditColumnComposite(group);

        // create radio buttons
        KindOfTestParamSelectionListener listener = new KindOfTestParamSelectionListener();

        testPolicyCmptTypeParameterBtn = uiToolkit.createRadioButton(c,
                Messages.NewRootParamFirstWizardPage_RadioButton_TestPolicyCmptTypeParameter);
        testPolicyCmptTypeParameterBtn.addSelectionListener(listener);

        uiToolkit.createVerticalSpacer(c, 1);

        testValueParameterBtn = uiToolkit.createRadioButton(c,
                Messages.NewRootParamFirstWizardPage_RadioButton_TestValueParameter);
        testValueParameterBtn.addSelectionListener(listener);

        uiToolkit.createVerticalSpacer(c, 1);

        testRuleParameterBtn = uiToolkit.createRadioButton(c,
                Messages.NewRootParamFirstWizardPage_RadioButton_TestRuleParameter);
        testRuleParameterBtn.addSelectionListener(listener);

        // set the default selection
        testPolicyCmptTypeParameterBtn.setSelection(true);
        prevSelection = testPolicyCmptTypeParameterBtn;

        setControl(group);

        setPageComplete(true);
    }

    /**
     * Connects the edit fields with the given controller to the given test parameter
     */
    void connectToModel(IpsObjectUIController controller, ITestParameter testParameter) {
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
    }

    /**
     * Informs the wizard that this page was displayed.
     * 
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage() {
        wizard.setMaxPageShown(PAGE_NUMBER);
        return super.getNextPage();
    }
}
