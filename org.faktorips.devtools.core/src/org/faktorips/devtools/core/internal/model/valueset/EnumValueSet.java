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

import static org.faktorips.devtools.core.model.DatatypeUtil.isNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
import org.faktorips.util.collections.ListComparator;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
     * A map with the values as keys and the index positions of the occurrences of a value as "map
     * value". The "map value" is a list containing the indexes of the occurrences.
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
        if (isNullValue(datatype, value) && isContainsNull()) {
            return true;
        }
        if (isAbstract()) {
            return true;
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
        IIpsProject contextProject = subset.getIpsProject();
        ValueDatatype datatype = findValueDatatype(contextProject);

        if (!(subset.isEnum())) {
            return false;
        }
        if (!datatypesCompatible(subset, datatype, contextProject)) {
            return false;
        }
        if (!isContainsNull() && subset.isContainsNull()) {
            return false;
        }
        if (isAbstract()) {
            return true;
        }
        if (subset.isAbstract()) {
            return false;
        }
        return containsAllValues((IEnumValueSet)subset, contextProject);
    }

    private boolean datatypesCompatible(IValueSet subset, ValueDatatype datatype, IIpsProject contextProject) {
        ValueDatatype subDatatype = subset.findValueDatatype(contextProject);
        return ObjectUtils.equals(datatype, subDatatype);
    }

    private boolean containsAllValues(IEnumValueSet subset, IIpsProject contextProject) {
        String[] subsetValues = subset.getValues();

        for (String value : subsetValues) {
            try {
                if (!containsValue(value, contextProject)) {
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
    protected EnumValueSetValidator createValidator(IValueSetOwner owner, ValueDatatype datatype) {
        return new EnumValueSetValidator(this, owner, datatype);
    }

    @Override
    public void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        list.add(createValidator(getValueSetOwner(), findValueDatatype(ipsProject)).validate());
    }

    @Override
    public MessageList validateValue(int index, IIpsProject ipsProject) throws CoreException {
        return createValidator(getValueSetOwner(), findValueDatatype(ipsProject)).validateValue(index);
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
        if (isAbstract()) {
            return toStringAbstractEnumValueSet();
        } else {
            final ValueDatatype type = findValueDatatype(getIpsProject());

            List<String> formattedValues = Lists.transform(values, new Function<String, String>() {
                @Override
                public String apply(String value) {
                    return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(type, value);
                }
            });
            return ENUM_VALUESET_START + StringUtils.join(formattedValues, ENUM_VALUESET_SEPARATOR_WITH_WHITESPACE)
                    + ENUM_VALUESET_END;
        }
    }

    @Override
    public String getCanonicalString() {
        if (isAbstract()) {
            return toStringAbstractEnumValueSet();
        } else {
            return ENUM_VALUESET_START + StringUtils.join(values, ENUM_VALUESET_SEPARATOR_WITH_WHITESPACE)
                    + ENUM_VALUESET_END;
        }
    }

    private String toStringAbstractEnumValueSet() {
        String nullText;
        if (isContainsNull()) {
            nullText = NLS.bind(Messages.ValueSet_includingNull,
                    IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        } else {
            nullText = NLS.bind(Messages.ValueSet_excludingNull,
                    IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        }
        return NLS.bind(Messages.EnumValueSet_abstract, nullText);
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
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG_ENUM);
        for (String value : values) {
            Element valueElement = doc.createElement(XML_VALUE);
            tagElement.appendChild(valueElement);
            ValueToXmlHelper.addValueToElement(value, valueElement, XML_DATA);
        }
        element.appendChild(tagElement);
    }

    @Override
    public IValueSet copy(IValueSetOwner parent, String id) {
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

    protected Map<String, List<Integer>> getValuesToIndexMap() {
        return valuesToIndexMap;
    }

    @Override
    public int compareTo(IValueSet o) {
        if (o.isEnum()) {
            IEnumValueSet otherEnum = (IEnumValueSet)o;
            return compareValueSetValues(otherEnum);
        } else {
            return compareDifferentValueSets(o);
        }
    }

    /**
     * Compare two enum value sets by comparing their values with each other one by one. Uses the
     * value set's data type to parse values where possible, otherwise compares the raw values
     * (strings).
     */
    protected int compareValueSetValues(IEnumValueSet otherEnum) {
        ValueDatatype datatype = findValueDatatype(getIpsProject());
        ValueDatatype otherDatatype = otherEnum.findValueDatatype(getIpsProject());
        if (!datatype.equals(otherDatatype)) {
            return datatype.compareTo(otherDatatype);
        } else {
            ListComparator<String> listComparator = ListComparator.listComparator(new ValueComparator(datatype));
            return listComparator.compare(values, otherEnum.getValuesAsList());
        }
    }

    @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class ValueComparator implements Comparator<String> {

        private final ValueDatatype datatype;

        public ValueComparator(ValueDatatype datatype) {
            this.datatype = datatype;
        }

        @Override
        public int compare(String value, String otherValue) {
            if (datatype.supportsCompare() && datatype.isParsable(value) && datatype.isParsable(otherValue)) {
                return datatype.compare(value, otherValue);
            } else {
                return ObjectUtils.compare(value, otherValue);
            }
        }

    }

}
