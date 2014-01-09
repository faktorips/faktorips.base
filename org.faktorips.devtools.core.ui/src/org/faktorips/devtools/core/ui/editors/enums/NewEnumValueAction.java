/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <tt>EnumValuesSection</tt> for creating new <tt>IEnumValue</tt>s.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class NewEnumValueAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "InsertRowAfter.gif"; //$NON-NLS-1$

    /** The table viewer linking the enumeration values UI table widget with the model data. */
    private final TableViewer tableViewer;

    /**
     * @param tableViewer The table viewer linking the table widget with the model data.
     * 
     * @throws NullPointerException If <tt>tableViewer</tt> is <tt>null</tt>.
     */
    public NewEnumValueAction(TableViewer tableViewer) {
        super();
        ArgumentCheck.notNull(tableViewer);

        this.tableViewer = tableViewer;

        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelNewValue);
        setToolTipText(Messages.EnumValuesSection_tooltipNewValue);
    }

    @Override
    public void run() {
        IEnumValueContainer enumValueContainer = (IEnumValueContainer)tableViewer.getInput();
        IEnumValue newEnumValue;
        try {
            newEnumValue = enumValueContainer.newEnumValue();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        tableViewer.refresh(true);
        Table table = tableViewer.getTable();
        table.setTopIndex(table.getItemCount() - 1);
        tableViewer.editElement(newEnumValue, 0);
    }

}
