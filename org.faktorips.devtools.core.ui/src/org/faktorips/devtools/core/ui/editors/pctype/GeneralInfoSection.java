/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

public class GeneralInfoSection extends IpsSection {

    private IPolicyCmptType policyCmptType;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(policyCmptType);

        this.policyCmptType = policyCmptType;
        extFactory = new ExtensionPropertyControlFactory(policyCmptType.getClass());

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
                try {
                    IPolicyCmptType supertype = (IPolicyCmptType)policyCmptType.findSupertype(policyCmptType
                            .getIpsProject());
                    if (supertype != null) {
                        IpsUIPlugin.getDefault().openEditor(supertype);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }

            }

        });

        PcTypeRefControl supertypeRefControl = toolkit
                .createPcTypeRefControl(policyCmptType.getIpsProject(), composite);
        bindingContext.bindContent(supertypeRefControl, policyCmptType, IType.PROPERTY_SUPERTYPE);

        toolkit.createFormLabel(composite, Messages.GeneralInfoSection_labelAbstractClass);
        Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(abstractCheckbox, policyCmptType, IType.PROPERTY_ABSTRACT);

        toolkit.createFormLabel(composite, Messages.GeneralInfoSection_labelProduct);
        Checkbox configuratedCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(configuratedCheckbox, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        Hyperlink link2 = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_labelType);
        link2.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType
                            .getIpsProject());
                    if (productCmptType != null) {
                        IpsUIPlugin.getDefault().openEditor(productCmptType);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }

            }

        });
        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(
                policyCmptType.getIpsProject(), composite, toolkit, false);
        bindingContext.bindContent(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        bindingContext.bindEnabled(productCmptTypeRefControl, policyCmptType,
                IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        // Register controls for focus handling.
        addFocusControl(supertypeRefControl);
        addFocusControl(abstractCheckbox);
        addFocusControl(productCmptTypeRefControl);
        addFocusControl(configuratedCheckbox);

        extFactory.createControls(composite, toolkit, policyCmptType);
        extFactory.bind(bindingContext);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}
