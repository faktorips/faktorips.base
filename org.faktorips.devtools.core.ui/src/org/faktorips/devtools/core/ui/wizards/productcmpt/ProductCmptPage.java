/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 *
 */
public class ProductCmptPage extends WizardPage {

    private final ResourceManager resourManager;

    private final NewProductCmptPMO pmo;

    private BindingContext bindingContext;

    private TypeSelectionComposite typeSelectionComposite;

    private UiUpdater uiUpdater;

    public ProductCmptPage(NewProductCmptPMO pmo) {
        super("create product component");
        this.pmo = pmo;
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        typeSelectionComposite = new TypeSelectionComposite(composite, toolkit);
        typeSelectionComposite.setTitle("Type");

        setControl(composite);

        bindControls(typeSelectionComposite);
    }

    void bindControls(final TypeSelectionComposite typeSelectionComposite) {
        uiUpdater = new UiUpdater(this);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateListViewer();

        bindingContext.bindContent(typeSelectionComposite.getListViewerField(), pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TYPE);
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        pmo.removePropertyChangeListener(uiUpdater);
    }

    private static class UiUpdater implements PropertyChangeListener {

        private final NewProductCmptPMO pmo;
        private ProductCmptPage productCmptPage;

        public UiUpdater(ProductCmptPage productCmptPage) {
            this.productCmptPage = productCmptPage;
            pmo = productCmptPage.pmo;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE)) {
                updateListViewer();
                productCmptPage.setTitle("Create new "
                        + IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(pmo.getSelectedBaseType()));
            }
            if (NewProductCmptPMO.PROPERTY_SELECTED_TYPE.equals(evt.getPropertyName())) {
                if (pmo.getSelectedType() == null) {
                    productCmptPage.typeSelectionComposite.setDescriptionTitle(StringUtils.EMPTY);
                    productCmptPage.typeSelectionComposite.setDescription(StringUtils.EMPTY);
                } else {
                    productCmptPage.typeSelectionComposite.setDescriptionTitle(IpsPlugin.getMultiLanguageSupport()
                            .getLocalizedLabel(pmo.getSelectedType()));
                    productCmptPage.typeSelectionComposite.setDescription(IpsPlugin.getMultiLanguageSupport()
                            .getLocalizedDescription(pmo.getSelectedType()));
                }
            }
        }

        void updateListViewer() {
            productCmptPage.typeSelectionComposite.setListInput(pmo.getSubtypes());
        }

    }

}
