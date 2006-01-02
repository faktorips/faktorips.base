package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 *
 */
public class RuleEditDialog extends IpsPartEditDialog {
    
    private IValidationRuleDef rule;
    
    // edit fields
    private TextField nameField;
    private TextField msgCodeField;
    private EnumValueField msgSeverityField;
    private TextField msgTextField;
    private CheckboxField appliedToAllField;
    private RuleFunctionsControl rfControl;

    /**
     * @param parentShell
     * @param title
     */
    public RuleEditDialog(IValidationRuleDef rule, Shell parentShell) {
        super(rule, parentShell, "Edit Validation Rule", true);
        this.rule = rule;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        
        TabFolder folder = (TabFolder)parent;
        
        TabItem msgPage = new TabItem(folder, SWT.NONE);
        msgPage.setText("Message");
        msgPage.setControl(createMessagePage(folder));
        
        TabItem functionsPage = new TabItem(folder, SWT.NONE);
        functionsPage.setText("Usage");
        functionsPage.setControl(createFunctionsPage(folder));
        
        TabItem attributesPage = new TabItem(folder, SWT.NONE);
        attributesPage.setText("Validated Attributes");
        attributesPage.setControl(createAttributesPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createMessagePage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(workArea);
        uiToolkit.createFormLabel(nameComposite, "Name:");
        Text nameText = uiToolkit.createText(nameComposite);
        nameText.setFocus();

        // message group
        Group msgGroup = uiToolkit.createGroup(workArea, "Message");
        Composite msgComposite = uiToolkit.createLabelEditColumnComposite(msgGroup);
        msgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(msgComposite, "Code:");
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, "Severity:");
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, "Text:");
        label.getParent().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        Text msgText = uiToolkit.createMultilineText(msgComposite);
        
        // create fields
        nameField = new TextField(nameText);
        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumValueField(severityCombo, MessageSeverity.getEnumType());;
        
        
        return workArea;
    }
    
    private Control createFunctionsPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox appliedToAllCheckbox = uiToolkit.createCheckbox(workArea, "Applied in all business functions");
        rfControl = new RuleFunctionsControl((IValidationRuleDef)super.getIpsPart(), workArea);
        
        appliedToAllField = new CheckboxField(appliedToAllCheckbox);
        return workArea;
    }
    
    private Control createAttributesPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        uiToolkit.createFormLabel(workArea, "Definition of the attributes this rule validates goes here.");
        return workArea;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#createUIController(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected IpsPartUIController createUIController(IIpsObjectPart part) {
        return new UIController(part);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, rule, IValidationRuleDef.PROPERTY_NAME);
        uiController.add(msgCodeField, rule, IValidationRuleDef.PROPERTY_MESSAGE_CODE);
        uiController.add(msgSeverityField, rule, IValidationRuleDef.PROPERTY_MESSAGE_SEVERITY);
        uiController.add(msgTextField, rule, IValidationRuleDef.PROPERTY_MESSAGE_TEXT);
        uiController.add(appliedToAllField, rule, IValidationRuleDef.PROPERTY_APPLIED_IN_ALL_FUNCTIONS);
    }
    
	protected Point getInitialSize() {
	    return new Point(500, 420);
	}
	
    class UIController extends IpsPartUIController {

        public UIController(IIpsObjectPart pdPart) {
            super(pdPart);
        }
        
        protected void validatePartAndUpdateUI() {
            super.validatePartAndUpdateUI();
            rfControl.refresh();
        }
    }

}
