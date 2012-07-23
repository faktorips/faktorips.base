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
        return isProductRelevant();
    }

    private boolean isValidMasterToDetail() {
        return isValid() && isCompositionMasterToDetail();
    }

    public boolean isConsiderInCopySupport() {
        return isValid() && !isDerived() && !isCompositionDetailToMaster();
    }

    public String getConstantNamePropertyName() {
        return "ASSOCIATION_" + getFieldName().toUpperCase();
    }

    public String getOldValueVariable() {
        return "old" + StringUtils.capitalize(getName());
    }

    public String getMethodNameSetInternal() {
        return getMethodNameSetter(false) + "Internal";
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
     * Returns then name of the setter method that is used to set this associations policy instance
     * as target (e.g. parent) of the target associations.
     * 
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationSet() {
        return getJavaNamingConvention().getSetterMethodName(getInverseAssociationName());
    }

    /**
     * Returns then name of the setInternal method that is used to set this associations policy
     * instance as target (e.g. parent) of the target associations.
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
        return "contains" + getInverseAssociationName();
    }

    /**
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationAdd() {
        return "add" + getInverseAssociationName();
    }

    /**
     * @throws NullPointerException if this association has no inverse association.
     */
    public String getMethodNameInverseAssociationRemove() {
        return "remove" + getInverseAssociationName();
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
    public XAssociation getInverseAssociation() {
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

    public String getVariableNameNewInstance() {
        return getMethodNameNew();
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

    public boolean isCompositionMasterToDetail() {
        return getAssociation().isCompositionMasterToDetail();
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

    public String getCreateChildFromXMLLocalVarName() {
        return getJavaNamingConvention().getMemberVarName(getName()) + "LocalVar";
    }

    /**
     * Returns the uncapitalized singular role-name. Use inside a loop to store the associated
     * policy instance.
     */
    public String getVisitorSupportLoopVarName() {
        return getJavaNamingConvention().getMemberVarName(getAssociation().getTargetRoleSingular());
    }

    /**
     * Returns uncapitalized target interface name, e.g. "iCoverage".
     */
    public String getCopySupportLoopVarName() {
        return StringUtils.uncapitalize(getTargetInterfaceName());
    }

    /**
     * Returns "copy"+the target className, e.g. "copyCoverage".
     */
    public String getCopySupportCopyVarName() {
        return "copy" + getTargetClassName();
    }
}
