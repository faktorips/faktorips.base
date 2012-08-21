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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;

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

    public boolean isGenerateField() {
        return isOneToMany() && !isDerivedUnion() || !isOneToMany() && !isDerivedUnion()
                && !isCompositionDetailToMaster();
    }

    public boolean isGenerateGetter() {
        if (isSharedAssociation()) {
            try {
                IPolicyCmptTypeAssociation associationWithSameName = getAssociation().findSuperAssociationWithSameName(
                        getIpsProject());
                return (associationWithSameName == null || !associationWithSameName.isSharedAssociation());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            return true;
        }
    }

    public boolean isConsiderInDeltaComputation() {
        return isValidMasterToDetail() && !isDerived();
    }

    public boolean isConsiderInEffectiveFromHasChanged() {
        return isValidMasterToDetail() && !isDerivedUnion();
    }

    public boolean isConsiderInCreateChildFromXML() {
        return isValidMasterToDetail() && !isDerivedUnion();
    }

    public boolean isConsiderInVisitorSupport() {
        return isValidMasterToDetail() && !isDerivedUnion();
    }

    public boolean isConsiderInCreateCreateUnresolvedReference() {
        return isValid() && isTypeAssociation();
    }

    public boolean isSetInverseAssociationInCopySupport() {
        return isValidComposition() && hasInverseAssociation();
    }

    private boolean isValidMasterToDetail() {
        return isValid() && isCompositionMasterToDetail();
    }

    public boolean isConsiderInCopySupport() {
        return isValid() && !isDerived() && !isCompositionDetailToMaster();
    }

    public boolean isGenerateNewChildMethods() {
        return isCompositionMasterToDetail() && !getTargetPolicyCmptClass().isAbstract();
    }

    public boolean isGenerateNewChildWithArgumentsMethod() {
        return getTargetPolicyCmptClass().isConfigured();
    }

    public String getConstantNamePropertyName() {
        return "ASSOCIATION_" + getFieldName().toUpperCase();
    }

    public String getOldValueVariable() {
        return "old" + StringUtils.capitalize(getName());
    }

    /**
     * If this is a detail to master association the
     * {@link #getMethodNameSetInternalUncapitalized()} is used as a result.
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     */
    public String getMethodNameSetInternal() {
        if (isCompositionDetailToMaster()) {
            return getMethodNameSetInternalUncapitalized();
        } else {
            return getMethodNameSetter(false) + "Internal";
        }
    }

    public String getMethodNameAddInternal() {
        return getMethodNameAdd() + "Internal";
    }

    protected String getMethodNameSetter(boolean toMany) {
        return getJavaNamingConvention().getSetterMethodName(getName(toMany));
    }

    public String getMethodNameNew() {
        return "new" + StringUtils.capitalize(getName(false));
    }

    public String getMethodNameRemove() {
        return "remove" + StringUtils.capitalize(getName(false));
    }

    public String getMethodNameInverseAssociationGet() {
        return getJavaNamingConvention().getGetterMethodName(getInverseAssociationName());
    }

    /**
     * Returns then name of the set method that is used to set this associations policy instance as
     * target (e.g. parent) of the target associations. The old implementation however does not
     * capitalize the role name until now erroneously. e.g. setpolicyPart() instead of
     * setPolicyPart() (if the role name is policyPart).
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     * 
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationSet() {
        // should be return
        // getJavaNamingConvention().getSetterMethodName(getInverseAssociationName());
        return "set" + getInverseAssociationName();
    }

    /**
     * Returns then name of the setInternal method that is used to set this associations policy
     * instance as target (e.g. parent) of the target associations. The old implementation however
     * does not capitalize the role name until now erroneously. e.g. setpolicyPartInternal() instead
     * of setPolicyPartInternal() (if the role name is policyPart).
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     * 
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationSetInternal() {
        return getMethodNameInverseAssociationSet() + "Internal";
    }

    /**
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationContains() {
        return "contains" + StringUtils.capitalize(getInverseAssociationName());
    }

    /**
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationAdd() {
        return "add" + StringUtils.capitalize(getInverseAssociationName());
    }

    /**
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationRemove() {
        return "remove" + StringUtils.capitalize(getInverseAssociationName());
    }

    /**
     * Returns the inverse association's name.
     * 
     * @throws NullPointerException if no inverse association exists.
     */
    protected String getInverseAssociationName() {
        return getInverseAssociation().getName();
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
                throw new NullPointerException(NLS.bind("PolicyCmptTypeAssociation {0} has no inverse association.",
                        getAssociation()));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if
     * <ul>
     * <li>
     * this association is a master-to-detail composition, and</li>
     * <li>
     * the association's target exists and is a dependent type (and not the aggregate root).</li>
     * </ul>
     * <code>false</code> otherwise.
     */
    public boolean isValidComposition() {
        IPolicyCmptType targetType = getTargetPolicyCmptType();
        return isCompositionMasterToDetail() && hasTarget() && isTargetTypeDependantType(targetType);
    }

    /**
     * Returns <code>true</code> if an inverse association is defined or if this association is a
     * valid composition ( {@link #isValidComposition()} )
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForRemove() {
        return isValidComposition() || hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if an inverse association is defined and this association is a
     * valid composition ( {@link #isValidComposition()} ) at the same time.
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForAdd() {
        return isValidComposition() && hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if an inverse association is defined and this association is a
     * valid composition ( {@link #isValidComposition()} ) at the same time.
     */
    public boolean isGenerateCodeToSynchronizeInverseCompositionForSet() {
        return isValidComposition() && hasInverseAssociation();
    }

    /**
     * Returns <code>true</code> if this association is an association (neither composition nor
     * aggregation) and an inverse association is defined. <code>false</code> otherwise.
     */
    public boolean isGenerateCodeToSynchronizeInverseAssociation() {
        return isTypeAssociation() && hasInverseAssociation();
    }

    public boolean isTypeConfigurableByProductCmptType() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    private IPolicyCmptType getPolicyCmptType() {
        return getAssociation().getPolicyCmptType();
    }

    private boolean isTargetTypeDependantType(IPolicyCmptType targetType) {
        try {
            return targetType.isDependantType();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean hasTarget() {
        return getTargetType() != null;
    }

    private IPolicyCmptType getTargetPolicyCmptType() {
        return (IPolicyCmptType)getTargetType();
    }

    /**
     * Returns the name of the variable used in new child methods. The old implementation does not
     * capitalize the role name until now erroneously. e.g. newpolicyPart instead of newPolicyPart
     * (if the role name is policyPart).
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     */
    public String getVariableNameNewInstance() {
        // should be return getMethodNameNew();
        return "new" + getName(false);
    }

    public String getTargetProductCmptVariableName() {
        return StringUtils.uncapitalize(getTargetProductCmptInterfaceName());
    }

    public String getTargetProductCmptInterfaceName() {
        XPolicyCmptClass xPolicyCmptClass = getTargetPolicyCmptClass();
        return xPolicyCmptClass.getProductComponentClassOrInterfaceName();
    }

    public String getMethodNameCreatePolicyCmptForTargetProductCmpt() {
        XPolicyCmptClass xPolicyCmptClass = getTargetPolicyCmptClass();
        return "create" + xPolicyCmptClass.getClassName();
    }

    private XPolicyCmptClass getTargetPolicyCmptClass() {
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
     * Returns <code>true</code> if this association is the inverse of a derived union association.
     * <code>false</code> else.
     * 
     * @throws NullPointerException if this association has no inverse association.
     */
    public boolean isInverseOfADerivedUnion() {
        return isSharedAssociation() || getInverseAssociation().isDerivedUnion();
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
     * Returns the uncapitalized singular role-name with the suffix "LocalVar". Use inside a loop to
     * store the associated policy instance. e.g. "baseCoverageLocalVar".
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

    /**
     * 
     /** If this is a detail to master association the
     * {@link #getMethodNameGetSingleUncapitalized()} is used as a result. Reproduces Bug in old
     * code generator for compatibility. see FIPS-1143.
     */
    @Override
    public String getMethodNameGetSingle() {
        if (isCompositionDetailToMaster()) {
            return getMethodNameGetSingleUncapitalized();
        } else {
            return super.getMethodNameGetSingle();
        }
    }

    /**
     * Returns the setter name for derived unions on policy side, which does not capitalize the role
     * name until now erroneously. e.g. getpolicyPart() instead of getPolicyPart() (if the role name
     * is policyPart).
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     */
    private String getMethodNameGetSingleUncapitalized() {
        return "get" + getName(false);
    }

    /**
     * Returns the setter name for derived unions on policy side, which does not capitalize the role
     * name until now erroneously. e.g. getpolicyPart() instead of getPolicyPart() (if the role name
     * is policyPart).
     * 
     * Reproduces Bug in old code generator for compatibility. see FIPS-1143.
     */
    private String getMethodNameSetInternalUncapitalized() {
        return "set" + getName(false) + "Internal";
    }
}
