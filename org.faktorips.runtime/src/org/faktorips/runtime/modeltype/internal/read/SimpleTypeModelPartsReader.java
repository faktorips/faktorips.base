/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal.read;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.modeltype.IModelElement;

/**
 * A simpler {@link TypeModelPartsReader} that creates it's own {@link ModelPartCollector} for the
 * case that a {@link IModelElement} is defined with annotations on a single getter method and all
 * child elements are listed in an annotation on their parent element.
 */
public class SimpleTypeModelPartsReader<E extends IModelElement, P extends Annotation, C extends Annotation> {
    private SimpleGetterMethodCollector<E, P, C> collector;
    private TypeModelPartsReader typeModelPartsReader;
    private Class<P> parentAnnotation;

    /**
     * @param parentAnnotation the type of annotation on the parent class
     * @param namesAccessor used to get the list of names from the parent class annotated with the
     *            {@code parentAnnotation}
     * @param childAnnotation the type of annotation identifying a child element
     * @param nameAccessor used to get the name of the child element from a method annotated with
     *            the {@code childAnnotation}
     * @param modelElementCreator used to create a new {@link IModelElement} with the name returned
     *            by the {@code nameAccessor} and a reference to the method annotated with the
     *            {@code childAnnotation}
     */
    public SimpleTypeModelPartsReader(Class<P> parentAnnotation, NamesAccessor<P> namesAccessor,
            Class<C> childAnnotation, NameAccessor<C> nameAccessor, ModelElementCreator<E> modelElementCreator) {
        this.parentAnnotation = parentAnnotation;
        collector = new SimpleGetterMethodCollector<E, P, C>(parentAnnotation, namesAccessor, childAnnotation,
                nameAccessor, modelElementCreator);
        typeModelPartsReader = new TypeModelPartsReader(collector);
    }

    /**
     * Reads the names of all child elements from the annotated class, finds the annotated child
     * elements and creates new parts referencing their parent model.
     * 
     * @return a map of all child elements referenced by their name in the order defined in the
     *         annotated class.
     */
    public LinkedHashMap<String, E> createParts(Class<?> annotatedClass, IModelElement parentModel) {
        return createParts(annotatedClass, annotatedClass, parentModel);
    }

    /**
     * Reads the names of all child elements from {@code classWithChildNameList}, finds the
     * annotated child elements in the {@code classWithGetterMethods} and creates new parts
     * referencing their parent model.
     * 
     * @return a map of all child elements referenced by their name in the order defined in the
     *         {@code classWithChildNameList}.
     */
    public LinkedHashMap<String, E> createParts(Class<?> classWithChildNameList,
            Class<?> classWithGetterMethods,
            IModelElement parentModel) {
        typeModelPartsReader.init(AnnotatedDeclaration.from(classWithChildNameList));
        readMethodsFromAnnotatedParentInterfaces(classWithGetterMethods);
        typeModelPartsReader.read(AnnotatedDeclaration.from(classWithGetterMethods));
        return collector.createParts(parentModel);
    }

    private void readMethodsFromAnnotatedParentInterfaces(Class<?> classWithGetterMethods) {
        if (classWithGetterMethods.isInterface()) {
            Deque<Class<?>> superInterfaces = new LinkedList<Class<?>>();
            Class<?> superInterfaceWithParentAnnotation = findSuperInterfaceWithParentAnnotation(classWithGetterMethods);
            while (superInterfaceWithParentAnnotation != null) {
                superInterfaces.push(superInterfaceWithParentAnnotation);
                superInterfaceWithParentAnnotation = findSuperInterfaceWithParentAnnotation(superInterfaceWithParentAnnotation);
            }
            while (!superInterfaces.isEmpty()) {
                typeModelPartsReader.read(AnnotatedDeclaration.from(superInterfaces.pop()));
            }
        }
    }

    private Class<?> findSuperInterfaceWithParentAnnotation(Class<?> iface) {
        for (Class<?> superInterface : iface.getInterfaces()) {
            if (superInterface.isAnnotationPresent(parentAnnotation)) {
                return superInterface;
            }
            Class<?> interfaceWithAnnotation = findSuperInterfaceWithParentAnnotation(superInterface);
            if (interfaceWithAnnotation != null) {
                return interfaceWithAnnotation;
            }
        }
        return null;
    }

    /**
     * Gets the list of names from the parent class annotated with the {@link Annotation} {@code A}
     */
    // @FunctionalInterface
    public static interface NamesAccessor<A extends Annotation> {

        /**
         * Gets the list of names from the parent class annotated with the {@link Annotation}
         * {@code A}
         */
        public String[] getNames(A annotation);
    }

    /**
     * Gets the name of the child element from a method annotated with the {@link Annotation}
     * {@code C}
     */
    // @FunctionalInterface
    public static interface NameAccessor<C extends Annotation> {

        /**
         * Gets the name of the child element from a method annotated with the {@link Annotation}
         * {@code C}
         */
        public String getName(C annotation);
    }

    /**
     * Creates a new {@link IModelElement} under the given parent element with the given name and
     * getter method.
     */
    // @FunctionalInterface
    public static interface ModelElementCreator<T extends IModelElement> {
        /**
         * Creates a new {@link IModelElement} under the given parent element with the given name
         * and getter method.
         */
        public T create(IModelElement parentElement, String name, Method getterMethod);
    }
}