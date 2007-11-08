/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
	                IPolicyCmptType supertype = type.findSupertype();
	                if (supertype!=null) {
                        IpsPlugin.getDefault().openEditor(supertype);
	                }
	            } catch (Exception e) {
	                IpsPlugin.logAndShowErrorDialog(e);
	            }
	            
	        }
	        
	    });
	    
	    PcTypeRefControl supertypeRefControl = toolkit.createPcTypeRefControl(type.getIpsProject(), composite);
        bindingContext.bindContent(supertypeRefControl, type, IType.PROPERTY_SUPERTYPE); 
	    
	    Composite c2 = toolkit.createLabelEditColumnComposite(client);
	    toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelAbstractClass);
	    Checkbox abstractCheckbox = toolkit.createCheckbox(c2);
	    bindingContext.bindContent(abstractCheckbox, type, IType.PROPERTY_ABSTRACT);
        
        toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelProduct);
	    Checkbox configuratedCheckbox = toolkit.createCheckbox(c2);
        bindingContext.bindContent(configuratedCheckbox, type, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);

        Hyperlink link2 = toolkit.createHyperlink(c2, Messages.GeneralInfoSection_labelType);
        link2.addHyperlinkListener(new HyperlinkAdapter() {
            
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IProductCmptType productCmptType = type.findProductCmptType(type.getIpsProject());
                    if (productCmptType!=null) {
                        IpsPlugin.getDefault().openEditor(productCmptType);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                
            }
            
        });
        ProductCmptType2RefControl productCmptTypeRefControl = new ProductCmptType2RefControl(type.getIpsProject(), c2, toolkit, false);
        bindingContext.bindContent(productCmptTypeRefControl, type, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        bindingContext.bindEnabled(productCmptTypeRefControl, type, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
	
	    // register controls for focus handling
	    addFocusControl(supertypeRefControl);
	    addFocusControl(abstractCheckbox);
	    addFocusControl(productCmptTypeRefControl);
	    addFocusControl(configuratedCheckbox);
	    
	    extFactory.createControls(c2,toolkit,type);
	    extFactory.bind(bindingContext);
	}
    

    /** 
     * {@inheritDoc}
     */
    protected void performRefresh() {
        bindingContext.updateUI();
    }
    
}
