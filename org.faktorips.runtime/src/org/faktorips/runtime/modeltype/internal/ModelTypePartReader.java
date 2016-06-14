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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.faktorips.runtime.model.annotation.AnnotatedType;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * Utility to initialize the parts of an annotated model type.
 * 
 */
public class ModelTypePartReader {

    private AnnotatedType annotatedModelType;

    private final LinkedHashMap<String, AttributeDescriptor> attributes = new LinkedHashMap<String, AttributeDescriptor>();

    private final LinkedHashMap<String, AssociationDescriptor> associations = new LinkedHashMap<String, AssociationDescriptor>();

    public ModelTypePartReader(AnnotatedType annotatedModelType) {
        this.annotatedModelType = annotatedModelType;
        readAnnotations();
    }

    private void readAnnotations() {
        initAttributes();
        initAssociations();
        readFields(annotatedModelType.getDeclaredFields());
        readMethods(annotatedModelType.getDeclaredMethods());
    }

    private void initAttributes() {
        if (annotatedModelType.is(IpsAttributes.class)) {
            String[] values = annotatedModelType.get(IpsAttributes.class).value();
            for (String name : values) {
                attributes.put(name, new AttributeDescriptor());
            }
        }
    }

    private void initAssociations() {
        if (annotatedModelType.is(IpsAssociations.class)) {
            String[] values = annotatedModelType.get(IpsAssociations.class).value();
            for (String name : values) {
                associations.put(name, new AssociationDescriptor());
            }
        }
    }

    private void readFields(List<Field> list) {
        for (Field field : list) {
            if (field.isAnnotationPresent(IpsAttribute.class)) {
                getAttributeDescriptor(field).field = field;
            }
        }
    }

    private void readMethods(List<Method> list) {
        for (Method method : list) {
            if (method.isAnnotationPresent(IpsAttribute.class)) {
                getAttributeDescriptor(method).getterMethod = method;
            }
            if (method.isAnnotationPresent(IpsAttributeSetter.class)) {
                getAttributeDescriptorForSetter(method).setterMethod = method;
            }
            if (method.isAnnotationPresent(IpsAssociation.class)) {
                getAssociationDescriptor(method).getterMethod = method;
            }
        }
    }

    public LinkedHashMap<String, IModelTypeAttribute> getAttributes(ModelType modelType) {
        LinkedHashMap<String, IModelTypeAttribute> result = new LinkedHashMap<String, IModelTypeAttribute>();
        for (Entry<String, AttributeDescriptor> entry : attributes.entrySet()) {
            AttributeDescriptor attributeDescriptor = entry.getValue();
            String name = entry.getKey();
            if (attributeDescriptor.isValid()) {
                result.put(name, attributeDescriptor.create(modelType));
            } else {
                throw new IllegalArgumentException(modelType.getAnnotatedModelType().toString() + " lists \"" + name
                        + "\" as one of it's @IpsAttributes but no matching @IpsAttribute could be found.");
            }
        }
        return result;
    }

    public LinkedHashMap<String, IModelTypeAssociation> getAssociations(ModelType modelType) {
        LinkedHashMap<String, IModelTypeAssociation> result = new LinkedHashMap<String, IModelTypeAssociation>();
        for (Entry<String, AssociationDescriptor> entry : associations.entrySet()) {
            AssociationDescriptor associationDescriptor = entry.getValue();
            String name = entry.getKey();
            if (associationDescriptor.isValid()) {
                result.put(name, associationDescriptor.create(modelType));
            } else {
                // else it must be defined in a super type but overridden (with the same name and
                // target) in this type. That leads to a different implementation being generated
                // but not a new annotation.
                IModelType superType = modelType.getSuperType();
                IModelTypeAssociation association = superType.getAssociation(name);
                if (association != null) {
                    result.put(name, association);
                } else {
                    throw new IllegalArgumentException(modelType.getAnnotatedModelType().toString() + " lists \""
                            + name
                            + "\" as one of it's @IpsAssociations but no matching @IpsAssociation could be found.");
                }
            }
        }
        return result;
    }

    private AttributeDescriptor getAttributeDescriptor(AnnotatedElement element) {
        AttributeDescriptor attributeDescriptor = attributes.get(element.getAnnotation(IpsAttribute.class).name());
        if (attributeDescriptor != null) {
            return attributeDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private AttributeDescriptor getAttributeDescriptorForSetter(AnnotatedElement element) {
        AttributeDescriptor attributeDescriptor = attributes.get(element.getAnnotation(IpsAttributeSetter.class)
                .value());
        if (attributeDescriptor != null) {
            return attributeDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private AssociationDescriptor getAssociationDescriptor(AnnotatedElement element) {
        AssociationDescriptor associationDescriptor = associations.get(element.getAnnotation(IpsAssociation.class)
                .name());
        if (associationDescriptor != null) {
            return associationDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private IllegalArgumentException invalidAnnotationsException(AnnotatedElement annotatedElement) {
        return new IllegalArgumentException("The element " + annotatedElement + " is not generated correctly.");
    }

    private static class AttributeDescriptor {

        private Field field;

        private Method getterMethod;

        private Method setterMethod;

        public AbstractModelTypeAttribute create(ModelType modelType) {
            if (field != null) {
                return new ModelTypeConstantAttribute(modelType, field);
            } else {
                return new ModelTypeAttribute(modelType, getterMethod, setterMethod);
            }
        }

        public boolean isValid() {
            return field != null || getterMethod != null;
        }

    }

    private static class AssociationDescriptor {

        private Method getterMethod;

        public IModelTypeAssociation create(ModelType modelType) {
            return new ModelTypeAssociation(modelType, getterMethod);
        }

        public boolean isValid() {
            return getterMethod != null;
        }

    }

}
