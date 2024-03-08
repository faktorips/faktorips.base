/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.policycmptbuilder;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.faktorips.devtools.model.builder.java.naming.XTypeBuilderClassNameProvider;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.builder.XPBuilder;
import org.faktorips.devtools.model.builder.xmodel.builder.XPBuilderUtil;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.runtime.internal.IpsStringUtils;

public class XPolicyBuilder extends XPolicyCmptClass
        implements XPBuilder<XPolicyBuilder, XPolicyBuilderAssociation, XPolicyAttribute> {

    private XTypeBuilderClassNameProvider nameProvider;
    private XPBuilderUtil<XPolicyBuilder, XPolicyBuilderAssociation, XPolicyAttribute> xpBuilderUtil;

    public XPolicyBuilder(IPolicyCmptType type, GeneratorModelContext context, ModelService modelService) {
        super(type, context, modelService);
        nameProvider = new XTypeBuilderClassNameProvider(this);
        xpBuilderUtil = new XPBuilderUtil<>(this);
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
     * @return name of the policy class
     */
    public String getPolicyName() {
        return nameProvider.getTypeName();

    }

    /**
     * Adds import statement for the policy class
     *
     * @return name of the policy class
     */
    @Override
    public String getTypeImplClassName() {
        return nameProvider.getTypeClassName();
    }

    /**
     * Adds import statement for the published interface of the policy, if published interface is
     * generated. If not, this method is the same as {@link #getImplClassName()}.
     */
    public String getPolicyPublishedInterfaceName() {
        return nameProvider.getTypePublishedInterfaceName();
    }

    public String getFactoryImplClassName() {
        return nameProvider.getFactoryImplClassName();
    }

    /**
     * @return the variable name of the policy by uncapitalizing the class name.
     */
    @Override
    public String getVariableName() {
        return nameProvider.getVariableName(getPolicyName());
    }

    @Override
    public XPolicyBuilder getSupertype() {
        if (hasSupertype()) {
            return (XPolicyBuilder)super.getSupertype();
        } else {
            return null;
        }
    }

    /**
     * @return attributes of the super type that are not overwritten
     */
    @Override
    public Set<XPolicyAttribute> getSuperAttributes() {
        return xpBuilderUtil.getSuperAttributes();
    }

    public Set<XPolicyAttribute> withThisGeneratorConfig(Set<XPolicyAttribute> superAttributes) {
        return superAttributes.stream()
                .map(a -> new XPolicyAttribute(a.getAttribute(), getContext(), getModelService(),
                        getGeneratorConfig()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * First checks if XPolicyAssociation is cashed. If yes return cashed associations. If not,
     * associations are retrieved from getType() and added to cache. Only associations that are not
     * inverse composition nor derived are added to the set to return.
     *
     * @return set of associations of the policy that are not derived or inverseComposition, casted
     *             to XPolicyBuilderAssociations
     */
    @Override
    public Set<XPolicyBuilderAssociation> getBuilderAssociations() {
        if (isCached(XPolicyBuilderAssociation.class)) {
            return getCachedObjects(XPolicyBuilderAssociation.class);
        } else {
            List<IPolicyCmptTypeAssociation> associations = getType().getPolicyCmptTypeAssociations();
            Set<XPolicyBuilderAssociation> associationsNeedToGenerate = initNodesForParts(associations,
                    XPolicyBuilderAssociation.class);
            for (Iterator<XPolicyBuilderAssociation> it = associationsNeedToGenerate.iterator(); it.hasNext();) {
                XPolicyBuilderAssociation association = it.next();
                if (association.isDerived() || association.isInverseComposition()) {
                    it.remove();
                }
            }
            putToCache(associationsNeedToGenerate);
            return associationsNeedToGenerate;
        }
    }

    @Override
    public Map<String, XPolicyBuilderAssociation> getSuperBuilderAssociationsAsMap() {
        return xpBuilderUtil.getSuperBuilderAssociationsAsMap();
    }

    /**
     * @return set of all associations that are inherited but not overwritten by this policy
     */
    public Collection<XPolicyBuilderAssociation> getSuperBuilderAssociations() {
        return xpBuilderUtil.getSuperBuilderAssociationsAsMap().values();
    }

    @Override
    public boolean hasSuperAssociationBuilder() {
        return xpBuilderUtil.hasSuperAssociationBuilder();
    }

    @Override
    public XPolicyBuilder getSuperBuilderForAssociationBuilder() {
        return xpBuilderUtil.getSuperBuilderForAssociationBuilder();
    }

    /**
     * Returns the implement class name of the product component class. Import is added.
     * {@inheritDoc}
     */
    @Override
    public String getProductCmptClassName() {
        return getProductCmptNode().getImplClassName();
    }

    public String getMethodNameAssociation(XPolicyBuilderAssociation association) {
        return IpsStringUtils.toLowerFirstChar(association.getName());
    }

    public String getMethodNameGetPolicyFromProductCmpt() {
        return new JavaNamingConvention().getGetterMethodName(getPolicyName() + "FromProductComponent");
    }

    /**
     * {@inheritDoc}
     *
     * Explicitly override super method to avoid errors in XPAND template.
     */
    @Override
    public Set<XPolicyAttribute> getAttributes() {
        return super.getAttributes();
    }

    @Override
    public boolean isGeneratePublishedInterfaces() {
        return getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
    }

}
