package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.PdObjectField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class GeneralInfoSection extends IpsSection {
    
    private final static String PROPERTY_PM_OBJECT_NAME = "com.bbv.faktorips.pctype.pmObjectName";
    	
    private IPolicyCmptType pcType;
    private IpsObjectUIController uiController;
    
    // edit fields
    private PdObjectField supertypeField;
    private CheckboxField abstractField;

    /**
     * @param parent
     * @param style
     * @param toolkit
     */
    public GeneralInfoSection(
            IPolicyCmptType pcType, 
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        initControls();
        setText("General information");
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.controls.UIToolkit)
     */ 
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);
        
        Hyperlink link = toolkit.createHyperlink(composite, "Superclass:");
        link.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent event) {
                try {
                    IPolicyCmptType supertype = (IPolicyCmptType)supertypeField.getPdObject(pcType.getIpsProject(), IpsObjectType.POLICY_CMPT_TYPE);
                    if (supertype!=null) {
                        IpsPlugin.getDefault().openEditor(supertype.getIpsSrcFile());    
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                
            }
            
        });
        
        PcTypeRefControl control = toolkit.createPcTypeRefControl(pcType.getIpsProject(), composite);
        
        Composite c2 = toolkit.createLabelEditColumnComposite(client);
        toolkit.createFormLabel(c2, "Abstract class:");
        Checkbox abstractCheckbox = toolkit.createCheckbox(c2);

        // create fields
        supertypeField = new PdObjectField(control);
        abstractField = new CheckboxField(abstractCheckbox);
        
        // connect fields to model properties
        uiController = new IpsObjectUIController(pcType);
        uiController.add(supertypeField, IPolicyCmptType.PROPERTY_SUPERTYPE);
        uiController.add(abstractField, IPolicyCmptType.PROPERTY_ABSTRACT);
        
        // TODO: remove pm extension from core
        IExtensionPropertyDefinition extProp = pcType.getIpsModel().getExtensionPropertyDefinition(IPolicyCmptType.class, PROPERTY_PM_OBJECT_NAME, true);
        if (extProp!=null) {
            toolkit.createFormLabel(c2, extProp.getDisplayName() + ":");
            uiController.add(extProp.newEditField((IpsObjectPartContainer)pcType, c2, toolkit), PROPERTY_PM_OBJECT_NAME);
        }
    }

    /*
     * Sets the focus explicitly to the supertype control. Otherwise the
     * hyperlink receives focus.
     *   
     * Overridden method.
     * @see org.eclipse.swt.widgets.Control#setFocus()
     */
    public boolean setFocus() {
        return supertypeField.getControl().setFocus();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
     */
    protected void performRefresh() {
        uiController.updateUI();
        
    }

    
}
