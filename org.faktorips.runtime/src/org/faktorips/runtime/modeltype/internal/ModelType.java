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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedType;
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

    private final AnnotatedType annotatedModelType;

    private final LinkedHashMap<String, IModelTypeAttribute> attributesByName;

    private final LinkedHashMap<String, IModelTypeAssociation> associationsByName;

    private final MessagesHelper messagesHelper;

    public ModelType(String name, AnnotatedType annotatedModelType) {
        super(name, annotatedModelType.get(IpsExtensionProperties.class));
        this.annotatedModelType = annotatedModelType;
        ModelTypePartReader annotationReader = new ModelTypePartReader(annotatedModelType);
        attributesByName = annotationReader.getAttributes(this);
        associationsByName = annotationReader.getAssociations(this);
        IpsDocumented ipsDocumented = annotatedModelType.get(IpsDocumented.class);
        messagesHelper = createMessageHelper(ipsDocumented, annotatedModelType.getClassLoader());
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getName(), IpsStringUtils.EMPTY);
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
    public IModelTypeAssociation getDeclaredAssociation(String name) {
        return associationsByName.get(name);
    }

    @Override
    public List<IModelTypeAssociation> getDeclaredAssociations() {
        return new ArrayList<IModelTypeAssociation>(associationsByName.values());
    }

    @Override
    public List<IModelTypeAssociation> getAssociations() {
        AssociationsCollector asscCollector = new AssociationsCollector();
        asscCollector.visitHierarchy(this);
        return asscCollector.result;
    }

    @Override
    public IModelTypeAssociation getAssociation(String name) {
        AssociationFinder finder = new AssociationFinder(name);
        finder.visitHierarchy(this);
        if (finder.association == null) {
            throw new IllegalArgumentException("The type " + this
                    + "(or one of it's supertypes) hasn't got an association " + name);
        }
        return finder.association;
    }

    @Override
    public List<IModelObject> getTargetObjects(IModelObject source, String associationName) {
        return getAssociation(associationName).getTargetObjects(source);
    }

    @Override
    public IModelTypeAttribute getDeclaredAttribute(int index) {
        return getDeclaredAttributes().get(index);
    }

    @Override
    public IModelTypeAttribute getDeclaredAttribute(String name) {
        IModelTypeAttribute attr = attributesByName.get(name);
        if (attr == null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute " + name);
        }
        return attr;
    }

    @Override
    public IModelTypeAttribute getAttribute(String name) {
        AttributeFinder finder = new AttributeFinder(name);
        finder.visitHierarchy(this);
        if (finder.attribute == null) {
            throw new IllegalArgumentException("The type " + this
                    + "(or one of it's supertypes) hasn't got an attribute " + name);
        }
        return finder.attribute;
    }

    @Override
    public List<IModelTypeAttribute> getDeclaredAttributes() {
        return new ArrayList<IModelTypeAttribute>(attributesByName.values());
    }

    @Override
    public List<IModelTypeAttribute> getAttributes() {
        AttributeCollector attrCollector = new AttributeCollector();
        attrCollector.visitHierarchy(this);
        return attrCollector.result;
    }

    @Override
    public Object getAttributeValue(IModelObject source, String attributeName) {
        return getAttribute(attributeName).getValue(source);
    }

    @Override
    public void setAttributeValue(IModelObject source, String attributeName, Object value) {
        getAttribute(attributeName).setValue(source, value);
    }

    /**
     * Returns the {@link AnnotatedType} object for this model type that should be used to read all
     * annotations.
     */
    protected AnnotatedType getAnnotatedModelType() {
        return annotatedModelType;
    }

    @Override
    public Class<?> getJavaClass() {
        return annotatedModelType.getImplementationClass();
    }

    @Override
    public Class<?> getJavaInterface() {
        return annotatedModelType.getPublishedInterface();
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

    static class AttributeCollector extends TypeHierarchyVisitor {

        private List<IModelTypeAttribute> result = new ArrayList<IModelTypeAttribute>(30);
        private Set<String> attributeNames = new HashSet<String>();

        @Override
        public boolean visitType(IModelType type) {
            for (IModelTypeAttribute declaredAttribute : type.getDeclaredAttributes()) {
                if (!attributeNames.contains(declaredAttribute.getName())) {
                    attributeNames.add(declaredAttribute.getName());
                    result.add(declaredAttribute);
                }
            }
            return true;
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
            attribute = ((ModelType)type).attributesByName.get(attrName);
            return attribute == null;
        }

    }

    static class AssociationsCollector extends TypeHierarchyVisitor {

        private List<IModelTypeAssociation> result = new ArrayList<IModelTypeAssociation>();
        private Set<String> associationNames = new HashSet<String>();

        @Override
        public boolean visitType(IModelType type) {
            for (IModelTypeAssociation declaredAssociation : type.getDeclaredAssociations()) {
                if (!associationNames.contains(declaredAssociation.getName())) {
                    associationNames.add(declaredAssociation.getName());
                    result.add(declaredAssociation);
                }
            }
            return true;
        }

    }

    static class AssociationFinder extends TypeHierarchyVisitor {

        private String associationName;
        private IModelTypeAssociation association = null;

        public AssociationFinder(String attrName) {
            super();
            this.associationName = attrName;
        }

        @Override
        public boolean visitType(IModelType type) {
            association = ((ModelType)type).associationsByName.get(associationName);
            return association == null;
        }

    }

}
