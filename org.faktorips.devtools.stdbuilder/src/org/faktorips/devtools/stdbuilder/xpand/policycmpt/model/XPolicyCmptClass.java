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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class XPolicyCmptClass extends XClass {

    private final Set<XPolicyAttribute> attributes;

    private final Set<XPolicyAssociation> associations;

    private final Set<XDerivedUnionAssociation> derivedUnionAssociations;

    public XPolicyCmptClass(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
        attributes = initNodesForParts(
                new LinkedHashSet<IPolicyCmptTypeAttribute>(policyCmptType.getPolicyCmptTypeAttributes()),
                XPolicyAttribute.class);
        associations = initNodesForParts(getPolicyAssociations(policyCmptType, false), XPolicyAssociation.class);
        // TODO derived unions müssen noch richtig "berechnet" werden siehe product side
        derivedUnionAssociations = new LinkedHashSet<XDerivedUnionAssociation>();
        // derivedUnionAssociations = initNodesForParts(getPolicyAssociations(policyCmptType,
        // false),
        // XDerivedUnionAssociation.class);
    }

    private Set<IPolicyCmptTypeAssociation> getPolicyAssociations(IPolicyCmptType policyCmptType, boolean derivedUnion) {
        Set<IPolicyCmptTypeAssociation> result = new LinkedHashSet<IPolicyCmptTypeAssociation>();
        List<IPolicyCmptTypeAssociation> policyCmptTypeAssociations = policyCmptType.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyCmptTypeAssociations) {
            if (policyCmptTypeAssociation.isDerivedUnion() == derivedUnion) {
                result.add(policyCmptTypeAssociation);
            }
        }
        return result;
    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the policyCmptType.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return getIpsObjectPartContainer();
    }

    public boolean isConfigured() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    public String getProductCmptClassName() {
        try {
            IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(
                    getIpsObjectPartContainer().getIpsProject());
            if (productCmptType != null) {
                XProductCmptClass xProductCmptClass = getModelNode(productCmptType, XProductCmptClass.class);
                return addImport(xProductCmptClass.getQualifiedName(BuilderAspect.IMPLEMENTATION));
            } else {
                return StringUtils.EMPTY;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isAggregateRoot() {
        try {
            return getPolicyCmptType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public List<String> getImplementedInterfaces() {
        List<String> list = super.getImplementedInterfaces();
        if (getModelContext().isGeneratePropertyChange() && !hasSupertype()) {
            list.add(addImport(INotificationSupport.class));
        }
        return list;
    }

    @Override
    protected String getBaseSuperclassName() {
        if (isConfigured()) {
            return addImport(AbstractConfigurableModelObject.class);
        } else {
            return addImport(AbstractModelObject.class);
        }
    }

    @Override
    public Set<XPolicyAttribute> getAttributes() {
        return new CopyOnWriteArraySet<XPolicyAttribute>(attributes);
    }

    @Override
    public Set<XPolicyAssociation> getAssociations() {
        return new CopyOnWriteArraySet<XPolicyAssociation>(associations);
    }

    @Override
    public Set<XDerivedUnionAssociation> getDerivedUnionAssociations() {
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(derivedUnionAssociations);
    }

    /**
     * Returns the simple name for the product component generation class associated with this
     * policy component class. An import will be added automatically.
     * 
     */
    protected String getProductGenerationClassName() {
        return getProductGenerationClassName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns the simple name for the product component generation class or interface associated
     * with this policy component class. An import will be added automatically.
     * <p>
     * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
     * whether the name of the published interface or the name of the implementing class - in case
     * no published interface is generated - is returned.
     * 
     * TODO FIPS-1059
     */
    public String getProductGenerationClassOrInterfaceName() {
        return getProductGenerationClassName(getBuilderAspectDependingOnSettings());
    }

    /**
     * Returns the simple name for the product component generation class or interface associated
     * with this policy component class. An import will be added automatically.
     * 
     */
    protected String getProductGenerationClassName(BuilderAspect aspect) {
        IProductCmptType prodType = getProductCmptType();
        XProductCmptGenerationClass xProductCmptGenClass = getModelNode(prodType, XProductCmptGenerationClass.class);
        String simpleName = xProductCmptGenClass.getSimpleName(aspect);
        return simpleName;
    }

    public String getProductGenerationArgumentName() {
        return getJavaNamingConvention().getMemberVarName(getProductGenerationClassName());
    }

    /**
     * Returns the simple name for the product component class or interface associated with this
     * policy component class. An import will be added automatically.
     * 
     */
    public String getProductComponentClassName(BuilderAspect aspect) {
        IProductCmptType prodType = getProductCmptType();
        XProductCmptClass xProductCmptClass = getModelNode(prodType, XProductCmptClass.class);
        String simpleName = xProductCmptClass.getSimpleName(aspect);
        return simpleName;
    }

    /**
     * Returns the simple name for the product component class associated with this policy component
     * class. An import will be added automatically.
     * 
     */
    protected String getProductComponentClassName() {
        return getProductComponentClassName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns the simple name for the product component class or interface associated with this
     * policy component class. An import will be added automatically.
     * <p>
     * This method lets the {@link GeneratorModelContext} and its {@link JavaClassNaming} decide
     * whether the name of the published interface or the name of the implementing class - in case
     * no published interface is generated - is returned .
     * 
     * TODO FIPS-1059
     * 
     */
    public String getProductComponentClassOrInterfaceName() {
        return getProductComponentClassName(getBuilderAspectDependingOnSettings());
    }

    /**
     * Finds the product component type associated with this policy component type and returns it.
     * Throws a {@link CoreRuntimeException} in case of a {@link CoreException}.
     */
    protected IProductCmptType getProductCmptType() {
        try {
            IProductCmptType prodType = getPolicyCmptType().findProductCmptType(getIpsProject());
            return prodType;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getMethodNameGetProductCmptGeneration() {
        return getJavaNamingConvention().getGetterMethodName(getProductGenerationClassName());
    }

    public String getMethodNameGetProductComponent() {
        return getJavaNamingConvention().getGetterMethodName(getProductComponentClassName());
    }

    public String getMethodNameSetProductComponent() {
        return getJavaNamingConvention().getSetterMethodName(getProductComponentClassName());
    }

    public String getProductComponentArgumentName() {
        return getJavaNamingConvention().getMemberVarName(getProductComponentClassName());
    }

    public boolean isAbstract() {
        return getPolicyCmptType().isAbstract();
    }
}
