/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.type.read.ProductAssociationCollector;
import org.faktorips.runtime.model.type.read.ProductAttributeCollector;
import org.faktorips.runtime.model.type.read.TableUsageCollector;
import org.faktorips.runtime.model.type.read.TypePartsReader;

/**
 * Corresponds to a design time {@code IProductCmptType}.
 */
public class ProductCmptType extends Type {

    /**
     * The name of the design time implementation class. Don't ask about the 2.
     */
    public static final String KIND_NAME = "ProductCmptType2";

    private final AnnotatedDeclaration generationDeclaration;

    private final LinkedHashMap<String, ProductAttribute> attributes;

    private final LinkedHashMap<String, ProductAssociation> associations;

    private final LinkedHashMap<String, TableUsage> tableUsages;

    public ProductCmptType(String name, AnnotatedDeclaration annotatedDeclaration) {
        super(name, annotatedDeclaration);
        generationDeclaration = isChangingOverTime()
                ? AnnotatedDeclaration.from(annotatedDeclaration.get(IpsChangingOverTime.class).value())
                : null;

        ProductAttributeCollector attributeCollector = new ProductAttributeCollector();
        ProductAssociationCollector associationCollector = new ProductAssociationCollector();
        TableUsageCollector tableUsageCollector = new TableUsageCollector();
        initParts(attributeCollector, associationCollector, tableUsageCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
        tableUsages = tableUsageCollector.createParts(this);
    }

    private void initParts(ProductAttributeCollector attributeCollector,
            ProductAssociationCollector associationCollector,
            TableUsageCollector tableUsageCollector) {
        TypePartsReader typePartsReader = new TypePartsReader(attributeCollector, associationCollector,
                tableUsageCollector);
        typePartsReader.init(getAnnotatedDeclaration());
        typePartsReader.read(getAnnotatedDeclaration());
        if (isChangingOverTime()) {
            typePartsReader.read(generationDeclaration);
        }
    }

    @Override
    protected String getKindName() {
        return KIND_NAME;
    }

    @Override
    protected List<Method> getDeclaredMethods() {
        List<Method> result = super.getDeclaredMethods();
        if (isChangingOverTime()) {
            result.addAll(generationDeclaration.getDeclaredMethods());
        }
        return result;
    }

    /**
     * Returns whether this product component type is changing over time.
     * 
     * @return <code>true</code> if it has generations else <code>false</code>
     */
    public boolean isChangingOverTime() {
        return getAnnotatedDeclaration().is(IpsChangingOverTime.class);
    }

    /**
     * Returns whether this product component type is a configuration for a policy component type.
     * 
     * @return <code>true</code> if this type configures a policy component type, <code>false</code>
     *             if not
     */
    public boolean isConfigurationForPolicyCmptType() {
        return getAnnotatedDeclaration().is(IpsConfigures.class);
    }

    /***
     * Returns whether this {@link ProductCmptType} is the same or sub-type compared to a reference
     * {@link ProductCmptType}.
     * 
     * @param reference the {@link ProductCmptType} to compare to
     * @return <code>true</code> if this type is the same or sub-type of the reference.
     */
    public boolean isSameOrSub(ProductCmptType reference) {
        if (reference.equals(this)) {
            return true;
        }
        return findSuperType().map(s -> s.isSameOrSub(reference)).orElse(false);
    }

    /**
     * Returns the policy component type which is configured by this product component type. If this
     * product component class has no configuration it throws a {@link NullPointerException}.
     * 
     * @see #isConfigurationForPolicyCmptType()
     * 
     * @return The configured policy component type
     * @throws NullPointerException if the product component type is not a configuration for a
     *             policy component type
     */
    public PolicyCmptType getPolicyCmptType() {
        return IpsModel.getPolicyCmptType(
                getAnnotatedDeclaration().get(IpsConfigures.class).value().asSubclass(IModelObject.class));
    }

    /**
     * Returns the {@link TableUsage} for the specified name. May look in super types if there is no
     * table usage in this type.
     * 
     * @param name The name of the table usage
     * @return The {@link TableUsage} with the specified name
     * 
     * @throws IllegalArgumentException if there is no table usage with the specified name
     */
    public TableUsage getTableUsage(String name) {
        TableUsageFinder finder = new TableUsageFinder(name);
        finder.visitHierarchy(this);
        if (finder.tableUsage == null) {
            throw new IllegalArgumentException(
                    "The type " + this + " (or one of it's super types) hasn't got a table usage \"" + name + "\"");
        }
        return finder.tableUsage;
    }

    /**
     * Returns a list of {@link TableUsage TableUsages} which are declared in this type. In contrast
     * to {@link #getTableUsages()} this does not return table usages of super types.
     * 
     * @return A list of {@link TableUsage TableUsages} declared in this type
     */
    public List<TableUsage> getDeclaredTableUsages() {
        return new ArrayList<>(tableUsages.values());
    }

    /**
     * Returns the {@link TableUsage} with the given {@code name} which is declared in this type.
     * Any table usage defined in the super types will not be returned.
     * 
     * @param name The name of the {@link TableUsage}
     * @return {@link TableUsage} declared in this type with the given name
     * 
     * @throws IllegalArgumentException if this type does not have a declared table usage with the
     *             given name
     */
    public TableUsage getDeclaredTableUsage(String name) {
        TableUsage tableUsage = tableUsages.get(IpsStringUtils.toLowerFirstChar(name));
        if (tableUsage == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared table usage " + name);
        }
        return tableUsage;
    }

    /**
     * Returns whether the {@link TableUsage} for the specified <code>name</code> is declared in
     * this type.
     */
    public boolean hasDeclaredTableUsage(String name) {
        return tableUsages.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    /**
     * Returns a list of {@link TableUsage TableUsages} which are declared in this type or in any
     * super type.
     * 
     * @return All {@link TableUsage TableUsages} accessible in this product type.
     */
    public List<TableUsage> getTableUsages() {
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
    public ProductCmptType getSuperType() {
        return findSuperType().orElse(null);
    }

    @Override
    public Optional<ProductCmptType> findSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return IpsModel.isProductCmptType(superclass)
                ? Optional.of(IpsModel.getProductCmptType(superclass.asSubclass(IProductComponent.class)))
                : Optional.empty();
    }

    @Override
    public ProductAttribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    @Override
    public ProductAttribute getDeclaredAttribute(String name) {
        ProductAttribute attr = attributes.get(IpsStringUtils.toLowerFirstChar(name));
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public List<ProductAttribute> getDeclaredAttributes() {
        return new ArrayList<>(attributes.values());
    }

    @Override
    public ProductAttribute getAttribute(String name) {
        return (ProductAttribute)super.getAttribute(name);
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        AttributeCollector<ProductAttribute> attrCollector = new AttributeCollector<>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    @Override
    public boolean isAttributeDeclared(String name) {
        return attributes.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    @Override
    public ProductAssociation getDeclaredAssociation(int index) {
        return (ProductAssociation)super.getDeclaredAssociation(index);
    }

    @Override
    public ProductAssociation getDeclaredAssociation(String name) {
        ProductAssociation productAssociation = associations.get(IpsStringUtils.toLowerFirstChar(name));
        if (productAssociation == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared association " + name);
        }
        return productAssociation;
    }

    @Override
    public List<ProductAssociation> getDeclaredAssociations() {
        return new ArrayList<>(new LinkedHashSet<>(associations.values()));
    }

    @Override
    public boolean isAssociationDeclared(String name) {
        return associations.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    @Override
    public ProductAssociation getAssociation(String name) {
        return (ProductAssociation)super.getAssociation(name);
    }

    @Override
    public List<ProductAssociation> getAssociations() {
        AssociationsCollector<ProductAssociation> asscCollector = new AssociationsCollector<>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }

    static class TableUsagesCollector extends TypeHierarchyVisitor {

        private List<TableUsage> result = new ArrayList<>();

        @Override
        public boolean visitType(Type type) {
            result.addAll(((ProductCmptType)type).getDeclaredTableUsages());
            return true;
        }

    }

    static class TableUsageFinder extends TypeHierarchyVisitor {

        private String tableUsageName;
        private TableUsage tableUsage = null;

        public TableUsageFinder(String name) {
            super();
            tableUsageName = name;
        }

        @Override
        public boolean visitType(Type type) {
            boolean hasDeclaredTableUsage = ((ProductCmptType)type).hasDeclaredTableUsage(tableUsageName);
            if (hasDeclaredTableUsage) {
                tableUsage = ((ProductCmptType)type).getDeclaredTableUsage(tableUsageName);
            }
            return !hasDeclaredTableUsage;
        }
    }

}
