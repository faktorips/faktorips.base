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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

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
 * appropriate <tt>IpsRefactoringDescriptor</tt>. Then the descriptor needs to be configured, for
 * example like this:
 * </p>
 * 
 * <pre>
 * RenameIpsElementDescriptor renameDescriptor = (RenameIpsElementDescriptor)contribution.createDescriptor();
 * renameDescriptor.setIpsElement(ipsElement);
 * </pre>
 * 
 * <p>
 * After that is done, the descriptor can be used to create a <tt>Refactoring</tt> instance by
 * invoking <tt>createRefactoring(RefactoringStatus)</tt> on the descriptor.
 * <p>
 * Note however, that it is much easier to create a refactoring instance configured for a specific
 * <tt>IIpsElement</tt> by just calling a getter method provided by <tt>IIpsElement</tt>, for
 * example <tt>getRenameRefactoring()</tt>.
 * 
 * @see IpsRefactoringContribution
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringDescriptor extends RefactoringDescriptor {

    /**
     * Creates an <tt>IpsRefactoringDescriptor</tt>.
     * 
     * @param contributionId The unique ID of the contributed Faktor-IPS refactoring.
     */
    protected IpsRefactoringDescriptor(String contributionId) {
        this(contributionId, null, Messages.IpsRefactoringDescriptor_noDescriptionAvailable, null,
                RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.STRUCTURAL_CHANGE
                        | RefactoringDescriptor.MULTI_CHANGE);
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
    }

}
