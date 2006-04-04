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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.ChangeParametersControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
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
    
    /**
     * Flag indicating that this dialog is only a view (which does not allow to 
     * modify its contents).
     */
    private boolean viewOnly = false;

    /**
     * Creates a new dialog which allows to edit a formula. This dialog is editable.
     * 
     * @param configElement The config element the formula is for.
     * @param parentShell The shell as parent for the dialog.
     * 
     * @throws CoreException if the config element is invalid (e.g. no datatype can be found for it).
     */
    public FormulaEditDialog(IConfigElement configElement, Shell parentShell) throws CoreException {
    	this(configElement, parentShell, false);
    }
    
    /**
     * Creates a new dialog which allows to edit a formula. 
     * 
     * @param configElement The config element the formula is for.
     * @param parentShell The shell as parent for the dialog.
     * @param viewOnly <code>true</code> to get a dialog where no modifications can be made, 
     *        <code>false</code> to get a unlocked, fully modifyable dialog.
     *        
     * @throws CoreException if the config element is invalid (e.g. no datatype can be found for it).
     */
    public FormulaEditDialog(IConfigElement configElement, Shell parentShell, boolean viewOnly) throws CoreException {
        super(configElement, parentShell, Messages.FormulaEditDialog_editFormula, true);
        ArgumentCheck.notNull(configElement);
        this.configElement = configElement;
        attribute = configElement.findPcTypeAttribute();
        this.viewOnly = viewOnly;
        
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartUIController createUIController(IIpsObjectPart part) {
        IpsPartUIController controller = new IpsPartUIController(part) {

			protected MessageList validatePartAndUpdateUI() {
				MessageList messages = super.validatePartAndUpdateUI();
				if(!messages.isEmpty()){
					Message firstMessage = messages.getMessage(0);
					setMessage(firstMessage.getText(),  getJFaceMessageType(firstMessage.getSeverity()));	
				}
				else{
					setMessage(""); //$NON-NLS-1$
				}
				return messages;
			}
        	
        };
        return controller;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.FormulaEditDialog_Formula);
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        super.setEnabledDescription(!viewOnly);

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
        parametersControl.setTableStyle(SWT.BORDER);
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersControl.setEnabled(!viewOnly);
        
        Text formulaText = uiToolkit.createMultilineText(c);
        try {
            FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(
					attribute, configElement.getIpsProject(), configElement
							.getExprCompiler());
            ContentAssistHandler.createHandlerForText(formulaText, CompletionUtil.createContentAssistant(completionProcessor));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        formulaText.setEnabled(!viewOnly);
        
        // create fields
        formulaField = new TextField(formulaText);

        return c;
    }
    
    private int getJFaceMessageType(int severity){
    	
    	if(Message.ERROR == severity){
    		return IMessageProvider.ERROR;
    	}
    	if(Message.INFO ==  severity){
    		return IMessageProvider.INFORMATION;
    	}
    	if(Message.WARNING == severity){
    		return IMessageProvider.WARNING;
    	}
    	
    	return IMessageProvider.NONE;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(formulaField, IConfigElement.PROPERTY_VALUE);
        List infos = ParameterInfo.createInfosAsList(attribute.getFormulaParameters());
        parametersControl.setInput(infos);
    }
    
    /**
     * Overrides the parent method and returns a combination of attribute name and datatype name of
     * the attribute this formula relates to.
     */
    protected String buildTitle() {
    	return attribute.getName() + " - " + attribute.getDatatype(); //$NON-NLS-1$
    }
    
}
