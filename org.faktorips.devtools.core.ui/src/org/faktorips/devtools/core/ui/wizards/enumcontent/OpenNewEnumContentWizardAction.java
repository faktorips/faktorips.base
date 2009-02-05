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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

/**
 * This action is responsible for opening the new enum content wizard.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class OpenNewEnumContentWizardAction extends OpenNewWizardAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public INewWizard createWizard() {
        return new NewEnumContentWizard();
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // Nothing to do
    }

}
