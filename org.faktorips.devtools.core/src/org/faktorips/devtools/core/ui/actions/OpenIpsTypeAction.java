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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.PdObjectSelectionDialog;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsTypeAction extends Action implements IWorkbenchWindowActionDelegate {
    
    public OpenIpsTypeAction() {
        super();
        setText("Open Type..."); 
        setDescription("Open a type in the editor"); 
        setToolTipText("Open a Type"); 
        // TODO setImageDescriptor(JavaPluginImages.DESC_TOOL_OPENTYPE);
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.OPEN_TYPE_ACTION);
    }

    public void run() {
        Shell parent= IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        String dialogTitle = "Open IPS Type"; 
        String dialogMessage = "&Select a type to open (? = any character, * = any String):"; 

        try {
            PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(parent, dialogTitle, dialogMessage);
            dialog.setElements(getPdObjects());
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (dialog.open()==Window.OK) {
                if (dialog.getResult().length>0) {
                    IIpsObject pdObject = (IIpsObject)dialog.getResult()[0];
                    IpsPlugin.getDefault().openEditor(pdObject);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        // TODO
        return IpsPlugin.getDefault().getIpsModel().getIpsProjects()[0].findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
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
