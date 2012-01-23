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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRefField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.util.message.MessageList;

public class FolderAndPackagePage extends WizardPage {

    private final NewProductDefinitionPMO pmo;
    private final BindingContext bindingContext;
    private FolderAndPackageUiUpdater uiUpdater;
    private IpsPckFragmentRefControl packageRefControl;
    private Combo rootFolder;
    private ComboViewerField<IIpsPackageFragmentRoot> rootFolderField;
    private Checkbox openEditor;

    public FolderAndPackagePage(NewProductDefinitionPMO pmo) {
        super(Messages.FolderAndPackagePage_title);
        this.pmo = pmo;
        bindingContext = new BindingContext();
        setTitle(NLS.bind(Messages.FolderAndPackagePage_page_title, pmo.getIpsObjectType().getDisplayName()));
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        ((GridLayout)composite.getLayout()).verticalSpacing = 15;

        Composite labelEditColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        toolkit.createLabel(labelEditColumnComposite, Messages.FolderAndPackagePage_label_rootFolder);
        rootFolder = toolkit.createCombo(labelEditColumnComposite);

        toolkit.createLabel(labelEditColumnComposite, Messages.FolderAndPackagePage_label_package);
        packageRefControl = toolkit.createPdPackageFragmentRefControl(labelEditColumnComposite);
        packageRefControl.setIpsPckFragmentRoot(pmo.getPackageRoot());

        toolkit.createHorizonzalLine(composite);

        openEditor = toolkit.createCheckbox(composite, Messages.FolderAndPackagePage_check_openInEditor);

        setControl(composite);
        bindControls();
    }

    private void bindControls() {
        rootFolderField = new ComboViewerField<IIpsPackageFragmentRoot>(rootFolder, IIpsPackageFragmentRoot.class);
        bindingContext.bindContent(rootFolderField, pmo, NewProductDefinitionPMO.PROPERTY_PACKAGE_ROOT);

        IpsPckFragmentRefField ipsPckFragmentRefField = new IpsPckFragmentRefField(packageRefControl);
        bindingContext.bindContent(ipsPckFragmentRefField, pmo, NewProductDefinitionPMO.PROPERTY_IPS_PACKAGE);
        uiUpdater = new FolderAndPackageUiUpdater(this, pmo);
        pmo.addPropertyChangeListener(uiUpdater);

        bindingContext.bindContent(openEditor, pmo, NewProductDefinitionPMO.PROPERTY_OPEN_EDITOR);

        uiUpdater.updateUI();
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

    private static class FolderAndPackageUiUpdater extends PageUiUpdater {

        private final NewProductDefinitionPMO pmo;

        public FolderAndPackageUiUpdater(FolderAndPackagePage page, NewProductDefinitionPMO pmo) {
            super(page);
            this.pmo = pmo;
        }

        /**
         * @return Returns the pmo.
         */
        public NewProductDefinitionPMO getPmo() {
            return pmo;
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
            if (NewProductDefinitionPMO.PROPERTY_PACKAGE_ROOT.equals(evt.getPropertyName())) {
                updatePackageFragmentControl();
            }
            if (NewProductDefinitionPMO.PROPERTY_IPS_PROJECT.equals(evt.getPropertyName())) {
                updateRootFolderCombo();
            }
            super.propertyChange(evt);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            updatePackageFragmentControl();
            updateRootFolderCombo();
        }

        void updatePackageFragmentControl() {
            getPage().packageRefControl.setIpsPckFragmentRoot(getPmo().getPackageRoot());
        }

        void updateRootFolderCombo() {
            try {
                if (getPmo().getIpsProject() != null) {
                    getPage().rootFolderField.setInput(getPmo().getIpsProject().getSourceIpsPackageFragmentRoots());
                }
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
