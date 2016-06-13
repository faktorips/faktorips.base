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
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsages;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.ITableUsageModel;

/**
 * Utility to initialize the parts of an annotated model type.
 * 
 */
public class ModelTypePartReader {

    private final AnnotatedType annotatedModelType;

    private final ModelType modeltype;

    private final LinkedHashMap<String, AttributeDescriptor> attributeDescriptors;

    private final LinkedHashMap<String, AssociationDescriptor> associationDescriptors;

    private final LinkedHashMap<String, TableUsageDescriptor> tableUsageDescriptors;

    public ModelTypePartReader(AnnotatedType annotatedModelType, ModelType modeltype) {
        this.annotatedModelType = annotatedModelType;
        this.modeltype = modeltype;
        attributeDescriptors = initAttributes();
        associationDescriptors = initAssociations();
        tableUsageDescriptors = initTableUsage();
    }

    public ModelTypePartContainer readAnnotations() {
        readFields(annotatedModelType.getDeclaredFields());
        readMethods(annotatedModelType.getDeclaredMethods());

        LinkedHashMap<String, IModelTypeAttribute> attributes = getAttributes(modeltype);
        LinkedHashMap<String, IModelTypeAssociation> associations = getAssociations(modeltype);
        LinkedHashMap<String, ITableUsageModel> tableUsages = getTables();

        return new ModelTypePartContainer(attributes, associations, tableUsages);
    }

    private LinkedHashMap<String, AttributeDescriptor> initAttributes() {
        LinkedHashMap<String, AttributeDescriptor> descriptors = new LinkedHashMap<String, AttributeDescriptor>();
        if (annotatedModelType.is(IpsAttributes.class)) {
            String[] values = annotatedModelType.get(IpsAttributes.class).value();
            for (String name : values) {
                descriptors.put(name, new AttributeDescriptor());
            }
        }
        return descriptors;
    }

    private LinkedHashMap<String, AssociationDescriptor> initAssociations() {
        LinkedHashMap<String, AssociationDescriptor> descriptors = new LinkedHashMap<String, AssociationDescriptor>();
        if (annotatedModelType.is(IpsAssociations.class)) {
            String[] values = annotatedModelType.get(IpsAssociations.class).value();
            for (String name : values) {
                descriptors.put(name, new AssociationDescriptor());
            }
        }
        return descriptors;
    }

    private LinkedHashMap<String, TableUsageDescriptor> initTableUsage() {
        LinkedHashMap<String, TableUsageDescriptor> descriptors = new LinkedHashMap<String, TableUsageDescriptor>();
        if (annotatedModelType.is(IpsTableUsages.class)) {
            String[] values = annotatedModelType.get(IpsTableUsages.class).value();
            for (String tableName : values) {
                descriptors.put(tableName, new TableUsageDescriptor());
            }
        }
        return descriptors;
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
            if (method.isAnnotationPresent(IpsTableUsage.class)) {
                getTableUsageDescriptor(method).getterMethod = method;
            }
        }
    }

    private LinkedHashMap<String, IModelTypeAttribute> getAttributes(ModelType modelType) {
        LinkedHashMap<String, IModelTypeAttribute> result = new LinkedHashMap<String, IModelTypeAttribute>();
        for (Entry<String, AttributeDescriptor> entry : attributeDescriptors.entrySet()) {
            AttributeDescriptor attributeDescriptor = entry.getValue();
            String name = entry.getKey();
            if (attributeDescriptor.isValid()) {
                result.put(name, attributeDescriptor.create(modelType));
            } else {
                // it could be defined in a super type but overridden (with the same name and
                // target) in this type. That leads to a different implementation being generated
                // but not a new annotation.
                IModelType superType = modelType.getSuperType();
                IModelTypeAttribute attribute = superType.getAttribute(name);
                if (attribute != null) {
                    IModelTypeAttribute overwritingAttribute = ((AbstractModelTypeAttribute)attribute)
                            .createOverwritingAttributeFor(modelType);
                    result.put(name, overwritingAttribute);
                } else {
                    throw new IllegalArgumentException(getAnnotatedJavaClass(modelType) + " lists \"" + name
                            + "\" as one of it's @IpsAttributes but no matching @IpsAttribute could be found.");
                }
            }
        }
        return result;
    }

    private Class<? extends Object> getAnnotatedJavaClass(ModelType modelType) {
        return modelType.getJavaInterface() == null ? modelType.getJavaClass() : modelType.getJavaInterface();
    }

    private LinkedHashMap<String, IModelTypeAssociation> getAssociations(ModelType modelType) {
        LinkedHashMap<String, IModelTypeAssociation> result = new LinkedHashMap<String, IModelTypeAssociation>();
        for (Entry<String, AssociationDescriptor> entry : associationDescriptors.entrySet()) {
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
                    ModelTypeAssociation overwritingAssociation = ((ModelTypeAssociation)association)
                            .createOverwritingAssociationFor(modelType);
                    result.put(name, overwritingAssociation);
                } else {
                    throw new IllegalArgumentException(getAnnotatedJavaClass(modelType) + " lists \"" + name
                            + "\" as one of it's @IpsAssociations but no matching @IpsAssociation could be found.");
                }
            }
        }
        return result;
    }

    private AttributeDescriptor getAttributeDescriptor(AnnotatedElement element) {
        AttributeDescriptor attributeDescriptor = attributeDescriptors.get(element.getAnnotation(IpsAttribute.class)
                .name());
        if (attributeDescriptor != null) {
            return attributeDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private AttributeDescriptor getAttributeDescriptorForSetter(AnnotatedElement element) {
        AttributeDescriptor attributeDescriptor = attributeDescriptors.get(element.getAnnotation(
                IpsAttributeSetter.class).value());
        if (attributeDescriptor != null) {
            return attributeDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private AssociationDescriptor getAssociationDescriptor(AnnotatedElement element) {
        AssociationDescriptor associationDescriptor = associationDescriptors.get(element.getAnnotation(
                IpsAssociation.class).name());
        if (associationDescriptor != null) {
            return associationDescriptor;
        } else {
            throw invalidAnnotationsException(element);
        }
    }

    private LinkedHashMap<String, ITableUsageModel> getTables() {
        LinkedHashMap<String, ITableUsageModel> tableUsages = new LinkedHashMap<String, ITableUsageModel>();
        for (Entry<String, TableUsageDescriptor> entry : tableUsageDescriptors.entrySet()) {
            tableUsages.put(entry.getKey(), entry.getValue().create(modeltype));
        }
        return tableUsages;
    }

    private TableUsageDescriptor getTableUsageDescriptor(AnnotatedElement element) {
        TableUsageDescriptor tableUsageDescriptor = tableUsageDescriptors.get(element
                .getAnnotation(IpsTableUsage.class).name());
        if (tableUsageDescriptor != null) {
            return tableUsageDescriptor;
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

    private static class TableUsageDescriptor {

        private Method getterMethod;

        public ITableUsageModel create(ModelType modelType) {
            return new TableUsageModel(modelType, getterMethod);
        }
    }
}
