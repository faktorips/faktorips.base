/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.value.ValueUtil;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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

    private Map<Integer, AttributeValues> columnAttributeValues = new HashMap<Integer, UniqueIdentifierValidator.AttributeValues>();

    public UniqueIdentifierValidator(EnumValueContainer container) {
        this.container = container;
        registerChangeListener();
    }

    private void registerChangeListener() {
        container.getIpsModel().addChangeListener(new ContentsChangeListener() {

            @Override
            public void contentsChanged(ContentChangeEvent event) {
                if (event.isAffected(container)) {
                    IIpsObjectPart part = event.getPart();
                    if (part instanceof IEnumAttributeValue) {
                        IEnumAttributeValue enumAttributeValue = (IEnumAttributeValue)part;
                        int index = getEnumAttributeIndex(enumAttributeValue);
                        columnAttributeValues.remove(index);
                    } else {
                        columnAttributeValues.clear();
                    }
                }
            }
        });
    }

    public List<String> getUniqueIdentifierViolations(IEnumAttributeValue enumAttributeValue) {
        HashMap<Integer, AttributeValues> columnAttributeValuesCopy = new HashMap<Integer, AttributeValues>(
                columnAttributeValues);
        List<String> violatingString = new ArrayList<String>();
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

    private AttributeValues getAttributeValues(HashMap<Integer, AttributeValues> columnAttributeValuesCopy, int index) {
        AttributeValues attributeValues = columnAttributeValuesCopy.get(index);
        if (attributeValues == null) {
            attributeValues = createAttributeValues(index);
            columnAttributeValuesCopy.put(index, attributeValues);
        }
        return attributeValues;
    }

    private AttributeValues createAttributeValues(int columnIndex) {
        AttributeValues attributeValues = new AttributeValues();
        for (IEnumValue value : container.getEnumValues()) {
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
        Set<LocalizedString> result = otherValueUtil.getLocalizedIdentifiers();
        return result;
    }

    private static class AttributeValues {

        private Map<LocalizedString, Integer> identifierCounts = new HashMap<LocalizedString, Integer>();

        public void addIdentifier(LocalizedString identifier) {
            Integer count = identifierCounts.get(identifier);
            if (count == null) {
                identifierCounts.put(identifier, 1);
            } else {
                identifierCounts.put(identifier, count + 1);
            }
        }

        public boolean isDuplicated(LocalizedString identifier) {
            final Integer count = identifierCounts.get(identifier);
            return count != null && count > 1;
        }

    }

}
