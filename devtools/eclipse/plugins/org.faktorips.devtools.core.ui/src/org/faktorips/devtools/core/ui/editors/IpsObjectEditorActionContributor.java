/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.faktorips.devtools.core.ui.actions.ToggleWorkingModeAction;

/**
 * Manages the installation and removal of global actions for all ips object editors.
 */
public class IpsObjectEditorActionContributor extends EditorActionBarContributor {

    private ToggleWorkingModeAction toggleWorkingModeAction;

    /**
     * Creates a new <code>IpsObjectEditorActionContributor</code>.
     */
    public IpsObjectEditorActionContributor() {
        toggleWorkingModeAction = new ToggleWorkingModeAction();
    }

    @Override
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(ToggleWorkingModeAction.ID, toggleWorkingModeAction);
    }

    @Override
    public void dispose() {
        super.dispose();
        toggleWorkingModeAction.dispose();
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
        toggleWorkingModeAction.setEditor(targetEditor);
    }

}
