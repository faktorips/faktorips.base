package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;

/**
 * A workbench window action delegate to open a new wizard dialog. 
 * The concrete wizard has to be supplied by implementing <code>createWizard()</code>
 * in the subclass.
 * 
 * @author Jan Ortmann
 */
public abstract class OpenNewWizardAction implements IWorkbenchWindowActionDelegate
{
    
    private IWorkbenchWindow window;
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }
    
	/**
	 * Implementations return the Wizard to be displayed in the
	 * WizardDialog. 
	 */
	public abstract INewWizard createWizard();
	
	/** 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		INewWizard wizard = createWizard();
		IStructuredSelection selection = getCurrentSelection();
		if (selection==null) {
			IpsPlugin.log(new IpsStatus("No selection available, can't open wizard!"));
			return;
		}
		wizard.init(window.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}
	
	private IStructuredSelection getCurrentSelection() {
		if (window != null) {
			ISelection selection= window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		IWorkbenchPart part = window.getPartService().getActivePart();
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return new StructuredSelection(((IFileEditorInput) input).getFile());
			}
		}
		return null;
	}
}
