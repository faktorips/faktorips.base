package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;


/**
 *
 */
public class MethodEditDialog extends IpsPartEditDialog implements ParameterListChangeListener {

    private IMethod method;
    
    // edit fields
    private ComboField modifierField;
    private CheckboxField abstractField;
    private TextField nameField;
    private TextButtonField datatypeField;
    private TextField bodyField;
    
    private ChangeParametersControl parametersControl;
    
    /**
     * @param parentShell
     * @param windowTitle
     */
    public MethodEditDialog(IMethod method, Shell parentShell) {
        super(method, parentShell, "Edit IMethod", true);
        this.method = method;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText("Signature");
        page.setControl(createGeneralPage(folder));
        
        page = new TabItem(folder, SWT.NONE);
        page.setText("Implementation");
        page.setControl(createBodyPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createGeneralPage(TabFolder folder) {
        
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createGridComposite(c, 1, false, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        
        // method properties
        Composite propertyPane = uiToolkit.createLabelEditColumnComposite(workArea);
        
        uiToolkit.createFormLabel(propertyPane, "Access Modifier:");
        Combo modifierCombo = uiToolkit.createCombo(propertyPane, Modifier.getEnumType());
        modifierCombo.setFocus();

        uiToolkit.createFormLabel(propertyPane, "Abstract:");
        Checkbox abstractCheckbox = uiToolkit.createCheckbox(propertyPane);
        
        uiToolkit.createFormLabel(propertyPane, "Type:");
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(method.getIpsProject(), propertyPane);
        datatypeControl.setVoidAllowed(true);
        datatypeControl.setOnlyValueDatatypesAllowed(false);
        

        uiToolkit.createFormLabel(propertyPane, "Name:");
        Text nameText = uiToolkit.createText(propertyPane);
        
        // parameters
        parametersControl = new ChangeParametersControl(workArea, SWT.NONE, "Parameters", method.getIpsProject()) {

            public MessageList validate(int paramIndex) throws CoreException {
                MessageList result = new MessageList();
                MessageList list = method.validate();
                for (int i=0; i<list.getNoOfMessages(); i++) {
                    if (isMessageForParameter(list.getMessage(i), paramIndex)) {
                        result.add(list.getMessage(i));
                    }
                }
                return result;
            }
            
            private boolean isMessageForParameter(Message msg, int paramIndex) {
                ObjectProperty[] op = msg.getInvalidObjectProperties(); 
                for (int j=0; j<op.length; j++) {
                    if (op[j].getObject() instanceof Parameter) {
                        if (((Parameter)op[j].getObject()).getIndex()==paramIndex) {
                            return true;
                        }
                    }
                }
                return false;
            }
            
            
        };
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        // create fields
        abstractField = new CheckboxField(abstractCheckbox);
        modifierField = new EnumValueField(modifierCombo, Modifier.getEnumType());
        nameField = new TextField(nameText);
        datatypeField = new TextButtonField(datatypeControl);
        
        return c;
    }
    
    private Control createBodyPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Text text = uiToolkit.createMultilineText(c);
        text.setEditable(false);
        bodyField = new TextField(text);
        return c;
    }

	protected Point getInitialSize() {
	    return new Point(800, 600);
	}
	
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(modifierField, IMethod.PROPERTY_MODIFIER);
        uiController.add(abstractField, IMethod.PROPERTY_ABSTRACT);
        uiController.add(nameField, IMethod.PROPERTY_NAME);
        uiController.add(datatypeField, IMethod.PROPERTY_DATATYPE);
        bodyField.setText(method.getBody());
        List infos = ParameterInfo.createInfosAsList(method.getParameters());
        parametersControl.setInput(infos);
        parametersControl.setParameterListChangeListener(this);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#buildTitle()
     */
    protected String buildTitle() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(method.getParent().getName());
        buffer.append('.');
        buffer.append(method.getDatatype());
        buffer.append(' ');
        buffer.append(method.getName());
        buffer.append('(');
        Parameter[] params = method.getParameters(); 
        for (int i=0; i<params.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterChanged(org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo)
     */
    public void parameterChanged(ParameterInfo parameter) {
        parameterListChanged();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterAdded(org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo)
     */
    public void parameterAdded(ParameterInfo parameter) {
        parameterListChanged();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterListChanged()
     */
    public void parameterListChanged() {
        Parameter[] params = ParameterInfo.createParameters(parametersControl.getInput());
        method.setParameters(params);
        setTitle(buildTitle());
    }

}
