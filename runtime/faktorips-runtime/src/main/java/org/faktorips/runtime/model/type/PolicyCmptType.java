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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsValidatedBy;
import org.faktorips.runtime.model.type.read.PolicyAssociationCollector;
import org.faktorips.runtime.model.type.read.PolicyAttributeCollector;
import org.faktorips.runtime.model.type.read.TypePartCollector;
import org.faktorips.runtime.model.type.read.TypePartsReader;
import org.faktorips.runtime.model.type.read.ValidationRuleCollector;

/**
 * Corresponds to a design time {@code IPolicyCmptType}.
 */
public class PolicyCmptType extends Type {

    public static final String KIND_NAME = "PolicyCmptType";

    private final LinkedHashMap<String, PolicyAttribute> attributes;

    private final LinkedHashMap<String, PolicyAssociation> associations;

    private final LinkedHashMap<String, ValidationRule> validationRules;

    public PolicyCmptType(String name, AnnotatedDeclaration annotatedDeclararation) {
        super(name, annotatedDeclararation);
        PolicyAttributeCollector attributeCollector = new PolicyAttributeCollector();
        PolicyAssociationCollector associationCollector = new PolicyAssociationCollector();
        initParts(annotatedDeclararation, attributeCollector, associationCollector);
        attributes = attributeCollector.createParts(this);
        associations = associationCollector.createParts(this);

        AnnotatedDeclaration validationClass = getClassContainingValidationRules(annotatedDeclararation);
        ValidationRuleCollector validationRuleCollector = new ValidationRuleCollector();
        initParts(validationClass, validationRuleCollector);
        validationRules = validationRuleCollector.createParts(this);
    }

    private void initParts(AnnotatedDeclaration annotatedDeclararation,
            TypePartCollector<?, ?>... collectors) {
        TypePartsReader typePartsReader = new TypePartsReader(collectors);
        typePartsReader.init(annotatedDeclararation);
        typePartsReader.read(annotatedDeclararation);
    }

    private AnnotatedDeclaration getClassContainingValidationRules(AnnotatedDeclaration policyCmptType) {
        if (policyCmptType.is(IpsValidatedBy.class)) {
            Class<?> validator = policyCmptType.get(IpsValidatedBy.class).value();
            return AnnotatedDeclaration.from(validator);
        } else {
            return policyCmptType;
        }
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
        return findSuperType().orElse(null);
    }

