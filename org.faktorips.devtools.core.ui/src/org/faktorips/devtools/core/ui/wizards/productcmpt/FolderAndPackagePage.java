/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;

public class FolderAndPackagePage extends WizardPage {

    private final NewProductCmptPMO pmo;
    private IpsPckFragmentRefControl packageRefControl;
    private IpsPckFragmentRootRefControl rootRefControl;
    private BindingContext bindingContext;

    protected FolderAndPackagePage(NewProductCmptPMO pmo) {
        super("Select Folder and Package");
        this.pmo = pmo;
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);

        Composite labelEditColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        toolkit.createLabel(labelEditColumnComposite, "Folder:");
        rootRefControl = toolkit.createPdPackageFragmentRootRefControl(labelEditColumnComposite, true);

        toolkit.createLabel(labelEditColumnComposite, "Package:");
        packageRefControl = toolkit.createPdPackageFragmentRefControl(labelEditColumnComposite);

        setControl(composite);
        bindControls();
    }

    private void bindControls() {
        bindingContext = new BindingContext();
        bindingContext.bindContent(packageRefControl, pmo, NewProductCmptPMO.PROPERTY_IPS_PACKAGE);
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
    }

}
