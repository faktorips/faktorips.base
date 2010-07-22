/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;

public class PullUpRefactoringWizard extends IpsRefactoringWizard {

    public PullUpRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, ipsElement, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new PullUpPage(getIpsElement()));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    private static class PullUpPage extends IpsRefactoringUserInputPage {

        PullUpPage(IIpsElement ipsElement) {
            super(ipsElement, "PullUpPage");
        }

        @Override
        public void createControl(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);
        }

        @Override
        protected void setPromptMessage() {

        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {

        }

    }

}
