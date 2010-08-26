/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
