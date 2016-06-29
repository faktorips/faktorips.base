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
import org.faktorips.runtime.modeltype.IPolicyAssociationModel;
import org.faktorips.runtime.modeltype.IPolicyAttributeModel;
import org.faktorips.runtime.modeltype.internal.read.ModelPartCollector;
import org.faktorips.runtime.modeltype.internal.read.PolicyAssociationModelCollector;
import org.faktorips.runtime.modeltype.internal.read.PolicyAttributeModelCollector;
import org.faktorips.runtime.modeltype.internal.read.TypeModelPartsReader;

public class PolicyModel extends ModelType implements IPolicyModel {

    public static final String KIND_NAME = "PolicyCmptType";

    private final LinkedHashMap<String, IPolicyAttributeModel> attributes;

    private final LinkedHashMap<String, IPolicyAssociationModel> associations;

    public PolicyModel(String name, AnnotatedDeclaration annotatedDeclararation) {
        super(name, annotatedDeclararation);
        PolicyAttributeModelCollector attributeCollector = new PolicyAttributeModelCollector();
        PolicyAssociationModelCollector associationCollector = new PolicyAssociationModelCollector();
        initParts(annotatedDeclararation, attributeCollector, associationCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
    }

    private void initParts(AnnotatedDeclaration annotatedDeclararation,
            PolicyAttributeModelCollector attributeCollector,
            PolicyAssociationModelCollector associationCollector) {
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
    public IPolicyAttributeModel getDeclaredAttribute(String name) {
        IPolicyAttributeModel attr = attributes.get(name);
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public IPolicyAttributeModel getDeclaredAttribute(int index) {
        return (IPolicyAttributeModel)super.getDeclaredAttribute(index);
    }

    @Override
    public List<IPolicyAttributeModel> getDeclaredAttributes() {
        return new ArrayList<IPolicyAttributeModel>(attributes.values());
    }

    @Override
    public IPolicyAttributeModel getAttribute(String name) {
        return (IPolicyAttributeModel)super.getAttribute(name);
    }

    @Override
    public List<IPolicyAttributeModel> getAttributes() {
        AttributeCollector<IPolicyAttributeModel> attrCollector = new AttributeCollector<IPolicyAttributeModel>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    @Override
    public IPolicyAssociationModel getDeclaredAssociation(String name) {
        return associations.get(name);
    }

    @Override
    public IPolicyAssociationModel getDeclaredAssociation(int index) {
        return (IPolicyAssociationModel)super.getDeclaredAssociation(index);
    }

    @Override
    public List<IPolicyAssociationModel> getDeclaredAssociations() {
        return new ArrayList<IPolicyAssociationModel>(new LinkedHashSet<IPolicyAssociationModel>(associations.values()));
    }

    @Override
    public IPolicyAssociationModel getAssociation(String name) {
        return (IPolicyAssociationModel)super.getAssociation(name);
    }

    @Override
    public List<IPolicyAssociationModel> getAssociations() {
        AssociationsCollector<IPolicyAssociationModel> asscCollector = new AssociationsCollector<IPolicyAssociationModel>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }
}
