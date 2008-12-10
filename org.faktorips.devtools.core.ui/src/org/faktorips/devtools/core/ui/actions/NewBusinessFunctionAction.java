/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Opens the wizard for creating a new BusinessFunction.
 * 
 * @author Peter Erzberger
 */
public class NewBusinessFunctionAction extends Action {
    private IWorkbenchWindow window;

    public NewBusinessFunctionAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText("Business Function");
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("missing")); //$NON-NLS-1$
    }

    public void run() {
        // TODO needs to be implemented. Therefore this class the Modelexplorer and all depending
        // classes need to be moved to the core.ui. plugin
    }

}
