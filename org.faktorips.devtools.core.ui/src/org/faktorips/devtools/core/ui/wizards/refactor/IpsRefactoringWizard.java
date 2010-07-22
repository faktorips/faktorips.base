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

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS refactoring wizards.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringWizard extends RefactoringWizard {

    /** The <tt>IIpsElement</tt> to be refactored. */
    private final IIpsElement ipsElement;

    /**
     * 
     * 
     * @param refactoring The refactoring used by the wizard.
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * @param flags Options for <tt>RefactoringWizard</tt>.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected IpsRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement, int flags) {
        super(refactoring, flags);
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;
        setChangeCreationCancelable(false);
    }

    /** Returns the name describing the <tt>IIpsElement</tt> to be refactored. */
    // TODO AW: This should be moved to the core model -> IIpsElement#getElementName().
    protected final String getIpsElementName() {
        String ipsElementName = "";
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.ElementNames_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.ElementNames_Method;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.ElementNames_Type;
        }
        return ipsElementName;
    }

    /** Returns the <tt>IIpsElement</tt> to be refactored. */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
    }

}
