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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * A {@link Type} represents a design time type of Faktor-IPS. It provides all meta data for the
 * type as well as for properties like {@linkplain Attribute attributes} or {@linkplain Association
 * associations}.
 */
@SuppressWarnings("deprecation")
public abstract class Type extends ModelElement implements IModelType {

    private final AnnotatedDeclaration annotatedDeclaration;

    private final MessagesHelper messagesHelper;

    public Type(String name, AnnotatedDeclaration annotatedDeclaration) {
        super(name, annotatedDeclaration.get(IpsExtensionProperties.class));
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
    @Override
    public Association getDeclaredAssociation(int index) {
        return getDeclaredAssociations().get(index);
    }

    /**
     * Returns the declared attribute at the given <code>index</code>.
     * 
     * @throws IndexOutOfBoundsException if no attribute exists for the given <code>index</code>.
     */
    @Override
    public Attribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    /**
     * Returns the association with the given <code>name</code> declared in this type or one of it's
     * super types. The name could either be the singular or the plural name.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    @Override
    public Association getAssociation(String name) {
        AssociationFinder finder = new AssociationFinder(name);
        finder.visitHierarchy(this);
        if (finder.association == null) {
            throw new IllegalArgumentException("The type " + this
                    + " (or one of it's super types) hasn't got an association \"" + name + "\"");
        }
        return finder.association;
    }

    /**
     * Returns the attribute with the given <code>name</code> declared in this type or one of it's
     * super types.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    @Override
    public Attribute getAttribute(String name) {
        AttributeFinder finder = new AttributeFinder(name);
        finder.visitHierarchy(this);
        if (finder.attribute == null) {
            throw new IllegalArgumentException("The type " + this
                    + " (or one of it's supertypes) hasn't got an attribute \"" + name + "\"");
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
    @Override
    public Class<?> getJavaClass() {
        return annotatedDeclaration.getImplementationClass();
    }

    /**
     * Returns the published interface for this type. Returns <code>null</code> if published
     * interfaces are not generated.
     */
    @Override
    public Class<?> getJavaInterface() {
        return annotatedDeclaration.getPublishedInterface();
    }

    public Class<?> getDeclarationClass() {
        return getJavaInterface() == null ? getJavaClass() : getJavaInterface();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (getSuperType() != null) {
            sb.append(" extends ");
            sb.append(getSuperType().getName());
        }
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
     *         annotated properties. <code>null</code> if no such method can be found.
     */
    public <T extends Annotation> Method searchDeclaredMethod(Class<T> annotationClass,
            AnnotatedElementMatcher<T> matcher) {
        List<Method> declaredMethods = getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(annotationClass) && matcher.matches(method.getAnnotation(annotationClass))) {
                return method;
            }
        }
        return null;
    }

    protected List<Method> getDeclaredMethods() {
        return getAnnotatedDeclaration().getDeclaredMethods();
    }

    /**
     * Returns a list containing all associations declared in this type. Associations defined in the
     * type's super types are not returned.
     */
    @Override
    public abstract List<? extends Association> getDeclaredAssociations();

    /**
     * Returns the type's associations including those defined in it's super types.
     */
    @Override
    public abstract List<? extends Association> getAssociations();

    /**
     * Returns the association with the given <code>name</code> declared in this type. Associations
     * defined in the type's super types are not considered. The name could either be the singular
     * or the plural name.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    @Override
    public abstract Association getDeclaredAssociation(String name);

    /**
     * Returns a list containing all attributes declared in this model type. Attributes defined in
     * the type's super types are not returned.
     */
    @Override
    public abstract List<? extends Attribute> getDeclaredAttributes();

    /**
     * Returns a list containing the type's attributes including those defined in the type's super
     * types.
     */
    @Override
    public abstract List<? extends Attribute> getAttributes();

    /**
     * Returns the attribute with the given <code>name</code> declared in this type. Attributes
     * defined in the type's super types are not returned.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    @Override
    public abstract Attribute getDeclaredAttribute(String name);

    /**
     * Returns this type's super type or <code>null</code> if it has none.
     */
    @Override
    public abstract Type getSuperType();

    /**
     * Matcher for methods or fields based on annotation properties.
     * 
     * @param <T> is the type of annotation that is expected.
     */
    public abstract static class AnnotatedElementMatcher<T extends Annotation> {
        /**
         * 
         * @param annotation the annotation found.
         * @return <code>true</code> if the annotation matches the condition, <code>false</code>
         *         else.
         */
        public abstract boolean matches(T annotation);
    }

    static class AttributeCollector<T extends Attribute> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<T>(30);
        private final Set<String> attributeNames = new HashSet<String>();

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
            this.attrName = attrName;
        }

        @Override
        public boolean visitType(Type type) {
            try {
                attribute = type.getDeclaredAttribute(attrName);
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }

    }

    static class AssociationsCollector<T extends Association> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<T>();
        private final Set<String> associationNames = new HashSet<String>();

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
            this.associationName = associationName;
        }

        @Override
        public boolean visitType(Type type) {
            association = type.getDeclaredAssociation(associationName);
            return association == null;
        }

    }

}
