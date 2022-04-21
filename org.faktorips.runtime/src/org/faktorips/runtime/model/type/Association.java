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
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;

/**
 * An association between two {@link Type Types}.
 */
public abstract class Association extends TypePart {

    private final IpsAssociation annotation;

    private final Method getter;

    public Association(Type type, Method getter) {
        super(getAssociationAnnotation(getter).name(), type,
                getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        this.annotation = getAssociationAnnotation(getter);
        this.getter = getter;
    }

    private static IpsAssociation getAssociationAnnotation(Method getterMethod) {
        return getterMethod.getAnnotation(IpsAssociation.class);
    }

    /**
     * Returns the plural label for this model element in the specified locale. If there is no
     * plural label in the specified locale, it tries to find the plural label in the default
     * locale. If there is also no plural label in the default locale the element's plural name is
     * returned.
     * 
     * @return the label for the given locale or the element's name if no label exists for the given
     *         locale nor in the default locale
     */
    public String getLabelForPlural(Locale locale) {
        return getDocumentation(locale, DocumentationKind.PLURAL_LABEL, getNamePlural());
    }

    /**
     * Returns what kind of association this is.
     */
    public AssociationKind getAssociationKind() {
        return annotation.kind();
    }

    /**
     * Returns the minimum cardinality for this association. <code>0</code> if no minimum is set.
     */
    public int getMinCardinality() {
        return annotation.min();
    }

    /**
     * Returns the maximum cardinality for this association. <code>Integer.MAX_VALUE</code> if no
     * maximum is set.
     */
    public int getMaxCardinality() {
        return annotation.max();
    }

    /**
     * Returns the plural form of this model type's name or the empty String if no plural for the
     * name is set.
     */
    public String getNamePlural() {
        return annotation.pluralName();
    }

    /**
     * Returns the target {@link Type} of this association.
     */
    public Type getTarget() {
        return IpsModel.getType(annotation.targetClass());
    }

    /**
     * Returns the singular or plural form of this association's name as used in code generation
     * depending on cardinality.
     */
    public String getUsedName() {
        return isTargetRolePluralRequired() ? getNamePlural() : getName();
    }

    private boolean isTargetRolePluralRequired() {
        return Iterable.class.isAssignableFrom(getter.getReturnType());
    }

    /**
     * Returns if this association is a derived union.
     */
    public boolean isDerivedUnion() {
        return getter.isAnnotationPresent(IpsDerivedUnion.class);
    }

    /**
     * Returns if this association is a subset of a derived union.
     */
    public boolean isSubsetOfADerivedUnion() {
        return getter.isAnnotationPresent(IpsSubsetOfDerivedUnion.class);
    }

    /**
     * Returns the name of the inverse association if it is defined.
     * 
     * @return The name of the inverse association or {@code null} if there is no inverse
     *         association or it is a product component associations
     */
    public String getInverseAssociation() {
        if (getter.isAnnotationPresent(IpsInverseAssociation.class)) {
            return getter.getAnnotation(IpsInverseAssociation.class).value();
        } else {
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the association has a matching association. For policy
     * associations that means it is configured by the product component. For product component
     * associations that means it configures a policy association.
     * 
     * @see #getMatchingAssociationName()
     * 
     * @return <code>true</code> if this association has a matching association.
     */
    public boolean isMatchingAssociationPresent() {
        return getter.isAnnotationPresent(IpsMatchingAssociation.class);
    }

    /**
     * Returns the name of the matching product respectively policy component type association or
     * <code>null</code> if no matching association is defined for this association.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the name of the matching product
     * association and vice versa.
     * 
     * @return The name of the matching association
     */
    public String getMatchingAssociationName() {
        if (getter.isAnnotationPresent(IpsMatchingAssociation.class)) {
            return getter.getAnnotation(IpsMatchingAssociation.class).name();
        } else {
            return null;
        }
    }

    /**
     * Returns the matching product respectively policy component type association or
     * <code>null</code> if no matching association is defined for this association.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a association for the
     * policy association this method returns the matching product association and vice versa.
     * 
     * @return The matching association
     */
    public Association getMatchingAssociation() {
        Type matchingAssociationSourceType = getMatchingAssociationSourceType();
        if (matchingAssociationSourceType == null) {
            return null;
        }
        return matchingAssociationSourceType.getAssociation(getMatchingAssociationName());
    }

    /**
     * Returns the matching product respectively policy component type association or an
     * {@link Optional#empty() empty Optional} if no matching association is defined for this
     * association.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a association for the
     * policy association this method returns the matching product association and vice versa.
     * 
     * @return The matching association
     */
    public Optional<? extends Association> findMatchingAssociation() {
        return Optional.ofNullable(getMatchingAssociation());
    }

    /**
     * Returns the qualified name of source type of the matching association or <code>null</code> if
     * no matching association is defined.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the qualified name of the source
     * of the matching product association and vice versa. The source is the type which defines the
     * matching association.
     * 
     * @return The qualified name of the matching association source
     */
    public String getMatchingAssociationSource() {
        Type matchingAssociationSource = getMatchingAssociationSourceType();
        if (matchingAssociationSource != null) {
            return matchingAssociationSource.getName();
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link Type} identified by {@link #getMatchingAssociationSource()}
     * 
     * @see #getMatchingAssociationSource()
     * 
     * @return The {@link Type} of the matching association source
     */
    public Type getMatchingAssociationSourceType() {
        if (getter.isAnnotationPresent(IpsMatchingAssociation.class)) {
            return IpsModel.getType(getter.getAnnotation(IpsMatchingAssociation.class).source());
        } else {
            return null;
        }
    }

    /**
     * Returns <code>true</code> if this association overrides another association. That means a
     * supertype declares an association with the same name.
     * 
     * @return <code>true</code> if this association overrides another, <code>false</code> if not
     * @see #getSuperAssociation()
     */
    public boolean isOverriding() {
        return getType().findSuperType().map(s -> s.isAssociationPresent(getName())).orElse(false);
    }

    /**
     * Returns the association that is overridden by this association if this association overrides
     * another one. Otherwise returns <code>null</code>.
     * 
     * @return The association that is overridden by this attribute.
     * @see #isOverriding()
     */
    public Association getSuperAssociation() {
        return findSuperAssociation().orElse(null);
    }

    /**
     * Returns the association that is overridden by this association if this association overrides
     * another one. Otherwise returns an {@link Optional#empty() empty Optional}.
     * 
     * @return The association that is overridden by this attribute.
     * @see #isOverriding()
     */
    public Optional<Association> findSuperAssociation() {
        return isOverriding() ? getType().findSuperType().map(s -> s.getAssociation(getName())) : Optional.empty();
    }

    protected Method getGetterMethod() {
        return getter;
    }

    public abstract Association createOverwritingAssociationFor(Type subType);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUsedName());
        sb.append(": ");
        sb.append(getTarget().getName());
        sb.append('(');
        sb.append(getAssociationKind());
        sb.append(' ');
        if (isDerivedUnion()) {
            sb.append(", Derived Union ");
        }
        if (isSubsetOfADerivedUnion()) {
            sb.append(", Subset of a Derived Union ");
        }
        sb.append(getMinCardinality());
        sb.append("..");
        sb.append(getMaxCardinality() == Integer.MAX_VALUE ? "*" : getMaxCardinality());
        if (isMatchingAssociationPresent()) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Determines whether this association is a ..1 ("to one") association.
     * 
     * @return <code>true</code> if this is a ..1 association, <code>false</code> else.
     */
    protected boolean isToOneAssociation() {
        return getMaxCardinality() == 1;
    }

}
