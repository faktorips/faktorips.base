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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A wizard to guide the user trough a Faktor-IPS move refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRefactoringWizard extends IpsRefactoringWizard {

    /**
     * Creates a <tt>MoveRefactoringWizard</tt>.
     * 
     * @param refactoring The refactoring used by the wizard.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public MoveRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, ipsElement, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/MoveAndRenameWizard.png"));
        setDefaultPageTitle(NLS.bind(Messages.MoveRefactoringWizard_title, getIpsElementName()));
    }

    @Override
    protected void addUserInputPages() {
        addPage(new MovePage(getIpsElement()));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

}
