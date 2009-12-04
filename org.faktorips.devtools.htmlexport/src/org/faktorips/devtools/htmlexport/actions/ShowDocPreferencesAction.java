package org.faktorips.devtools.htmlexport.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class ShowDocPreferencesAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public ShowDocPreferencesAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
        IViewRegistry viewRegistry = window.getWorkbench().getViewRegistry();
        
//        window.getWorkbench().get
        
        
	    IViewDescriptor[] views = viewRegistry.getViews();
	    
	    StringBuilder builder = new StringBuilder();
	    for (IViewDescriptor viewDescriptor : views) {
            builder.append("\n").append(viewDescriptor.getId());
        }
	    
        IViewDescriptor view = viewRegistry.find("org.eclipse.jdt.ui.PackageExplorer");
	    
		MessageDialog.openInformation(
			window.getShell(),
			"Fipsdoc Plug-in",
			"factorips doc preferences:\n" + view +  "\n" + view.getClass() + "\n\n" + builder.toString());
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}