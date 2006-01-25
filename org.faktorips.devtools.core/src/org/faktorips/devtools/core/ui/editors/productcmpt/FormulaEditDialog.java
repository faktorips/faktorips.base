package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.devtools.core.ui.editors.pctype.ChangeParametersControl;
import org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;


/**
 *
 */
public class FormulaEditDialog extends IpsPartEditDialog {
    
    // the formula configuration element being edited
    private IConfigElement configElement;
    
    // the attribute the element is based on.
    private IAttribute attribute;
    
    // control to display & edit the formula parameters
    private ChangeParametersControl parametersControl;
    
    // edit fields
    private TextField formulaField;

    public FormulaEditDialog(IConfigElement configElement, Shell parentShell) throws CoreException {
        super(configElement, parentShell, Messages.FormulaEditDialog_editFormula, true);
        ArgumentCheck.notNull(configElement);
        this.configElement = configElement;
        attribute = configElement.findPcTypeAttribute();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.FormulaEditDialog_Formula);
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        GridLayout layout = (GridLayout)c.getLayout();
        layout.verticalSpacing = 20;

        parametersControl = new ChangeParametersControl(c, SWT.NONE, Messages.FormulaEditDialog_availableParameters, configElement.getIpsProject()) {

            public MessageList validate(int paramIndex) throws CoreException {
                MessageList result = new MessageList();
                MessageList list = configElement.validate();
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
        parametersControl.setCanAddParameters(false);
        parametersControl.setCanChangeParameterTypes(false);
        parametersControl.setCanChangeParameterNames(false);
        parametersControl.setCanMoveParameters(false);
        parametersControl.setNumOfRowsHint(1 + attribute.getNumOfFormulaParameters());
        parametersControl.setTableStyle(SWT.BORDER);
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Text formulaText = uiToolkit.createMultilineText(c);
        try {
            FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(configElement.getIpsProject(), configElement.getExprCompiler());
            ContentAssistHandler.createHandlerForText(formulaText, CompletionUtil.createContentAssistant(completionProcessor));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        // create fields
        formulaField = new TextField(formulaText);

        return c;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(formulaField, IConfigElement.PROPERTY_VALUE);
        List infos = ParameterInfo.createInfosAsList(attribute.getFormulaParameters());
        parametersControl.setInput(infos);
    }
    
}
