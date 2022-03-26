/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NamesAccessor;

class SimpleGetterMethodCollector<E extends ModelElement, P extends Annotation, C extends Annotation> extends
        TypePartCollector<E, SimpleGetterMethodModelDescriptor<E>> {
    private Class<P> annotationClass;
    private NamesAccessor<P> namesAccessor;
    private ModelElementCreator<E> modelElementCreator;

    public SimpleGetterMethodCollector(Class<P> classAnnotationClass, NamesAccessor<P> namesAccessor,
            Class<C> getterAnnotationClass, NameAccessor<C> nameAccessor, ModelElementCreator<E> modelElementCreator) {
        super(Arrays.<AnnotationProcessor<?, SimpleGetterMethodModelDescriptor<E>>> asList(
                new SimpleGetterMethodAnnotationProcessor<C, SimpleGetterMethodModelDescriptor<E>>(
                        getterAnnotationClass, nameAccessor)));
        this.annotationClass = classAnnotationClass;
        this.namesAccessor = namesAccessor;
        this.modelElementCreator = modelElementCreator;
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(annotationClass)) {
            return namesAccessor.getNames(annotatedDeclaration.get(annotationClass));
        } else {
            return NO_NAMES;
        }
    }

    @Override
    protected SimpleGetterMethodModelDescriptor<E> createDescriptor() {
        return new SimpleGetterMethodModelDescriptor<>(modelElementCreator);
    }

    @Override
    public LinkedHashMap<String, E> createParts(ModelElement parentModel) {
        checkIfAllDeclaredNamesHaveCorrespondingMethod();
        return super.createParts(parentModel);
    }

    private void checkIfAllDeclaredNamesHaveCorrespondingMethod() {
        LinkedList<String> declaredButNoGetterFound = new LinkedList<>();
        for (Entry<String, SimpleGetterMethodModelDescriptor<E>> entry : getDescriptors().entrySet()) {
            if (entry.getValue().getGetterMethod() == null) {
                declaredButNoGetterFound.add(entry.getKey());
            }
        }
        if (!declaredButNoGetterFound.isEmpty()) {
            String s = declaredButNoGetterFound.size() > 1 ? "s" : "";
            throw new IllegalStateException("No getter method" + s + " found for annotated part" + s + " \""
                    + IpsStringUtils.join(declaredButNoGetterFound, "\", \"") + "\"");
        }
    }
}