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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Event;

/**
 * A base class for actions which enabled state depends on the current selection within the workbench.
 * See http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html for more information about choosing the
 * right selection provider to register this action with.
 * Furthermore it uses the selection provider to determine the current selection and provide it to the 
 * execute(IStructuredSelection) method when this action is executed. Therefore this action can only be used with 
 * a not <code>null</code> selection provider.
 *  
 *  
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 * @author Peter Erzberger
 */

public abstract class AbstractSelectionChangedListenerAction extends Action implements ISelectionChangedListener{
    
    /**
     * The selectionProvider given at instantiation. This is used to retrieve the currently
     * selected objects when the runWithEvent() method is called.
     */
	private ISelectionProvider selectionProvider;
	
    //indicates if the disposed method has been called. A disposed IpsDeleteAction cannot be executed anymore
    private boolean disposed = false;
    
    /**
     * Subclasses implement to deletion of the object in 
     * @param selection
     */
	protected abstract void execute(IStructuredSelection selection);
	
    /**
     * Returns true. Subclasses can implement this method to determine the enabled state of this action depending 
     * on the provided selection.
     */
    protected boolean isEnabled(ISelection selection){
        return true;
    }

    /**
     * Returns the selection provider hold by this action.
     */
    protected ISelectionProvider getSelectionProvider(){
        checkDisposedState();
        return selectionProvider;
    }
    
    /**
	 * Creates a DeleteAction with the given SelectionProvider. This SelectionProvider
	 * is used to retrieve the selected objects that are deleted from the object model
	 * while executing the run() method.
     * <p>
     * The given <code>ISelectionProvider</code> must not be <code>null</code>.
	 */
	public AbstractSelectionChangedListenerAction(ISelectionProvider provider){
		selectionProvider= provider;
        provider.addSelectionChangedListener(this);
	}
	
    /**
	 * {@inheritDoc}
	 */
	public void runWithEvent(Event event) {
		ISelection selection= getSelectionProvider().getSelection();
		if(!(selection instanceof IStructuredSelection)){
			return;
		}
        execute((IStructuredSelection)selection);
    }
	    
    /**
     * Calls setEnabledState(ISelection selection).
     */
    public void selectionChanged(SelectionChangedEvent event) {
        setEnabled(isEnabled(event.getSelection()));
    }
    
    /**
     * Users of this class might make sure that the disposed method is called when this action is no logger in usage.
     * This method unregisters this action from the selection provider so that it doesn't react to its events. 
     * In addition the disposeInternal method is called which can be imlemented by subclasses for disposal work.
     */
    public final void dispose(){
        getSelectionProvider().removeSelectionChangedListener(this);
        disposeInternal();
        disposed = true;
    }

    /**
     * Empty implementation that can be overidden by subclasses to clean up issues.
     */
    protected void disposeInternal(){
    }
    
    protected final void checkDisposedState(){
        if(disposed){
            throw new RuntimeException(NLS.bind("This {0} has already been disposed.", AbstractSelectionChangedListenerAction.class)); //$NON-NLS-1$
        }
    }
}
