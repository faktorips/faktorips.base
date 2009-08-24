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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <tt>EnumValuesSection</tt> for moving <tt>IEnumValue</tt>s.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class MoveEnumValueAction extends Action {

    /** The name of the image for the move up action. */
    private final String IMAGE_NAME_UP = "ArrowUp.gif";

    /** The name of the image for the move down action. */
    private final String IMAGE_NAME_DOWN = "ArrowDown.gif";

    /** The table viewer linking the enumeration values UI table widget with the model data. */
    private TableViewer enumValuesTableViewer;

    /** Flag indicating whether to move up or down. */
    private boolean up;

    /**
     * Creates a new <tt>MoveEnumValueAction</tt>.
     * 
     * @param enumValuesTableViewer The table viewer linking the table widget with the model data.
     * @param up Flag indicating whether the selected <tt>IEnumValue</tt> shall be moved upwards or
     *            downwards.
     * 
     * @throws NullPointerException If <tt>enumValuesTableViewer</tt> is <tt>null</tt>.
     */
    public MoveEnumValueAction(TableViewer enumValuesTableViewer, boolean up) {
        super();
        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;
        this.up = up;

        if (up) {
            setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME_UP));
            setText(Messages.EnumValuesSection_labelMoveEnumValueUp);
            setToolTipText(Messages.EnumValuesSection_tooltipMoveEnumValueUp);
        } else {
            setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME_DOWN));
            setText(Messages.EnumValuesSection_labelMoveEnumValueDown);
            setToolTipText(Messages.EnumValuesSection_tooltipMoveEnumValueDown);
        }
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection)enumValuesTableViewer.getSelection();
        if (selection == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Iterator<IEnumValue> selectedEnumValues = selection.iterator();
        List<IEnumValue> enumValuesToMove = new ArrayList<IEnumValue>();
        while (selectedEnumValues.hasNext()) {
            enumValuesToMove.add(selectedEnumValues.next());
        }

        if (enumValuesToMove.size() < 1) {
            return;
        }

        IEnumValueContainer enumValueContainer = ((IEnumValue)selection.getFirstElement()).getEnumValueContainer();
        List<IEnumValue> allEnumValues = enumValueContainer.getEnumValues();

        if (up) {
            // Move all selected EnumValues upwards.
            // ----------------------------------------
            IEnumValue firstSelectedEnumValue = enumValuesToMove.get(0);
            int index = enumValueContainer.getIndexOfEnumValue(firstSelectedEnumValue);
            // If the the first selected EnumValue is the first item we do not move at all.
            if (index == 0) {
                return;
            }

            // Perform moving starting with first selected EnumValue.
            try {
                enumValueContainer.moveEnumValues(enumValuesToMove, true);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

        } else {
            // Move all selected EnumValues downwards.
            // ----------------------------------------
            IEnumValue lastSelectedEnumValue = enumValuesToMove.get(enumValuesToMove.size() - 1);
            int index = enumValueContainer.getIndexOfEnumValue(lastSelectedEnumValue);
            // If the the last selected EnumValue is the last item we do not move at all.
            if (index == allEnumValues.size() - 1) {
                return;
            }

            // Perform moving starting with last selected EnumValue.
            int numberToMove = enumValuesToMove.size();
            List<IEnumValue> orderedValues = new ArrayList<IEnumValue>(numberToMove);
            for (int i = numberToMove - 1; i >= 0; i--) {
                orderedValues.add(enumValuesToMove.get(i));
            }
            try {
                enumValueContainer.moveEnumValues(orderedValues, false);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        enumValuesTableViewer.refresh(true);
    }

}
