/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsObjectAction extends Action implements IWorkbenchWindowActionDelegate {
    
    public OpenIpsObjectAction() {
        super();
        setText("Open IPS Object..."); 
        setDescription("Open a IPS object in the editor"); 
        setToolTipText("Open a IPS Object"); 
        setAccelerator(SWT.CTRL | SWT.SHIFT | 'I');
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("OpenIpsObject.gif"));
    }

    public void run() {
        Shell parent= IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        String dialogTitle = "Open IPS Object"; 
        String dialogMessage = "&Select a object to open (? = any character, * = any String):"; 

        try {
            OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(parent, dialogTitle, dialogMessage);
            dialog.setElements(getIpsObjects());
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (dialog.open()==Window.OK) {
                IIpsObject object = dialog.getSelectedObject();
                if (object!=null) {
                    IpsPlugin.getDefault().openEditor(object);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
    }
    
    /**
     * @return all relevant IIpsObjects for the current perspective.
     */
    protected IIpsObject[] getIpsObjects(){
        IIpsObject[] objects = new IIpsObject[0];
        if(getCurrentPerspective().getId().equals("org.eclipse.team.ui.TeamSynchronizingPerspective")){
            return objects;
        }
        try {
            objects = getAllIpsObjects();
        } catch (CoreException e) {
            // if we fail to get the objects, we won't be able to open them either.
        }
        if(getCurrentPerspective().getId().equals("org.faktorips.devtools.core.productDefinitionPerspective")){
            List list = new ArrayList();
            for (int i = 0; i < objects.length; i++) {
                IIpsObject object = objects[i];
                if(object.getIpsObjectType().isProductDefinitionType()){
                    list.add(object);
                }
            }
            objects = (IIpsObject[])list.toArray(new IIpsObject[list.size()]);
        }
        return objects;
    }
    
    public IPerspectiveDescriptor getCurrentPerspective(){
        return IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    }

    /**
     * Calls <code>IIpsProject#findAllIpsObjects()</code> on all projects in the workspace
     * and returns the collective list of <code>IIpsObject</code>s.
     * 
     * @throws CoreException if getting objects from a <code>IIpsProject</code> fails.
     */
    public IIpsObject[] getAllIpsObjects() throws CoreException {
        List list = new ArrayList();
        IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            IIpsProject project = projects[i];
            project.findAllIpsObjects(list);
        }
        Set set = new HashSet();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            IIpsObject object = (IIpsObject)iter.next();
            set.add(object);
        }
        return (IIpsObject[])set.toArray(new IIpsObject[set.size()]);
    }

    //---- IWorkbenchWindowActionDelegate ------------------------------------------------

    public void run(IAction action) {
        run();
    }
    
    public void dispose() {
        // do nothing.
    }
    
    public void init(IWorkbenchWindow window) {
        // do nothing.
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }
}
