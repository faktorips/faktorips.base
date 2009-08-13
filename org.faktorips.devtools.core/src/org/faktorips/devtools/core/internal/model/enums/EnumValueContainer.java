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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumValueContainer</code>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumValueContainer
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumValueContainer extends BaseIpsObject implements IEnumValueContainer {

    /** Collection containing the enumeration values. */
    private IpsObjectPartCollection<IEnumValue> enumValues;

    /**
     * A map of maps. Each map inside the map stores all values of a unique identifier and
     * associates them with a list of <tt>IEnumAttributeValue</tt>s. This list is intended to be
     * used to validate unique identifiers. Each entry of the outer map stands for another unique
     * identifier. The number that is used as the key for each unique identifier is meant to be the
     * number of the enum attribute being the unique identifier.
     */
    private Map<Integer, Map<String, List<IEnumAttributeValue>>> uniqueIdentifierValidationCache;

    /** Flag indicating whether the unique identifier validation cache has been already initialized. */
    private boolean uniqueIdentifierValidationCacheInitialized;

    /**
     * Creates a new <code>EnumValueContainer</code>.
     * 
     * @param file The IPS source file in which this IPS object will be stored in.
     */
    protected EnumValueContainer(IIpsSrcFile file) {
        super(file);

        enumValues = new IpsObjectPartCollection<IEnumValue>(this, EnumValue.class, IEnumValue.class,
                IEnumValue.XML_TAG);
        uniqueIdentifierValidationCache = new HashMap<Integer, Map<String, List<IEnumAttributeValue>>>();
    }

    public List<IEnumValue> getEnumValues() {
        List<IEnumValue> valuesList = new ArrayList<IEnumValue>();
        IIpsObjectPart[] parts = enumValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            valuesList.add((IEnumValue)currentObjectPart);
        }
        return valuesList;
    }

    public List<String> findAllIdentifierAttributeValues(IIpsProject ipsProject) {
        try {
            List<String> valueIds = new ArrayList<String>(getEnumValuesCount());
            IEnumType enumType = findEnumType(ipsProject);
            IEnumAttribute isIdentifierEnumAttribute = enumType.findIdentiferAttribute(ipsProject);
            if (isIdentifierEnumAttribute != null) {
                for (IEnumValue enumValue : getEnumValues()) {
                    IEnumAttributeValue value = enumValue.findEnumAttributeValue(ipsProject, isIdentifierEnumAttribute);
                    if (value == null) {
                        break;
                    }
                    valueIds.add(value.getValue());
                }
            }
            return valueIds;

        } catch (CoreException e) {
            throw new RuntimeException("Unable to determine the value ids of this enum type.", e);
        }
    }

    public IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject) throws CoreException {
        if (identifierAttributeValue == null) {
            return null;
        }

        IEnumType enumType = findEnumType(ipsProject);
        IEnumAttribute identifierAttribute = enumType.findIdentiferAttribute(ipsProject);
        for (IEnumValue enumValue : getEnumValues()) {
            IEnumAttributeValue value = enumValue.findEnumAttributeValue(ipsProject, identifierAttribute);
            if (value == null) {
                continue;
            }
            if (identifierAttributeValue.equals(value.getValue())) {
                return enumValue;
            }
        }

        return null;
    }

    public IEnumValue newEnumValue() throws CoreException {
        IEnumType enumType = findEnumType(getIpsProject());

        // Creation not possible if enumeration type can't be found.
        if (enumType == null) {
            return null;
        }

        // TODO pk 16-06-2009 activate when bug #1439 is fixed
        // ((IpsModel)getIpsModel()).stopBroadcastingChangesMadeByCurrentThread();

        // Create new enumeration value.
        IEnumValue newEnumValue = (IEnumValue)newPart(EnumValue.class);

        /*
         * Add as many enumeration attribute values as there are enumeration attributes in the
         * enumeration type.
         */
        boolean includeLiteralNames = this instanceof IEnumType;
        for (int i = 0; i < enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralNames); i++) {
            newEnumValue.newEnumAttributeValue();
        }

        // TODO pk 16-06-2009 activate when bug #1439 is fixed
        // ((IpsModel)getIpsModel()).resumeBroadcastingChangesMadeByCurrentThread();
        objectHasChanged(ContentChangeEvent.newPartAddedEvent(newEnumValue));

        return newEnumValue;
    }

    public int getEnumValuesCount() {
        return enumValues.size();
    }

    public int moveEnumValue(IEnumValue enumValue, boolean up) throws CoreException {
        ArgumentCheck.notNull(enumValue);

        int index = enumValues.indexOf(enumValue);
        if (index == -1) {
            throw new NoSuchElementException();
        }
        return enumValues.moveParts(new int[] { index }, up)[0];
    }

    public int getIndexOfEnumValue(IEnumValue enumValue) {
        ArgumentCheck.notNull(enumValue);

        int index = enumValues.indexOf(enumValue);
        if (index >= 0) {
            return index;
        }
        throw new NoSuchElementException();
    }

    public void clear() {
        clearUniqueIdentifierValidationCache();
        enumValues.clear();
        objectHasChanged();
    }

    /**
     * Adds a new map for a unique key to the list of maps maintained by this
     * <tt>EnumValueContainer</tt> for unique identifier validation.
     */
    void addUniqueIdentifierToValidationCache(int uniqueEnumAttributeIndex) {
        uniqueIdentifierValidationCache.put(new Integer(uniqueEnumAttributeIndex),
                new HashMap<String, List<IEnumAttributeValue>>());
    }

    /**
     * Removes the map for the given unique key identified by the index of its enum attribute from
     * the unique identifier validation cache.
     * <p>
     * Does nothing if there exists no entry for this unique identifier.
     */
    void removeUniqueIdentifierFromValidationCache(int uniqueEnumAttributeIndex) {
        uniqueIdentifierValidationCache.remove(new Integer(uniqueEnumAttributeIndex));
    }

    /**
     * Returns whether the unique identifier validation cache contains a mapping for the given enum
     * attribute index.
     */
    boolean containsValidationCacheUniqueIdentifier(int uniqueEnumAttributeIndex) {
        return uniqueIdentifierValidationCache.containsKey(new Integer(uniqueEnumAttributeIndex));
    }

    /**
     * Handles the deletion of enum attributes in respect to the unique identifier validation cache.
     * 
     * @param enumAttributeIndex The index of the deleted enum attribute.
     */
    void handleEnumAttributeDeletion(int enumAttributeIndex) {
        // All keys that are a higher number then the index must be decremented by 1.
        List<Map<String, List<IEnumAttributeValue>>> movingMaps = new LinkedList<Map<String, List<IEnumAttributeValue>>>();
        List<Integer> movingKeys = new LinkedList<Integer>();
        Integer[] keyArray = getCachedUniqueIdentifierKeys();
        for (Integer key : keyArray) {
            int keyValue = key.intValue();
            if (keyValue > enumAttributeIndex) {
                movingMaps.add(uniqueIdentifierValidationCache.get(new Integer(keyValue)));
                movingKeys.add(new Integer(keyValue));
            }
        }
        for (int i = 0; i < movingMaps.size(); i++) {
            int newKeyValue = movingKeys.get(i) - 1;
            uniqueIdentifierValidationCache.put(new Integer(newKeyValue), movingMaps.get(i));
        }
    }

    /**
     * Registers a unique identifier value to the validation cache.
     * <p>
     * If the given <tt>uniqueIdentifier</tt> is <tt>null</tt> this operation will do nothing.
     * 
     * @param uniqueEnumAttributeIndex The index of the unique identifier enum attribute.
     * @param uniqueIdentifier The value that is the unique identifier.
     * @param enumAttributeValue The enum attribute value that stores the entry.
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException If there is no unique identifier in the validation cache
     *             corresponding to the <tt>uniqueEnumAttributeIndex</tt>.
     */
    void addValidationCacheUniqueIdentifierEntry(int uniqueEnumAttributeIndex,
            String uniqueIdentifier,
            IEnumAttributeValue enumAttributeValue) {

        ArgumentCheck.notNull(enumAttributeValue);
        Integer outerKey = new Integer(uniqueEnumAttributeIndex);
        ArgumentCheck.isTrue(uniqueIdentifierValidationCache.containsKey(outerKey));

        if (uniqueIdentifier == null) {
            return;
        }

        Map<String, List<IEnumAttributeValue>> identifierMap = uniqueIdentifierValidationCache.get(outerKey);
        List<IEnumAttributeValue> enumAttributeValues = null;
        if (identifierMap.containsKey(uniqueIdentifier)) {
            enumAttributeValues = identifierMap.get(uniqueIdentifier);
        } else {
            enumAttributeValues = new LinkedList<IEnumAttributeValue>();
        }
        enumAttributeValues.add(enumAttributeValue);
        identifierMap.put(uniqueIdentifier, enumAttributeValues);
    }

    /**
     * Removes a unique identifier from the validation cache. If the map for the given unique
     * identifier value is empty after the operation (contains no enum attribute values anymore), it
     * will be removed from the cache, too.
     * <p>
     * This operation does nothing if the given <tt>uniqueIdentifier</tt> is <tt>null</tt> or there
     * is no such unique stored in the cache.
     * 
     * @param uniqueEnumAttributeIndex The index of the unique identifier enum attribute.
     * @param uniqueIdentifier The value that is the unique identifier.
     * @param enumAttributeValue The enum attribute value that stores the entry.
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     */
    void removeValidationCacheUniqueIdentifierEntry(int uniqueEnumAttributeIndex,
            String uniqueIdentifier,
            IEnumAttributeValue enumAttributeValue) {

        ArgumentCheck.notNull(enumAttributeValue);
        Integer outerKey = new Integer(uniqueEnumAttributeIndex);

        if (uniqueIdentifier == null || !(uniqueIdentifierValidationCache.containsKey(outerKey))) {
            return;
        }

        Map<String, List<IEnumAttributeValue>> identifierMap = uniqueIdentifierValidationCache.get(outerKey);
        List<IEnumAttributeValue> enumAttributeValues = identifierMap.get(uniqueIdentifier);
        if (enumAttributeValues != null) {
            enumAttributeValues.remove(enumAttributeValue);
            if (enumAttributeValues.size() == 0) {
                identifierMap.remove(uniqueIdentifier);
            }
        }
    }

    /** Returns whether the unique identifier validation cache has already been initialized. */
    boolean isUniqueIdentifierValidationCacheInitialized() {
        return uniqueIdentifierValidationCacheInitialized;
    }

    /**
     * Initializes the unique identifier validation cache. The operation might also fail. Returns
     * <tt>true</tt> on success, <tt>false</tt> on failure.
     */
    boolean initUniqueIdentifierValidationCache() throws CoreException {
        uniqueIdentifierValidationCache.clear();
        uniqueIdentifierValidationCacheInitialized = initUniqueIdentifierValidationCacheImpl();
        return uniqueIdentifierValidationCacheInitialized;
    }

    /**
     * Subclass implementation needs to initialize the validation cache.
     * <p>
     * Must return <tt>true</tt> if the operation was successful, <tt>false</tt> if not.
     * 
     * @throws CoreException May throw this kind of exception if the need arises.
     */
    abstract boolean initUniqueIdentifierValidationCacheImpl() throws CoreException;

    /** Initializes the unique identifier entries. */
    void initValidationCacheUniqueIdentifierEntries(List<IEnumAttribute> uniqueEnumAttributes, IEnumType enumType)
            throws CoreException {

        ArgumentCheck.notNull(uniqueEnumAttributes);
        for (IEnumValue currentEnumValue : getEnumValues()) {
            List<IEnumAttributeValue> uniqueEnumAttributeValues = currentEnumValue.findUniqueEnumAttributeValues(
                    uniqueEnumAttributes, getIpsProject());
            for (int i = 0; i < uniqueEnumAttributeValues.size(); i++) {
                IEnumAttributeValue currentUniqueAttributeValue = uniqueEnumAttributeValues.get(i);
                int currentReferencedAttributeIndex = enumType.getIndexOfEnumAttribute(uniqueEnumAttributes.get(i));
                addValidationCacheUniqueIdentifierEntry(currentReferencedAttributeIndex, currentUniqueAttributeValue
                        .getValue(), currentUniqueAttributeValue);
            }
        }
    }

    /**
     * Returns the list from the unique identifier validation cache corresponding to the given
     * unique identifier value for the given unique identifier identified by the given enum
     * attribute index.
     * 
     * @param enumAttributeIndex The index of the enum attribute.
     * @param uniqueIdentifierValue The value of the unique identifier.
     * 
     * @throws NullPointerException If <tt>uniqueIdentifierValue</tt> is <tt>null</tt>.
     */
    List<IEnumAttributeValue> getValidationCacheListForUniqueIdentifier(int enumAttributeIndex,
            String uniqueIdentifierValue) {

        ArgumentCheck.notNull(uniqueIdentifierValue);
        Integer outerKey = new Integer(enumAttributeIndex);
        ArgumentCheck.isTrue(uniqueIdentifierValidationCache.containsKey(outerKey));
        return uniqueIdentifierValidationCache.get(outerKey).get(uniqueIdentifierValue);
    }

    /**
     * Handles the movement of enum attributes in respect to the unique identifier validation cache.
     * 
     * @param index The index identifying the moved enum attribute.
     * @param up Flag indicating whether the enum attribute was moved up or down.
     */
    void handleMoveEnumAttributeForUniqueIdentifierValidationCache(int index, boolean up) {
        int modification = up ? -1 : 1;
        int otherAffectedIndex = index + modification;

        Integer key = new Integer(index);
        Integer otherKey = new Integer(otherAffectedIndex);
        Map<String, List<IEnumAttributeValue>> keyMap = uniqueIdentifierValidationCache.get(key);
        Map<String, List<IEnumAttributeValue>> otherKeyMap = uniqueIdentifierValidationCache.get(otherKey);

        // At least one of the two affected keys must be existent in the validation cache.
        boolean keyAffected = keyMap != null;
        boolean otherKeyAffected = otherKeyMap != null;
        if (!(keyAffected || otherKeyAffected)) {
            return;
        }

        if (keyAffected) {
            removeUniqueIdentifierFromValidationCache(key);
        }
        if (otherKeyAffected) {
            removeUniqueIdentifierFromValidationCache(otherKey);
        }
        if (keyAffected) {
            uniqueIdentifierValidationCache.put(new Integer(index + modification), keyMap);
        }
        if (otherKeyAffected) {
            uniqueIdentifierValidationCache.put(new Integer(otherAffectedIndex - modification), otherKeyMap);
        }
    }

    Integer[] getCachedUniqueIdentifierKeys() {
        Set<Integer> keys = uniqueIdentifierValidationCache.keySet();
        return keys.toArray(new Integer[keys.size()]);
    }

    public void clearUniqueIdentifierValidationCache() {
        uniqueIdentifierValidationCache.clear();
        uniqueIdentifierValidationCacheInitialized = false;
    }

    @Override
    public void initFromXml(Element element) {
        super.initFromXml(element);
        clearUniqueIdentifierValidationCache();
    }

}
