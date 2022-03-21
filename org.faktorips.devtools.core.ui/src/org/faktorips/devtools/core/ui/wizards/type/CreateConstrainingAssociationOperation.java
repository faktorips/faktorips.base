/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

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
     * @throws IllegalArgumentException if the argument's classes do not match as described above or
     *             if the arguments are <code>null</code>
     */
    public CreateConstrainingAssociationOperation(IType sourceType, IAssociation constrainedAssociation,
            IType targetType) {
        this.sourceType = sourceType;
        this.constrainedAssociation = constrainedAssociation;
        this.targetType = targetType;
        assertValidity();
    }

    private void assertValidity() {
        assertNotNull();
        assertSameType();
        assertAssociationMatchesType();
        assertTargetSubtype();
    }

    private void assertNotNull() {
        assertNotNull(getSourceType());
        assertNotNull(getConstrainedAssociation());
        assertNotNull(getTargetType());
    }

    private void assertNotNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException(Messages.CreateConstrainingAssociationOperation_argumentsMustNotBeNull);
        }
    }

    private void assertSameType() {
        if (!getSourceType().getClass().equals(getTargetType().getClass())) {
            throw new IllegalArgumentException(
                    NLS.bind(Messages.CreateConstrainingAssociationOperation_sourceAndTargetTypeMustBeOfSameClass,
                            getSourceType().getClass(), getTargetType().getClass()));
        }
    }

    private void assertAssociationMatchesType() {
        IType typeContainingTheAssociation = getConstrainedAssociation().getType();
        if (!getSourceType().getClass().equals(typeContainingTheAssociation.getClass())) {
            throw new IllegalArgumentException(
                    NLS.bind(Messages.CreateConstrainingAssociationOperation_sourceTypeAndAssociationClassMustMatch,
                            getSourceType().getClass(), typeContainingTheAssociation.getClass()));
        }
    }

    private void assertTargetSubtype() {
        if (!isTargetTypeValid()) {
            throw new IllegalArgumentException(NLS.bind(
                    Messages.CreateConstrainingAssociationOperation_targetTypeMustBeSubclassOfTheConstrainedAssociationTarget,
                    getTargetType().getName(), getConstrainedTargetType().getName()));
        }
    }

    private boolean isTargetTypeValid() {
        IType constrainedTargetType = getConstrainedTargetType();
        return getTargetType().isSubtypeOrSameType(constrainedTargetType, getIpsProject());
    }

    private IType getConstrainedTargetType() {
        IType constrainedTargetType = getConstrainedAssociation().findTarget(getIpsProject());
        return constrainedTargetType;
    }

    /**
     * Executes the operation to create the defined associations.
     */
    public void execute() {
        IAssociation constrainingAssociation = setUpConstrainingAssociation();
        setUpInverseAssociationIfApplicable(constrainingAssociation);
        IAssociation matchingAssociation = setupMatchingAssociationIfPossible(constrainingAssociation);
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
            if (isInverseRequired(constrainedAssoc)) {
                createInverseAssociation(assoc, constrainedAssoc);
            }
        }
    }

    private boolean isCreateInverseApplicable(IAssociation constrainingAssociation) {
        return constrainingAssociation instanceof IPolicyCmptTypeAssociation;
    }

    private boolean isInverseRequired(IPolicyCmptTypeAssociation constrainedAssoc) {
        return constrainedAssoc != null && constrainedAssoc.hasInverseAssociation();
    }

    private void createInverseAssociation(IPolicyCmptTypeAssociation assoc,
            IPolicyCmptTypeAssociation constrainedAssoc) {
        IPolicyCmptTypeAssociation templateInverseAssociation = constrainedAssoc
                .findInverseAssociation(getIpsProject());
        IPolicyCmptType inverseAssocSourceType = (IPolicyCmptType)assoc.findTarget(getIpsProject());
        boolean isTargetSrcFileDirty = inverseAssocSourceType.getIpsSrcFile().isDirty();
        inverseAssocSourceType.constrainAssociation(templateInverseAssociation, assoc.getType());
        saveIfNecessary(inverseAssocSourceType, isTargetSrcFileDirty);
    }

    private IAssociation setupMatchingAssociationIfPossible(IAssociation constrainingAssociation) {
        IType matchingSourceType = findMatchingType(getSourceType());
        IType matchingTargetType = findMatchingType(getTargetType());
        if (matchingSourceType != null && matchingTargetType != null) {
            return createMatchingAssociation(constrainingAssociation, matchingSourceType, matchingTargetType);
        }
        return null;
    }

    private IAssociation createMatchingAssociation(IAssociation constrainingAssociation,
            IType matchingSourceType,
            IType matchingTargetType) {
        if (isMatchingAssociationRequired(constrainingAssociation)) {
            IAssociation constrainedMatchingAssociation = getConstrainedMatchingAssociation();
            boolean isMatchingSourceSrcFileDirty = matchingSourceType.getIpsSrcFile().isDirty();
            IAssociation createdConstrainAssociation = matchingSourceType
                    .constrainAssociation(constrainedMatchingAssociation, matchingTargetType);
            saveIfNecessary(matchingSourceType, isMatchingSourceSrcFileDirty);
            return createdConstrainAssociation;
        }
        return null;
    }

    private void saveIfNecessary(IType type, boolean wasDirty) {
        if (!wasDirty) {
            type.getIpsSrcFile().save(true, null);
        }
    }

    private IType findMatchingType(IType type) {
        if (type instanceof IProductCmptType) {
            IProductCmptType productCmptType = (IProductCmptType)type;
            return productCmptType.findPolicyCmptType(getIpsProject());
        } else if (type instanceof IPolicyCmptType) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)type;
            return policyCmptType.findProductCmptType(getIpsProject());
        } else {
            return null;
        }
    }

    private boolean isMatchingAssociationRequired(IAssociation constrainingAssociation) {
        return getConstrainedMatchingAssociation() != null && constrainingAssociation.findMatchingAssociation() == null;
    }

    private IAssociation getConstrainedMatchingAssociation() {
        return getConstrainedAssociation().findMatchingAssociation();
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
