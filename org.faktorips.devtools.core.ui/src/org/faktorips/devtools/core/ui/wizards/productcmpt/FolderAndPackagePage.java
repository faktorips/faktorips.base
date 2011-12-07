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
import org.faktorips.util.message.MessageList;

public class FolderAndPackagePage extends WizardPage {

    private final NewProductCmptPMO pmo;
    private final BindingContext bindingContext;
    private FolderAndPackageUiUpdater uiUpdater;
    private IpsPckFragmentRefControl packageRefControl;
    private Combo rootFolder;
    private ComboViewerField<IIpsPackageFragmentRoot> rootFolderField;

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
        rootFolder = toolkit.createCombo(labelEditColumnComposite);

        toolkit.createLabel(labelEditColumnComposite, "Package:");
        packageRefControl = toolkit.createPdPackageFragmentRefControl(labelEditColumnComposite);
        packageRefControl.setIpsPckFragmentRoot(pmo.getPackageRoot());

        setControl(composite);
        bindControls();
    }

    private void bindControls() {
        rootFolderField = new ComboViewerField<IIpsPackageFragmentRoot>(rootFolder, IIpsPackageFragmentRoot.class);
        bindingContext.bindContent(rootFolderField, pmo, NewProductCmptPMO.PROPERTY_PACKAGE_ROOT);

        IpsPckFragmentRefField ipsPckFragmentRefField = new IpsPckFragmentRefField(packageRefControl);
        bindingContext.bindContent(ipsPckFragmentRefField, pmo, NewProductCmptPMO.PROPERTY_IPS_PACKAGE);
        uiUpdater = new FolderAndPackageUiUpdater(this, pmo);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateUi();
        bindingContext.updateUI();
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
        if (uiUpdater != null) {
            pmo.removePropertyChangeListener(uiUpdater);
        }
    }

    private static class FolderAndPackageUiUpdater extends UiUpdater {

        public FolderAndPackageUiUpdater(FolderAndPackagePage page, NewProductCmptPMO pmo) {
            super(page, pmo);
        }

        /**
         * @return Returns the page.
         */
        @Override
        public FolderAndPackagePage getPage() {
            return (FolderAndPackagePage)super.getPage();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NewProductCmptPMO.PROPERTY_PACKAGE_ROOT.equals(evt.getPropertyName())) {
                updatePackageFragmentControl();
            }
            if (NewProductCmptPMO.PROPERTY_IPS_PROJECT.equals(evt.getPropertyName())) {
                updateRootFolderCombo();
            }
            super.propertyChange(evt);
        }

        void updateUi() {
            updatePackageFragmentControl();
            updateRootFolderCombo();
        }

        void updatePackageFragmentControl() {
            getPage().packageRefControl.setIpsPckFragmentRoot(getPmo().getPackageRoot());
        }

        void updateRootFolderCombo() {
            try {
                getPage().rootFolderField.setInput(getPmo().getIpsProject().getSourceIpsPackageFragmentRoots());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        @Override
        protected MessageList validatePage() {
            MessageList messageList = getPmo().getValidator().validateFolderAndPackage();
            return messageList;
        }

    }

}
