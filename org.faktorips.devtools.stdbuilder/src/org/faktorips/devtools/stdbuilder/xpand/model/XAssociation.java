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

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;

public abstract class XAssociation extends AbstractGeneratorModelNode {

    public XAssociation(IAssociation association, GeneratorModelContext context, ModelService modelService) {
        super(association, context, modelService);
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
    public String getMethodNameAdd() {
        return "add" + StringUtils.capitalize(getName(false));
    }

    /**
     * The name of the setter method - only used for one-to-one associations.
     */
    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName(false));
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

    /**
     * Returns <code>true</code> if this association is a strict subset of the given derived union.
     * Returns <code>false</code> if this association is an indirect subset or subset of second
     * grade (or higher), i.e. if it is the subset of another derived union, that is itself a subset
     * of the given derived union. Returns <code>false</code> in all other cases.
     * 
     * @param derivedUnionAssociation the derived union to test against.
     */
    public boolean isSubsetOf(XDerivedUnionAssociation derivedUnionAssociation) {
        if (getAssociation().getSubsettedDerivedUnion().equals(derivedUnionAssociation.getName())) {
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
        return getSubsettedDerivedUnion().isRecursiveSubsetOf(derivedUnionAssociation);
    }

    /**
     * Returns the derived union this association is a subset of.
     * 
     * @throws NullPointerException if this is not a subset of a derived union.
     * @throws NullPointerException if no derived union could be found for this subset.
     */
    public XDerivedUnionAssociation getSubsettedDerivedUnion() {
        if (!isSubsetOfADerivedUnion()) {
            throw new NullPointerException(NLS.bind(
                    "The association {0} is not a subset of a derived union. Unable to determine derived union.",
                    getAssociation()));
        }
        try {
            IPolicyCmptTypeAssociation derivedUnion = (IPolicyCmptTypeAssociation)getAssociation()
                    .findSubsettedDerivedUnion(getIpsProject());
            if (derivedUnion == null) {
                throw new NullPointerException(
                        NLS.bind("No derived union found for association {0}.", getAssociation()));
            }
            return getModelNode(derivedUnion, XDerivedUnionAssociation.class);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Getting the type of the association
     * 
     * @return The IType that is the parent of the association
     */
    protected IType getTypeOfAssociation() {
        return getAssociation().getType();
    }

    /**
     * Getting the target type of the association
     * 
     * @return The IType that is the parent of the association
     */
    protected IType getTargetType() {
        try {
            return getAssociation().findTarget(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getTargetInterfaceName() {
        IType target = getTargetType();
        XClass modelNode = getModelNode(target, getTargetModelNodeType());
        return modelNode.getSimpleName(BuilderAspect.getValue(isGeneratingPublishedInterfaces()));
    }

    public String getTargetClassName() {
        IType target = getTargetType();
        XClass xClass = getModelNode(target, getTargetModelNodeType());
        return getClassName(xClass);
    }

    public String getTargetQualifiedClassName() {
        IType target = getTargetType();
        XClass xClass = getModelNode(target, getTargetModelNodeType());
        return xClass.getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

    public boolean isAbstractTarget() {
        return getTargetType().isAbstract();
    }

    /**
     * In contrast to {@link #getTargetClassName()} this method returns the class name of the type
     * this association is part of.
     */
    public String getClassName() {
        IType target = getIType();
        XClass xClass = getModelNode(target, getTargetModelNodeType());
        return getClassName(xClass);
    }

    private IType getIType() {
        return getAssociation().getType();
    }

    protected String getClassName(XClass xClass) {
        return xClass.getSimpleName(BuilderAspect.IMPLEMENTATION);
    }

    protected Class<? extends XClass> getTargetModelNodeType() {
        // TODO is there a better way? Cannot move to subclass because of derived unions
        Class<? extends IType> targetClass = getTargetType().getClass();
        if (IProductCmptType.class.isAssignableFrom(targetClass)) {
            return XProductCmptClass.class;
        } else if (IPolicyCmptType.class.isAssignableFrom(targetClass)) {
            return XPolicyCmptClass.class;
        } else {
            throw new RuntimeException("Illegal association target type " + targetClass);
        }
    }

    public String getMethodNameGetNumOf() {
        return getJavaNamingConvention().getGetterMethodName(
                "NumOf" + StringUtils.capitalize(getAssociation().getTargetRolePlural()));
    }

    public String getMethodNameContains() {
        return "contains" + StringUtils.capitalize(getName(false));
    }

}
