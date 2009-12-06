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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;

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

    /** Argument identifying the <tt>IType</tt>. */
    private static final String TYPE_ARGUMENT = "type";

    /** Argument identifying the <tt>IIpsObjectPart</tt>. */
    private static final String PART_ARGUMENT = "part";

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
     * Sets the value of the <tt>Type</tt> argument.
     * 
     * @param type The <tt>IType</tt> that is either to be renamed or contains the
     *            <tt>IIpsObjectPart</tt> to be renamed.
     */
    public void setTypeArgument(IType type) {
        getArguments().put(TYPE_ARGUMENT, type.getQualifiedName());
    }

    /**
     * Sets the value of the <tt>Part</tt> argument.
     * 
     * @param ipsObjectPart The <tt>IIpsObjectPart</tt> to be renamed.
     */
    public void setPartArgument(IIpsObjectPart ipsObjectPart) {
        getArguments().put(PART_ARGUMENT, ipsObjectPart.getName());
    }

    /** Returns the value of the <tt>Type</tt> argument. */
    public String getTypeArgument() {
        return getArguments().get(TYPE_ARGUMENT);
    }

    /** Returns the value of <tt>Part</tt> argument. */
    public String getPartArgument() {
        return getArguments().get(PART_ARGUMENT);
    }

    @Override
    public void internalInit() throws CoreException {
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(getProject());

        if (getID().equals(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE)) {
            ipsElement = ipsProject.findPolicyCmptType(getTypeArgument()).getPolicyCmptTypeAttribute(getPartArgument());

        } else if (getID().equals(IIpsRefactorings.RENAME_PRODUCT_CMPT_TYPE_ATTRIBUTE)) {
            ipsElement = ipsProject.findProductCmptType(getTypeArgument()).getProductCmptTypeAttribute(
                    getPartArgument());
        }
    }

    /**
     * Returns the <tt>IIpsElement</tt> to be renamed or <tt>null</tt> if the descriptor has not
     * been initialized yet.
     */
    public IIpsElement getIpsElement() {
        return ipsElement;
    }
}
