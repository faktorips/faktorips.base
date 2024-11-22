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

import static java.util.function.Predicate.not;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.type.read.FormulaCollector;
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

    private final LinkedHashMap<String, Formula> formulas;

    public ProductCmptType(String name, AnnotatedDeclaration annotatedDeclaration) {
        super(name, annotatedDeclaration);
        generationDeclaration = isChangingOverTime()
                ? AnnotatedDeclaration.from(annotatedDeclaration.get(IpsChangingOverTime.class).value())
                : null;

        ProductAttributeCollector attributeCollector = new ProductAttributeCollector();
        ProductAssociationCollector associationCollector = new ProductAssociationCollector();
        TableUsageCollector tableUsageCollector = new TableUsageCollector();
        FormulaCollector formulaCollector = new FormulaCollector();
        initParts(attributeCollector, associationCollector, tableUsageCollector, formulaCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);
        tableUsages = tableUsageCollector.createParts(this);
        formulas = formulaCollector.createParts(this);
    }

    private void initParts(ProductAttributeCollector attributeCollector,
            ProductAssociationCollector associationCollector,
            TableUsageCollector tableUsageCollector,
            FormulaCollector formulaCollector) {
        TypePartsReader typePartsReader = new TypePartsReader(attributeCollector, associationCollector,
                tableUsageCollector, formulaCollector);
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
     * Returns a list of {@link Formula Formulas} which are declared in this type. In contrast to
     * {@link #getFormulas()} this does not return formulas of super types.
     *
     * @return A list of {@link Formula Formulas} declared in this type
     * @since 24.7
     */
    public List<Formula> getDeclaredFormulas() {
        return new ArrayList<>(formulas.values());
    }

    /**
     * Returns the {@link Formula} with the given {@code name} which is declared in this type. Any
     * formula defined in the super types will not be returned.
     *
     * @param name The name of the {@link Formula}
     * @return {@link Formula} declared in this type with the given name
     *
     * @throws IllegalArgumentException if this type does not have a declared formula with the given
     *             name
     * @since 24.7
     */
    public Formula getDeclaredFormula(String name) {
        Formula formula = formulas.get(IpsStringUtils.toLowerFirstChar(name));
        if (formula == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared formula " + name);
        }
        return formula;
    }

    /**
     * Returns whether the {@link Formula} for the specified <code>name</code> is declared in this
     * type.
     */
    public boolean hasDeclaredFormula(String name) {
        return formulas.containsKey(IpsStringUtils.toLowerFirstChar(name));
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

    /**
     * Returns the {@link Formula} for the specified name. May look in super types if there is no
     * formula in this type.
     *
     * @param name The name of the formula
     * @return The {@link Formula} with the specified name
     *
     * @throws IllegalArgumentException if there is no table usage with the specified name
     * @since 24.7
     */
    public Formula getFormula(String name) {
        FormulaFinder finder = new FormulaFinder(name);
        finder.visitHierarchy(this);
        if (finder.formula == null) {
            throw new IllegalArgumentException(
                    "The type " + this + " (or one of it's super types) hasn't got a formula \"" + name + "\"");
        }
        return finder.formula;
    }

    /**
     * @return all formulas defined on this type.
     * @since 24.7
     */
    public List<Formula> getFormulas() {
        FormulasCollector fCollector = new FormulasCollector();
        fCollector.visitHierarchy(this);
        return fCollector.result;
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

    public <T extends Annotation> Optional<Field> findDeclaredFieldFromGeneration(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        Class<?> genDeclarationClass = getGenerationDeclarationClass();
        Stream<Field> fields = Stream.of(genDeclarationClass.getDeclaredFields());
        if (genDeclarationClass.isInterface()) {
            fields = Stream.concat(fields, Stream.of(getGenerationJavaClass().getDeclaredFields()));
        }
        return fields
                .filter(field -> isMatchingField(annotationClass, matcher, field))
                .findFirst();
    }

    private <T extends Annotation> boolean isMatchingField(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher,
            Field field) {
        return field.isAnnotationPresent(annotationClass)
                && matcher.matches(field.getAnnotation(annotationClass));
    }

    @Override
    public <T extends Annotation> Optional<Field> findDeclaredField(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        return super.findDeclaredField(annotationClass, matcher);
    }

    /**
     * Validates the given product component against this type, provided it matches.
     *
     * @param productComponent the enum value instances to validate
     * @param messages a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     *
     * @throws IllegalArgumentException if the given product component does not match this type
     *
     * @since 25.1
     */
    public void validate(IProductComponent productComponent,
            MessageList messages,
            IValidationContext context) {
        if (!getJavaClass().isInstance(productComponent)) {
            throw new IllegalArgumentException(productComponent + " is not a " + this);
        }
        var validFroms = collectValidFroms(productComponent);
        getAttributes().forEach(a -> validatePart(a, messages, context, productComponent, validFroms));
        if (isConfigurationForPolicyCmptType()) {
            getPolicyCmptType().getAttributes().stream().filter(PolicyAttribute::isProductRelevant)
                    .forEach(a -> validatePart(a, messages, context, productComponent, validFroms));
        }
        getAssociations().stream().filter(not(ProductAssociation::isDerivedUnion))
                .forEach(a -> validatePart(a, messages, context, productComponent, validFroms));
    }

    private <T extends TypePart> void validatePart(T part,
            MessageList messages,
            IValidationContext context,
            IProductComponent productComponent,
            List<Calendar> validFroms) {
        if (part.isChangingOverTime()) {
            validFroms.forEach(effectiveDate -> part.validate(messages, context, productComponent, effectiveDate));
        } else {
            part.validate(messages, context, productComponent, null);
        }
    }

    private List<Calendar> collectValidFroms(IProductComponent productComponent) {
        var validFroms = productComponent.getRepository()
                .getProductComponentGenerations(productComponent).stream()
                .map(IProductComponentGeneration::getValidFrom)
                .map(this::toCalendar)
                .toList();
        if (validFroms.isEmpty()) {
            validFroms = new ArrayList<>();
            validFroms.add(toCalendar(productComponent.getValidFrom()));
        }
        return validFroms;
    }

    private Calendar toCalendar(DateTime d) {
        return d == null ? null : d.toGregorianCalendar(TimeZone.getDefault());
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

    static class FormulasCollector extends TypeHierarchyVisitor {

        private final List<Formula> result = new ArrayList<>();

        @Override
        public boolean visitType(Type type) {
            result.addAll(((ProductCmptType)type).getDeclaredFormulas());
            return true;
        }

    }

    static class FormulaFinder extends TypeHierarchyVisitor {

        private String formulaName;
        private Formula formula = null;

        public FormulaFinder(String formulaName) {
            super();
            this.formulaName = formulaName;
        }

        @Override
        public boolean visitType(Type type) {
            boolean hasDeclaredFormula = ((ProductCmptType)type).hasDeclaredFormula(formulaName);
            if (hasDeclaredFormula) {
                formula = ((ProductCmptType)type).getDeclaredFormula(formulaName);
            }
            return !hasDeclaredFormula;
        }

    }
}
