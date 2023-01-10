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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * A {@link Type} represents a design time type of Faktor-IPS. It provides all meta data for the
 * type as well as for properties like {@linkplain Attribute attributes} or {@linkplain Association
 * associations}.
 */
public abstract class Type extends ModelElement {

    private final AnnotatedDeclaration annotatedDeclaration;

    private final MessagesHelper messagesHelper;

    public Type(String name, AnnotatedDeclaration annotatedDeclaration) {
        super(name, annotatedDeclaration.get(IpsExtensionProperties.class), Deprecation.of(annotatedDeclaration));
        this.annotatedDeclaration = annotatedDeclaration;
        IpsDocumented ipsDocumented = annotatedDeclaration.get(IpsDocumented.class);
        messagesHelper = createMessageHelper(ipsDocumented, annotatedDeclaration.getClassLoader());
    }

    protected abstract String getKindName();

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(getName(), getKindName(), IpsStringUtils.EMPTY);
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    /**
     * Returns the association at the given <code>index</code>. Associations defined in the type's
     * super types are not returned.
     * 
     * @throws IndexOutOfBoundsException if no association exists for the given <code>index</code>.
     */
    public Association getDeclaredAssociation(int index) {
        return getDeclaredAssociations().get(index);
    }

    /**
     * Returns the declared attribute at the given <code>index</code>.
     * 
     * @throws IndexOutOfBoundsException if no attribute exists for the given <code>index</code>.
     */
    public Attribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    /**
     * Returns the association with the given <code>name</code> declared in this type or one of it's
     * super types. The name could either be the singular or the plural name.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    public Association getAssociation(String name) {
        AssociationFinder finder = new AssociationFinder(name);
        finder.visitHierarchy(this);
        if (finder.association == null) {
            throw new IllegalArgumentException(
                    "The type " + this + " (or one of it's super types) hasn't got an association \"" + name + "\"");
        }
        return finder.association;
    }

    /**
     * Returns the attribute with the given <code>name</code> declared in this type or one of it's
     * super types.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public Attribute getAttribute(String name) {
        AttributeFinder finder = new AttributeFinder(name);
        finder.visitHierarchy(this);
        if (finder.attribute == null) {
            throw new IllegalArgumentException(
                    "The type " + this + " (or one of it's supertypes) hasn't got an attribute \"" + name + "\"");
        }
        return finder.attribute;
    }

    /**
     * Returns the {@link AnnotatedDeclaration} object for this model type that should be used to
     * read all annotations.
     */
    protected AnnotatedDeclaration getAnnotatedDeclaration() {
        return annotatedDeclaration;
    }

    /**
     * Returns the Java class for this type.
     */
    public Class<?> getJavaClass() {
        return annotatedDeclaration.getImplementationClass();
    }

    /**
     * Returns the published interface for this type. Returns <code>null</code> if published
     * interfaces are not generated.
     *
     * @see #findJavaInterface() findJavaInterface() for null-safe processing
     */
    public Class<?> getJavaInterface() {
        return annotatedDeclaration.getPublishedInterface();
    }

    /**
     * Returns the published interface for this type. if published interfaces are generated,
     * otherwise an {@link Optional#empty() empty Optional}.
     */
    public Optional<Class<?>> findJavaInterface() {
        return Optional.ofNullable(getJavaInterface());
    }

    public Class<?> getDeclarationClass() {
        return getJavaInterface() == null ? getJavaClass() : getJavaInterface();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        findSuperType().ifPresent(s -> {
            sb.append(" extends ");
            sb.append(s.getName());
        });
        return sb.toString();
    }

    /**
     * Searches for a method with the given annotation that matches the condition defined by a
     * {@link AnnotatedElementMatcher matcher}. Only methods in this type's declaration class are
     * considered, thus no methods from super classes are found.
     * 
     * @param annotationClass the class of the annotation the method must be annotated with
     * @param matcher matcher to determine if the annotation has the correct properties
     * @return the first method that is both annotated with the given annotation and has the correct
     *             annotated properties. <code>null</code> if no such method can be found.
     * 
     * @see #findDeclaredMethod(Class, AnnotatedElementMatcher) findDeclaredMethod for null-safe
     *          processing
     */
    public <T extends Annotation> Method searchDeclaredMethod(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        return findDeclaredMethod(annotationClass, matcher).orElse(null);
    }

