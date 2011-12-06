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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRefField;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;

public class FolderAndPackagePage extends WizardPage {

    private final NewProductCmptPMO pmo;
    private final BindingContext bindingContext;
    private UiUpdater uiUpdater;
    private IpsPckFragmentRefField ipsPckFragmentRefField;

    protected FolderAndPackagePage(NewProductCmptPMO pmo) {
        super("Select Folder and Package");
        this.pmo = pmo;
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);

        Composite labelEditColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        toolkit.createLabel(labelEditColumnComposite, "Root Folder:");
        Combo rootFolder = toolkit.createCombo(labelEditColumnComposite);

        toolkit.createLabel(labelEditColumnComposite, "Package:");
        IpsPckFragmentRefControl packageRefControl = toolkit
                .createPdPackageFragmentRefControl(labelEditColumnComposite);
        packageRefControl.setIpsPckFragmentRoot(pmo.getPackageRoot());

        setControl(composite);
        bindControls(rootFolder, packageRefControl);
    }

    private void bindControls(Combo rootFolder, IpsPckFragmentRefControl packageRefControl) {
        ComboViewerField<IIpsPackageFragmentRoot> rootFolderField = new ComboViewerField<IIpsPackageFragmentRoot>(
                rootFolder, IIpsPackageFragmentRoot.class);
        bindingContext.bindContent(rootFolderField, pmo, NewProductCmptPMO.PROPERTY_PACKAGE_ROOT);

        ipsPckFragmentRefField = new IpsPckFragmentRefField(packageRefControl);
        bindingContext.bindContent(ipsPckFragmentRefField, pmo, NewProductCmptPMO.PROPERTY_IPS_PACKAGE);
        bindingContext.updateUI();
        uiUpdater = new UiUpdater(pmo, rootFolderField, packageRefControl);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateUi();
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
        if (uiUpdater != null) {
            pmo.removePropertyChangeListener(uiUpdater);
        }
    }

    private static class UiUpdater implements PropertyChangeListener {

        private final IpsPckFragmentRefControl pckFragmentRef;

        private final NewProductCmptPMO pmo;

        private final ComboViewerField<IIpsPackageFragmentRoot> rootFolderField;

        public UiUpdater(NewProductCmptPMO pmo, ComboViewerField<IIpsPackageFragmentRoot> rootFolderField,
                IpsPckFragmentRefControl ipsPckFragmentRef) {
            this.pmo = pmo;
            this.rootFolderField = rootFolderField;
            pckFragmentRef = ipsPckFragmentRef;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NewProductCmptPMO.PROPERTY_PACKAGE_ROOT.equals(evt.getPropertyName())) {
                updatePackageFragmentControl();
            }
            if (NewProductCmptPMO.PROPERTY_IPS_PROJECT.equals(evt.getPropertyName())) {
                updateRootFolderCombo();
            }

        }

        void updateUi() {
            updatePackageFragmentControl();
            updateRootFolderCombo();
        }

        void updatePackageFragmentControl() {
            pckFragmentRef.setIpsPckFragmentRoot(pmo.getPackageRoot());
        }

        void updateRootFolderCombo() {
            try {
                rootFolderField.setInput(pmo.getIpsProject().getSourceIpsPackageFragmentRoots());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

    }

}
