/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.util.ArgumentCheck;

/**
 * Section to edit supertype, abstract flag, changing over time flag, layer supertype flag, and
 * configured <code>IPolicyCmptType</code>.
 * 
 * @author Jan Ortmann
 */
public class GeneralInfoSection extends IpsSection {

    private IProductCmptType productCmptType;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(productCmptType);

        this.productCmptType = productCmptType;
        extFactory = new ExtensionPropertyControlFactory(productCmptType);

        initControls();
        setText(Messages.GeneralInfoSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        // Supertype
        Hyperlink link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_supertypeLabel);
        link.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IpsUIPlugin.getDefault().openEditor(productCmptType.findSupertype(productCmptType.getIpsProject()));
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }

            }

        });

        ProductCmptType2RefControl supertypeRefControl = new ProductCmptType2RefControl(
                productCmptType.getIpsProject(), composite, toolkit, false);
        getBindingContext().bindContent(supertypeRefControl, productCmptType, IType.PROPERTY_SUPERTYPE);

        Composite modifierComposite = toolkit.createGridComposite(client, 3, false, false);

        // Abstract flag
        Checkbox abstractCheckbox = toolkit
                .createCheckbox(modifierComposite, Messages.GeneralInfoSection_abstractLabel);
        ((GridData)abstractCheckbox.getLayoutData()).grabExcessHorizontalSpace = false;
        getBindingContext().bindContent(abstractCheckbox, productCmptType, IType.PROPERTY_ABSTRACT);

        // ChangingOverTime flag
        String changingOverTimePluralName = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural();
        Checkbox changingOverTimeCheckbox = toolkit.createCheckbox(modifierComposite,
                NLS.bind(Messages.GeneralInfoSection_changingOverTimeLabel, changingOverTimePluralName));
        ((GridData)changingOverTimeCheckbox.getLayoutData()).grabExcessHorizontalSpace = false;
        getBindingContext().bindContent(changingOverTimeCheckbox, productCmptType,
                IProductCmptType.PROPERTY_CHANGING_OVER_TIME);

        // Layer Supertype flag
        Checkbox layerSupertypeCheckbox = toolkit.createCheckbox(modifierComposite,
                Messages.GeneralInfoSection_label_layerSupertype);
        toolkit.grabHorizontalSpace(layerSupertypeCheckbox, false);
        // layerSupertypeCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        getBindingContext().bindContent(layerSupertypeCheckbox, productCmptType,
                IProductCmptType.PROPERTY_LAYER_SUPERTYPE);

        extFactory.createControls(composite, toolkit, productCmptType, IExtensionPropertyDefinition.POSITION_TOP);
        extFactory.bind(getBindingContext());

        // Configured Checkbox
        Checkbox configuratedCheckbox = toolkit.createCheckbox(modifierComposite,
                Messages.GeneralInfoSection_configuresLabel);
        toolkit.grabHorizontalSpace(configuratedCheckbox, false);
        getBindingContext().bindContent(configuratedCheckbox, productCmptType,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);

        Composite configComposite = toolkit.createGridComposite(client, 1, false, false);
        // the text field should be directly beneath the checkbox
        ((GridLayout)configComposite.getLayout()).verticalSpacing = 0;

        // Reference to PolicyCmptType
        Composite policyCmptTypeComposite = toolkit.createGridComposite(configComposite, 2, false, false);
        ((GridLayout)policyCmptTypeComposite.getLayout()).marginLeft = 16;
        link = toolkit.createHyperlink(policyCmptTypeComposite, Messages.GeneralInfoSection_configuredTypeLabel);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IPolicyCmptType policyCmptType = productCmptType
                            .findPolicyCmptType(productCmptType.getIpsProject());
                    if (policyCmptType != null) {
                        IpsUIPlugin.getDefault().openEditor(policyCmptType);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        getBindingContext().bindEnabled(link, productCmptType,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);

        PcTypeRefControl control = toolkit.createPcTypeRefControl(productCmptType.getIpsProject(),
                policyCmptTypeComposite);
        getBindingContext().bindContent(control, productCmptType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);
        getBindingContext().bindEnabled(control, productCmptType,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);

        extFactory.createControls(composite, toolkit, productCmptType, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(getBindingContext());
    }

}
