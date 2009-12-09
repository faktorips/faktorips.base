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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.faktorips.devtools.core.internal.model.pctype.refactor.RenamePolicyCmptTypeAttributeProcessor;
import org.faktorips.devtools.core.internal.model.productcmpttype.refactor.RenameProductCmptTypeAttributeProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.ArgumentCheck;

/**
 * The <tt>RenameIpsElementDescriptor</tt> is used to configure the Faktor-IPS rename refactorings.
 * <p>
 * There are two arguments to be set by using the corresponding setters: <tt>Type</tt> and
 * <tt>Part</tt>.
 * <p>
 * <tt>Type</tt> identifies the <tt>IType</tt> that is either renamed itself or contains the
 * <tt>IIpsObjectPart</tt> to be renamed (identified by the <tt>Part</tt> argument).
 * 
 * @see IType
 * @see IIpsObjectPart
 * 
 * @author Alexander Weickmann
 */
public final class RenameIpsElementDescriptor extends IpsRefactoringDescriptor {

    /** The <tt>IIpsElement</tt> to be renamed. */
    private IIpsElement ipsElement;

    /**
     * Creates a <tt>RenameIpsElementDescriptor</tt>.
     * 
     * @param contributionId The unique ID of the refactoring contribution.
     */
    public RenameIpsElementDescriptor(String contributionId) {
        super(contributionId);
    }

    /**
     * Creates a <tt>RenameIpsElementDescriptor</tt>.
     * 
     * @param contributionId The unique ID of the refactoring contribution.
     * @param project The project the refactoring is working with.
     * @param description A description for the refactoring.
     * @param comment Any (optional) comment for the refactoring.
     * @param flags Refactoring flags as specified by <tt>RefactoringDescriptor</tt>.
     */
    public RenameIpsElementDescriptor(String contributionId, String project, String description, String comment,
            int flags) {

        super(contributionId, project, description, comment, flags);
    }

    /**
     * Sets the <tt>IIpsElement</tt> to be renamed.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    public void setIpsElement(IIpsElement ipsElement) {
        ArgumentCheck.notNull(ipsElement);
        this.ipsElement = ipsElement;
    }

    /** Returns the <tt>IIpsElement</tt> to be renamed or <tt>null</tt> if it has not been set. */
    public IIpsElement getIpsElement() {
        return ipsElement;
    }

    @Override
    public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        RenameProcessor renameProcessor;
        if (ipsElement instanceof IPolicyCmptTypeAttribute) {
            renameProcessor = new RenamePolicyCmptTypeAttributeProcessor((IPolicyCmptTypeAttribute)ipsElement);
        } else if (ipsElement instanceof IProductCmptTypeAttribute) {
            renameProcessor = new RenameProductCmptTypeAttributeProcessor((IProductCmptTypeAttribute)ipsElement);
        } else {
            throw new RuntimeException("The IPS element " + ipsElement + " does not support the rename refactoring.");
        }
        return new RenameRefactoring(renameProcessor);
    }

}