    @Override
    public Optional<PolicyCmptType> findSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return IpsModel.isPolicyCmptType(superclass)
                ? Optional.of(IpsModel.getPolicyCmptType(superclass.asSubclass(IModelObject.class)))
                : Optional.empty();
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
        return new ArrayList<>(attributes.values());
    }

    @Override
    public PolicyAttribute getAttribute(String name) {
        return (PolicyAttribute)super.getAttribute(name);
    }

    @Override
    public List<PolicyAttribute> getAttributes() {
        AttributeCollector<PolicyAttribute> attrCollector = new AttributeCollector<>();
        attrCollector.visitHierarchy(this);
        return attrCollector.getResult();
    }

    /**
     * Returns a list with {@link ModelObjectAttribute ModelObjectAttributes} with all
     * {@link #getAttributes()} of this {@link PolicyCmptType}
     * 
     * @param modelObject the {@link IModelObject} linked to the attributes
     * @return a list with {@link ModelObjectAttribute ModelObjectAttributes}
     * @since 21.6
     */
    public List<ModelObjectAttribute> getModelObjectAttributes(IModelObject modelObject) {
        return getAttributes().stream().map(a -> ModelObjectAttribute.of(modelObject, a)).collect(Collectors.toList());
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
        return new ArrayList<>(new LinkedHashSet<>(associations.values()));
    }

    @Override
    public PolicyAssociation getAssociation(String name) {
        return (PolicyAssociation)super.getAssociation(name);
    }

    @Override
    public List<PolicyAssociation> getAssociations() {
        AssociationsCollector<PolicyAssociation> asscCollector = new AssociationsCollector<>();
        asscCollector.visitHierarchy(this);
        return asscCollector.getResult();
    }

    @Override
    public boolean isAssociationDeclared(String name) {
        return associations.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    @Override
    public boolean isAttributeDeclared(String name) {
        return attributes.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    /**
     * Returns the {@link ValidationRule} with the given <code>name</code> declared in this type.
     * {@link ValidationRule ValidationRules} defined in the type's super types is not returned.
     * 
     * @param name the name of the {@link ValidationRule}
     * @return {@link ValidationRule} with the given <code>name</code> if it was found in this type
     * 
     * @throws IllegalArgumentException if no {@link ValidationRule} with the given
     *             <code>name</code> exists
     */
    public ValidationRule getDeclaredValidationRule(String name) {
        ValidationRule rule = validationRules.get(IpsStringUtils.toLowerFirstChar(name));
        if (rule == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got the validation rule " + name);
        }
        return rule;
    }

    /**
     * Returns the declared {@link ValidationRule} at the given <code>index</code>.
     * 
     * @param index the position at which the {@link ValidationRule} is expected in the list of
     *            declared {@link ValidationRule ValidationRules}
     * @return the declared {@link ValidationRule} at the given <code>index</code>
     * @throws IndexOutOfBoundsException if no {@link ValidationRule} exists for the given
     *             <code>index</code>
     */
    public ValidationRule getDeclaredValidationRule(int index) {
        return getDeclaredValidationRules().get(index);
    }

    /**
     * Returns a list containing all {@link ValidationRule ValidationRules} declared in this model
     * type. {@link ValidationRule ValidationRules} defined in the type's super types are not
     * returned.
     * 
     * @return the list of {@link ValidationRule ValidationRules} declared in this type
     */
    public List<ValidationRule> getDeclaredValidationRules() {
        return new ArrayList<>(validationRules.values());
    }

    /**
     * Returns the {@link ValidationRule} with the given <code>name</code> declared in this type or
     * one of its super types.
     * 
     * @param name the name of the {@link ValidationRule}
     * @return {@link ValidationRule} with the given <code>name</code> declared in this type or one
     *             of its super types
     * @throws IllegalArgumentException if no {@link ValidationRule} with the given
     *             <code>name</code> exists
     */
    public ValidationRule getValidationRule(String name) {
        ValidationRule rule = validationRuleFinder(name);
        if (rule == null) {
            throw new IllegalArgumentException(
                    "The type " + this + " (or one of its supertypes) hasn't got the validation rule \"" + name + "\"");
        }
        return rule;
    }

    /**
     * Returns a list containing all the {@link ValidationRule ValidationRules} including those
     * defined in the super types.
     * 
     * @return the list of all {@link ValidationRule ValidationRules} declared in this type and in
     *             its super types
     */
    public List<ValidationRule> getValidationRules() {
        RuleCollector ruleCollector = new RuleCollector();
        ruleCollector.visitHierarchy(this);
        return ruleCollector.getResult();
    }

    /**
     * Returns whether the {@link ValidationRule} with the given <code>name</code> is declared in
     * this type. {@link ValidationRule ValidationRules} defined in the type's super types are not
     * considered.
     * 
     * @param name the name of the {@link ValidationRule}
     * @return <code>true</code> if the {@link ValidationRule} is declared in this type,
     *             <code>false</code> if not
     */
    public boolean isValidationRuleDeclared(String name) {
        return validationRules.containsKey(IpsStringUtils.toLowerFirstChar(name));
    }

    /**
     * Returns whether the {@link ValidationRule} with the given <code>name</code> is declared in
     * this type or in any supertype.
     *
     * @param name the name of the {@link ValidationRule}
     * @return <code>true</code> if the {@link ValidationRule} is declared in this type or in any
     *             supertype, <code>false</code> if not
     */
    public boolean isValidationRulePresent(String name) {
        return validationRuleFinder(name) != null;
    }

    private ValidationRule validationRuleFinder(String name) {
        ValidationRuleFinder finder = new ValidationRuleFinder(name);
        finder.visitHierarchy(this);
        return finder.validationRule;
    }

    static class ValidationRuleFinder extends TypeHierarchyVisitor {

        private String validationRuleName;
        private ValidationRule validationRule = null;

        public ValidationRuleFinder(String validationRuleName) {
            this.validationRuleName = IpsStringUtils.toLowerFirstChar(validationRuleName);
        }

        @Override
        public boolean visitType(Type type) {
            boolean isValidationRuleDeclared = ((PolicyCmptType)type).isValidationRuleDeclared(validationRuleName);
            if (isValidationRuleDeclared) {
                validationRule = ((PolicyCmptType)type).getDeclaredValidationRule(validationRuleName);
            }
            return !isValidationRuleDeclared;
        }

    }

    static class RuleCollector extends TypeHierarchyVisitor {
        private final List<ValidationRule> result = new ArrayList<>();
        private final Set<String> validationRulesNames = new HashSet<>();

        @Override
        public boolean visitType(Type type) {
            for (ValidationRule declaredValidationRule : ((PolicyCmptType)type).getDeclaredValidationRules()) {
                if (!validationRulesNames.contains(declaredValidationRule.getName())) {
                    validationRulesNames.add(declaredValidationRule.getName());
                    result.add(declaredValidationRule);
                }
            }
            return true;
        }

        public List<ValidationRule> getResult() {
            return result;
        }
    }
}
