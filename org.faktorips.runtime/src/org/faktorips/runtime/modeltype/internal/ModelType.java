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
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.TypeHierarchyVisitor;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * 
 * @author Daniel Hohenberger
 */
public abstract class ModelType extends AbstractModelElement implements IModelType {

    private final AnnotatedDeclaration annotatedDeclaration;

    private final MessagesHelper messagesHelper;

    public ModelType(String name, AnnotatedDeclaration annotatedModelType) {
        super(name, annotatedModelType.get(IpsExtensionProperties.class));
        this.annotatedDeclaration = annotatedModelType;
        IpsDocumented ipsDocumented = annotatedModelType.get(IpsDocumented.class);
        messagesHelper = createMessageHelper(ipsDocumented, annotatedModelType.getClassLoader());
    }

    protected abstract String getKindName();

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getName(), getKindName(), IpsStringUtils.EMPTY);
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    @Override
    public IModelTypeAssociation getDeclaredAssociation(int index) {
        return getDeclaredAssociations().get(index);
    }

    @Override
    public IModelTypeAttribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    @Override
    public IModelTypeAssociation getAssociation(String name) {
        AssociationFinder finder = new AssociationFinder(name);
        finder.visitHierarchy(this);
        if (finder.association == null) {
            throw new IllegalArgumentException("The type " + this
                    + " (or one of it's super types) hasn't got an association \"" + name + "\"");
        }
        return finder.association;
    }

    @Override
    public IModelTypeAttribute getAttribute(String name) {
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

    @Override
    public Class<?> getJavaClass() {
        return annotatedDeclaration.getImplementationClass();
    }

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
     * {@link AnnotatedElementMatcher matcher}. Only methods in this type model's declaration class
     * are considered, thus no methods from super classes are found.
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

    static class AttributeCollector<T extends IModelTypeAttribute> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<T>(30);
        private final Set<String> attributeNames = new HashSet<String>();

        @Override
        public boolean visitType(IModelType type) {
            for (IModelTypeAttribute declaredAttribute : type.getDeclaredAttributes()) {
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
        private IModelTypeAttribute attribute = null;

        public AttributeFinder(String attrName) {
            super();
            this.attrName = attrName;
        }

        @Override
        public boolean visitType(IModelType type) {
            try {
                attribute = ((ModelType)type).getDeclaredAttribute(attrName);
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }

    }

    static class AssociationsCollector<T extends IModelTypeAssociation> extends TypeHierarchyVisitor {

        private final List<T> result = new ArrayList<T>();
        private final Set<String> associationNames = new HashSet<String>();

        @Override
        public boolean visitType(IModelType type) {
            for (IModelTypeAssociation declaredAssociation : type.getDeclaredAssociations()) {
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
        private IModelTypeAssociation association = null;

        public AssociationFinder(String associationName) {
            super();
            this.associationName = associationName;
        }

        @Override
        public boolean visitType(IModelType type) {
            association = ((ModelType)type).getDeclaredAssociation(associationName);
            return association == null;
        }

    }

}