    /**
     * Searches for a method with the given annotation that matches the condition defined by a
     * {@link AnnotatedElementMatcher matcher}. Only methods in this type's declaration class are
     * considered, thus no methods from super classes are found.
     * 
     * @param annotationClass the class of the annotation the method must be annotated with
     * @param matcher matcher to determine if the annotation has the correct properties
     * @return the first method that is both annotated with the given annotation and has the correct
     *             annotated properties. {@link Optional#empty()} if no such method can be found.
     */
    public <T extends Annotation> Optional<Method> findDeclaredMethod(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        return getDeclaredMethods().stream()
                .filter(method -> method.isAnnotationPresent(annotationClass)
                        && matcher.matches(method.getAnnotation(annotationClass)))
                .findFirst();
    }

    protected List<Method> getDeclaredMethods() {
        return getAnnotatedDeclaration().getDeclaredMethods();
    }

    /**
     * Searches for a field with the given annotation that matches the condition defined by a
     * {@link AnnotatedElementMatcher matcher}. Only fields in this type's declaration class are
     * considered, thus no fields from super classes are found.
     * 
     * @param annotationClass the class of the annotation the field must be annotated with
     * @param matcher matcher to determine if the annotation has the correct properties
     * @return the first field that is both annotated with the given annotation and has the correct
     *             annotated properties. {@link Optional#empty()} if no such field can be found.
     */
    public <T extends Annotation> Optional<Field> findDeclaredField(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        return getDeclaredFields().stream()
                .filter(field -> field.isAnnotationPresent(annotationClass)
                        && matcher.matches(field.getAnnotation(annotationClass)))
                .findFirst();
    }

    protected List<Field> getDeclaredFields() {
        return getAnnotatedDeclaration().getDeclaredFields();
    }

    /**
     * Returns a list containing all associations declared in this type. Associations defined in the
     * type's super types are not returned.
     * 
     * @return the list of associations declared in this type
     */
    public abstract List<? extends Association> getDeclaredAssociations();

    /**
     * Returns the type's associations including those defined in it's super types.
     * 
     * @return the list of all associations declared in this type or in any super type
     */
    public abstract List<? extends Association> getAssociations();

    /**
     * Returns the association with the given <code>name</code> declared in this type. Associations
     * defined in the type's super types are not considered. The name could either be the singular
     * or the plural name.
     * 
     * @param name the name of the association
     * @return the association if it was found in this type
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists
     * 
     * @see #isAssociationDeclared(String)
     */
    public abstract Association getDeclaredAssociation(String name);

    /**
     * Returns whether the association with the given <code>name</code> is declared in this type.
     * Associations defined in the type's super types are not considered.
     * 
     * @param name the name of the association
     * @return <code>true</code> if the association is declared in this type, <code>false</code> if
     *             not
     * 
     * @see #isAssociationPresent(String)
     */
    public abstract boolean isAssociationDeclared(String name);

    /**
     * Returns whether the association with the given <code>name</code> is declared in this type or
     * in any supertype.
     * 
     * @param name the name of the association
     * @return <code>true</code> if the association is declared in this type or in any supertype,
     *             <code>false</code> if not
     * 
     * @see #isAssociationDeclared(String)
     */
    public boolean isAssociationPresent(String name) {
        AssociationFinder finder = new AssociationFinder(name);
        finder.visitHierarchy(this);
        return finder.association != null;
    }

    /**
     * Returns a list containing all attributes declared in this model type. Attributes defined in
     * the type's super types are not returned.
     * 
     * @return the list of attributes declared in this type
     */
    public abstract List<? extends Attribute> getDeclaredAttributes();

    /**
     * Returns whether the attribute with the given <code>name</code> is declared in this type.
     * Attributes defined in the type's super types are not considered.
     * 
     * @param name the name of the attribute
     * @return <code>true</code> if the attribute is declared in this type, <code>false</code> if
     *             not
     */
    public abstract boolean isAttributeDeclared(String name);

    /**
     * Returns whether the attribute with the given <code>name</code> is declared in this type or in
     * any supertype.
     * 
     * @param name the name of the attribute
     * @return <code>true</code> if the attribute is declared in this type or in any supertype,
     *             <code>false</code> if not
     */
    public boolean isAttributePresent(String name) {
        AttributeFinder finder = new AttributeFinder(name);
        finder.visitHierarchy(this);
        return finder.attribute != null;
    }

