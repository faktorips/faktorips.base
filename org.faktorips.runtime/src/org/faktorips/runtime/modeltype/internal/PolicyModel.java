/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IPolicyModelAssociation;
import org.faktorips.runtime.modeltype.IPolicyModelAttribute;
import org.faktorips.runtime.modeltype.internal.read.ModelPartCollector;
import org.faktorips.runtime.modeltype.internal.read.PolicyModelAssociationCollector;
import org.faktorips.runtime.modeltype.internal.read.PolicyModelAttributeCollector;
import org.faktorips.runtime.modeltype.internal.read.TypeModelPartsReader;

public class PolicyModel extends ModelType implements IPolicyModel {

    public static final String KIND_NAME = "PolicyCmptType";

    private final LinkedHashMap<String, IPolicyModelAttribute> attributes;

    private final LinkedHashMap<String, IPolicyModelAssociation> associations;

    public PolicyModel(String name, AnnotatedDeclaration annotatedDeclararation) {
        super(name, annotatedDeclararation);
        PolicyModelAttributeCollector attributeCollector = new PolicyModelAttributeCollector();
        PolicyModelAssociationCollector associationCollector = new PolicyModelAssociationCollector();
        initParts(annotatedDeclararation, attributeCollector, associationCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
    }

    private void initParts(AnnotatedDeclaration annotatedDeclararation,
            PolicyModelAttributeCollector attributeCollector,
            PolicyModelAssociationCollector associationCollector) {
        TypeModelPartsReader typeModelPartsReader = new TypeModelPartsReader(Arrays.<ModelPartCollector<?, ?>> asList(
                attributeCollector, associationCollector));
        typeModelPartsReader.init(annotatedDeclararation);
        typeModelPartsReader.read(annotatedDeclararation);
    }

    @Override
    protected String getKindName() {
        return KIND_NAME;
    }

    @Override
    public boolean isConfiguredByPolicyCmptType() {
        return getAnnotatedDeclaration().is(IpsConfiguredBy.class);
    }

    @Override
    public ProductModel getProductCmptType() {
        return (ProductModel)Models.getProductModel(getAnnotatedDeclaration().get(IpsConfiguredBy.class).value()
                .asSubclass(IProductComponent.class));
    }

    @Override
    public IPolicyModel getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return Models.isPolicyModel(superclass) ? Models.getPolicyModel(superclass.asSubclass(IModelObject.class))
                : null;
    }

    @Override
    public IPolicyModelAttribute getDeclaredAttribute(String name) {
        IPolicyModelAttribute attr = attributes.get(name);
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public IPolicyModelAttribute getDeclaredAttribute(int index) {
        return (IPolicyModelAttribute)super.getDeclaredAttribute(index);
    }

    @Override
    public List<IPolicyModelAttribute> getDeclaredAttributes() {
        return new ArrayList<IPolicyModelAttribute>(attributes.values());
    }

    @Override
    public IPolicyModelAttribute getAttribute(String name) {
        return (IPolicyModelAttribute)super.getAttribute(name);
    }

    @Override
    public List<IPolicyModelAttribute> getAttributes() {
        AttributeCollector<IPolicyModelAttribute> attrCollector = new AttributeCollector<IPolicyModelAttribute>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    @Override
    public IPolicyModelAssociation getDeclaredAssociation(String name) {
        return associations.get(name);
    }

    @Override
    public IPolicyModelAssociation getDeclaredAssociation(int index) {
        return (IPolicyModelAssociation)super.getDeclaredAssociation(index);
    }

    @Override
    public List<IPolicyModelAssociation> getDeclaredAssociations() {
        return new ArrayList<IPolicyModelAssociation>(new LinkedHashSet<IPolicyModelAssociation>(associations.values()));
    }

    @Override
    public IPolicyModelAssociation getAssociation(String name) {
        return (IPolicyModelAssociation)super.getAssociation(name);
    }

    @Override
    public List<IPolicyModelAssociation> getAssociations() {
        AssociationsCollector<IPolicyModelAssociation> asscCollector = new AssociationsCollector<IPolicyModelAssociation>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }
}
