/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.runtime.internal.ValueToXmlHelper;
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

    public static final String ENUM_VALUESET_SEPARATOR = "|"; //$NON-NLS-1$

    public static final String ENUM_VALUESET_SEPARATOR_WITH_WHITESPACE = " " + ENUM_VALUESET_SEPARATOR + " "; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String ENUM_VALUESET_START = "{"; //$NON-NLS-1$

    public static final String ENUM_VALUESET_END = "}"; //$NON-NLS-1$

    public static final String ENUM_VALUESET_EMPTY = ENUM_VALUESET_START + ENUM_VALUESET_END;

    public static final String XML_TAG_ENUM = ValueToXmlHelper.XML_TAG_ENUM;

    private static final String XML_DATA = ValueToXmlHelper.XML_TAG_DATA;

    private static final String XML_VALUE = ValueToXmlHelper.XML_TAG_VALUE;

    /** The values in the set as list */
    private List<String> values = new ArrayList<String>();

    /**
     * A map with the values as keys and the index positions of the occurrences of a value as
     * "map value". The "map value" is a list containing the indexes of the occurrences.
     */
    private Map<String, List<Integer>> valuesToIndexMap = new HashMap<String, List<Integer>>();

    public EnumValueSet(IValueSetOwner parent, String partId) {
        super(ValueSetType.ENUM, parent, partId);
    }

    public EnumValueSet(IValueSetOwner parent, List<String> values, String partId) {
        this(parent, partId);
        this.values = values;
        refillValuesToIndexMap();
    }

    @Override
    public String[] getValues() {
        return values.toArray(new String[values.size()]);
    }

    @Override
    public List<String> getValuesAsList() {
        return new ArrayList<String>(values);
    }

    @Override
    public void move(List<Integer> indexes, boolean up) {
        ListElementMover<String> mover = new ListElementMover<String>(values);
        int[] indexesArray = new int[indexes.size()];
        int i = 0;
        for (Integer index : indexes) {
            indexesArray[i] = index;
        }
        mover.move(indexesArray, up);
        refillValuesToIndexMap();
        objectHasChanged();
    }

    @Override
    public List<Integer> getPositions(String value) {
        List<Integer> positions = new ArrayList<Integer>();
        List<Integer> indexes = valuesToIndexMap.get(value);
        if (indexes != null) {
            positions.addAll(indexes);
            Collections.sort(positions);
        }
        return positions;
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreException {
        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (datatype == null) {
            return false;
        }
        if (isNullValue(value, datatype)) {
            return isContainsNull();
        }
        if (isAbstract()) {
            return true;
        }
        if (!datatype.isParsable(value)) {
            return false;
        }

        return isValueInEnum(value, datatype);
    }

    /**
     * Runs through all the enum values and returns true if the expected value was found. To compare
     * the equality of the values we need to ask the datatype. For performance optimization we first
     * check equality. If the value is not parsable, the equals check may throw a
     * IllegalArgumentException or NullPointerException
     */
    private boolean isValueInEnum(String value, ValueDatatype datatype) {
        for (String each : values) {
            try {
                if ((ObjectUtils.equals(each, value) || datatype.areValuesEqual(each, value))) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                continue;
            } catch (NullPointerException e) {
                continue;
            }
        }
        return false;
    }

    @Override
    public boolean containsValueSet(IValueSet subset) {
        ValueDatatype datatype = getValueDatatype();
        if (!checkDatatypes(subset, datatype)) {
            return false;
        }

        if (isAbstract()) {
            return true;
        }
        if (subset.isAbstract()) {
            return false;
        }
        return containsAllValues(subset);
    }

    private boolean checkDatatypes(IValueSet subset, ValueDatatype datatype) {
        ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
        if (datatype == null || !datatype.equals(subDatatype)) {
            return false;
        }

        if (!(subset instanceof EnumValueSet)) {
            return false;
        }
        return true;
    }

    private boolean containsAllValues(IValueSet subset) {
        IEnumValueSet enumSubset = (IEnumValueSet)subset;
        String[] subsetValues = enumSubset.getValues();

        for (String value : subsetValues) {
            try {
                if (!containsValue(value, getIpsProject())) {
                    return false;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public void addValue(String val) {
        addValueWithoutTriggeringChangeEvent(val);
        objectHasChanged();
    }

    @Override
    public void addValues(List<String> values) {
        for (String value : values) {
            addValueWithoutTriggeringChangeEvent(value);
        }
        objectHasChanged();
    }

    protected void addValueWithoutTriggeringChangeEvent(String newValue) {
        values.add(newValue);
        Integer newIndex = values.size() - 1;
        setValueWithoutTriggeringChangeEvent(newValue, newIndex);
    }

    private void setValueWithoutTriggeringChangeEvent(String newValue, Integer newIndex) {
        List<Integer> indexList = valuesToIndexMap.get(newValue);
        if (indexList == null) {
            indexList = new ArrayList<Integer>(1);
            valuesToIndexMap.put(newValue, indexList);
        }
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

    @Override
    public void removeValues(List<String> values) {
        for (String value : values) {
            removeWithoutTriggeringChangeEvents(value);
        }
        objectHasChanged();
    }

    /**
     * Removes the value without triggering a change event. If the value occurs multiple times in
     * the set, all occurrences are removed.
     */
    private void removeWithoutTriggeringChangeEvents(String value) {
        List<Integer> indexes = valuesToIndexMap.remove(value);
        if (indexes == null) {
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
        List<Integer> indexes = valuesToIndexMap.get(oldValue);
        // need to cast the index to Object to use the remove(Object) method instead of remove(int)
        indexes.remove((Object)index);
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

        if (datatype != null && datatype.isPrimitive() && isContainsNull()) {
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
        ObjectProperty parentOP = new ObjectProperty(getParent(), IValueSetOwner.PROPERTY_VALUE_SET);
        String value = values.get(index);
        if (datatype == null) {
            String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsableDatatypeUnknown, getNotNullValue(value));
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, msg, Message.WARNING, op, parentOP));
        } else if (!datatype.isParsable(value) || isSpecialNull(value, datatype)) {
            String msg = NLS
                    .bind(Messages.EnumValueSet_msgValueNotParsable, getNotNullValue(value), datatype.getName());
            list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, op, parentOP));
        }
    }

    private void checkForDuplicate(MessageList list, int index) {
        String value = values.get(index);
        if (valuesToIndexMap.get(value).size() > 1) {
            ObjectProperty op = new ObjectProperty(this, PROPERTY_VALUES, index);
            list.add(createMsgForDuplicateValues(value, op));
        }
    }

    private void checkForDuplicates(MessageList list) {
        for (String value : valuesToIndexMap.keySet()) {
            List<Integer> indexes = valuesToIndexMap.get(value);
            if (indexes.size() <= 1) {
                continue;
            }
            List<ObjectProperty> ops = new ArrayList<ObjectProperty>(indexes.size());
            ops.add(new ObjectProperty(getValueSetOwner(), IValueSetOwner.PROPERTY_VALUE_SET));
            for (Integer index : indexes) {
                ops.add(new ObjectProperty(this, PROPERTY_VALUES, index));
            }
            list.add(createMsgForDuplicateValues(value, ops.toArray(new ObjectProperty[ops.size()])));
        }
    }

    private Message createMsgForDuplicateValues(String value, ObjectProperty... ops) {
        String msg = NLS.bind(Messages.EnumValueSet_msgDuplicateValue, getNotNullValue(value));
        return new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, ops);
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

        return isNullValue(value, datatype);
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
        return formatList(values);
    }

    private String formatList(List<String> stringValues) {
        StringBuilder stringBuilder = new StringBuilder(ENUM_VALUESET_START);
        for (Iterator<String> iterator = stringValues.iterator(); iterator.hasNext();) {
            String value = iterator.next();
            stringBuilder.append(value);
            if (iterator.hasNext()) {
                stringBuilder.append(ENUM_VALUESET_SEPARATOR_WITH_WHITESPACE);
            }
        }
        stringBuilder.append(ENUM_VALUESET_END);
        return stringBuilder.toString();
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
            String value = ValueToXmlHelper.getValueFromElement(valueEl, XML_DATA);
            addValueWithoutTriggeringChangeEvent(value);
        }
        setContainsNullWithoutTriggeringEvent(Boolean.valueOf(el.getAttribute(PROPERTY_CONTAINS_NULL)).booleanValue());
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG_ENUM);
        for (String value : values) {
            Element valueElement = doc.createElement(XML_VALUE);
            tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(isContainsNull()));
            tagElement.appendChild(valueElement);
            ValueToXmlHelper.addValueToElement(value, valueElement, XML_DATA);
        }
        element.appendChild(tagElement);
    }

    @Override
    public IValueSet copy(IValueSetOwner parent, String id) {
        EnumValueSet copy = new EnumValueSet(parent, id);
        copy.values = new ArrayList<String>(values);
        copy.setContainsNullWithoutTriggeringEvent(this.isContainsNull());
        copy.refillValuesToIndexMap();
        return copy;
    }

    @Override
    public void copyPropertiesFrom(IValueSet source) {
        values.clear();
        values.addAll(((EnumValueSet)source).values);
        setContainsNullWithoutTriggeringEvent(source.isContainsNull());
        refillValuesToIndexMap();
        objectHasChanged();
    }

    @Override
    public void addValuesFromDatatype(EnumDatatype datatype) {
        String[] valueIds = datatype.getAllValueIds(true);
        addValues(Arrays.asList(valueIds));
    }

    @Override
    public boolean isContainsNull() {
        return values.contains(null);
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        if (!isContainsNull() && containsNull) {
            addValue(null);
        } else if (!containsNull) {
            removeValue(null);
        }
    }

    public void setContainsNullWithoutTriggeringEvent(boolean containsNull) {
        if (!isContainsNull() && containsNull) {
            addValueWithoutTriggeringChangeEvent(null);
        } else if (!containsNull) {
            removeWithoutTriggeringChangeEvents(null);
        }
    }

}
