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
     * @throws NullPointerException If enumValuesTableViewer is <code>null</code>.
     */
    public MoveEnumValueAction(TableViewer enumValuesTableViewer, boolean up) {
        super();

        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;
        this.up = up;

        if (up) {
            setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("ArrowUp.gif"));
            setText(Messages.EnumValuesSection_labelMoveEnumValueUp);
            setToolTipText(Messages.EnumValuesSection_tooltipMoveEnumValueUp);
        } else {
            setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("ArrowDown.gif"));
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

        IEnumValue enumValue = (IEnumValue)selection.getFirstElement();
        if (enumValue != null) {
            IEnumValueContainer enumValueContainer = (IEnumValueContainer)enumValue.getParent();
            try {
                if (up) {
                    //TODO pk: hier würde sich eine Methode moveEnumValue(boolean up) anbieten. Das würde dieses
                    //if auflösen
                    enumValueContainer.moveEnumValueUp(enumValue);
                } else {
                    enumValueContainer.moveEnumValueDown(enumValue);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            enumValuesTableViewer.refresh(true);
        }
    }
}
