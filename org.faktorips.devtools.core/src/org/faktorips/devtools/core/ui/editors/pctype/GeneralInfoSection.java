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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.IpsObjectField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class GeneralInfoSection extends IpsSection implements ValueChangeListener {
    
    private IPolicyCmptType pcType;
    private IpsObjectUIController uiController;
    
    // edit fields
    private IpsObjectField supertypeField;
    private CheckboxField abstractField;
    private TextField productCmptTypeNameField;
    private CheckboxField configuratedField;    
    private ExtensionPropertyControlFactory extFactory;
    private PctEditor editor;
    
    /**
     * @param parent
     * @param style
     * @param toolkit
     */
    public GeneralInfoSection(
            IPolicyCmptType pcType, 
            Composite parent, 
            UIToolkit toolkit,
            PctEditor editor) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        this.editor = editor;
        
        extFactory = new ExtensionPropertyControlFactory(pcType.getClass());
        
        initControls();
        setText(Messages.GeneralInfoSection_title);
    }

    /**
	 * Overridden.
	 */ 
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
	    client.setLayout(new GridLayout(1, false));
	    Composite composite = toolkit.createLabelEditColumnComposite(client);
	    
	    Hyperlink link = toolkit.createHyperlink(composite, Messages.GeneralInfoSection_linkSuperclass);
	    link.addHyperlinkListener(new HyperlinkAdapter() {
	
	        public void linkActivated(HyperlinkEvent event) {
	            try {
	                IPolicyCmptType supertype = (IPolicyCmptType)supertypeField.getIpsObject(pcType.getIpsProject(), IpsObjectType.POLICY_CMPT_TYPE);
	                if (supertype!=null) {
                        IpsPlugin.getDefault().openEditor(supertype);
	                }
	            } catch (Exception e) {
	                IpsPlugin.logAndShowErrorDialog(e);
	            }
	            
	        }
	        
	    });
	    
	    PcTypeRefControl control = toolkit.createPcTypeRefControl(pcType.getIpsProject(), composite);
	    
	    Composite c2 = toolkit.createLabelEditColumnComposite(client);
	    toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelAbstractClass);
	    Checkbox abstractCheckbox = toolkit.createCheckbox(c2);
	    toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelProduct);
	    Checkbox configuratedCheckbox = toolkit.createCheckbox(c2);
	    toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelType);
	    Text productCmptTypeNameText = toolkit.createText(c2);
	
	    // register controls for focus handling
	    addFocusControl(control);
	    addFocusControl(abstractCheckbox);
	    addFocusControl(productCmptTypeNameText);
	    addFocusControl(configuratedCheckbox);
	    
	    // create fields
	    supertypeField = new IpsObjectField(control);
	    abstractField = new CheckboxField(abstractCheckbox);
	    productCmptTypeNameField = new TextField(productCmptTypeNameText);
	    configuratedField = new CheckboxField(configuratedCheckbox);
        configuratedField.addChangeListener(this);
        
	    // connect fields to model properties
	    uiController = new IpsObjectUIController(pcType);
	    uiController.add(supertypeField, IPolicyCmptType.PROPERTY_SUPERTYPE);
	    uiController.add(abstractField, IPolicyCmptType.PROPERTY_ABSTRACT);
	    uiController.add(productCmptTypeNameField, IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
	    uiController.add(configuratedField, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
	    
	    extFactory.createControls(c2,toolkit,(IIpsObjectPartContainer)pcType);
	    extFactory.connectToModel(uiController);
	}
    

    /** 
     * Overridden.
     */
    protected void performRefresh() {
        uiController.updateUI();
        
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        Runnable r = new Runnable(){
            public void run() {
                editor.refreshEditor();
            }
        };
        getDisplay().asyncExec(r);
    }
}
