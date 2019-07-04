/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel.productcmptbuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.builder.XPBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.builder.XPBuilderUtil;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.XTypeBuilderClassNameProvider;

public class XProductBuilder extends XProductCmptClass
        implements XPBuilder<XProductBuilder, XProductBuilderAssociation, XProductAttribute> {

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
    @Override
    public String getTypeImplClassName() {
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
    @Override
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

    public String getProdGenFieldName() {
        if (hasSupertype()) {
            if (getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject())) {
                return "((" + getProdGenImplClassName() + ") " + "getCurrentGeneration())";
            } else {
                return "getCurrentGeneration()";
            }
        } else {
            return "currentGeneration";
        }
    }

    private String getGenerationConceptName() {
        return getProductCmptGenerationNode().getJavaClassNameProvider()
                .getAbbreviationForGenerationConcept(getIpsObjectPartContainer().getIpsSrcFile());
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

    @Override
    public boolean isGeneratePublishedInterfaces() {
        return getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
    }
}
