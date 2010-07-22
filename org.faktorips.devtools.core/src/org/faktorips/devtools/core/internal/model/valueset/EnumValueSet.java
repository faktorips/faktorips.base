/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
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
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
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

    /** The values in the set as list */
    private ArrayList<String> values = new ArrayList<String>();

    /**
     * A map with the values as keys and the index positions of the occurences of a value as
     * "map value". If a value occurs once, the "map value" the index is stored as single Integer.
     * If a value occurs more than once, the "map value" is a list containing the indexes of the
     * occurrences.
     */
    private Map<String, Object> valuesToIndexMap = new HashMap<String, Object>();

    public EnumValueSet(IIpsObjectPart parent, String partId) {
        super(ValueSetType.ENUM, parent, partId);
    }

    @Override
    public String[] getValues() {
        return values.toArray(new String[values.size()]);
    }

    @Override
    public List<Integer> getPositions(String value) {
        List<Integer> positions = new ArrayList<Integer>();
        Object o = valuesToIndexMap.get(value);
        if (o instanceof Integer) {
            positions.add((Integer)o);
        } else if (o instanceof List) {
            positions.addAll((Collection<? extends Integer>)o);
            Collections.sort(positions);
        }
        return positions;
    }

    @Override
    public boolean containsValue(String value,
            MessageList list,
            Object invalidObject,
            String invalidProperty,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(list);
        ValueDatatype datatype = findValueDatatype(ipsProject);
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
        // An abstract value set is considered containing all values. See #isAbstract()
        if (isAbstract()) {
            return true;
        }

        if (!datatype.isParsable(value)) {
            String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsable, value, datatype.getName());
            addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, getProperty(invalidProperty,
                    IConfigElement.PROPERTY_VALUE));
            return false;
        }

        for (String each : values) {
            if (datatype.isParsable(each) && datatype.areValuesEqual(each, value)) {
                return true;
            }
        }

        String text = Messages.EnumValueSet_msgValueNotInEnumeration;
        addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text, invalidObject, getProperty(invalidProperty,
                IConfigElement.PROPERTY_VALUE));

        return false;
    }

    @Override
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
        }
        if (subset.isAbstract()) {
            return false; // this set is concrete
        }
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

    @Override
    public boolean containsValueSet(IValueSet subset) {
        return containsValueSet(subset, new MessageList(), null, null);
    }

    @Override
    public void addValue(String val) {
        addValueWithoutTriggeringChangeEvent(val);
        objectHasChanged();
    }

    private void addValueWithoutTriggeringChangeEvent(String newValue) {
        values.add(newValue);
        Integer newIndex = values.size() - 1;
        setValueWithoutTriggeringChangeEvent(newValue, newIndex);
    }

    private void setValueWithoutTriggeringChangeEvent(String newValue, Integer newIndex) {
        Object o = valuesToIndexMap.get(newValue);
        if (o == null) {
            valuesToIndexMap.put(newValue, newIndex);
            return;
        }
        if (o instanceof Integer) {
            List<Integer> indexList = new ArrayList<Integer>(2);
            indexList.add((Integer)o);
            indexList.add(newIndex);
            valuesToIndexMap.put(newValue, indexList);
            return;
        }
        List<Integer> indexList = (List<Integer>)o;
        indexList.add(newIndex);
    }

    @Override
    public void removeValue(int index) {
        values.remove(index);
        refillValuesToIndexMap();
        objectHasChanged();
    }

    @Override
    public void removeValue(String value) {
        removeWithoutTriggeringChangeEvents(value);
        objectHasChanged();
    }

    /**
     * Removes the value without triggering a change event. If the value occurs multiple times in
     * the set, all occurences are removed.
     */
    private void removeWithoutTriggeringChangeEvents(String value) {
        Object o = valuesToIndexMap.remove(value);
        if (o == null) {
            return;
        }
        if (o instanceof Integer) {
            Integer index = (Integer)o;
            values.remove(index.intValue()); // Without intValue(), we call remove(Object o)!!!
            if (index == values.size()) {
                return;
            }
            refillValuesToIndexMap();
            return;
        }
        for (Iterator<String> it = values.iterator(); it.hasNext();) {
            String each = it.next();
            if (ObjectUtils.equals(each, value)) {
                it.remove();
            }
        }
        refillValuesToIndexMap();
    }

    private void refillValuesToIndexMap() {
        valuesToIndexMap.clear();
        for (int i = 0; i < values.size(); i++) {
            setValueWithoutTriggeringChangeEvent(values.get(i), i);
        }
    }

    @Override
    public String getValue(int index) {
        return values.get(index);
    }

    @Override
    public void setValue(int index, String value) {
        String oldValue = values.get(index);
        values.set(index, value);
        Object o = valuesToIndexMap.get(oldValue);
        if (o instanceof Integer) {
            valuesToIndexMap.remove(oldValue);
        } else if (o instanceof List) {
            List<Integer> indexes = (List<Integer>)o;
            indexes.remove((Object)index);
            if (indexes.size() == 1) {
                valuesToIndexMap.put(oldValue, indexes.get(0));
            }
        }
        setValueWithoutTriggeringChangeEvent(value, index);
        valueChanged(oldValue, value);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public String[] getValuesNotContained(IEnumValueSet otherSet) {
        List<String> result = new ArrayList<String>();
        if (otherSet == null) {
            return result.toArray(new String[result.size()]);
        }
        for (int i = 0; i < otherSet.size(); i++) {
            if (!values.contains(otherSet.getValue(i))) {
                result.add(otherSet.getValue(i));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        ValueDatatype datatype = getValueDatatype();

        int numOfValues = values.size();
        for (int i = 0; i < numOfValues; i++) {
            validateValueWithoutDuplicateCheck(list, i, datatype);
        }
        checkForDuplicates(list);

        if (datatype != null && datatype.isPrimitive() && getContainsNull()) {
            String text = Messages.EnumValueSet_msgNullNotSupported;
            list.add(new Message(MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, this, PROPERTY_CONTAINS_NULL));
        }

    }

    @Override
    public MessageList validateValue(int index, IIpsProject ipsProject) throws CoreException {
        MessageList list = new MessageList();
        validateValueWithoutDuplicateCheck(list, index, getValueDatatype());
        if (list.getSeverity() != Message.ERROR) {
            checkForDuplicate(list, index);
        }
        return list;
    }

    private void validateValueWithoutDuplicateCheck(MessageList list, int index, ValueDatatype datatype) {
        ObjectProperty op = new ObjectProperty(this, PROPERTY_VALUES, index);
        String value = values.get(index);
        if (datatype == null) {
            String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsableDatatypeUnknown, getNotNullValue(value));
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, msg, Message.WARNING, op));
        } else if (!datatype.isParsable(value) || isSpecialNull(value, datatype)) {
            String msg = NLS
                    .bind(Messages.EnumValueSet_msgValueNotParsable, getNotNullValue(value), datatype.getName());
            list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, this, PROPERTY_VALUES));
        }
    }

    private void checkForDuplicate(MessageList list, int index) {
        String value = values.get(index);
        Object o = valuesToIndexMap.get(value);
        if (o instanceof List) {
            ObjectProperty op = new ObjectProperty(this, PROPERTY_VALUES, index);
            list.add(createMsgForDuplicateValues(value, op));
        }
    }

    private void checkForDuplicates(MessageList list) {
        for (String value : valuesToIndexMap.keySet()) {
            Object o = valuesToIndexMap.get(value);
            if (o instanceof List) {
                List<Integer> indexes = (List<Integer>)o;
                List<ObjectProperty> ops = new ArrayList<ObjectProperty>(indexes.size());
                for (Integer index : indexes) {
                    ops.add(new ObjectProperty(this, PROPERTY_VALUES, index));
                }
                list.add(createMsgForDuplicateValues(value, ops));
            }
        }
    }

    private Message createMsgForDuplicateValues(String value, ObjectProperty op) {
        String msg = NLS.bind(Messages.EnumValueSet_msgDuplicateValue, getNotNullValue(value));
        return new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, op);
    }

    private Message createMsgForDuplicateValues(String value, List<ObjectProperty> ops) {
        String msg = NLS.bind(Messages.EnumValueSet_msgDuplicateValue, getNotNullValue(value));
        return new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, ops.toArray(new ObjectProperty[ops.size()]));
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
        if (isAbstract()) {
            return super.toString() + "(abstract)"; //$NON-NLS-1$
        }
        return super.toString() + ":" + values.toString(); //$NON-NLS-1$
    }

    @Override
    public String toShortString() {
        ValueDatatype type = getValueDatatype();
        if (type != null && type instanceof EnumDatatype && ((EnumDatatype)type).isSupportingNames()) {
            List<String> result = new ArrayList<String>(values.size());
            for (String id : values) {
                String formatedEnumText = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(type, id);
                result.add(formatedEnumText);
            }
            return result.toString();
        }
        return values.toString();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        values.clear();
        valuesToIndexMap.clear();
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        NodeList children = el.getElementsByTagName(XML_VALUE);
        for (int i = 0; i < children.getLength(); i++) {
            Element valueEl = (Element)children.item(i);
            String value = ValueToXmlHelper.getValueFromElement(valueEl, "Data"); //$NON-NLS-1$
            addValueWithoutTriggeringChangeEvent(value);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        for (String value : values) {
            Element valueElement = doc.createElement(XML_VALUE);
            tagElement.appendChild(valueElement);
            ValueToXmlHelper.addValueToElement(value, valueElement, "Data");
        }
        element.appendChild(tagElement);
    }

    @Override
    public IValueSet copy(IIpsObjectPart parent, String id) {
        EnumValueSet copy = new EnumValueSet(parent, id);
        copy.values = new ArrayList<String>(values);
        copy.refillValuesToIndexMap();
        return copy;
    }

    @Override
    public void copyPropertiesFrom(IValueSet source) {
        values.clear();
        values.addAll(((EnumValueSet)source).values);
        refillValuesToIndexMap();
        objectHasChanged();
    }

    @Override
    public void addValuesFromDatatype(EnumDatatype datatype) {
        String[] valueIds = datatype.getAllValueIds(true);
        for (String valueId : valueIds) {
            addValue(valueId);
        }
    }

    @Override
    public boolean getContainsNull() {
        return values.contains(null);
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        boolean old = getContainsNull();

        if (old != containsNull) {
            if (containsNull) {
                values.add(null);
            } else {
                values.remove(null);
            }
        }

        valueChanged(old, containsNull);
    }

}
