/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAttribute extends AbstractModelElement implements IModelTypeAttribute {

    private ModelType modelType;

    private Class<?> datatype;

    private String datatypeName;

    private ValueSetType valueSetType = ValueSetType.AllValues;

    private AttributeType attributeType = AttributeType.CHANGEABLE;

    private boolean isProductRelevant = false;

    public ModelTypeAttribute(ModelType modelType) {
        super(modelType.getRepository());
        this.modelType = modelType;
    }

    public IModelType getModelType() {
        return modelType;
    }

    public Class<?> getDatatype() throws ClassNotFoundException {
        if (datatype == null) {
            datatype = findDatatype();
        }
        return datatype;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public ValueSetType getValueSetType() {
        return valueSetType;
    }

    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals(PROPERTY_DATATYPE)) {
                datatypeName = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_VALUE_SET_TYPE)) {
                valueSetType = ValueSetType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_ATTRIBUTE_TYPE)) {
                attributeType = AttributeType.forName(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_PRODUCT_RELEVANT)) {
                isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            }
        }

        parser.next();
        initLabelsFromXml(parser);

        initExtPropertiesFromXml(parser);
    }

    protected Class<?> findDatatype() {
        String actualName = datatypeName;
        int arraydepth = 0;
        while (actualName.lastIndexOf('[') > 0) {
            actualName = actualName.substring(0, actualName.lastIndexOf('['));
            arraydepth++;
        }
        if (arraydepth > 0) {
            if ("boolean".equals(actualName)) {
                actualName = "Z";
            } else if ("byte".equals(actualName)) {
                actualName = "B";
            } else if ("char".equals(actualName)) {
                actualName = "C";
            } else if ("double".equals(actualName)) {
                actualName = "D";
            } else if ("float".equals(actualName)) {
                actualName = "F";
            } else if ("int".equals(actualName)) {
                actualName = "I";
            } else if ("long".equals(actualName)) {
                actualName = "J";
            } else if ("short".equals(actualName)) {
                actualName = "S";
            } else {
                actualName = "L" + actualName + ";";
            }
            char[] da = new char[arraydepth];
            java.util.Arrays.fill(da, '[');
            actualName = new String(da) + actualName;
        }
        if (actualName.equals(boolean.class.getName())) {
            return boolean.class;
        } else if (actualName.equals(byte.class.getName())) {
            return byte.class;
        } else if (actualName.equals(char.class.getName())) {
            return char.class;
        } else if (actualName.equals(double.class.getName())) {
            return double.class;
        } else if (actualName.equals(float.class.getName())) {
            return float.class;
        } else if (actualName.equals(int.class.getName())) {
            return int.class;
        } else if (actualName.equals(long.class.getName())) {
            return long.class;
        } else if (actualName.equals(short.class.getName())) {
            return short.class;
        }
        return loadClass(actualName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatypeName);
        sb.append('(');
        sb.append(attributeType);
        sb.append(", ");
        sb.append(valueSetType);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

}
