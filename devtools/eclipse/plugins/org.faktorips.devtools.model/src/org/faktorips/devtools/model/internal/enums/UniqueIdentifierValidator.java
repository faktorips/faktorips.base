/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.value.ValueUtil;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.values.LocalizedString;

/**
 * This is an validation utility for {@link IEnumAttributeValue} unique identifiers.
 * <p>
 * The performance is optimized for whole enum validations. To reach best performance we create a
 * map for every column of the enum. The cache is cleared on every change event concerning the
 * corresponding enum container. The performance for validating a single value is still more than
 * enough.
 * 
 * @author dirmeier
 */
class UniqueIdentifierValidator {

    private final EnumValueContainer container;

    private IEnumType enumType;

    private Map<Integer, AttributeValues> columnAttributeValues = new ConcurrentHashMap<>(
            16, 0.75f, 1);

    public UniqueIdentifierValidator(EnumValueContainer container) {
        this.container = container;
        registerChangeListener();
    }

    private void registerChangeListener() {
        container.getIpsModel().addChangeListener(event -> {
            if (isRelevantChangeEvent(event)) {
                IIpsObjectPartContainer part = event.getPart();
                if (part instanceof IEnumAttributeValue enumAttributeValue) {
                    int index = getEnumAttributeIndex(enumAttributeValue);
                    columnAttributeValues.remove(index);
                } else {
                    columnAttributeValues.clear();
                }
            }
        });
    }

    private boolean isRelevantChangeEvent(ContentChangeEvent event) {
        if (event.isAffected(container)) {
            return true;
        } else {
            if (!(container instanceof IEnumType)) {
                if (enumType == null) {
                    getEnumType();
                }
                return enumType != null ? event.isAffected(enumType) : false;
            }
        }
        return false;
    }

    private void getEnumType() {
        enumType = container.findEnumType(container.getIpsProject());
    }

    public List<String> getUniqueIdentifierViolations(IEnumAttributeValue enumAttributeValue) {
        ConcurrentHashMap<Integer, AttributeValues> columnAttributeValuesCopy = new ConcurrentHashMap<>(
                columnAttributeValues);
        List<String> violatingString = new ArrayList<>();
        Set<LocalizedString> localizedIdentifyerList = getLocalizedIdentifiers(enumAttributeValue);
        int index = getEnumAttributeIndex(enumAttributeValue);
        AttributeValues attributeValues = getAttributeValues(columnAttributeValuesCopy, index);
        for (LocalizedString localizedString : localizedIdentifyerList) {
            if (attributeValues.isDuplicated(localizedString)) {
                violatingString.add(localizedString.getValue());
            }
        }
        columnAttributeValues = columnAttributeValuesCopy;
        return violatingString;
    }

    private synchronized AttributeValues getAttributeValues(
            ConcurrentHashMap<Integer, AttributeValues> columnAttributeValuesCopy,
            int index) {
        AttributeValues attributeValues = columnAttributeValuesCopy.get(index);
        if (attributeValues == null) {
            attributeValues = createAttributeValues(index);
            AttributeValues previousValues = columnAttributeValuesCopy.putIfAbsent(index, attributeValues);
            if (previousValues != null) {
                attributeValues = previousValues;
            }
        }
        return attributeValues;
    }

    private AttributeValues createAttributeValues(int columnIndex) {
        AttributeValues attributeValues = new AttributeValues();
        List<IEnumValue> aggregatedEnumValues = container.findAggregatedEnumValues();
        for (IEnumValue value : aggregatedEnumValues) {
            IEnumAttributeValue enumAttributeValue = value.getEnumAttributeValues().get(columnIndex);
            Set<LocalizedString> localizedIdentifyerList = getLocalizedIdentifiers(enumAttributeValue);
            for (LocalizedString localizedString : localizedIdentifyerList) {
                attributeValues.addIdentifier(localizedString);
            }
        }
        return attributeValues;
    }

    private int getEnumAttributeIndex(IEnumAttributeValue enumAttributeValue) {
        return enumAttributeValue.getEnumValue().getIndexOfEnumAttributeValue(enumAttributeValue);
    }

    private Set<LocalizedString> getLocalizedIdentifiers(IEnumAttributeValue enumAttributeValue) {
        ValueUtil otherValueUtil = ValueUtil.createUtil(enumAttributeValue.getValue());
        return otherValueUtil.getLocalizedIdentifiers();
    }

    private static class AttributeValues {

        private Map<LocalizedString, Integer> identifierCounts = new ConcurrentHashMap<>(16,
                0.75f, 1);

        public void addIdentifier(LocalizedString identifier) {
            identifierCounts.put(identifier, identifierCounts.getOrDefault(identifier, 0) + 1);
        }

        public boolean isDuplicated(LocalizedString identifier) {
            final Integer count = identifierCounts.get(identifier);
            return count != null && count > 1;
        }

    }

}
