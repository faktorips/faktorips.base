/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.productcmptbuilder.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.builder.model.XPBuilder;
import org.faktorips.devtools.stdbuilder.xpand.builder.model.XPBuilderUtil;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.policycmptbuilder.XTypeBuilderClassNameProvider;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;

public class XProductBuilder extends XProductCmptClass implements
XPBuilder<XProductBuilder, XProductBuilderAssociation, XProductAttribute> {

    private XTypeBuilderClassNameProvider nameProvider;
    private XPBuilderUtil<XProductBuilder, XProductBuilderAssociation, XProductAttribute> xpBuilderUtil;

    public XProductBuilder(IProductCmptType type, GeneratorModelContext context, ModelService modelService) {
        super(type, context, modelService);
        nameProvider = new XTypeBuilderClassNameProvider(this);
        xpBuilderUtil = new XPBuilderUtil<XProductBuilder, XProductBuilderAssociation, XProductAttribute>(this);
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return nameProvider.getBuilderNameProvider();
    }

    /**
     * {@inheritDoc} No import statement is added. For import statement, see
     * {@link #getImplClassName()}
     * 
     * @return Name of the builder
     */
    @Override
    public String getName() {
        return nameProvider.getName();
    }

    /**
     * No import statement added
     * 
     * @return name of the product class
     */
    public String getProductName() {
        return nameProvider.getTypeName();
    }

    /**
     * Adds import statement for the policy class
     * 
     * @return name of the product class
     */
    public String getProductImplClassName() {
        return nameProvider.getTypeClassName();
    }

    /**
     * Adds import statement for the published interface of the product, if published interface is
     * generated. If not, this method is the same as {@link #getImplClassName()}.
     */
    public String getProductPublishedInterfaceName() {
        return nameProvider.getTypePublishedInterfaceName();
    }

    public String getFactoryImplClassName() {
        return nameProvider.getFactoryImplClassName();
    }

    /**
     * @return the variable name of the product by uncapitalizing the class name.
     */
    public String getVariableName() {
        return nameProvider.getVariableName(getProductName());
    }

    public String getProdGenImplClassName() {
        return getProductCmptGenerationNode().getImplClassName();
    }

    public String getProdGenPublishedInterfaceName() {
        return getProductCmptGenerationNode().getPublishedInterfaceName();
    }

    @Override
    public XProductBuilder getSupertype() {
        if (hasSupertype()) {
            return (XProductBuilder)super.getSupertype();
        } else {
            return null;
        }
    }

    /**
     * Overwritten to also get changing attributes
     */
    @Override
    public Set<XProductAttribute> getAttributes() {
        if (isCached(XProductAttribute.class)) {
            return filterAttributes(getCachedObjects(XProductAttribute.class));
        } else {
            Set<XProductAttribute> nodesForParts = initNodesForParts(getType().getProductCmptTypeAttributes(),
                    XProductAttribute.class);
            putToCache(nodesForParts);
            return filterAttributes(nodesForParts);
        }
    }

    @Override
    public Set<XProductAttribute> getSuperAttributes() {
        return xpBuilderUtil.getSuperAttributes();
    }

    /**
     * Overwritten to also get changing associations
     */
    @Override
    public Set<XProductAssociation> getAssociations() {
        if (isCached(XProductAssociation.class)) {
            return getCachedObjects(XProductAssociation.class);
        } else {
            Set<XProductAssociation> nodesForParts = initNodesForParts(getType().getProductCmptTypeAssociations(),
                    XProductAssociation.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    @Override
    public Set<XProductBuilderAssociation> getBuilderAssociations() {
        Set<XProductBuilderAssociation> builderAssociations = new HashSet<XProductBuilderAssociation>();
        for (XProductAssociation association : getAssociations()) {
            if (!association.isDerived()) {
                builderAssociations.add(getModelNode(association.getAssociation(), XProductBuilderAssociation.class));
            }
        }
        return builderAssociations;
    }

    @Override
    public Map<String, XProductBuilderAssociation> getSuperBuilderAssociationsAsMap() {
        return xpBuilderUtil.getSuperBuilderAssociationsAsMap();
    }

    /**
     * @return set of all associations that are inherited but not overwritten by this product
     */
    public Collection<XProductBuilderAssociation> getSuperBuilderAssociations() {
        return xpBuilderUtil.getSuperBuilderAssociationsAsMap().values();
    }

    @Override
    public boolean hasSuperAssociationBuilder() {
        return xpBuilderUtil.hasSuperAssociationBuilder();
    }

    @Override
    public XProductBuilder getSuperBuilderForAssociationBuilder() {
        return xpBuilderUtil.getSuperBuilderForAssociationBuilder();
    }

    /**
     * 
     * @return true if @Override is needed for method of product generation, else false.
     */
    public boolean isNeedOverrideForProductGenSetter() {
        if (hasSupertype()) {
            XProductBuilder supertype = getSupertype();
            if (supertype.isChangingOverTime() && !supertype.isAbstract()) {
                return true;
            } else {
                return getSupertype().isNeedOverrideForProductGenSetter();
            }
        } else {
            return false;
        }
    }

    /**
     * This method returns the name of the policy field if this builder doesn't have super type,
     * else returns the name of the getter method from the super type and append the String to cast
     * it to the required policy class.
     * 
     * @return the name of the policy field or super call
     */
    public String getProductFieldName() {
        if (hasSupertype()) {
            if (isGeneratePublishedInterfaces()) {
                return "((" + getProductImplClassName() + ") " + "get())";
            } else {
                return "get()";
            }
        } else {
            return getVariableName();
        }
    }

    public String getProdGenFieldName() {
        if (hasSupertype()) {
            if (isGeneratePublishedInterfaces()) {
                return "((" + getProdGenImplClassName() + ") " + "getCurrentGeneration())";
            } else {
                return "getCurrentGeneration()";
            }
        } else {
            return "currentGeneration";
        }
    }

    public String getProdOrGenFieldName(Boolean changing) {
        if (changing) {
            return getProdGenFieldName();
        } else {
            return getProductFieldName();
        }
    }

    private String getGenerationConceptName() {
        return getProductCmptGenerationNode().getJavaClassNameProvider().getAbbreviationForGenerationConcept(
                getIpsObjectPartContainer());
    }

    public String getMethodNameGeneration() {
        return StringUtils.uncapitalize(getGenerationConceptName());
    }

    public String getMethodNameSetGeneration() {
        return "setCurrent" + StringUtils.capitalize(getGenerationConceptName());
    }

    public String getMethodNameGetLatestGeneration() {
        return "getLatest" + StringUtils.capitalize(getGenerationConceptName());
    }

    public String getMethodNameSetLatestGeneration() {
        return "latest" + StringUtils.capitalize(getGenerationConceptName());
    }
}
