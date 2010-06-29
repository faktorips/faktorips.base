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
import org.eclipse.jface.viewers.TableViewer;
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
    private TableViewer enumValuesTableViewer;

    /**
     * Creates a new <tt>NewEnumValueAction</tt>.
     * 
     * @param enumValuesTableViewer The table viewer linking the table widget with the model data.
     * 
     * @throws NullPointerException If <tt>enumValuesTableViewer</tt> is <tt>null</tt>.
     */
    public NewEnumValueAction(TableViewer enumValuesTableViewer) {
        super();
        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;

        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelNewValue);
        setToolTipText(Messages.EnumValuesSection_tooltipNewValue);
    }

    @Override
    public void run() {
        // Do nothing if there are no columns yet.
        if (enumValuesTableViewer.getColumnProperties().length <= 0) {
            return;
        }

        IEnumValueContainer enumValueContainer = (IEnumValueContainer)enumValuesTableViewer.getInput();
        try {
            enumValueContainer.newEnumValue();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        enumValuesTableViewer.refresh(true);
    }

}
