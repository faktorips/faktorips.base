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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.TypeHierarchyVisitor;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelType extends AbstractModelElement implements IModelType {

    private List<IModelTypeAssociation> associations = new ArrayList<IModelTypeAssociation>();
    private Map<String, IModelTypeAssociation> associationsByName = new HashMap<String, IModelTypeAssociation>();

    private List<IModelTypeAttribute> attributes = new ArrayList<IModelTypeAttribute>();
    private Map<String, IModelTypeAttribute> attributesByName = new HashMap<String, IModelTypeAttribute>();

    private Class<?> implementationClass;
    private Class<?> interfaceClass;

    public ModelType(String name, Class<?> implementationClass) {
        super(name);
        this.implementationClass = implementationClass;
    }

    @Override
    public IModelTypeAssociation getDeclaredAssociation(int index) {
        return associations.get(index);
    }

    @Override
    public IModelTypeAssociation getDeclaredAssociation(String name) {
        return associationsByName.get(name);
    }

    @Override
    public List<IModelTypeAssociation> getDeclaredAssociations() {
        return Collections.unmodifiableList(associations);
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
        return attributes.get(index);
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
        return Collections.unmodifiableList(attributes);
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

    @Override
    public Class<?> getJavaClass() {
        return implementationClass;
    }

    @Override
    public Class<?> getJavaInterface() {
        return interfaceClass;
    }

    @Override
    public IModelType getSuperType() {
        return Models.getModelType(implementationClass.getSuperclass());
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

        List<IModelTypeAttribute> result = new ArrayList<IModelTypeAttribute>(30);

        @Override
        public boolean visitType(IModelType type) {
            result.addAll(type.getDeclaredAttributes());
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

        List<IModelTypeAssociation> result = new ArrayList<IModelTypeAssociation>();

        @Override
        public boolean visitType(IModelType type) {
            result.addAll(type.getDeclaredAssociations());
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
