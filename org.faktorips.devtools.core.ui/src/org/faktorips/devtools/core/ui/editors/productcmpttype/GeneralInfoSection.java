/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
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

/**
 * Section to edit supertype, abstract flag and configured <tt>IPolicyCmptType</tt>.
 * 
 * @author Jan Ortmann
 */
public class GeneralInfoSection extends IpsSection implements ContentsChangeListener {

    private IProductCmptType productCmptType;

    private ProductCmptTypeEditorPage productCmptTypeEditorPage;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(ProductCmptTypeEditorPage productCmptTypeEditorPage,
            final IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {

        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(productCmptType);

        this.productCmptType = productCmptType;
        this.productCmptTypeEditorPage = productCmptTypeEditorPage;
        extFactory = new ExtensionPropertyControlFactory(productCmptType.getClass());

        initControls();
        setText(Messages.GeneralInfoSection_title);

        productCmptType.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                productCmptType.getIpsModel().removeChangeListener(GeneralInfoSection.this);
            }
        });
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
        bindingContext.bindContent(supertypeRefControl, productCmptType, IType.PROPERTY_SUPERTYPE);

        // Abstract flag
        toolkit.createLabel(composite, Messages.GeneralInfoSection_abstractLabel);
        Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(abstractCheckbox, productCmptType, IType.PROPERTY_ABSTRACT);

        // Reference to PolicyCmptType
        toolkit.createFormLabel(composite, Messages.GeneralInfoSection_configuresLabel);
        Checkbox configuratedCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(configuratedCheckbox, productCmptType,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);

        link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_configuredTypeLabel);
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

        PcTypeRefControl control = toolkit.createPcTypeRefControl(productCmptType.getIpsProject(), composite);
        bindingContext.bindContent(control, productCmptType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);
        bindingContext.bindEnabled(control, productCmptType,
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);

        extFactory.createControls(composite, toolkit, productCmptType);
        extFactory.bind(bindingContext);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    public void contentsChanged(ContentChangeEvent event) {
        // Return if the content changed was not the ProductCmptType to be edited.
        if (!(event.getIpsSrcFile().equals(productCmptType.getIpsSrcFile()))) {
            return;
        }

        switch (event.getEventType()) {
            case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                // Here is no "break;" by intention!
            case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                propertyChanged();
                break;
        }
    }

    private void propertyChanged() {
        if (productCmptTypeEditorPage.methodsSection != null) {
            productCmptTypeEditorPage.methodsSection.methodsComposite.updateOverrideButtonEnabledState();
        }
    }

}
