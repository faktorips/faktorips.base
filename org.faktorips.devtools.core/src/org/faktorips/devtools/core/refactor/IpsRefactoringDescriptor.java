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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * An <tt>IpsRefactoringDescriptor</tt> can be used to start a Faktor-IPS refactoring head-less.
 * <p>
 * To do this, clients first must obtain the <tt>IpsRefactoringContribution</tt> identifying the
 * Faktor-IPS refactoring to be started. The following code shows how that is done:
 * </p>
 * 
 * <pre>
 * IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
 *         .getRefactoringContribution(contributionId);
 * </pre>
 * 
 * <p>
 * Clients should then call <tt>createDescriptor</tt> on the contribution and cast it to the
 * appropriate <tt>IpsRefactoringDescriptor</tt>. The methods <tt>setProject(String)</tt> and
 * <tt>setXXXArgument(...)</tt> provided by the descriptor are then used to configure the
 * refactoring, for example like this:
 * </p>
 * 
 * <pre>
 * RenameIpsElementDescriptor renameDescriptor = (RenameIpsElementDescriptor)contribution.createDescriptor();
 * renameDescriptor.setProject(ipsElement.getIpsProject().getName());
 * IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)ipsElement;
 * renameDescriptor.setTypeArgument(policyCmptTypeAttribute.getPolicyCmptType());
 * renameDescriptor.setPartArgument(policyCmptTypeAttribute);
 * </pre>
 * 
 * <p>
 * After that is done, the descriptor can be used to create a <tt>Refactoring</tt> instance by
 * invoking <tt>createRefactoring(IpsRefactoringDescriptor)</tt> on the
 * <tt>IpsRefactoringContribution</tt>.
 * 
 * @see IpsRefactoringContribution
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringDescriptor extends RefactoringDescriptor {

    /** A map containing all arguments configuring the refactoring. */
    private Map<String, String> arguments;

    /**
     * Creates an <tt>IpsRefactoringDescriptor</tt>.
     * 
     * @param contributionId The unique ID of the contributed Faktor-IPS refactoring.
     */
    protected IpsRefactoringDescriptor(String contributionId) {
        this(contributionId, null, "Faktor-IPS Refactoring.", null, RefactoringDescriptor.BREAKING_CHANGE
                | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
    }

    /**
     * Creates an <tt>IpsRefactoringDescriptor</tt>.
     * 
     * @param contributionId The unique ID of the contributed Faktor-IPS refactoring.
     * @param project The project the refactoring is working with.
     * @param description A description for the refactoring.
     * @param comment Any (optional) comment for the refactoring.
     * @param flags Refactoring flags as specified by <tt>RefactoringDescriptor</tt>.
     */
    protected IpsRefactoringDescriptor(String contributionId, String project, String description, String comment,
            int flags) {

        super(contributionId, project, description, comment, flags);
        arguments = new HashMap<String, String>();
    }

    /**
     * This operation initializes the descriptor by loading any necessary resources.
     * <p>
     * This operation needs not to be called by clients.
     * 
     * @throws CoreException If an error occurs while initializing.
     */
    public abstract void internalInit() throws CoreException;

    /** Grants subclasses access to the argument map. */
    protected final Map<String, String> getArguments() {
        return arguments;
    }

    @Override
    public final Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
                .getRefactoringContribution(getID());
        Refactoring refactoring = contribution.createRefactoring(this);
        return refactoring;
    }

}
