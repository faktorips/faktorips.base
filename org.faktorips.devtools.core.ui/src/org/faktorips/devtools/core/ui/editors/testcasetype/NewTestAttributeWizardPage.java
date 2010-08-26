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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Wizard page shows the to be created type of the test attribute. The type could be a test
 * attribute which is based on a policy cmpt type attribute or a test attribute which is not based
 * on a policy cmpt type attribute.
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

    @Override
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

        String typeName = QNameUtil.getUnqualifiedName(wizard.geTestPolicyCmptTypeParameter().getDatatype());
        String text = NLS.bind(Messages.NewTestAttributeWizardPage_radioBtnLabelBasedOnPolicyCmptTypeAttr, typeName);
        modelTestAttributeBtn = uiToolkit.createRadioButton(c, text);
        modelTestAttributeBtn.addSelectionListener(listener);
        modelTestAttributeBtn.setSelection(true);

        uiToolkit.createVerticalSpacer(c, 1);

        text = NLS.bind(Messages.NewTestAttributeWizardPage_radioBtnLabelNotBasedOnPolicyCmptTypeAttr, typeName);
        nonModelTestAttributeBtn = uiToolkit.createRadioButton(c, text);
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
        @Override
        public void widgetSelected(SelectionEvent e) {
            wizard.kindOfTestAttrHasChanged();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
