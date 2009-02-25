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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.enums.Messages;

/**
 * This action is used by the <code>EnumValuesSection</code> for moving enum values.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class MoveEnumValueAction extends AbstractEnumAction {

    /** Flag indicating whether to move up or down. */
    private boolean up;

    /**
     * Creates a new <code>MoveEnumValueAction</code>.
     * 
     * @param tableViewer The table viewer linking the ui table widget with the model data.
     * @param up Flag indicating whether the selected enum value shall be moved upwards or
     *            downwards.
     */
    public MoveEnumValueAction(TableViewer tableViewer, boolean up) {
        super(tableViewer);
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
    public void run(IStructuredSelection selection) {
        if (selection == null) {
            return;
        }

        IEnumValue enumValue = (IEnumValue)selection.getFirstElement();
        if (enumValue != null) {
            IEnumValueContainer enumValueContainer = (IEnumValueContainer)enumValue.getParent();
            try {
                if (up) {
                    enumValueContainer.moveEnumValueUp(enumValue);
                } else {
                    enumValueContainer.moveEnumValueDown(enumValue);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            tableViewer.refresh(true);
        }
    }
}
