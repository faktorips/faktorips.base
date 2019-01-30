/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.type.read.PolicyAssociationCollector;
import org.faktorips.runtime.model.type.read.PolicyAttributeCollector;
import org.faktorips.runtime.model.type.read.TypePartsReader;

/**
 * Corresponds to a design time {@code IPolicyCmptType}.
 */
public class PolicyCmptType extends Type {

    public static final String KIND_NAME = "PolicyCmptType";

    private final LinkedHashMap<String, PolicyAttribute> attributes;

    private final LinkedHashMap<String, PolicyAssociation> associations;

    public PolicyCmptType(String name, AnnotatedDeclaration annotatedDeclararation) {
        super(name, annotatedDeclararation);
        PolicyAttributeCollector attributeCollector = new PolicyAttributeCollector();
        PolicyAssociationCollector associationCollector = new PolicyAssociationCollector();
        initParts(annotatedDeclararation, attributeCollector, associationCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
    }

    private void initParts(AnnotatedDeclaration annotatedDeclararation,
            PolicyAttributeCollector attributeCollector,
            PolicyAssociationCollector associationCollector) {
        TypePartsReader typePartsReader = new TypePartsReader(attributeCollector, associationCollector);
        typePartsReader.init(annotatedDeclararation);
        typePartsReader.read(annotatedDeclararation);
    }

    @Override
    protected String getKindName() {
        return KIND_NAME;
    }

    /**
     * Returns whether this policy component type is configured by a product component type. If this
     * method returns <code>true</code> you could use {@link #getProductCmptType()} to get the type
     * of the configuring product.
     * 
     * @return <code>true</code> if this policy component type is configured else <code>false</code>
     */
    public boolean isConfiguredByProductCmptType() {
        return getAnnotatedDeclaration().is(IpsConfiguredBy.class);
    }

    /**
     * Returns the {@link ProductCmptType} that configures this policy component type. Throws an
     * {@link IllegalArgumentException} if this policy component type is not configured. Use
     * {@link #isConfiguredByProductCmptType()} to check whether it is configured or not.
     * 
     * @return the {@link ProductCmptType} that configures this policy component type
     * @throws NullPointerException if this policy component type is not configured
     * 
     */
    public ProductCmptType getProductCmptType() {
        return IpsModel.getProductCmptType(
                getAnnotatedDeclaration().get(IpsConfiguredBy.class).value().asSubclass(IProductComponent.class));
    }

    @Override
    public PolicyCmptType getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return IpsModel.isPolicyCmptType(superclass)
                ? IpsModel.getPolicyCmptType(superclass.asSubclass(IModelObject.class)) : null;
    }

    @Override
    public PolicyAttribute getDeclaredAttribute(String name) {
        PolicyAttribute attr = attributes.get(IpsStringUtils.toLowerFirstChar(name));
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public PolicyAttribute getDeclaredAttribute(int index) {
        return (PolicyAttribute)super.getDeclaredAttribute(index);
    }

    @Override
    public List<PolicyAttribute> getDeclaredAttributes() {
        return new ArrayList<PolicyAttribute>(attributes.values());
    }

    @Override
    public PolicyAttribute getAttribute(String name) {
        return (PolicyAttribute)super.getAttribute(name);
    }

    @Override
    public List<PolicyAttribute> getAttributes() {
        AttributeCollector<PolicyAttribute> attrCollector = new AttributeCollector<PolicyAttribute>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    @Override
    public PolicyAssociation getDeclaredAssociation(String name) {
        PolicyAssociation policyAssociation = associations.get(IpsStringUtils.toLowerFirstChar(name));
        if (policyAssociation == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared association " + name);
        }
        return policyAssociation;
    }

    @Override
    public PolicyAssociation getDeclaredAssociation(int index) {
        return (PolicyAssociation)super.getDeclaredAssociation(index);
    }

    @Override
    public List<PolicyAssociation> getDeclaredAssociations() {
        return new ArrayList<PolicyAssociation>(new LinkedHashSet<PolicyAssociation>(associations.values()));
    }

    @Override
    public PolicyAssociation getAssociation(String name) {
        return (PolicyAssociation)super.getAssociation(name);
    }

    @Override
    public List<PolicyAssociation> getAssociations() {
        AssociationsCollector<PolicyAssociation> asscCollector = new AssociationsCollector<PolicyAssociation>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }

    @Override
    protected boolean hasDeclaredAssociation(String name) {
        return associations.containsKey(name);
    }

    @Override
    protected boolean hasDeclaredAttribute(String name) {
        return attributes.containsKey(name);
    }
}
