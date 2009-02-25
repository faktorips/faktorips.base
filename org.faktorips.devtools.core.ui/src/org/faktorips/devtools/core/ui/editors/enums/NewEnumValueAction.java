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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.enums.Messages;

/**
 * This action is used by the <code>EnumValuesSection</code> for adding new enum values.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class NewEnumValueAction extends AbstractEnumAction {

    /**
     * Creates a new <code>NewEnumValueAction</code>.
     * 
     * @param tableViewer The table viewer linking the ui table widget with the model data.
     */
    public NewEnumValueAction(TableViewer tableViewer) {
        super(tableViewer);
        
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("InsertRowAfter.gif"));
        setText(Messages.EnumValuesSection_labelNewValue);
        setToolTipText(Messages.EnumValuesSection_tooltipNewValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        // Do nothing if there are no columns yet
        if (tableViewer.getColumnProperties().length <= 0) {
            return;
        }

        IEnumValueContainer enumValueContainer = (IEnumValueContainer)tableViewer.getInput();
        enumValueContainer.newEnumValue();
        tableViewer.refresh(true);
    }

}
