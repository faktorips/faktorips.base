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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueTypeMismatch;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of <code>IEnumValueContainer</code>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumValueContainer
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumValueContainer extends BaseIpsObject implements IEnumValueContainer {

    /** Collection containing the enumeration values. */
    private IpsObjectPartCollection<IEnumValue> enumValues;

    private final UniqueIdentifierValidator uniqueIdentifierValidator;

    /**
     * Maps values of the identifier attribute to concrete {@link IEnumValue}s. Used for quick
     * {@link IEnumValue} access by identifier value.
     */
    private final Map<String, IEnumValue> enumValuesByIdentifier = new ConcurrentHashMap<>(16, 0.75f,
            1);

    /**
     * The {@link IEnumAttribute} that is marked as default identifier. Values for this attribute
     * are used to access the corresponding {@link IEnumValue}s.
     * <p>
     * This reference is set when loading an {@link IEnumValueContainer} and checked on every enum
     * value access. If it does no longer exist or if it is no longer the default identifier
     * attribute the reference is updated accordingly.
     */
    private IEnumAttribute identifierAttribute;

    /**
     * @param file The IPS source file in which this IPS object will be stored in.
     */
    protected EnumValueContainer(IIpsSrcFile file) {
        super(file);
        enumValues = new IpsObjectPartCollection<>(this, EnumValue.class, IEnumValue.class,
                IEnumValue.XML_TAG);
        uniqueIdentifierValidator = new UniqueIdentifierValidator(this);
    }

    @Override
    public List<IEnumValue> getEnumValues() {
        List<IEnumValue> valuesList = new ArrayList<>();
        valuesList.addAll(enumValues.asList());
        return valuesList;
    }

    @Override
    public List<String> findAllIdentifierAttributeValues(IIpsProject ipsProject) {
        List<String> valueIds = new ArrayList<>();
        IEnumType enumType = findEnumType(ipsProject);
        IEnumAttribute isIdentifierEnumAttribute = enumType.findIdentiferAttribute(ipsProject);
        List<IEnumValue> allEnumValues = findAggregatedEnumValues();
        for (IEnumValue enumValue : allEnumValues) {
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(isIdentifierEnumAttribute);
            if (enumAttributeValue != null) {
                valueIds.add(enumAttributeValue.getValue().getDefaultLocalizedContent(ipsProject));
            }
        }
        return valueIds;
    }

    @Override
    public IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject)
            {
        if (identifierAttributeValue == null) {
            return null;
        }

        checkIdentifierAttribute(ipsProject);
        if (identifierAttribute == null) {
            return null;
        }

        return getEnumValueForValidIdentifierAttribute(identifierAttributeValue);
    }

    /**
     * Ensures that the default identifier attribute is set and still marked as such.
     * <p>
     * If that is not the case the correct identifier attribute is searched (it also might become
     * null if none can be found) and the enum value by identifier map is re-initialized.
     */
    private void checkIdentifierAttribute(IIpsProject ipsProject) {
        if (identifierAttribute == null || identifierAttribute.isDeleted()
                || !identifierAttribute.findIsIdentifier(ipsProject)) {
            identifierAttribute = findEnumType(ipsProject).findIdentiferAttribute(ipsProject);
            if (identifierAttribute != null) {
                reinitEnumValuesByIdentifierMap();
            }
        }
    }

    /**
     * Re-initializes the enum value by identifier map.
     * <p>
     * This operation may only be called if a valid identifier attribute is set.
     */
    private void reinitEnumValuesByIdentifierMap() {
        enumValuesByIdentifier.clear();
        List<IEnumValue> aggregatedEnumValues = findAggregatedEnumValues();
        for (IEnumValue enumValue : aggregatedEnumValues) {
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(identifierAttribute);
            if (enumAttributeValue == null) {
                continue;
            }
            String identifier = enumAttributeValue.getValue().getDefaultLocalizedContent(getIpsProject());
            if (identifier != null && identifier.length() > 0) {
                enumValuesByIdentifier.put(identifier, enumValue);
            }
        }
    }

    private IEnumValue getEnumValueForValidIdentifierAttribute(String identifierAttributeValue) {
        IEnumValue enumValue = enumValuesByIdentifier.get(identifierAttributeValue);
        if (enumValue != null) {
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(identifierAttribute);
            String newIdentifier = enumAttributeValue.getValue().getDefaultLocalizedContent(getIpsProject());
            if (StringUtils.equals(newIdentifier, identifierAttributeValue)) {
                return enumValue;
            } else {
                enumValuesByIdentifier.remove(identifierAttributeValue);
            }
        }

        /*
         * If no enum value is found in the map for the given identifier attribute value it is still
         * very possible that such an enum value exists (because the map might not be up-to-date,
         * e.g. if the identifier value has been changed).
         */
        List<IEnumValue> aggregatedEnumValues = findAggregatedEnumValues();
        for (IEnumValue currentEnumValue : aggregatedEnumValues) {
            IEnumAttributeValue enumAttributeValue = currentEnumValue.getEnumAttributeValue(identifierAttribute);
            if (enumAttributeValue == null) {
                continue;
            }
            String newIdentifier = enumAttributeValue.getValue().getDefaultLocalizedContent(getIpsProject());
            if (StringUtils.equals(newIdentifier, identifierAttributeValue)) {
                enumValue = currentEnumValue;
                /*
                 * Now that we found the enum value that was not in the map we need to find and
                 * remove the old key and add the new one.
                 */
                for (Entry<String, IEnumValue> entry : enumValuesByIdentifier.entrySet()) {
                    if (entry.getValue() == enumValue) {
                        enumValuesByIdentifier.remove(entry.getKey());
                        enumValuesByIdentifier.put(newIdentifier, enumValue);
                        break;
                    }
                }
                return enumValue;
            }
        }

        return null;
    }

    @Override
    public IEnumValue newEnumValue() {
        final IEnumType enumType = findEnumType(getIpsProject());

        // Creation not possible if enumeration type can't be found.
        if (enumType == null) {
            return null;
        }

        return ((IpsModel)getIpsModel()).executeModificationsWithSingleEvent(
                new SingleEventModification<IEnumValue>(getIpsSrcFile()) {

                    private IEnumValue newEnumValue;

                    @Override
                    public boolean execute() {
                        newEnumValue = createEnumValueSingleEvent(enumType);
                        return newEnumValue != null;
                    }

                    @Override
                    public IEnumValue getResult() {
                        return newEnumValue;
                    }

                    @Override
                    public ContentChangeEvent modificationEvent() {
                        return ContentChangeEvent.newPartAddedEvent(newEnumValue);
                    }
                });
    }

    private IEnumValue createEnumValueSingleEvent(final IEnumType enumType) {
        IEnumValue newEnumValue = newPart(EnumValue.class);

        boolean includeLiteralNames = EnumValueContainer.this instanceof IEnumType;
        for (IEnumAttribute enumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralNames)) {
            if (enumAttribute.isEnumLiteralNameAttribute()) {
                newEnumValue.newEnumLiteralNameAttributeValue();
            } else {
                newEnumValue.newEnumAttributeValue();
            }
        }
        return newEnumValue;
    }

    @Override
    public int getEnumValuesCount() {
        return enumValues.size();
    }

    @Override
    public int[] moveEnumValues(final List<IEnumValue> enumValuesToMove, final boolean up) {
        ArgumentCheck.notNull(enumValuesToMove);
        final int numberToMove = enumValuesToMove.size();
        if (numberToMove == 0) {
            return new int[0];
        }

        return ((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<int[]>(getIpsSrcFile()) {
                    private int[] indices = new int[numberToMove];

                    @Override
                    public boolean execute() {
                        for (int i = 0; i < numberToMove; i++) {
                            IEnumValue currentEnumValue = enumValuesToMove.get(i);
                            int index = getIndexOfEnumValue(currentEnumValue);
                            indices[i] = index;
                        }
                        indices = enumValues.moveParts(indices, up);
                        return true;
                    }

                    @Override
                    public int[] getResult() {
                        return indices;
                    }
                });
    }

    @Override
    public int getIndexOfEnumValue(IEnumValue enumValue) {
        ArgumentCheck.notNull(enumValue);
        return enumValues.indexOf(enumValue);
    }

    @Override
    public void clear() {
        enumValues.clear();
        objectHasChanged();
    }

    List<String> getUniqueIdentifierViolations(IEnumAttributeValue enumAttributeValue) {
        return uniqueIdentifierValidator.getUniqueIdentifierViolations(enumAttributeValue);
    }

    @Override
    public boolean deleteEnumValues(final List<IEnumValue> enumValuesToDelete) {
        if (enumValuesToDelete == null) {
            return false;
        }

        return ((IpsModel)getIpsModel()).executeModificationsWithSingleEvent(
                new SingleEventModification<Boolean>(getIpsSrcFile()) {

                    private Boolean changed = false;

                    @Override
                    protected boolean execute() {
                        for (IEnumValue currentEnumValue : enumValuesToDelete) {
                            if ((enumValues.contains(currentEnumValue))) {
                                currentEnumValue.delete();
                                changed = true;
                            }
                        }
                        return changed;
                    }

                    @Override
                    protected Boolean getResult() {
                        return changed;
                    }
                });
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        boolean removed = super.removePartThis(part);
        if (removed && part instanceof IEnumValue) {
            /*
             * If the removed part was an enum value we need to also remove it from the enum values
             * by identifier map.
             */
            IEnumValue enumValue = (IEnumValue)part;
            for (String identifier : enumValuesByIdentifier.keySet()) {
                IEnumValue storedEnumValue = enumValuesByIdentifier.get(identifier);
                if (storedEnumValue == enumValue) {
                    enumValuesByIdentifier.remove(identifier);
                    break;
                }
            }
        }
        return removed;
    }

    @Override
    public void fixEnumAttributeValues(final IEnumAttribute enumAttribute) {
        fixEnumAttributeValuesInternal(enumAttribute);
        objectHasChanged();
    }

    private void fixEnumAttributeValuesInternal(final IEnumAttribute enumAttribute) {
        for (IEnumValue enumValue : enumValues) {
            fixEnumAttributeValue(enumAttribute, enumValue);
        }
    }

    private void fixEnumAttributeValue(final IEnumAttribute enumAttribute, IEnumValue enumValue) {
        IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(enumAttribute);
        if (enumAttributeValue != null) {
            enumAttributeValue.fixValueType(enumAttribute.isMultilingual());
        }
    }

    @Override
    public void fixAllEnumAttributeValues() {
        IEnumType enumType;
        enumType = findEnumType(getIpsProject());
        if (enumType != null) {
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
            for (IEnumAttribute enumAttribute : enumAttributes) {
                fixEnumAttributeValuesInternal(enumAttribute);
            }
        }
        objectHasChanged();
    }

    @Override
    public Map<String, ValueTypeMismatch> checkAllEnumAttributeValueTypeMismatch() {
        Map<String, ValueTypeMismatch> map = new ConcurrentHashMap<>(16, 0.9f, 1);
        IEnumType enumType = findEnumType(getIpsProject());
        if (enumType != null) {
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
            for (IEnumAttribute enumAttribute : enumAttributes) {
                map.put(enumAttribute.getName(), checkValueTypeMismatch(enumAttribute));
            }
        }
        return map;
    }

    @Override
    public ValueTypeMismatch checkValueTypeMismatch(IEnumAttribute enumAttribute) {
        ValueTypeMismatch typeMismatch = ValueTypeMismatch.NO_MISMATCH;
        for (IEnumValue enumValue : enumValues) {
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(enumAttribute);
            if (enumAttributeValue != null) {
                typeMismatch = enumAttributeValue.checkValueTypeMismatch(enumAttribute);
                if (!ValueTypeMismatch.NO_MISMATCH.equals(typeMismatch)) {
                    break;
                }
            }
        }
        return typeMismatch;
    }

}
