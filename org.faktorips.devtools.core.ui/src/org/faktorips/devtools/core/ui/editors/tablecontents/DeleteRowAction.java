/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.Messages;

/**
 * Action for deleting a single row in a tableviewer.
 * 
 * @author Stefan Widmaier
 */
public class DeleteRowAction extends IpsAction {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;

    /**
     * Creates an action that, when run, deletes the (first) selected row in the given
     * <code>TableViewer</code>.
     */
    public DeleteRowAction(TableViewer tableViewer, ContentPage page) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        setControlWithDataChangeableSupport(page);
        setText(Messages.DeleteRowAction_Label);
        setToolTipText(Messages.DeleteRowAction_Tooltip);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("DeleteRow.gif")); //$NON-NLS-1$
    }

    /**
     * Deletes the first selected row in the tableviewer and refreshes thereafter. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IRow) {
            ((IRow)selected).delete();
        }
        tableViewer.refresh(false);
    }
}
