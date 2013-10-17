/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

/**
 * An operation for creating:
 * <ul>
 * <li>a constraining association</li>
 * <li>the corresponding constraining association on product-/policy-side (IOW matching
 * association), in case the constrained association has a matching association.</li>
 * <li>the inverse association of a constraining association (policy side only), in case the
 * constrained association has an inverse association.</li>
 * </ul>
 * Only creates constraining, matching and inverse associations if required and if they have not
 * been created yet.
 * 
 * @author widmaier
 */
public class CreateConstrainingAssociationOperation {

    private final IType sourceType;
    private final IAssociation constrainedAssociation;
    private final IType targetType;

    /**
     * Asserts the given arguments and throws an {@link IllegalArgumentException} if types don't
     * match.
     * 
     * @param sourceType the type to create a constraining association in. Must be the same class as
     *            the targetType (both {@link IPolicyCmptType} or both {@link IProductCmptType}.
     * @param constrainedAssociation the association to be constrained. Must be an
     *            {@link IPolicyCmptTypeAssociation} if the given types are {@link IPolicyCmptType
     *            IPolicyCmptTypes}, and an {@link IProductCmptTypeAssociation} if the given types
     *            are {@link IProductCmptType IProductCmptTypes}
     * @param targetType the target type of the newly created constraining association. Must be a
     *            sub-type or the same type as the constrained association's target. Must be the
     *            same class as the sourceType (both {@link IPolicyCmptType} or both
     *            {@link IProductCmptType}.
     * @throws IllegalArgumentException if the argument's classes do not match as described above
     */
    public CreateConstrainingAssociationOperation(IType sourceType, IAssociation constrainedAssociation,
            IType targetType) {
        this.sourceType = sourceType;
        this.constrainedAssociation = constrainedAssociation;
        this.targetType = targetType;
        assertValidity();
    }

    private void assertValidity() {
        assertSameType();
        assertAssociationMatchesType();
        assertTargetSubtype();
    }

    private void assertSameType() {
        if (!getSourceType().getClass().equals(getTargetType().getClass())) {
            throw new IllegalArgumentException(
                    NLS.bind(
                            "The source type ({0}) and target type ({1}) must either be both IPolicyCmptTypes or both IProductCmptTypes", //$NON-NLS-1$
                            getSourceType().getClass(), getTargetType().getClass()));
        }
    }

    private void assertAssociationMatchesType() {
        IType typeContainingTheAssociation = getConstrainedAssociation().getType();
        if (!getSourceType().getClass().equals(typeContainingTheAssociation.getClass())) {
            throw new IllegalArgumentException(
                    NLS.bind(
                            "The source type ({0}) and association's type ({1}) must match (either IPolicyCmptType and IPolicyCmptTypeAssociation or IProductCmptType and IProductCmptTypeAssociation)", //$NON-NLS-1$
                            getSourceType().getClass(), typeContainingTheAssociation.getClass()));
        }
    }

    private void assertTargetSubtype() {
        if (!isTargetTypeValid()) {
            throw new IllegalArgumentException(
                    NLS.bind(
                            "The given target type ({0}) must be a subtype of (or same type as) the constrained association's target type ({1})", //$NON-NLS-1$
                            getTargetType(), getConstrainedTargetType()));
        }
    }

    private boolean isTargetTypeValid() {
        try {
            IType constrainedTargetType = getConstrainedTargetType();
            return getTargetType().isSubtypeOrSameType(constrainedTargetType, getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IType getConstrainedTargetType() {
        try {
            IType constrainedTargetType = getConstrainedAssociation().findTarget(getIpsProject());
            return constrainedTargetType;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Executes the operation to create the defined associations.
     */
    public void execute() {
        IAssociation constrainingAssociation = setUpConstrainingAssociation();
        setUpInverseAssociationIfApplicable(constrainingAssociation);
        IAssociation matchingAssociation = setupMatchingAssociationIfPossible();
        setUpInverseAssociationIfApplicable(matchingAssociation);
    }

    private IAssociation setUpConstrainingAssociation() {
        return getSourceType().constrainAssociation(getConstrainedAssociation(), getTargetType());
    }

    private void setUpInverseAssociationIfApplicable(IAssociation constrainingAssociation) {
        if (isCreateInverseApplicable(constrainingAssociation)) {
            IPolicyCmptTypeAssociation assoc = (IPolicyCmptTypeAssociation)constrainingAssociation;
            IPolicyCmptTypeAssociation constrainedAssoc = (IPolicyCmptTypeAssociation)assoc
                    .findConstrainedAssociation(getIpsProject());
            if (isInverseReqired(constrainedAssoc)) {
                createInverseAssociation(assoc, constrainedAssoc);
            }
        }
    }

    private boolean isCreateInverseApplicable(IAssociation constrainingAssociation) {
        return constrainingAssociation instanceof IPolicyCmptTypeAssociation;
    }

    private boolean isInverseReqired(IPolicyCmptTypeAssociation constrainedAssoc) {
        return constrainedAssoc.hasInverseAssociation();
    }

    private void createInverseAssociation(IPolicyCmptTypeAssociation assoc, IPolicyCmptTypeAssociation constrainedAssoc) {
        try {
            IPolicyCmptTypeAssociation templateInverseAssociation = constrainedAssoc
                    .findInverseAssociation(getIpsProject());
            IPolicyCmptType targetType = (IPolicyCmptType)assoc.findTarget(getIpsProject());
            targetType.constrainAssociation(templateInverseAssociation, assoc.getType());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IAssociation setupMatchingAssociationIfPossible() {
        IType matchingSourceType = findMatchingType(getSourceType());
        IType matchingTargetType = findMatchingType(getTargetType());
        if (matchingSourceType != null && matchingTargetType != null) {
            return createMatchingAssociation(matchingSourceType, matchingTargetType);
        }
        return null;
    }

    private IAssociation createMatchingAssociation(IType matchingSourceType, IType matchingTargetType) {
        if (isMatchingAssociationRequired()) {
            IAssociation constrainedMatchingAssociation = getConstrainedMatchingAssociation();
            return matchingSourceType.constrainAssociation(constrainedMatchingAssociation, matchingTargetType);
        }
        return null;
    }

    private IType findMatchingType(IType type) {
        try {
            if (type instanceof IProductCmptType) {
                IProductCmptType productCmptType = (IProductCmptType)type;
                return productCmptType.findPolicyCmptType(getIpsProject());
            } else if (type instanceof IPolicyCmptType) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)type;
                return policyCmptType.findProductCmptType(getIpsProject());
            } else {
                return null;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isMatchingAssociationRequired() {
        return getConstrainedMatchingAssociation() != null;
    }

    private IAssociation getConstrainedMatchingAssociation() {
        try {
            return getConstrainedAssociation().findMatchingAssociation();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public IAssociation getConstrainedAssociation() {
        return constrainedAssociation;
    }

    public IType getSourceType() {
        return sourceType;
    }

    public IType getTargetType() {
        return targetType;
    }

    private IIpsProject getIpsProject() {
        return getSourceType().getIpsProject();
    }
}