    /**
     * Returns a list containing the type's attributes including those defined in the type's super
     * types.
     * 
     * @return the list of all attributes declared in this type or in any supertype
     */
    public abstract List<? extends Attribute> getAttributes();

    /**
     * Returns the attribute with the given <code>name</code> declared in this type. Attributes
     * defined in the type's super types are not returned.
     * 
     * @param name the name of the attribute
     * @return the attribute if it was found in this type
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists
     * 
     * @see #isAttributeDeclared(String)
     */
    public abstract Attribute getDeclaredAttribute(String name);

    /**
     * Returns whether this type has a super type.
     * 
     * @return <code>true</code> if this type has a supertype, <code>false</code> if not
     */
    public boolean isSuperTypePresent() {
        return getSuperType() != null;
    }

    /**
     * Returns this type's super type or <code>null</code> if it has none.
     * 
     * @see #findSuperType() findSuperType for null-safe processing
     */
    public abstract Type getSuperType();

    /**
     * Returns this type's super type if {@link #isSuperTypePresent() present}, otherwise an
     * {@link Optional#empty() empty Optional}.
     */
    public Optional<? extends Type> findSuperType() {
        return Optional.ofNullable(getSuperType());
    }

    @Override
    public String getDocumentation(Locale locale, DocumentationKind type, String fallback) {
        return Documentation.of(this, type, locale, fallback, this::findSuperType);
    }

    /**
     * Matcher for methods or fields based on annotation properties.
     * 
     * @param <T> is the type of annotation that is expected.
     */
    @FunctionalInterface
    public interface AnnotatedElementMatcher<T extends Annotation> {
        /**
         * 
         * @param annotation the annotation found.
         * @return <code>true</code> if the annotation matches the condition, <code>false</code>
         *             else.
         */
        boolean matches(T annotation);
    }

    static class AttributeCollector<T extends Attribute> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<>(30);
        private final Set<String> attributeNames = new HashSet<>();

        @Override
        public boolean visitType(Type type) {
            for (Attribute declaredAttribute : type.getDeclaredAttributes()) {
                if (!attributeNames.contains(declaredAttribute.getName())) {
                    attributeNames.add(declaredAttribute.getName());
                    @SuppressWarnings("unchecked")
                    T castedAttribute = (T)declaredAttribute;
                    result.add(castedAttribute);
                }
            }
            return true;
        }

        public List<T> getResult() {
            return result;
        }

    }

    static class AttributeFinder extends TypeHierarchyVisitor {

        private String attrName;
        private Attribute attribute = null;

        public AttributeFinder(String attrName) {
            super();
            this.attrName = IpsStringUtils.toLowerFirstChar(attrName);
        }

        @Override
        public boolean visitType(Type type) {
            boolean hasDeclaredAttribute = type.isAttributeDeclared(attrName);
            if (hasDeclaredAttribute) {
                attribute = type.getDeclaredAttribute(attrName);
            }
            return !hasDeclaredAttribute;
        }

    }

    static class AssociationsCollector<T extends Association> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<>();
        private final Set<String> associationNames = new HashSet<>();

        @Override
        public boolean visitType(Type type) {
            for (Association declaredAssociation : type.getDeclaredAssociations()) {
                if (!associationNames.contains(declaredAssociation.getName())) {
                    associationNames.add(declaredAssociation.getName());
                    @SuppressWarnings("unchecked")
                    T castedAssociation = (T)declaredAssociation;
                    result.add(castedAssociation);
                }
            }
            return true;
        }

        protected List<T> getResult() {
            return result;
        }

    }

    static class AssociationFinder extends TypeHierarchyVisitor {

        private String associationName;
        private Association association = null;

        public AssociationFinder(String associationName) {
            super();
            this.associationName = IpsStringUtils.toLowerFirstChar(associationName);
        }

        @Override
        public boolean visitType(Type type) {
            boolean hasDeclaredAssociation = type.isAssociationDeclared(associationName);
            if (hasDeclaredAssociation) {
                association = type.getDeclaredAssociation(associationName);
            }
            return !hasDeclaredAssociation;
        }
    }

}
