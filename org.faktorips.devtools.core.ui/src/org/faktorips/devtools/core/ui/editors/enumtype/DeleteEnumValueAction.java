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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;

/**
 * This action is used by the <code>EnumValuesSection</code> for deleting enum values.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class DeleteEnumValueAction extends IpsAction {

    // The table viewer linking the ui table widget with the model data
    private TableViewer tableViewer;

    /**
     * Creates a new <code>DeleteEnumValueAction</code>.
     * 
     * @param tableViewer The table viewer linking the ui table widget with the model data.
     */
    public DeleteEnumValueAction(TableViewer tableViewer) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("Delete.gif"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        if (selection == null) {
            return;
        }

        IEnumValue enumValue = (IEnumValue)selection.getFirstElement();
        if (enumValue != null) {
            enumValue.delete();
            tableViewer.refresh(true);
        }
    }

}
