/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * EnumSet represents a value set of discrete values, each value has to be explicitly defined.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSet extends ValueSet implements IEnumValueSet {

    public static final String XML_TAG = "Enum"; //$NON-NLS-1$    
    private static final String XML_VALUE = "Value"; //$NON-NLS-1$

    private ArrayList elements = new ArrayList();

    public EnumValueSet(IIpsObjectPart parent, int partId) {
        super(ValueSetType.ENUM, parent, partId);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValues() {
        return (String[])elements.toArray(new String[elements.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value) {
        return containsValue(value, new MessageList(), null, null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value, MessageList list, Object invalidObject, String invalidProperty) {
        if (list == null) {
            throw new NullPointerException("MessageList required"); //$NON-NLS-1$
        }

        ValueDatatype datatype = getValueDatatype();
        if (datatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.EnumValueSet__msgDatatypeUnknown,
                    invalidObject, getProperty(invalidProperty, IConfigElement.PROPERTY_VALUE));
            // if the value is null we can still decide if the value is part of the set
            if (value == null && getContainsNull()) {
                return true;
            }
            return false;
        }

        if (value == null && getContainsNull()) {
            return true;
        }
        /*
         * An abstract valueset is considered containing all values. See #isAbstract()
         */
        if (isAbstract()) {
            return true;
        }

        if (!datatype.isParsable(value)) {
            String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsable, value, datatype.getName());
            addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, getProperty(invalidProperty,
                    IConfigElement.PROPERTY_VALUE));
            return false;
        }

        for (Iterator it = elements.iterator(); it.hasNext();) {
            String each = (String)it.next();
            if (datatype.isParsable(each) && datatype.areValuesEqual(each, value)) {
                return true;
            }
        }

        String text = Messages.EnumValueSet_msgValueNotInEnumeration;
        addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text, invalidObject, getProperty(invalidProperty,
                IConfigElement.PROPERTY_VALUE));

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list, Object invalidObject, String invalidProperty) {
        if (list == null) {
            throw new NullPointerException("MessageList required"); //$NON-NLS-1$
        }

        ValueDatatype datatype = getValueDatatype();
        ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
        if (datatype == null || subDatatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.EnumValueSet__msgDatatypeUnknown,
                    invalidObject, getProperty(invalidProperty, PROPERTY_VALUES));
            return false;
        }

        if (!(subset instanceof EnumValueSet)) {
            addMsg(list, MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING, Messages.EnumValueSet_msgNotAnEnumValueset,
                    invalidObject, getProperty(invalidProperty, PROPERTY_VALUES));
            return false;
        }

        if (!datatype.getQualifiedName().equals(subDatatype.getQualifiedName())) {
            String msg = NLS.bind(Messages.EnumValueSet_msgDatatypeMissmatch, subDatatype.getQualifiedName(), datatype
                    .getQualifiedName());
            addMsg(list, MSGCODE_DATATYPES_NOT_MATCHING, msg, invalidObject, getProperty(invalidProperty,
                    PROPERTY_VALUES));
            return false;
        }

        /*
         * An abstract valueset is considered containing all values and thus all non-abstract
         * EnumValueSets. See #isAbstract()
         */
        if (isAbstract()) {
            return true;
        } else {
            IEnumValueSet enumSubset = (IEnumValueSet)subset;
            String[] subsetValues = enumSubset.getValues();

            boolean contains = true;
            MessageList dummy = new MessageList();
            for (int i = 0; i < subsetValues.length && contains; i++) {
                contains = this.containsValue(subsetValues[i], dummy, invalidObject, getProperty(invalidProperty,
                        PROPERTY_VALUES));
            }

            if (!contains) {
                String msg = NLS.bind(Messages.EnumValueSet_msgNotSubset, enumSubset.toShortString(), toShortString());
                addMsg(list, MSGCODE_NOT_SUBSET, msg, invalidObject, getProperty(invalidProperty, PROPERTY_VALUES));
            }

            return contains;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset) {
        return containsValueSet(subset, new MessageList(), null, null);
    }

    /**
     * {@inheritDoc}
     */
    public void addValue(String val) {
        elements.add(val);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void removeValue(int index) {
        elements.remove(index);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void removeValue(String value) {
        elements.remove(value);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public String getValue(int index) {
        return (String)elements.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(int index, String value) {
        String oldValue = (String)elements.get(index);
        elements.set(index, value);
        valueChanged(oldValue, value);
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return elements.size();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValuesNotContained(IEnumValueSet otherSet) {
        List result = new ArrayList();
        if (otherSet == null) {
            return (String[])result.toArray(new String[result.size()]);
        }
        for (int i = 0; i < otherSet.size(); i++) {
            if (!elements.contains(otherSet.getValue(i))) {
                result.add(otherSet.getValue(i));
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        ValueDatatype datatype = getValueDatatype();

        int numOfValues = elements.size();
        for (int i = 0; i < numOfValues; i++) {
            String value = (String)elements.get(i);
            if (datatype == null) {
                String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsableDatatypeUnknown, getNotNullValue(value));
                list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, msg, Message.WARNING, this, PROPERTY_VALUES));
            } else if (!datatype.isParsable(value) || isSpecialNull(value, datatype)) {
                String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsable, getNotNullValue(value), datatype
                        .getName());
                list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, this, PROPERTY_VALUES));
            }
        }
        for (int i = 0; i < numOfValues - 1; i++) {
            String valueOfi = (String)elements.get(i);

            for (int j = i + 1; j < numOfValues; j++) {
                String valueOfj = (String)elements.get(j);
                if ((valueOfj == null && valueOfi == null) || (valueOfi != null && valueOfi.equals(valueOfj))) {
                    String msg = NLS.bind(Messages.EnumValueSet_msgDuplicateValue, getNotNullValue(valueOfi));
                    list.add(new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, this, PROPERTY_VALUES));
                    list.add(new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, this, PROPERTY_VALUES));
                }
            }
        }

        if (datatype != null && datatype.isPrimitive() && getContainsNull()) {
            String text = Messages.EnumValueSet_msgNullNotSupported;
            list.add(new Message(MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, this, PROPERTY_CONTAINS_NULL));
        }

    }

    /**
     * Returns whether the given value represents the special null value for the given datatype.
     */
    private boolean isSpecialNull(String value, ValueDatatype datatype) {
        if (datatype.isPrimitive()) {
            return false;
        }

        if (value == null) {
            return false;
        }

        return datatype.isNull(value);
    }

    private String getNotNullValue(String value) {
        if (value == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + elements.toString(); //$NON-NLS-1$
    }

    public String toShortString() {
        ValueDatatype type = getValueDatatype();
        if (type != null && type instanceof EnumDatatype && ((EnumDatatype)type).isSupportingNames()) {
            List result = new ArrayList(elements.size());
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                String id = (String)iter.next();
                String formatedEnumText = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(type, id);
                result.add(formatedEnumText);
            }
            return result.toString();
        }
        return elements.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        elements.clear();
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        NodeList children = el.getElementsByTagName(XML_VALUE);
        for (int i = 0; i < children.getLength(); i++) {
            Element valueEl = (Element)children.item(i);
            String value = ValueToXmlHelper.getValueFromElement(valueEl, "Data"); //$NON-NLS-1$
            elements.add(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element valueElement = doc.createElement(XML_VALUE);
            tagElement.appendChild(valueElement);
            String value = (String)iter.next();
            ValueToXmlHelper.addValueToElement(value, valueElement, "Data"); //$NON-NLS-1$
        }
        element.appendChild(tagElement);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet copy(IIpsObjectPart parent, int id) {
        EnumValueSet retValue = new EnumValueSet(parent, id);

        retValue.elements = new ArrayList(elements);

        return retValue;
    }

    /**
     * {@inheritDoc}
     */
    public void addValuesFromDatatype(EnumDatatype datatype) {
        String[] valueIds = datatype.getAllValueIds(true);
        for (int i = 0; i < valueIds.length; i++) {
            addValue(valueIds[i]);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setValuesOf(IValueSet target) {
        if (!(target instanceof EnumValueSet)) {
            throw new IllegalArgumentException("The given value set is not an enum value set"); //$NON-NLS-1$
        }

        if (target == this) {
            return;
        }

        elements.clear();
        elements.addAll(((EnumValueSet)target).elements);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean getContainsNull() {
        return elements.contains(null);
    }

    /**
     * {@inheritDoc}
     */
    public void setContainsNull(boolean containsNull) {
        boolean old = getContainsNull();

        if (old != containsNull) {
            if (containsNull) {
                elements.add(null);
            } else {
                elements.remove(null);
            }
        }

        valueChanged(old, containsNull);
    }
}
