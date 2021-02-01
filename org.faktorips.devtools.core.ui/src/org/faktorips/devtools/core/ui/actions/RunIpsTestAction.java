/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action delegate to run tests.
 */
public class RunIpsTestAction extends ActionDelegate {

    private IStructuredSelection selection = StructuredSelection.EMPTY;

    @Override
    public void selectionChanged(IAction action, ISelection newSelection) {
        if (newSelection instanceof IStructuredSelection) {
            selection = (IStructuredSelection)newSelection;
        } else {
            selection = StructuredSelection.EMPTY;
        }
    }

    @Override
    public void runWithEvent(IAction action, Event event) {
        new IpsTestAction(null).run(selection);
    }

}
