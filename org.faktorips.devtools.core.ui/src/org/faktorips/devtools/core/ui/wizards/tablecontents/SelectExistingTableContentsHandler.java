/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;

public class SelectExistingTableContentsHandler extends AbstractAddTableContentsHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.wizards.tablecontents.addTableContents"; //$NON-NLS-1$

    public static final String PARAMETER_TABLE_USAGE = "org.faktorips.devtools.core.ui.wizards.tablecontents.addTableContents.tableUsage"; //$NON-NLS-1$

    @Override
    public void openDialog(ITableContentUsage setToUsage, Shell shell, boolean autoSave) {
        OpenIpsObjectSelectionDialog selectDialog = getSelectDialog(setToUsage, shell);
        boolean dirtyState = setToUsage.getIpsSrcFile().isDirty();
        selectDialog.setMessage(Messages.SelectExistingTableContentsHandler_selectionDialogTitle);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                IIpsSrcFile selectedObject = (IIpsSrcFile)selectDialog.getSelectedObject();
                setToUsage.setTableContentName(selectedObject.getQualifiedNameType().getName());
                if (autoSave && !dirtyState) {
                    setToUsage.getIpsSrcFile().save(null);
                }
            }
        }
    }

    private OpenIpsObjectSelectionDialog getSelectDialog(ITableContentUsage setToUsage, Shell shell) {
        SingleTypeSelectIpsObjectContext context = new SingleTypeSelectIpsObjectContext(setToUsage.getIpsProject(),
                IpsObjectType.TABLE_CONTENTS, new TableContentsViewerFilter(setToUsage));
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(shell,
                Messages.AddLinkAction_selectDialogTitle, context);
        return dialog;
    }

    @Override
    protected String getTableUsageParameter() {
        return PARAMETER_TABLE_USAGE;
    }

    private static class TableContentsViewerFilter extends ViewerFilter {

        private ITableStructureUsage tableStructureUsage;

        public TableContentsViewerFilter(ITableContentUsage setToUsage) {
            tableStructureUsage = setToUsage.findTableStructureUsage(setToUsage.getIpsProject());
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IIpsSrcFile
                    && IpsObjectType.TABLE_CONTENTS.equals(((IIpsSrcFile)element).getIpsObjectType())) {
                IIpsSrcFile srcFile = (IIpsSrcFile)element;
                String tableStructure;
                tableStructure = srcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                for (String structure : tableStructureUsage.getTableStructures()) {
                    if (tableStructure != null && tableStructure.equals(structure)) {
                        return true;
                    }
                }
            }
            return false;

        }
    }

}
