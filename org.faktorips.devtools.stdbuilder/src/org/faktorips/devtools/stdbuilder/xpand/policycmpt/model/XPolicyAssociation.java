/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.util.StringUtil;

/**
 * Generator model class representing a policy association.
 * 
 * @author widmaier
 */
public class XPolicyAssociation extends XAssociation {

    public XPolicyAssociation(IPolicyCmptTypeAssociation ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    @Override
    public IPolicyCmptTypeAssociation getAssociation() {
        return (IPolicyCmptTypeAssociation)super.getAssociation();
    }

    public Set<XDetailToMasterDerivedUnionAssociation> getSubsettedDetailToMasterAssociations() {
        return getSubsettedDetailToMasterAssociationsInternal(new HashSet<String>(), getTypeOfAssociation());
    }

    private Set<XDetailToMasterDerivedUnionAssociation> getSubsettedDetailToMasterAssociationsInternal(Set<String> resultingNames,
            IType currentType) {
        LinkedHashSet<XDetailToMasterDerivedUnionAssociation> resultingAssociations = new LinkedHashSet<XDetailToMasterDerivedUnionAssociation>();
        try {
            if (isCompositionDetailToMaster()) {
                if (isSharedAssociation()) {
                    if (!isSharedAssociationImplementedInSuperclass()) {
                        IPolicyCmptTypeAssociation sharedAssociationHost = getAssociation().findSharedAssociationHost(
                                getIpsProject());
                        resultingAssociations.add(getModelNode(sharedAssociationHost,
                                XDetailToMasterDerivedUnionAssociation.class));
                        resultingNames.add(sharedAssociationHost.getName());
                    }
                } else {
                    XPolicyAssociation masterToDetailAssociation = getInverseAssociation();
                    if (masterToDetailAssociation.isSubsetOfADerivedUnion()) {
                        XDerivedUnionAssociation subsettedDerivedUnion = masterToDetailAssociation
                                .getSubsettedDerivedUnion();
                        XPolicyAssociation derivedUnionAssociation = getModelNode(
                                subsettedDerivedUnion.getAssociation(), XPolicyAssociation.class);
                        // it is possible that the derived union does not specify a inverse but
                        // subset does
                        if (derivedUnionAssociation.hasInverseAssociation()) {
                            XPolicyAssociation detailToMasterDerivedUnion = derivedUnionAssociation
                                    .getInverseAssociation();
                            if (!resultingNames.contains(detailToMasterDerivedUnion.getName())) {
                                XPolicyAssociation superAssociationWithSameName = detailToMasterDerivedUnion
                                        .getSuperAssociationWithSameName();
                                if (superAssociationWithSameName == null) {
                                    XDetailToMasterDerivedUnionAssociation detailToMasterDerivedUnionAssociation = getModelNode(
                                            detailToMasterDerivedUnion.getAssociation(),
                                            XDetailToMasterDerivedUnionAssociation.class);
                                    resultingAssociations.add(detailToMasterDerivedUnionAssociation);
                                    resultingNames.add(detailToMasterDerivedUnion.getName());
                                }
                                if (superAssociationWithSameName != null
                                        || derivedUnionAssociation.isSubsetOfADerivedUnion()) {
                                    resultingAssociations
                                            .addAll(detailToMasterDerivedUnion
                                                    .getSubsettedDetailToMasterAssociationsInternal(resultingNames,
                                                            currentType));
                                }
                            }
                        }
                    }
                }
                // This part handles the case that there is a derived union with the same name in
                // super class that is not already part of the result.
                if (currentType == getTypeOfAssociation() && !isSharedAssociation()
                        && !resultingNames.contains(getName())) {
                    XPolicyAssociation superAssociationWithSameName = getSuperAssociationWithSameName();
                    if (superAssociationWithSameName != null) {
                        XPolicyAssociation inverseOfSuperAssociation = superAssociationWithSameName
                                .getInverseAssociation();
                        if (inverseOfSuperAssociation == null) {
                            throw new RuntimeException("Cannot find inverse association of "
                                    + superAssociationWithSameName);
                        }
                        if (inverseOfSuperAssociation.isDerived()) {
                            resultingAssociations.add(getModelNode(superAssociationWithSameName.getAssociation(),
                                    XDetailToMasterDerivedUnionAssociation.class));
                            resultingNames.add(superAssociationWithSameName.getName());
                        }
                    }
                }

            }
            return resultingAssociations;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * This method returns the association found by
     * {@link IPolicyCmptTypeAssociation#findSuperAssociationWithSameName(org.faktorips.devtools.core.model.ipsproject.IIpsProject)}
     * <p>
     * Normally we want to throw an exception if there is not such an association. In this case we
     * simply return null because testing would be with equal low performance as this method itself.
     * 
     */
    protected XPolicyAssociation getSuperAssociationWithSameName() {
        IPolicyCmptTypeAssociation superAssociationWithSameName = (IPolicyCmptTypeAssociation)getAssociation()
                .findSuperAssociationWithSameName(getIpsProject());
        if (superAssociationWithSameName != null) {
            return getModelNode(superAssociationWithSameName, XPolicyAssociation.class);
        } else {
            return null;
        }
    }

    public boolean hasSuperAssociationWithSameName() {
        return getSuperAssociationWithSameName() != null;
    }

    /**
     * Returns true if this association is a derived union or an inverse of a derived union
     * association.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean isDerived() {
        if (super.isDerived()) {
            return true;
        }
        if (isCompositionDetailToMaster() && !isSharedAssociation()) {
            return getInverseAssociation().isDerivedUnion();
        }
        return false;
    }

    public boolean isQualified() {
        return getAssociation().isQualified();
    }

    /**
     * This method returns true if the maximum cardinality is greater than one also if it is a
     * qualified association.
     * <p>
     * Normally the method isOneToMany returns true for qualified associations which maximum
     * cardinality is one. @see {@link IAssociation#is1ToMany()}
     * 
     * @return True if the maximum cardinality is greater than one
     */
    public boolean isOneToManyIgnoringQualifier() {
        return getAssociation().is1ToManyIgnoringQualifier();
    }

    /**
     * Returns <code>true</code> for:
     * <ul>
     * <li>oneToMany associations (but not derived unions, see below)</li>
     * <li>oneToOne associations (but <code>false</code> for onToOne-detailToMaster associations)</li>
     * </ul>
     * Returns <code>false</code> for
     * <ul>
     * <li>shared associations</li>
     * </ul>
     */
    public boolean isGenerateField() {
        if (isDerived() || isConstrain()) {
            return false;
        } else {
            return !isSharedAssociationImplementedInSuperclass();
        }
    }

    /**
     * Returns true if a <em>NORMAL</em> getter needs to be generated for this association. Maybe an
     * getter for the derived union is still generated, depending on the list of derived union
     * associations get from {@link XPolicyCmptClass#getSubsettedDerivedUnions()} or
     * {@link XPolicyCmptClass#getDetailToMasterDerivedUnionAssociations()}
     */
    public boolean isGenerateGetter() {
        if (isDerived() || isSharedAssociation()) {
            return false;
        } else if (isCompositionDetailToMaster()) {
            return !hasSuperAssociationWithSameName();
        } else {
            return true;
        }
    }

    @Override
    public boolean isGenerateAbstractGetter(boolean generatingInterface) {
        return super.isGenerateAbstractGetter(generatingInterface)
                && (!isCompositionDetailToMaster() || !hasSuperAssociationWithSameName());
    }

    @Override
    protected boolean isSubsetImplementedInSameType(XType currentContextType) {
        if (isCompositionDetailToMaster()) {
            XDetailToMasterDerivedUnionAssociation derivedUnionAssociation = getModelNode(getAssociation(),
                    XDetailToMasterDerivedUnionAssociation.class);
            Set<XPolicyAssociation> subsetAssociations = derivedUnionAssociation
                    .getDetailToMasterSubsetAssociations((XPolicyCmptClass)currentContextType);
            return !subsetAssociations.isEmpty();
        } else {
            return super.isSubsetImplementedInSameType(currentContextType);
        }
    }

    /**
     * Returns true if a qualified getter needs to be generated for this association.
     */
    public boolean isGenerateQualifiedGetter() {
        return isOneToMany() && isQualified();
    }

    /**
     * Returns true if a setter needs to be generated for this association.
     */
    public boolean isGenerateSetter() {
        return !isOneToMany() && !isDerived();
    }

    /**
     * Returns true if a add method needs to be generated for this association
     */
    public boolean isGenerateAddAndRemoveMethod() {
        return isOneToMany() && !isDerived();
    }

    public boolean isConsiderInDeltaComputation() {
        return isMasterToDetail() && !isDerived() && !isConstrain();
    }

    public boolean isConsiderInEffectiveFromHasChanged() {
        return isMasterToDetail() && !isDerived() && ((XPolicyCmptClass)getTargetModelNode()).isConfigured()
                && !isConstrain();
    }

    public boolean isConsiderInCreateChildFromXML() {
        return isMasterToDetail() && !isDerivedUnion() && !isConstrain();
    }

    public boolean isConsiderInVisitorSupport() {
        return isMasterToDetail() && !isDerivedUnion() && !isConstrain();
    }

    public boolean isConsiderInCreateCreateUnresolvedReference() {
        return isTypeAssociation() && !isConstrain();
    }

    public boolean isSetInverseAssociationInCopySupport() {
        return isMasterToDetail() && hasInverseAssociation();
    }

    public boolean isConsiderInCopySupport() {
        return !isCompositionDetailToMaster() && !isDerived() && !isConstrain();
    }

    public boolean isConsiderInValidateDependents() {
        return isMasterToDetail() && !getAssociation().isSubsetOfADerivedUnion() && !isConstrain();
    }

    public boolean isGenerateNewChildMethods() {
        return isMasterToDetail() && !getTargetPolicyCmptClass().isAbstract() && !isDerivedUnion();
    }

    public boolean isNeedOverrideForConstrainNewChildMethod() {
        if (isConstrain()) {
            if (getSuperAssociationWithSameName().isGenerateNewChildMethods()) {
                return true;
            } else {
                return getSuperAssociationWithSameName().isNeedOverrideForConstrainNewChildMethod();
            }
        } else {
            return false;
        }
    }

    public boolean isGenerateNewChildWithArgumentsMethod() {
        return getTargetPolicyCmptClass().isConfigured();
    }

    /**
     * Returns true if this association is a detail-to-master composition that is directly
     * implemented. Not implemented associations are for example derived associations and shared
     * associations that are already implemented in super class.
     */
    public boolean isImplementedDetailToMasterAssociation() {
        return isCompositionDetailToMaster() && isGenerateField();
    }

    public String getConstantNamePropertyName() {
        String constName = getName(isOneToMany());
        if (isGenerateSeparatedCamelCase()) {
            constName = StringUtil.camelCaseToUnderscore(constName, false);
        }
        return "ASSOCIATION_" + constName.toUpperCase();
    }

    public String getConstantNameMaxCardinalityFor() {
        String constName = getName();
        if (isGenerateSeparatedCamelCase()) {
            constName = StringUtil.camelCaseToUnderscore(constName, false);
        }
        return "MAX_MULTIPLICITY_OF_" + constName.toUpperCase();
    }

    public String getOldValueVariable() {
        return "old" + StringUtils.capitalize(getName());
    }

    /**
     * Returns the method name for internal setters.
     */
    public String getMethodNameSetOrAddInternal() {
        return getMethodNameSetOrAdd() + "Internal";
    }

    /**
     * Returns "new" plus the capitalized association name.
     */
    public String getMethodNameNew() {
        return "new" + StringUtils.capitalize(getName(false));
    }

    public String getMethodNameRemove() {
        return "remove" + StringUtils.capitalize(getName(false));
    }

    /**
     * Returns the model node for the inverse association represented by this association node.
     * 
     * @throws NullPointerException if this association has no inverse association.
     */
    public XPolicyAssociation getInverseAssociation() {
        try {
            IPolicyCmptTypeAssociation inverseAssoc = getAssociation().findInverseAssociation(getIpsProject());
            if (inverseAssoc != null) {
                return getModelNode(inverseAssoc, XPolicyAssociation.class);
            } else {
                throw new RuntimeException(NLS.bind("PolicyCmptTypeAssociation {0} has no inverse association.",
                        getAssociation()));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if an inverse association is defined or if this association is a
     * valid composition ( {@link #isMasterToDetail()} )
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForRemove() {
        // TODO FIPS-1141 correct but not in old code generator:
        // return (isMasterToDetail() || isTypeAssociation()) && hasInverseAssociation();
        try {
            return (hasInverseAssociation() || getAssociation().isComposition()
                    && ((IPolicyCmptType)getTargetType()).isDependantType());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if an inverse association is defined and this association is a
     * valid composition ( {@link #isMasterToDetail()} ) at the same time.
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForAdd() {
        return isMasterToDetail() && hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if an inverse association is defined and this association is a
     * valid composition ( {@link #isMasterToDetail()} ) at the same time.
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForSet() {
        return isMasterToDetail() && hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if this association is an association (neither composition nor
     * aggregation) and an inverse association is defined. <code>false</code> otherwise.
     */
    public boolean isGenerateCodeToSynchronizeInverseAssociation() {
        return isTypeAssociation() && hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if we need to generate internal setter or adder methods.
     * 
     */
    public boolean isGenerateInternalSetterOrAdder() {
        return isGenerateChangeSupport();
    }

    public boolean isTypeConfigurableByProductCmptType() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    private IPolicyCmptType getPolicyCmptType() {
        return getAssociation().getPolicyCmptType();
    }

    /**
     * /** Returns "new" plus the capitalized association name.
     * 
     * @See {@link #getMethodNameNew()}
     */
    public String getVariableNameNewInstance() {
        return getMethodNameNew();
    }

    public String getTargetProductCmptVariableName() {
        return StringUtils.uncapitalize(getTargetProductCmptInterfaceName());
    }

    public String getTargetProductCmptInterfaceName() {
        return getTargetProductCmptInterfaceName(this);
    }

    /**
     * Returns the interface name of the target product component of the matching association and in
     * case of this is a constraining association it searches for the constrained (base) association
     * and returns the interface name of this target product component.
     */
    public String getTargetProductCmptInterfaceNameBase() {
        if (isConstrain()) {
            XPolicyAssociation constrainedAssociation = getConstrainedAssociation();
            return getTargetProductCmptInterfaceName(constrainedAssociation);
        } else {
            return getTargetProductCmptInterfaceName();
        }
    }

    private String getTargetProductCmptInterfaceName(XPolicyAssociation xAssociation) {
        XPolicyCmptClass xPolicyCmptClass = xAssociation.getTargetPolicyCmptClass();
        return xPolicyCmptClass.getProductCmptClassName();
    }

    public String getMethodNameCreatePolicyCmptForTargetProductCmpt() {
        XPolicyCmptClass xPolicyCmptClass = getTargetPolicyCmptClass();
        return "create" + xPolicyCmptClass.getImplClassName();
    }

    /**
     * Returns the generator model node of the target.
     * 
     */
    public XPolicyCmptClass getTargetPolicyCmptClass() {
        try {
            IPolicyCmptType polCmptType = getAssociation().findTargetPolicyCmptType(getIpsProject());
            return getModelNode(polCmptType, XPolicyCmptClass.class);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isProductRelevant() {
        try {
            return getAssociation().isConstrainedByProductStructure(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isComposition() {
        return getAssociation().isComposition();
    }

    public boolean isSharedAssociation() {
        return getAssociation().isSharedAssociation();
    }

    /**
     * Returns true if this is a shared association that is already implemented in super class. If
     * this is no shared association this method always returns false.
     * 
     * @return True if this is a shared association and it is already implemented in a super class.
     *         Returns false if it is no shared association or it is not already implement
     */
    public boolean isSharedAssociationImplementedInSuperclass() {
        if (isSharedAssociation()) {
            XPolicyAssociation superAssociationNode = getSuperAssociationWithSameName();
            return !superAssociationNode.isDerived();
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> if this association is the inverse of a master to detail
     * composition. IOW it is a detail to master association.
     */
    public boolean isInverseComposition() {
        return isCompositionDetailToMaster();
    }

    public boolean isCompositionDetailToMaster() {
        return getAssociation().isCompositionDetailToMaster();
    }

    /**
     * Returns whether or not the type of this association is "association" (and not composition or
     * aggregation).
     */
    public boolean isTypeAssociation() {
        return getAssociation().isAssoziation();
    }

    public boolean hasInverseAssociation() {
        try {
            return getAssociation().findInverseAssociation(getIpsProject()) != null;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Name of a local variable used inside a loop to store the associated policy instance. e.g.
     * "baseCoverageLocalVar".
     */
    public String getCreateChildFromXMLLocalVarName() {
        return getJavaNamingConvention().getMemberVarName(getName()) + "LocalVar";
    }

    /**
     * Returns the uncapitalized singular role-name. Use inside a loop to store the associated
     * policy instance. e.g. "baseCoverage".
     */
    public String getVisitorSupportLoopVarName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    /**
     * Returns uncapitalized target interface name, e.g. "iCoverage".
     */
    public String getCopySupportLoopVarNameInternal() {
        return StringUtils.uncapitalize(getTargetInterfaceName());
    }

    /**
     * Returns uncapitalized target class name, e.g. "coverage".
     */
    public String getCopySupportLoopVarName() {
        return StringUtils.uncapitalize(getTargetClassName());
    }

    /**
     * Returns "copy"+the target className, e.g. "copyCoverage".
     */
    public String getCopySupportCopyVarName() {
        return "copy" + getTargetClassName();
    }

    @Override
    protected XPolicyAssociation getConstrainedAssociation() {
        return (XPolicyAssociation)super.getConstrainedAssociation();
    }

    /**
     * TODO Workaround for old code generator FIPS-1143. @see {@link #getMethodNameGetter()}
     */
    @Override
    public String getMethodNameGetSingle() {
        if (isOneToMany()) {
            return super.getMethodNameGetSingle();
        } else {
            return getMethodNameGetter();
        }
    }

    /**
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143. One-To-One Getters are
     * generated without capitalized names.
     */
    @Override
    public String getMethodNameGetter() {
        if (!isOneToMany()) {
            return "get" + getName(false);
        } else {
            return super.getMethodNameGetter();
        }
    }

}
