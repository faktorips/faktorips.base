/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
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

public class GeneralInfoSection extends IpsSection {

    private IPolicyCmptType policyCmptType;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(policyCmptType);

        this.policyCmptType = policyCmptType;
        extFactory = new ExtensionPropertyControlFactory(policyCmptType);

        initControls();
        setText(Messages.GeneralInfoSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        Hyperlink link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_linkSuperclass);
        link.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                IPolicyCmptType supertype = (IPolicyCmptType)policyCmptType
                        .findSupertype(policyCmptType.getIpsProject());
                if (supertype != null) {
                    IpsUIPlugin.getDefault().openEditor(supertype);
                }
            }

        });

        PcTypeRefControl supertypeRefControl = toolkit.createPcTypeRefControl(policyCmptType.getIpsProject(),
                composite);
        getBindingContext().bindContent(supertypeRefControl, policyCmptType, IType.PROPERTY_SUPERTYPE);

        extFactory.createControls(composite, toolkit, policyCmptType, IExtensionPropertyDefinition.POSITION_TOP);
        extFactory.bind(getBindingContext());

        Composite modifierComposite = toolkit.createGridComposite(client, 1, false, false);

        // Abstract flag
        Checkbox abstractCheckbox = toolkit.createCheckbox(modifierComposite,
                Messages.GeneralInfoSection_labelAbstractClass);
        toolkit.grabHorizontalSpace(abstractCheckbox, false);
        getBindingContext().bindContent(abstractCheckbox, policyCmptType, IType.PROPERTY_ABSTRACT);

        Checkbox refCheckbox = toolkit.createCheckbox(modifierComposite, Messages.GeneralInfoSection_labelProduct);
        toolkit.grabHorizontalSpace(refCheckbox, false);
        getBindingContext().bindContent(refCheckbox, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        // Reference to ProductCmptType
        Composite refComposite = toolkit.createGridComposite(client, 1, false, false);
        // the text field should be directly beneath the checkbox
        ((GridLayout)refComposite.getLayout()).verticalSpacing = 0;

        Composite productCmptTypeComposite = toolkit.createGridComposite(refComposite, 2, false, false);
        ((GridLayout)productCmptTypeComposite.getLayout()).marginLeft = 16;

        Hyperlink refLink = toolkit.createHyperlink(productCmptTypeComposite, Messages.GeneralInfoSection_labelType);
        refLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
                if (productCmptType != null) {
                    IpsUIPlugin.getDefault().openEditor(productCmptType);
                }
            }

        });
        getBindingContext().bindEnabled(refLink, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(
                policyCmptType.getIpsProject(), productCmptTypeComposite, toolkit, false);
        getBindingContext().bindContent(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        getBindingContext().bindEnabled(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        extFactory.createControls(composite, toolkit, policyCmptType, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(getBindingContext());
    }
}
