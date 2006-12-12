/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.IRow;
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
        this.tableViewer= tableViewer;
        setControlWithDataChangeableSupport(page);
        setText(Messages.DeleteRowAction_Label);
        setToolTipText(Messages.DeleteRowAction_Tooltip);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("Delete.gif")); //$NON-NLS-1$
    }
    
    /**
     * Deletes the first selected row in the tableviewer and refreshes thereafter.
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        Object selected= selection.getFirstElement();
        if(selected instanceof IRow){
            ((IRow)selected).delete();
        }
        tableViewer.refresh(false);
    }
}
