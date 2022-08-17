/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.IpsProjectRefField;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.productdefinition.PageUiUpdater;
import org.faktorips.devtools.core.ui.wizards.productdefinition.TypeSelectionComposite;
import org.faktorips.runtime.MessageList;

public class TypeSelectionPage extends WizardPage {

    private final ResourceManager resourManager;

    private final NewProductCmptPMO pmo;

    private final BindingContext bindingContext;

    private TypeSelectionUpdater typeSelectionUpdater;

    private IpsProjectRefControl ipsProjectRefControl;

    private TypeSelectionComposite typeSelectionComposite;

    public TypeSelectionPage(NewProductCmptPMO pmo) {
        super(Messages.TypeSelectionPage_name);
        this.pmo = pmo;
        setTitle(NLS.bind(Messages.TypeSelectionPage_title, pmo.getIpsObjectType().getDisplayName()));
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, true);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.verticalSpacing = 10;

        Composite twoColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        // Select Project
        toolkit.createLabel(twoColumnComposite, Messages.TypeSelectionPage_label_project);
        ipsProjectRefControl = toolkit.createIpsProjectRefControl(twoColumnComposite);
        ipsProjectRefControl.setOnlyProductDefinitionProjects(true);

        toolkit.createHorizonzalLine(composite);

        typeSelectionComposite = new TypeSelectionComposite(composite, toolkit, bindingContext, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE, pmo.getBaseTypes());
        typeSelectionComposite.setTitle(Messages.TypeSelectionPage_label_type);

        setControl(composite);

        bindControls();
    }

    void bindControls() {
        IpsProjectRefField ipsProjectRefField = new IpsProjectRefField(ipsProjectRefControl);

        bindingContext.bindContent(ipsProjectRefField, pmo, NewProductCmptPMO.PROPERTY_IPS_PROJECT);

        typeSelectionComposite.addDoubleClickListener(new DoubleClickListener(this));
        typeSelectionUpdater = new TypeSelectionUpdater(this, pmo);
        pmo.addPropertyChangeListener(typeSelectionUpdater);

        typeSelectionUpdater.updateUI();
        bindingContext.updateUI();
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        if (typeSelectionUpdater != null) {
            pmo.removePropertyChangeListener(typeSelectionUpdater);
        }
    }

    private static class TypeSelectionUpdater extends PageUiUpdater {

        private final NewProductCmptPMO pmo;

        public TypeSelectionUpdater(TypeSelectionPage page, NewProductCmptPMO pmo) {
            super(page);
            this.pmo = pmo;
        }

        /**
         * @return Returns the pmo.
         */
        public NewProductCmptPMO getPmo() {
            return pmo;
        }

        @Override
        public TypeSelectionPage getPage() {
            return (TypeSelectionPage)super.getPage();
        }

        @Override
        protected MessageList validatePage() {
            return getPmo().getValidator().validateTypeSelection();
        }

    }

    private static class DoubleClickListener implements IDoubleClickListener {

        private final TypeSelectionPage page;

        public DoubleClickListener(TypeSelectionPage page) {
            this.page = page;
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            if (page.canFlipToNextPage()) {
                page.getWizard().getContainer().showPage(page.getNextPage());
            }

        }
    }

}
