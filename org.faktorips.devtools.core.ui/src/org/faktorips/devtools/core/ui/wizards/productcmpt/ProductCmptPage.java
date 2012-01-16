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
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.wizards.productdefinition.PageUiUpdater;
import org.faktorips.devtools.core.ui.wizards.productdefinition.TypeSelectionComposite;
import org.faktorips.util.message.MessageList;

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

    private ProductCmptPageUiUpdater uiUpdater;

    private Text nameText;

    private Text runtimeId;

    private Text versionIdText;

    private Label versionIdLabel;

    private DateControlField<GregorianCalendar> effectiveDateField;

    public ProductCmptPage(NewProductCmptPMO pmo) {
        super(Messages.ProductCmptPage_name);
        this.pmo = pmo;
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.verticalSpacing = 10;

        typeSelectionComposite = new TypeSelectionComposite(composite, toolkit);
        typeSelectionComposite.setTitle(Messages.ProductCmptPage_label_selectType);

        toolkit.createHorizonzalLine(composite);

        Composite nameAndIdComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(nameAndIdComposite, Messages.ProductCmptPage_label_name);

        nameText = toolkit.createText(nameAndIdComposite);

        toolkit.createLabel(nameAndIdComposite, Messages.ProductCmptPage_label_effectiveFrom);

        Composite dateComposite = toolkit.createGridComposite(nameAndIdComposite, 3, false, false);

        DateControl dateControl = new DateControl(dateComposite, toolkit);
        effectiveDateField = new DateControlField<GregorianCalendar>(dateControl, GregorianCalendarFormat.newInstance());

        IChangesOverTimeNamingConvention changesOverTimeNamingConvention = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention();
        versionIdLabel = toolkit.createLabel(dateComposite,
                changesOverTimeNamingConvention.getVersionConceptNameSingular()
                        + Messages.ProductCmptPage_label_versionSuffix);
        versionIdText = toolkit.createText(dateComposite);

        toolkit.createLabel(nameAndIdComposite, Messages.ProductCmptPage_label_runtimeId);
        runtimeId = toolkit.createText(nameAndIdComposite);
        runtimeId.setEnabled(pmo.isCanEditRuntimeId());

        setControl(composite);

        bindControls(typeSelectionComposite);

        uiUpdater.updateUI();
        bindingContext.updateUI();
    }

    void bindControls(final TypeSelectionComposite typeSelectionComposite) {
        uiUpdater = new ProductCmptPageUiUpdater(this, pmo);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateSelectedBaseType();

        bindingContext.bindContent(typeSelectionComposite.getListViewerField(), pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TYPE);

        bindingContext.bindContent(nameText, pmo, NewProductCmptPMO.PROPERTY_KIND_ID);
        bindingContext.bindContent(effectiveDateField, pmo, NewProductCmptPMO.PROPERTY_EFFECTIVE_DATE);
        bindingContext.bindContent(versionIdText, pmo, NewProductCmptPMO.PROPERTY_VERSION_ID);
        bindingContext.bindContent(runtimeId, pmo, NewProductCmptPMO.PROPERTY_RUNTIME_ID);

        bindingContext.bindVisible(versionIdLabel, pmo, NewProductCmptPMO.PROPERTY_NEED_VERSION_ID, true,
                effectiveDateField.getControl());
        bindingContext.bindVisible(versionIdText, pmo, NewProductCmptPMO.PROPERTY_NEED_VERSION_ID, true,
                effectiveDateField.getControl());
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        fixPreviousPage();
        // setting the actual message if there is any but do not show as error
        setMessage(getMessage());
        nameText.selectAll();
        nameText.setFocus();
    }

    /**
     * Previous page may be wrong if dialog was started by new menu.
     */
    private void fixPreviousPage() {
        IWizardPage page0 = getWizard().getPages()[0];
        if (getPreviousPage() != page0) {
            page0.setPreviousPage(getPreviousPage());
            setPreviousPage(page0);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        if (uiUpdater != null) {
            pmo.removePropertyChangeListener(uiUpdater);
        }
    }

    private static class ProductCmptPageUiUpdater extends PageUiUpdater {

        private final NewProductCmptPMO pmo;

        public ProductCmptPageUiUpdater(ProductCmptPage productCmptPage, NewProductCmptPMO pmo) {
            super(productCmptPage);
            this.pmo = pmo;
        }

        /**
         * @return Returns the pmo.
         */
        public NewProductCmptPMO getPmo() {
            return pmo;
        }

        /**
         * @return Returns the productCmptPage.
         */
        @Override
        public ProductCmptPage getPage() {
            return (ProductCmptPage)super.getPage();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE)) {
                updateSelectedBaseType();
            }
            super.propertyChange(evt);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            updateSelectedBaseType();
        }

        @Override
        protected void updatePageMessages() {
            super.updatePageMessages();
            if (StringUtils.isEmpty(getPage().getMessage()) && StringUtils.isNotEmpty(getPmo().getName())) {
                getPage().setMessage(NLS.bind(Messages.ProductCmptPage_msg_fullName, getPmo().getName()));
            }
        }

        public void updateSelectedBaseType() {
            getPage().typeSelectionComposite.setListInput(getPmo().getSubtypes());
            IProductCmptType selectedBaseType = getPmo().getSelectedBaseType();
            if (selectedBaseType != null) {
                getPage().setTitle(
                        NLS.bind(Messages.ProductCmptPage_title,
                                IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(selectedBaseType)));
            } else {
                getPage().setTitle(StringUtils.EMPTY);
            }
        }

        @Override
        protected MessageList validatePage() {
            MessageList messageList = getPmo().getValidator().validateProductCmptPage();
            return messageList;
        }

    }

}
