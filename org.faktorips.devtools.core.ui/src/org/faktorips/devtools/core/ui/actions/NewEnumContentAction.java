/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumcontent.OpenNewEnumContentWizardAction;

/**
 * Opens the wizard for creating a new {@link IEnumContent}.
 * 
 * @author Alexander Weickmann
 */
public class NewEnumContentAction extends Action {

    /** The file name of the image for this action */
    private final String IMAGE_FILENAME = "NewEnumContent.gif"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    /**
     * Creates a new <code>NewEnumContentAction</code>.
     */
    public NewEnumContentAction(IWorkbenchWindow window) {
        super();

        this.window = window;
        setText(Messages.NewEnumContentAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
    }

    @Override
    public void run() {
        OpenNewEnumContentWizardAction action = new OpenNewEnumContentWizardAction();
        action.init(window);
        action.run(this);
    }

}
