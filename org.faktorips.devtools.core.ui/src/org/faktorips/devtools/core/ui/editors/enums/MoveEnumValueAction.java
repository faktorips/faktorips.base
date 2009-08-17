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
 * This action is used by the <code>EnumValuesSection</code> for moving enum values.
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

    /** The enum values table viewer linking the enum values ui table widget with the model data. */
    private TableViewer enumValuesTableViewer;

    /** Flag indicating whether to move up or down. */
    private boolean up;

    /**
     * Creates a new <code>MoveEnumValueAction</code>.
     * 
     * @param enumValuesTableViewer The enum values table viewer linking the enum values ui table
     *            widget with the model data.
     * @param up Flag indicating whether the selected enum value shall be moved upwards or
     *            downwards.
     * 
     * @throws NullPointerException If <code>enumValuesTableViewer</code> is <code>null</code>.
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

    /**
     * {@inheritDoc}
     */
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
            // Move all selected enum values upwards
            // ----------------------------------------

            IEnumValue firstSelectedEnumValue = enumValuesToMove.get(0);
            int index = enumValueContainer.getIndexOfEnumValue(firstSelectedEnumValue);
            // If the the first selected enum value is the first item we do not move at all
            if (index == 0) {
                return;
            }

            // Perform moving starting with first selected enum value
            try {
                enumValueContainer.moveEnumValues(enumValuesToMove, true);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

        } else {
            // Move all selected enum values downwards
            // ----------------------------------------

            IEnumValue lastSelectedEnumValue = enumValuesToMove.get(enumValuesToMove.size() - 1);
            int index = enumValueContainer.getIndexOfEnumValue(lastSelectedEnumValue);
            // If the the last selected enum value is the last item we do not move at all
            if (index == allEnumValues.size() - 1) {
                return;
            }

            // Perform moving starting with last selected enum value
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
