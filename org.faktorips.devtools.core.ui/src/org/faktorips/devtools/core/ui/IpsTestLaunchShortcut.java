/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

/**
 * Lauch shortcut to run ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(IEditorPart editor, String mode) {
        if (editor instanceof IpsObjectEditor) {
            IIpsObject objectInEditor = ((IpsObjectEditor)editor).getIpsObject();
            IpsTestAction runTestAction = new IpsTestAction(null, mode);
            runTestAction.run(new StructuredSelection(objectInEditor));
        }
    }

    @Override
    public void launch(ISelection selection, String mode) {
        IpsTestAction runTestAction = new IpsTestAction(null, mode);
        runTestAction.run(new StructuredSelection(selection));
    }

}
