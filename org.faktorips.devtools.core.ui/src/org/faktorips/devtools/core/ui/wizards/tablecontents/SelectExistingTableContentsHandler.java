/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;

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
                    try {
                        setToUsage.getIpsSrcFile().save(true, null);
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
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
            try {
                tableStructureUsage = setToUsage.findTableStructureUsage(setToUsage.getIpsProject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IIpsSrcFile
                    && IpsObjectType.TABLE_CONTENTS.equals(((IIpsSrcFile)element).getIpsObjectType())) {
                IIpsSrcFile srcFile = (IIpsSrcFile)element;
                String tableStructure;
                try {
                    tableStructure = srcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                    return false;
                }
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
