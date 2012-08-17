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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenJavaClassNameProvider;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.MasterToDetailFilter;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.runtime.internal.ProductComponentGeneration;

public class XProductCmptGenerationClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = true;

    private final IJavaClassNameProvider prodGenJavaClassNameProvider;

    private final Set<XProductAttribute> attributes;

    private final Set<XPolicyAttribute> configuredAttributes;

    private final Set<XProductAssociation> associations;

    private final Set<XProductAssociation> masterToDetailAssociations;

    private final Set<XDerivedUnionAssociation> derivedUnionAssociations;

    public XProductCmptGenerationClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
        prodGenJavaClassNameProvider = createProductCmptGenJavaClassNaming(getLanguageUsedInGeneratedSourceCode());

        attributes = initNodesForParts(getProductAttributes(CHANGE_OVER_TIME), XProductAttribute.class);
        configuredAttributes = initNodesForParts(getConfiguredAttributes(CHANGE_OVER_TIME), XPolicyAttribute.class);
        associations = initNodesForParts(getProductAssociations(CHANGE_OVER_TIME), XProductAssociation.class);
        masterToDetailAssociations = initNodesForParts(
                getAssociations(ipsObjectPartContainer, IProductCmptTypeAssociation.class, new MasterToDetailFilter()),
                XProductAssociation.class);
        derivedUnionAssociations = initNodesForParts(getProductDerivedUnionAssociations(CHANGE_OVER_TIME),
                XDerivedUnionAssociation.class);
    }

    public static ProductCmptGenJavaClassNameProvider createProductCmptGenJavaClassNaming(Locale locale) {
        return new ProductCmptGenJavaClassNameProvider(locale) {

            @Override
            public String getImplClassName(IIpsSrcFile ipsSrcFile) {
                return super.getImplClassName(ipsSrcFile);// + "_X";
            }

        };
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return prodGenJavaClassNameProvider;
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return super.getIpsObjectPartContainer();
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponentGeneration.class.getName());
    }

    @Override
    public Set<XProductCmptGenerationClass> getClassHierarchy() {
        return super.getClassHierarchy(XProductCmptGenerationClass.class);
    }

    /**
     * Returns the class hierarchy of the corresponding policy component type.
     * 
     * @return The policy component class hierarchy
     */
    public Set<XPolicyCmptClass> getPolicyTypeClassHierarchy() {
        if (isConfigurationForPolicyCmptType()) {
            XPolicyCmptClass policyCmptClass = getPolicyCmptClass();
            Set<XPolicyCmptClass> result = policyCmptClass.getClassHierarchy();
            return result;
        } else {
            return new LinkedHashSet<XPolicyCmptClass>();
        }
    }

    public String getProductCmptClassName(BuilderAspect aspect) {
        XProductCmptClass modelNode = getModelNode(getProductCmptType(), XProductCmptClass.class);
        return modelNode.getSimpleName(aspect);
    }

    public String getMethodNameGetProductCmpt() {
        XProductCmptClass productCmptClass = getModelNode(getType(), XProductCmptClass.class);
        return getJavaNamingConvention().getGetterMethodName(
                productCmptClass.getSimpleName(BuilderAspect.IMPLEMENTATION));
    }

    @Override
    public Set<XProductAttribute> getAttributes() {
        return new CopyOnWriteArraySet<XProductAttribute>(attributes);
    }

    @Override
    public Set<XPolicyAttribute> getConfiguredAttributes() {
        return new CopyOnWriteArraySet<XPolicyAttribute>(configuredAttributes);
    }

    @Override
    public Set<XProductAssociation> getAssociations() {
        return new CopyOnWriteArraySet<XProductAssociation>(associations);
    }

    @Override
    public Set<XDerivedUnionAssociation> getDerivedUnionAssociations() {
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(derivedUnionAssociations);
    }

    @Override
    public Set<XAssociation> getMasterToDetailAssociations() {
        return new CopyOnWriteArraySet<XAssociation>(masterToDetailAssociations);
    }
}
