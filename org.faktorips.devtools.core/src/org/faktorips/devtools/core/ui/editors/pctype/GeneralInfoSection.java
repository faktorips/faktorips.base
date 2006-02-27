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
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.PdObjectField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class GeneralInfoSection extends IpsSection {
    
    private IPolicyCmptType pcType;
    private IpsObjectUIController uiController;
    
    // edit fields
    private PdObjectField supertypeField;
    private CheckboxField abstractField;
    private TextField productCmptTypeNameField;
    private CheckboxField configuratedField;    
    private ExtensionPropertyControlFactory extFactory;

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
        toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelAbstractClass);
        Checkbox abstractCheckbox = toolkit.createCheckbox(c2);
        toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelProduct);
        Checkbox configuratedCheckbox = toolkit.createCheckbox(c2);
        toolkit.createFormLabel(c2, Messages.GeneralInfoSection_labelType);
        Text productCmptTypeNameText = toolkit.createText(c2);

        // create fields
        supertypeField = new PdObjectField(control);
        abstractField = new CheckboxField(abstractCheckbox);
        productCmptTypeNameField = new TextField(productCmptTypeNameText);
        configuratedField = new CheckboxField(configuratedCheckbox);
        
        // connect fields to model properties
        uiController = new IpsObjectUIController(pcType);
        uiController.add(supertypeField, IPolicyCmptType.PROPERTY_SUPERTYPE);
        uiController.add(abstractField, IPolicyCmptType.PROPERTY_ABSTRACT);
        uiController.add(productCmptTypeNameField, IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
        uiController.add(configuratedField, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE);
        
        extFactory.createControls(c2,toolkit,(IpsObjectPartContainer)pcType);
        extFactory.connectToModel(uiController);
    }
    

    /*
     * Sets the focus explicitly to the supertype control. Otherwise the
     * hyperlink receives focus.
     */
    public boolean setFocus() {
        return supertypeField.getControl().setFocus();
    }
    
    /** 
     * Overridden.
     */
    protected void performRefresh() {
        uiController.updateUI();
        
    }

    
}
