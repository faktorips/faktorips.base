package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.PdObjectSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;


/**
 *
 */
abstract class IpsObjectRefControl extends TextButtonControl {
    
    private IIpsProject pdProject;
    
    private String dialogTitle;
    private String dialogMessage;
    
    private IpsObjectCompletionProcessor completionProcessor;
    
    /**
     * @param parent
     * @param style
     */
    public IpsObjectRefControl(
            IIpsProject project,
            Composite parent, 
            UIToolkit toolkit,
            String dialogTitle,
            String dialogMessage) {
        super(parent, toolkit, "Browse");
        this.pdProject = project;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        completionProcessor = new IpsObjectCompletionProcessor(this);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    public void setPdProject(IIpsProject project) {
        this.pdProject = project;
        setButtonEnabled(project!=null && project.exists());
    }
    
    public IIpsProject getPdProject() {
        return pdProject;
    }
    
    protected void buttonClicked() {
        try {
            PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(getShell(), dialogTitle, dialogMessage);
            dialog.setElements(getPdObjects());
            if (dialog.open()==Window.OK) {
                if (dialog.getResult().length>0) {
                    IIpsObject pdObject = (IIpsObject)dialog.getResult()[0];
                    setText(pdObject.getQualifiedName());
                } else {
                    setText("");
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    protected abstract IIpsObject[] getPdObjects() throws CoreException;
    
}
