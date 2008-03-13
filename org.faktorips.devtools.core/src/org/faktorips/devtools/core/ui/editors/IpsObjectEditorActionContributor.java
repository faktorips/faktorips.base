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
    
    public IpsObjectEditorActionContributor() {
        toggleWorkingModeAction = new ToggleWorkingModeAction();
    }

    /**
     * {@inheritDoc}
     */
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(ToggleWorkingModeAction.ID, toggleWorkingModeAction);
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
        toggleWorkingModeAction.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
        toggleWorkingModeAction.setEditor(targetEditor);
    }
}
