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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <code>EnumValuesSection</code> for adding new enum values.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class NewEnumValueAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "InsertRowAfter.gif";

    /**
     * The enum values table viewer linking the enumeration values UI table widget with the model
     * data.
     */
    private TableViewer enumValuesTableViewer;

    /**
     * Creates a new <code>NewEnumValueAction</code>.
     */
    public NewEnumValueAction(TableViewer enumValuesTableViewer) {
        super();
        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;

        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelNewValue);
        setToolTipText(Messages.EnumValuesSection_tooltipNewValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // Do nothing if there are no columns yet.
        if (enumValuesTableViewer.getColumnProperties().length <= 0) {
            return;
        }

        final IEnumValueContainer enumValueContainer = (IEnumValueContainer)enumValuesTableViewer.getInput();
        IEnumValue newEnumValue = null;
        try {
            newEnumValue = enumValueContainer.newEnumValue();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        /*
         * Show dialog to the user if the enumeration value could not be created. This can only
         * happen, when the enumValueContainer is an enumeration content and the referenced
         * enumeration type could not be found.
         */
        if (newEnumValue == null) {
            Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();
            display.asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.EnumValuesSection_titleEnumValueCreationNotPossible, NLS.bind(
                                    Messages.EnumValuesSection_msgEnumValueCreationNotPossibleDueToNotFoundEnumType,
                                    ((IEnumContent)enumValueContainer).getEnumType()));
                }
            });
        }

        enumValuesTableViewer.refresh(true);
    }

}
