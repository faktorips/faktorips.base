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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAttribute extends AbstractModelElement implements IModelTypeAttribute {
    
    private Class<?> datatype;
    private ValueSetType valueSetType = ValueSetType.AllValues;
    private AttributeType attributeType = AttributeType.changeable;
    private boolean isProductRelevant = false;
    
    /**
     * {@inheritDoc}
     */
    public Class<?> getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public AttributeType getAttributeType() {
        return attributeType;
    }

    /**
     * {@inheritDoc}
     */
    public ValueSetType getValueSetType() {
        return valueSetType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("datatype")) {
                this.datatype = findDatatype(parser.getAttributeValue(i));
            }else if (parser.getAttributeLocalName(i).equals("valueSetType")) {
                this.valueSetType = ValueSetType.valueOf(parser.getAttributeValue(i));
            }else if (parser.getAttributeLocalName(i).equals("attributeType")) {
                this.attributeType = AttributeType.valueOf(parser.getAttributeValue(i));
            }else if (parser.getAttributeLocalName(i).equals("isProductRelevant")) {
                this.isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            }
        }
        initExtPropertiesFromXml(parser);
    }

    private Class<?> findDatatype(String attributeValue) {
        // TODO Auto-generated method stub
        return null;
    }

}
