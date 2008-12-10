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

package org.faktorips.devtools.core.ui;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Lauch shortcut to run ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestLaunchShortcut implements ILaunchShortcut {

    /**
     * {@inheritDoc}
     */
    public void launch(IEditorPart editor, String mode) {
        if (editor instanceof IpsObjectEditor){
            IIpsObject objectInEditor = ((IpsObjectEditor)editor).getIpsObject();
            IpsTestAction runTestAction = new IpsTestAction(null, mode);
            runTestAction.run(new StructuredSelection(objectInEditor));        
        }
    }

    /**
     * {@inheritDoc}
     */
    public void launch(ISelection selection, String mode) {
        IpsTestAction runTestAction = new IpsTestAction(null, mode);
        runTestAction.run(new StructuredSelection(selection));
    }
}
