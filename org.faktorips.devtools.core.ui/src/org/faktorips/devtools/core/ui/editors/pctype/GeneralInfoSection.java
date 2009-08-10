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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
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


/**
 *
 */
public class GeneralInfoSection extends IpsSection  {
    
    private IPolicyCmptType type;
    
    private ExtensionPropertyControlFactory extFactory;
    
    public GeneralInfoSection(
            IPolicyCmptType pcType, 
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(pcType);
        this.type = pcType;
        extFactory = new ExtensionPropertyControlFactory(pcType.getClass());
        initControls();
        setText(Messages.GeneralInfoSection_title);
    }

    /**
     * {@inheritDoc}
     */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
	    client.setLayout(new GridLayout(1, false));
	    Composite composite = toolkit.createLabelEditColumnComposite(client);
	    
	    Hyperlink link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_linkSuperclass);
	    link.addHyperlinkListener(new HyperlinkAdapter() {
	
	        public void linkActivated(HyperlinkEvent event) {
	            try {
	                IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(type.getIpsProject());
	                if (supertype!=null) {
                        IpsUIPlugin.getDefault().openEditor(supertype);
	                }
	            } catch (Exception e) {
	                IpsPlugin.logAndShowErrorDialog(e);
	            }
	            
	        }
	        
	    });
	    
	    PcTypeRefControl supertypeRefControl = toolkit.createPcTypeRefControl(type.getIpsProject(), composite);
        bindingContext.bindContent(supertypeRefControl, type, IType.PROPERTY_SUPERTYPE); 
	    
	    toolkit.createFormLabel(composite, Messages.GeneralInfoSection_labelAbstractClass);
	    Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
	    bindingContext.bindContent(abstractCheckbox, type, IType.PROPERTY_ABSTRACT);
        
        toolkit.createFormLabel(composite, Messages.GeneralInfoSection_labelProduct);
	    Checkbox configuratedCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(configuratedCheckbox, type, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        Hyperlink link2 = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_labelType);
        link2.addHyperlinkListener(new HyperlinkAdapter() {
            
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IProductCmptType productCmptType = type.findProductCmptType(type.getIpsProject());
                    if (productCmptType!=null) {
                        IpsUIPlugin.getDefault().openEditor(productCmptType);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                
            }
            
        });
        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(type.getIpsProject(), composite, toolkit, false);
        bindingContext.bindContent(productCmptTypeRefControl, type, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        bindingContext.bindEnabled(productCmptTypeRefControl, type, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
	
	    // register controls for focus handling
	    addFocusControl(supertypeRefControl);
	    addFocusControl(abstractCheckbox);
	    addFocusControl(productCmptTypeRefControl);
	    addFocusControl(configuratedCheckbox);
	    
	    extFactory.createControls(composite, toolkit, type);
	    extFactory.bind(bindingContext);
	}
    

    /** 
     * {@inheritDoc}
     */
    protected void performRefresh() {
        bindingContext.updateUI();
    }
    
}
