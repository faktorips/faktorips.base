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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action that navigates to the table structure which underlies the table contents.
 */
class NavigateToTableStructureAction extends Action {

    private ITableContents tableContents;

    public NavigateToTableStructureAction(ITableContents tableContents) {
        this.tableContents = tableContents;
        setText(Messages.NavigateToTableStructureAction_Label);
        setToolTipText(Messages.NavigateToTableStructureAction_ToolTip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TableStructure.gif")); //$NON-NLS-1$
    }

    /**
     * Opens the editor for the table structure which these table contents are based on.
     */
    @Override
    public void run() {
        try {
            if (!IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                // if the property changed while the editor is open
                return;
            }
            ITableStructure tableStructure = tableContents.findTableStructure(tableContents.getIpsProject());
            if (tableStructure != null) {
                IpsUIPlugin.getDefault().openEditor(tableStructure);
            }
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
