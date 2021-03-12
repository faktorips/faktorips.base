/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * Action that navigates to the table structure which underlies the table contents.
 */
class NavigateToTableStructureAction extends Action {

    private ITableContents tableContents;

    public NavigateToTableStructureAction(ITableContents tableContents) {
        this.tableContents = tableContents;
        setText(Messages.NavigateToTableStructureAction_Label);
        setToolTipText(Messages.NavigateToTableStructureAction_ToolTip);
        setImageDescriptor(IIpsDecorators.getDefaultImageDescriptor(TableStructure.class));
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
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
