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

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <tt>EnumValuesSection</tt> for deleting <tt>IEnumValue</tt>s.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class DeleteEnumValueAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "Delete.gif";

    /** The table viewer linking the enumeration values UI table widget with the model data. */
    private TableViewer enumValuesTableViewer;

    /**
     * Creates a new <tt>DeleteEnumValueAction</tt>.
     * 
     * @param enumValuesTableViewer The table viewer linking the table widget with the model data.
     * 
     * @throws NullPointerException If <tt>enumValuesTableViewer</tt> is <tt>null</tt>.
     */
    public DeleteEnumValueAction(TableViewer enumValuesTableViewer) {
        super();
        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;

        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelDeleteValue);
        setToolTipText(Messages.EnumValuesSection_tooltipDeleteValue);
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection)enumValuesTableViewer.getSelection();
        if (selection == null) {
            return;
        }

        // Determine EnumValues to delete and obtain the last selected item.
        @SuppressWarnings("unchecked")
        Iterator<IEnumValue> selectedEnumValues = selection.iterator();
        List<IEnumValue> enumValuesToDelete = new ArrayList<IEnumValue>();
        IEnumValue lastSelectedEnumValue = null;
        while (selectedEnumValues.hasNext()) {
            IEnumValue currentSelectedEnumValue = selectedEnumValues.next();
            enumValuesToDelete.add(currentSelectedEnumValue);
            lastSelectedEnumValue = currentSelectedEnumValue;
        }

        if (lastSelectedEnumValue == null) {
            return;
        }

        // Obtain the index of the last selected EnumValue.
        IEnumValueContainer enumValueContainer = lastSelectedEnumValue.getEnumValueContainer();
        int lastIndex = enumValueContainer.getIndexOfEnumValue(lastSelectedEnumValue);
        List<IEnumValue> enumValuesList = enumValueContainer.getEnumValues();

        // Set the new selection to the item below the last selected item if possible.
        if (enumValueContainer.getEnumValuesCount() > lastIndex + 1) {
            IStructuredSelection newSelection = new StructuredSelection(enumValuesList.get(lastIndex + 1));
            enumValuesTableViewer.setSelection(newSelection, true);
        }

        // Delete the previously selected EnumValues now.
        enumValueContainer.deleteEnumValues(enumValuesToDelete);
    }

}
