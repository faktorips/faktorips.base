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
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A wizard to guide the user trough a Faktor-IPS rename refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringWizard extends RefactoringWizard {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /**
     * Creates a <tt>RenameRefactoringWizard</tt>.
     * 
     * @param refactoring The refactoring used by the wizard.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public RenameRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/RenameWizard.png"));
        setChangeCreationCancelable(false);
        this.ipsElement = ipsElement;

        String ipsElementName = "";
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.RenameRefactoringWizard_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.RenameRefactoringWizard_Method;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.RenameRefactoringWizard_Type;
        }
        setDefaultPageTitle(NLS.bind(Messages.RenameRefactoringWizard_title, ipsElementName));
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenamePage(ipsElement));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

}
