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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumcontent.OpenNewEnumContentWizardAction;

/**
 * Opens the wizard for creating a new enum content.
 * 
 * @author Alexander Weickmann
 */
public class NewEnumContentAction extends Action {

    // The image for the action
    private final String IMAGE = "NewEnumContent.gif"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    /**
     * Creates a new <code>NewEnumContentAction</code>.
     * 
     * @param window
     */
    public NewEnumContentAction(IWorkbenchWindow window) {
        super();

        this.window = window;
        setText(Messages.NewEnumContentAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        OpenNewEnumContentWizardAction action = new OpenNewEnumContentWizardAction();
        action.init(window);
        action.run(this);
    }

}
