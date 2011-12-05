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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * The second page of the {@link NewProductCmptWizard}. In this page you could select the concrete
 * {@link IProductCmptType} and specify the name, version id and runtime id of the new product
 * component.
 * <p>
 * If the wizard is started by an context sensitive action that has already set the correct base
 * type, the wizard would start with this page automatically.
 * 
 * @since 3.6
 * @author dirmeier
 */
public class ProductCmptPage extends WizardPage {

    private final ResourceManager resourManager;

    private final NewProductCmptPMO pmo;

    private BindingContext bindingContext;

    private TypeSelectionComposite typeSelectionComposite;

    private UiUpdater uiUpdater;

    private Text nameText;

    private Label fullNameLabel;

    private Text runtimeId;

    private Text versionIdText;

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
        typeSelectionComposite.setTitle("Type:");

        toolkit.createHorizonzalLine(composite);

        Composite nameAndIdComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(nameAndIdComposite, "Name:");
        Composite nameComposite = toolkit.createGridComposite(nameAndIdComposite, 3, false, false);

        nameText = toolkit.createText(nameComposite);
        toolkit.createLabel(nameComposite, "Generation:");
        nameText.setFocus();

        versionIdText = toolkit.createText(nameComposite);
        ((GridData)versionIdText.getLayoutData()).widthHint = 20;

        toolkit.createVerticalSpacer(nameAndIdComposite, 0);
        fullNameLabel = toolkit.createLabel(nameAndIdComposite, "Full Name:");

        toolkit.createLabel(nameAndIdComposite, "Runtime ID:");
        runtimeId = toolkit.createText(nameAndIdComposite);
        runtimeId.setEnabled(pmo.isCanEditRuntimeId());

        setControl(composite);

        bindControls(typeSelectionComposite);

        uiUpdater.updateUi();
        bindingContext.updateUI();
    }

    void bindControls(final TypeSelectionComposite typeSelectionComposite) {
        uiUpdater = new UiUpdater(this);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateSelectedBaseType();

        bindingContext.bindContent(typeSelectionComposite.getListViewerField(), pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TYPE);

        bindingContext.bindContent(nameText, pmo, NewProductCmptPMO.PROPERTY_NAME);
        bindingContext.bindContent(versionIdText, pmo, NewProductCmptPMO.PROPERTY_VERSION_ID);
        bindingContext.bindContent(runtimeId, pmo, NewProductCmptPMO.PROPERTY_RUNTIME_ID);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        nameText.selectAll();
        nameText.setFocus();
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
                updateSelectedBaseType();
            }
            if (NewProductCmptPMO.PROPERTY_SELECTED_TYPE.equals(evt.getPropertyName())) {
                updateSelectedType();
            }
            if (NewProductCmptPMO.PROPERTY_NAME.equals(evt.getPropertyName())
                    || NewProductCmptPMO.PROPERTY_VERSION_ID.equals(evt.getPropertyName())) {
                updateNameOrVersionId();
            }
        }

        public void updateUi() {
            updateSelectedBaseType();
            updateSelectedType();
            updateNameOrVersionId();
        }

        public void updateNameOrVersionId() {
            productCmptPage.fullNameLabel.setText(NLS.bind("Full Name: {0}", pmo.getFullName()));
        }

        public void updateSelectedType() {
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

        public void updateSelectedBaseType() {
            productCmptPage.typeSelectionComposite.setListInput(pmo.getSubtypes());
            IProductCmptType selectedBaseType = pmo.getSelectedBaseType();
            if (selectedBaseType != null) {
                productCmptPage.setTitle("Create new "
                        + IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(selectedBaseType));
            } else {
                productCmptPage.setTitle(StringUtils.EMPTY);
            }
        }

    }

}
