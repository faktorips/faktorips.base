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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.IProductModelAssociation;
import org.faktorips.runtime.modeltype.IProductModelAttribute;
import org.faktorips.runtime.modeltype.ITableUsageModel;
import org.faktorips.runtime.modeltype.TypeHierarchyVisitor;
import org.faktorips.runtime.modeltype.internal.read.ModelPartCollector;
import org.faktorips.runtime.modeltype.internal.read.ProductModelAssociationCollector;
import org.faktorips.runtime.modeltype.internal.read.ProductModelAttributeCollector;
import org.faktorips.runtime.modeltype.internal.read.TableUsageCollector;
import org.faktorips.runtime.modeltype.internal.read.TypeModelPartsReader;

public class ProductModel extends ModelType implements IProductModel {

    public static final String KIND_NAME = "ProductCmptType2";

    private final AnnotatedDeclaration generationDeclaration;

    private final LinkedHashMap<String, IProductModelAttribute> attributes;

    private final LinkedHashMap<String, IProductModelAssociation> associations;

    private final LinkedHashMap<String, ITableUsageModel> tableUsages;

    public ProductModel(String name, AnnotatedDeclaration annotatedDeclaration) {
        super(name, annotatedDeclaration);
        generationDeclaration = isChangingOverTime() ? AnnotatedDeclaration.from(annotatedDeclaration.get(
                IpsChangingOverTime.class).value()) : null;

        ProductModelAttributeCollector attributeCollector = new ProductModelAttributeCollector();
        ProductModelAssociationCollector associationCollector = new ProductModelAssociationCollector();
        TableUsageCollector tableUsageCollector = new TableUsageCollector();
        initParts(attributeCollector, associationCollector, tableUsageCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
        tableUsages = tableUsageCollector.createParts(this);
    }

    private void initParts(ProductModelAttributeCollector attributeCollector,
            ProductModelAssociationCollector associationCollector,
            TableUsageCollector tableUsageCollector) {
        TypeModelPartsReader typeModelPartsReader = new TypeModelPartsReader(Arrays.<ModelPartCollector<?, ?>> asList(
                attributeCollector, associationCollector, tableUsageCollector));
        typeModelPartsReader.init(getAnnotatedDeclaration());
        typeModelPartsReader.read(getAnnotatedDeclaration());
        if (isChangingOverTime()) {
            typeModelPartsReader.read(generationDeclaration);
        }
    }

    @Override
    protected String getKindName() {
        return KIND_NAME;
    }

    @Override
    protected List<Method> getDeclaredMethods() {
        List<Method> result = super.getDeclaredMethods();
        result.addAll(generationDeclaration.getDeclaredMethods());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChangingOverTime() {
        return getAnnotatedDeclaration().is(IpsChangingOverTime.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationForPolicyCmptType() {
        return getAnnotatedDeclaration().is(IpsConfigures.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPolicyModel getPolicyCmptType() {
        return Models.getPolicyModel(getAnnotatedDeclaration().get(IpsConfigures.class).value()
                .asSubclass(IModelObject.class));
    }

    @Override
    public ITableUsageModel getTableUsage(String name) {
        TableUsageFinder finder = new TableUsageFinder(name);
        finder.visitHierarchy(this);
        if (finder.tableUsage == null) {
            throw new IllegalArgumentException("The type " + this
                    + " (or one of it's super types) hasn't got a table usage \"" + name + "\"");
        }
        return finder.tableUsage;
    }

    @Override
    public List<ITableUsageModel> getDeclaredTableUsages() {
        return new ArrayList<ITableUsageModel>(tableUsages.values());
    }

    public ITableUsageModel getDeclaredTableUsage(String name) {
        ITableUsageModel tableUsage = tableUsages.get(name);
        if (tableUsage == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared table usage " + name);
        }
        return tableUsage;
    }

    @Override
    public List<ITableUsageModel> getTableUsages() {
        TableUsagesCollector tuCollector = new TableUsagesCollector();
        tuCollector.visitHierarchy(this);
        return tuCollector.result;
    }

    public Class<?> getGenerationJavaClass() {
        if (generationDeclaration != null) {
            return generationDeclaration.getImplementationClass();
        } else {
            return null;
        }
    }

    public Class<?> getGenerationJavaInterface() {
        if (generationDeclaration != null) {
            return generationDeclaration.getPublishedInterface();
        } else {
            return null;
        }
    }

    public Class<?> getGenerationDeclarationClass() {
        return getGenerationJavaInterface() == null ? getGenerationJavaClass() : getGenerationJavaInterface();
    }

    @Override
    public IProductModel getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return Models.isProductModel(superclass) ? Models.getProductModel(superclass
                .asSubclass(IProductComponent.class)) : null;
    }

    @Override
    public IProductModelAttribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    @Override
    public IProductModelAttribute getDeclaredAttribute(String name) {
        IProductModelAttribute attr = attributes.get(name);
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public List<IProductModelAttribute> getDeclaredAttributes() {
        return new ArrayList<IProductModelAttribute>(attributes.values());
    }

    @Override
    public IProductModelAttribute getAttribute(String name) {
        return (IProductModelAttribute)super.getAttribute(name);
    }

    @Override
    public List<IProductModelAttribute> getAttributes() {
        AttributeCollector<IProductModelAttribute> attrCollector = new AttributeCollector<IProductModelAttribute>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    @Override
    public IProductModelAssociation getDeclaredAssociation(int index) {
        return (IProductModelAssociation)super.getDeclaredAssociation(index);
    }

    @Override
    public IProductModelAssociation getDeclaredAssociation(String name) {
        return associations.get(name);
    }

    @Override
    public List<IProductModelAssociation> getDeclaredAssociations() {
        return new ArrayList<IProductModelAssociation>(new LinkedHashSet<IProductModelAssociation>(
                associations.values()));
    }

    @Override
    public IProductModelAssociation getAssociation(String name) {
        return (IProductModelAssociation)super.getAssociation(name);
    }

    @Override
    public List<IProductModelAssociation> getAssociations() {
        AssociationsCollector<IProductModelAssociation> asscCollector = new AssociationsCollector<IProductModelAssociation>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }

    static class TableUsagesCollector extends TypeHierarchyVisitor {

        private List<ITableUsageModel> result = new ArrayList<ITableUsageModel>();

        @Override
        public boolean visitType(IModelType type) {
            result.addAll(((IProductModel)type).getDeclaredTableUsages());
            return true;
        }

    }

    static class TableUsageFinder extends TypeHierarchyVisitor {

        private String tableUsageName;
        private ITableUsageModel tableUsage = null;

        public TableUsageFinder(String name) {
            super();
            this.tableUsageName = name;
        }

        @Override
        public boolean visitType(IModelType type) {
            try {
                tableUsage = ((ProductModel)type).getDeclaredTableUsage(tableUsageName);
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }
    }
}
