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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Wizard page shows the to be created type of the test attribute. 
 * The type could be a test attribute which is based on a policy cmpt type attribute or 
 * a test attribute which is not based on a policy cmpt type attribute.
 * 
 * @author Joerg Ortmann
 */
public class NewTestAttributeWizardPage extends WizardPage {
    private static final String PAGE_ID = "NewTestAttributeWizardPage"; //$NON-NLS-1$
    private NewTestAttributeWizard wizard;
    
    private Button modelTestAttributeBtn;
    private Button nonModelTestAttributeBtn;
    
    protected NewTestAttributeWizardPage(NewTestAttributeWizard wizard) {
        super(PAGE_ID, Messages.NewTestAttributeWizardPage_wizardPageTitle, null);
        setDescription(Messages.NewTestAttributeWizardPage_wizardPageDescription);
        this.wizard = wizard;
    }

    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite group = uiToolkit.createGroup(parent, Messages.NewTestAttributeWizardPage_groupLabel);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        group.setLayout(layout);
        
        Composite c = uiToolkit.createLabelEditColumnComposite(group);
        
        // create radio buttons
        KindOfTestAttributeSelectionListener listener = new KindOfTestAttributeSelectionListener();
        
        modelTestAttributeBtn = uiToolkit.createRadioButton(c, Messages.NewTestAttributeWizardPage_radioBtnLabelBasedOnPolicyCmptTypeAttr);
        modelTestAttributeBtn.addSelectionListener(listener);
        
        uiToolkit.createVerticalSpacer(c, 1);
        
        nonModelTestAttributeBtn = uiToolkit.createRadioButton(c, Messages.NewTestAttributeWizardPage_radioBtnLabelNotBasedOnPolicyCmptTypeAttr);
        nonModelTestAttributeBtn.addSelectionListener(listener);
        
        setControl(group);
    }

    public boolean isBasedOnPolicyCmptTypeAttributes() {
        return modelTestAttributeBtn.getSelection();
    }
    
    /**
     * Listener for the radio buttons.
     */
    private class KindOfTestAttributeSelectionListener implements SelectionListener {
        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
                wizard.kindOfTestAttrHasChanged();
        }

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }    
}
