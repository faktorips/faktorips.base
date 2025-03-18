/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.type.AssociationKind;

public abstract class XAssociation extends AbstractGeneratorModelNode {

    public XAssociation(IAssociation association, GeneratorModelContext context, ModelService modelService) {
        super(association, context, modelService);
    }

    public XAssociation(IAssociation association, GeneratorModelContext context, ModelService modelService,
            LocalizedStringsSet localizedStringsSet) {
        super(association, context, modelService, localizedStringsSet);
    }

    @Override
    public IAssociation getIpsObjectPartContainer() {
        return (IAssociation)super.getIpsObjectPartContainer();
    }

    public IAssociation getAssociation() {
        return getIpsObjectPartContainer();
    }

    public String getName(boolean plural) {
        if (plural) {
            return getAssociation().getTargetRolePlural();
        } else {
            return getAssociation().getTargetRoleSingular();
        }
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName(isOneToMany()));
    }

    public String getMethodNameGetter() {
        return getMethodNameGetter(isOneToMany());
    }

    public String getMethodNameGetSingle() {
        return getJavaNamingConvention().getGetterMethodName(getName(false));
    }

    protected String getMethodNameGetter(boolean toMany) {
        return getJavaNamingConvention().getGetterMethodName(getName(toMany));
    }

    /**
     * The name of the adder method - only used for one-to-many associations.
     */
    public String getMethodNameSetOrAdd() {
        if (isOneToMany()) {
            return "add" + IpsStringUtils.toUpperFirstChar(getName(false));
        } else {
            return getJavaNamingConvention().getSetterMethodName(getName(false));
        }
    }

    /**
     * The name of the remove method - only used for one-to-many associations.
     */
    public String getMethodNameRemove(boolean plural) {
        return (plural ? "removeAll" : "remove") + IpsStringUtils.toUpperFirstChar(getName(plural));
    }

    /**
     * Returns true if this association is a one to many association and false if it is one to one.
     *
     * @return true for one to many and false for one to one associations
     */
    public boolean isOneToMany() {
        return getAssociation().is1ToMany();
    }

    public boolean isDerivedUnion() {
        return getAssociation().isDerivedUnion();
    }

    public boolean isSubsetOfADerivedUnion() {
        return getAssociation().isSubsetOfADerivedUnion();
    }

    public boolean isDerived() {
        return getAssociation().isDerived();
    }

    public boolean isMasterToDetail() {
        return getAssociation().getAssociationType().isMasterToDetail();
    }

    public boolean isAssociation() {
        return getAssociation().getAssociationType().isAssoziation();
    }

    /**
     * Returns <code>true</code> if this association is a strict subset of the given derived union.
     * Returns <code>false</code> if this association is an indirect subset or subset of second
     * grade (or higher), i.e. if it is the subset of another derived union, that is itself a subset
     * of the given derived union. Returns <code>false</code> in all other cases.
     *
     * @param derivedUnionAssociation the derived union to test against.
     */
    public boolean isSubsetOf(XDerivedUnionAssociation derivedUnionAssociation) {
        if (getAssociation().isSubsetOfDerivedUnion(derivedUnionAssociation.getAssociation(), getIpsProject())) {
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this association is a subset or indirect subset of the given
     * derived union. An indirect subset or subset of second grade (or higher) is an association
     * that is the subset of another derived union, that is itself a subset of the given derived
     * union. Returns <code>false</code> in all other cases.
     *
     * @param derivedUnionAssociation the derived union to test against.
     * @see #isSubsetOf(XDerivedUnionAssociation)
     */
    public boolean isRecursiveSubsetOf(XDerivedUnionAssociation derivedUnionAssociation) {
        if (!isSubsetOfADerivedUnion()) {
            return false;
        }
        if (isSubsetOf(derivedUnionAssociation)) {
            return true;
        }
        XDerivedUnionAssociation subsettedDerivedUnions = getSubsettedDerivedUnion();
        return subsettedDerivedUnions.isRecursiveSubsetOf(derivedUnionAssociation);
    }

    /**
     * Returns the derived union this association is a subset of.
     *
     * @throws RuntimeException if this is not a subset of a derived union.
     * @throws NullPointerException if no derived union could be found for this subset.
     */
    public XDerivedUnionAssociation getSubsettedDerivedUnion() {
        if (!isSubsetOfADerivedUnion()) {
            throw new RuntimeException(NLS.bind(
                    "The association {0} is not a subset of a derived union. Unable to determine derived union.",
                    getAssociation()));
        }
        IAssociation derivedUnion = getAssociation().findSubsettedDerivedUnion(getIpsProject());
        if (derivedUnion == null) {
            throw new NullPointerException(
                    NLS.bind("No derived union found for association {0}.", getAssociation()));
        }
        return getModelNode(derivedUnion, XDerivedUnionAssociation.class);
    }

    public boolean isConstrain() {
        return getAssociation().isConstrain();
    }

    public boolean isQualified() {
        return getAssociation().isQualified();
    }

    protected XAssociation getConstrainedAssociation() {
        IAssociation constrainedAssociation = getAssociation().findConstrainedAssociation(getIpsProject());
        return getModelNode(constrainedAssociation, getClass());
    }

    public int getMinCardinality() {
        return getAssociation().getMinCardinality();
    }

    public int getMaxCardinality() {
        return getAssociation().getMaxCardinality();
    }

    /**
     * Getting the type of the association
     *
     * @return The IType that is the parent of the association
     */
    protected IType getSourceType() {
        return getAssociation().getType();
    }

    public XType getSourceModelNode() {
        return getModelNode(getSourceType(), getModelNodeType(true));
    }

    public XType getSourceModelNodeNotConsiderChangingOverTime() {
        return getModelNode(getSourceType(), getModelNodeType(false));
    }

    /**
     * Getting the target type of the association
     *
     * @return The IType that is the parent of the association
     */
    protected IType getTargetType() {
        return getAssociation().findTarget(getIpsProject());
    }

    public String getTargetName() {
        XClass xClass = getTargetModelNode();
        return xClass.getName();
    }

    public String getTargetInterfaceName() {
        XClass xClass = getTargetModelNode();
        return xClass.getPublishedInterfaceName();
    }

    /**
     * Returns the target interface name as does {@link #getTargetInterfaceName()}. But in case of a
     * constrained/overwritten association, the interface name of the target of the constrained
     * association (that one in the super class) is returned.
     */
    public String getTargetInterfaceNameBase() {
        if (isConstrain()) {
            XClass xClass = getConstrainedAssociation().getTargetModelNode();
            return xClass.getPublishedInterfaceName();
        } else {
            return getTargetInterfaceName();
        }
    }

    public String getTargetClassName() {
        XClass xClass = getTargetModelNode();
        return getClassName(xClass);
    }

    public XClass getTargetModelNode() {
        IType target = getTargetType();
        return getModelNode(target, getModelNodeType(false));
    }

    public String getTargetQualifiedClassName() {
        XClass xClass = getTargetModelNode();
        return xClass.getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

    public boolean isAbstractTarget() {
        return getTargetType().isAbstract();
    }

    public String getTypeName() {
        return getSourceType().getName();
    }

    protected String getClassName(XClass xClass) {
        return xClass.getSimpleName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns the concrete class of {@link XType} that is the source of this association.
     *
     * @param considerChangeOverTime In case of a product association and this parameter is
     *            <code>true</code> we check whether it is configured to change over time or not and
     *            return a generation type or the normal product type.
     *
     * @return The class of the {@link XType} that could be used to get the source type of this
     *             association
     */
    protected Class<? extends XType> getModelNodeType(boolean considerChangeOverTime) {
        // TODO is there a better way? Cannot move to subclass because of derived unions
        return switch (getAssociation()) {
            case IProductCmptTypeAssociation productAsso when productAsso.isChangingOverTime()
                    && considerChangeOverTime -> XProductCmptGenerationClass.class;
            case IProductCmptTypeAssociation $ -> XProductCmptClass.class;
            case IPolicyCmptTypeAssociation $ -> XPolicyCmptClass.class;
            default -> throw new RuntimeException("Illegal kind of association " + getAssociation());
        };
    }

    public String getMethodNameGetNumOf() {
        // TODO Bad hack to be compatible with old code generator
        if (XProductClass.class.isAssignableFrom(getModelNodeType(false))) {
            return getJavaNamingConvention().getGetterMethodName("NumOf" + getAssociation().getTargetRolePlural());
        }
        return getJavaNamingConvention()
                .getGetterMethodName("NumOf" + IpsStringUtils.toUpperFirstChar(getAssociation().getTargetRolePlural()));
    }

    public String getMethodNameContains() {
        return "contains" + IpsStringUtils.toUpperFirstChar(getName(false));
    }

    /**
     * Returns true if an abstract getter (normally in interface but may be in implementation if we
     * do not generate published interfaces) needs to be generated for this association. This method
     * may be overwritten for special cases.
     *
     * @param generatingInterface true if we just generating an interface, false if we generate an
     *            implementation class
     *
     */
    public boolean isGenerateAbstractGetter(boolean generatingInterface) {
        if (!isDerived()) {
            return false;
        }
        if (generatingInterface) {
            return true;
        }
        if (isSubsetImplementedInSameType(getSourceModelNode())) {
            return false;
        }
        return !(getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject())
                && getSourceModelNode().isAbstract());
    }

    protected boolean isSubsetImplementedInSameType(XType contextType) {
        XDerivedUnionAssociation derivedUnionAssociation = getModelNode(getAssociation(),
                XDerivedUnionAssociation.class);
        Set<XAssociation> subsetAssociations = derivedUnionAssociation.getSubsetAssociations(contextType);
        return !subsetAssociations.isEmpty();
    }

    /**
     * Returns the type of the association, e.g. association, composition. Note that aggregations
     * are returned as composition.
     *
     * @return type of the association
     * @see AssociationKind
     */
    public AssociationKind getAssociationKind() {
        org.faktorips.devtools.model.type.AssociationType associationType = getAssociation().getAssociationType();
        if (associationType.isMasterToDetail()) {
            return AssociationKind.Composition;
        } else if (associationType.isCompositionDetailToMaster()) {
            return AssociationKind.CompositionToMaster;
        } else if (associationType.isAssoziation()) {
            return AssociationKind.Association;
        } else {
            // should not occur
            return null;
        }
    }

    public XAssociation getMatchingAssociation() {
        IAssociation matchingAssociation = getAssociation().findMatchingAssociation();
        if (matchingAssociation != null) {
            return getModelNode(matchingAssociation, getMatchingClass());
        } else {
            return null;
        }
    }

    protected String getVarNameAvoidCollisionWithPluralName(String potentialName) {
        if (potentialName.equals(getName(true))) {
            return "a" + IpsStringUtils.toUpperFirstChar(getJavaNamingConvention().getMemberVarName(potentialName));
        } else {
            return getJavaNamingConvention().getMemberVarName(potentialName);
        }
    }

    protected abstract Class<? extends XAssociation> getMatchingClass();

    public abstract AnnotatedJavaElementType getAnnotatedJavaElementTypeForGetter();
}
