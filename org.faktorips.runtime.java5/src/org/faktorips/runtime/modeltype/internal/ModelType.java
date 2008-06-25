/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelType extends AbstractModelElement implements IModelType {

    private List<IModelTypeAssociation> associations = new ArrayList<IModelTypeAssociation>();
    private Map<String, IModelTypeAssociation> associationsByName = new HashMap<String, IModelTypeAssociation>();

    private List<IModelTypeAttribute> attributes = new ArrayList<IModelTypeAttribute>();
    private Map<String, IModelTypeAttribute> attributesByName = new HashMap<String, IModelTypeAttribute>();

    private String className;
    private String superTypeName;

    public ModelType(IRuntimeRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAssociation getAssociation(int index) {
        return associations.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAssociation getAssociation(String name) {
        return associationsByName.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAssociation> getAssociations() {
        return Collections.unmodifiableList(associations);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAttribute getAttribute(int index) throws IndexOutOfBoundsException {
        return attributes.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAttribute getAttribute(String name) throws IllegalArgumentException {
        return attributesByName.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassNotFoundException
     */
    public Class<?> getJavaClass() throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(className);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @SuppressWarnings("unchecked")
    public IModelType getSuperType() {
        if (superTypeName != null && superTypeName.length() > 0) {
            try {
                return getRepository().getModelType(
                        (Class<? extends IConfigurableModelObject>)this.getClass().getClassLoader().loadClass(
                                superTypeName));
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("class")) {
                this.className = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("supertype")) {
                this.superTypeName = parser.getAttributeValue(i);
            }
        }
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ExtensionProperties")) {
                        initExtPropertiesFromXml(parser);
                    } else if (parser.getLocalName().equals("ModelTypeAttributes")) {
                        initModelTypeAttributesFromXML(parser);
                    } else if (parser.getLocalName().equals("ModelTypeAssociations")) {
                        initModelTypeAssociationsFromXML(parser);
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initModelTypeAttributesFromXML(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAttribute")) {
                        IModelTypeAttribute attribute = new ModelTypeAttribute(getRepository());
                        attribute.initFromXml(parser);
                        attributes.add(attribute);
                        attributesByName.put(attribute.getName(), attribute);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAttributes")) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initModelTypeAssociationsFromXML(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAssociation")) {
                        IModelTypeAssociation association = new ModelTypeAssociation(getRepository());
                        association.initFromXml(parser);
                        associations.add(association);
                        associationsByName.put(association.getName(), association);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAssociations")) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (superTypeName != null && superTypeName.length() > 0) {
            sb.append(" extends ");
            sb.append(superTypeName);
        }
        return sb.toString();
    }

}
