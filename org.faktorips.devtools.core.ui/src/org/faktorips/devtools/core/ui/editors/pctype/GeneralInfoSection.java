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

package org.faktorips.devtools.core.ui.editors.pctype;

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

public class GeneralInfoSection extends IpsSection implements ContentsChangeListener {

    private IPolicyCmptType policyCmptType;

    private PolicyCmptTypeEditorPage policyCmptTypeEditorPage;

    private ExtensionPropertyControlFactory extFactory;

    public GeneralInfoSection(PolicyCmptTypeEditorPage policyCmptTypeEditorPage, final IPolicyCmptType policyCmptType,
            Composite parent, UIToolkit toolkit) {

        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(policyCmptType);

        this.policyCmptType = policyCmptType;
        this.policyCmptTypeEditorPage = policyCmptTypeEditorPage;
        extFactory = new ExtensionPropertyControlFactory(policyCmptType.getClass());

        initControls();
        setText(Messages.GeneralInfoSection_title);

        policyCmptType.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                policyCmptType.getIpsModel().removeChangeListener(GeneralInfoSection.this);
            }
        });
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
        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(policyCmptType
                .getIpsProject(), composite, toolkit, false);
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

    public void contentsChanged(ContentChangeEvent event) {
        // Return if the content changed was not the PolicyCmptType to be edited.
        if (!(event.getIpsSrcFile().equals(policyCmptType.getIpsSrcFile()))) {
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
        if (policyCmptTypeEditorPage.attributesSection != null) {
            policyCmptTypeEditorPage.attributesSection.attributesComposite.updateOverrideButtonEnabledState();
        }
        if (policyCmptTypeEditorPage.methodsSection != null) {
            policyCmptTypeEditorPage.methodsSection.methodsComposite.updateOverrideButtonEnabledState();
        }
    }

}
