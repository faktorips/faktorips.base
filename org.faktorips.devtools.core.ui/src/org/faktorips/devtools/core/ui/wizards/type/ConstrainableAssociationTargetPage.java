/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

public class ConstrainableAssociationTargetPage extends WizardPage {

    private ConstrainableAssociationWizard wizard;
    private UIToolkit toolkit;
    private BindingContext bindingContext;
    private ConstrainableAssociationPmo constrainableAssociationPmo;

    public ConstrainableAssociationTargetPage(ConstrainableAssociationWizard wizard, UIToolkit toolkit,
            BindingContext bindingContext, ConstrainableAssociationPmo constrainableAssociationPmo) {
        super("");
        setDescription("");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        this.constrainableAssociationPmo = constrainableAssociationPmo;

        setPageComplete(false);

    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData gd = null;
        composite.setLayout(layout);

        Label messageLabel = createMessageArea(composite);
        if (messageLabel != null) {
            gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            gd.horizontalSpan = 2;
            messageLabel.setLayoutData(gd);
        }
        setControl(composite);
    }

    private Label createMessageArea(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        if (getSelectionText() != null) {
            label.setText(getSelectionText());
        }
        label.setFont(composite.getFont());
        return label;
    }

    private String getSelectionText() {
        return constrainableAssociationPmo.getSelectedAssociationText();
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }
}
