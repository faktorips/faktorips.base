/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumtype.OpenNewEnumTypeWizardAction;
import org.faktorips.devtools.model.enums.IEnumType;

/**
 * Opens the wizard for creating a new {@link IEnumType}.
 * 
 * @author Alexander Weickmann
 */
public class NewEnumTypeAction extends Action {

    /** The file name of the image for this action. */
    private static final String IMAGE_FILENAME = "NewEnumType.gif"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    /**
     * Creates a new <code>NewEnumTypeAction</code>.
     */
    public NewEnumTypeAction(IWorkbenchWindow window) {
        super();

        this.window = window;
        setText(Messages.NewEnumTypeAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
    }

    @Override
    public void run() {
        OpenNewEnumTypeWizardAction action = new OpenNewEnumTypeWizardAction();
        action.init(window);
        action.run(this);
    }

}
