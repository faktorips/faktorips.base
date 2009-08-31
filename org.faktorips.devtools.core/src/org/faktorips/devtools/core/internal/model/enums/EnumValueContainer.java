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
import org.faktorips.devtools.core.internal.model.IpsModel;
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
 * Implementation of <tt>IEnumValueContainer</tt>, see the corresponding interface for more details.
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
     * index of the <tt>IEnumAttribute</tt> being the unique identifier.
     */
    private Map<Integer, Map<String, List<IEnumAttributeValue>>> uniqueIdentifierValidationCache;

    /** Flag indicating whether the unique identifier validation cache has been already initialized. */
    private boolean uniqueIdentifierValidationCacheInitialized;

    /**
     * Creates a new <tt>EnumValueContainer</tt>.
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
                    IEnumAttributeValue value = enumValue.getEnumAttributeValue(isIdentifierEnumAttribute);
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
            IEnumAttributeValue value = enumValue.getEnumAttributeValue(identifierAttribute);
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
        final IEnumType enumType = findEnumType(getIpsProject());

        // Creation not possible if enumeration type can't be found.
        if (enumType == null) {
            return null;
        }

        return executeModificationsWithSingleEvent(new SingleEventModification<IEnumValue>() {

            IEnumValue newEnumValue;

            @Override
            public boolean execute() throws CoreException {
                // Create new enumeration value.
                newEnumValue = (IEnumValue)newPart(EnumValue.class);

                /*
                 * Add as many enumeration attribute values as there are enumeration attributes in
                 * the enumeration type.
                 */
                boolean includeLiteralNames = EnumValueContainer.this instanceof IEnumType;
                for (int i = 0; i < enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralNames); i++) {
                    newEnumValue.newEnumAttributeValue();
                }
                return true;
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

    public int getEnumValuesCount() {
        return enumValues.size();
    }

    public int[] moveEnumValues(final List<IEnumValue> enumValuesToMove, final boolean up) throws CoreException {
        ArgumentCheck.notNull(enumValuesToMove);
        final int numberToMove = enumValuesToMove.size();
        if (numberToMove == 0) {
            return new int[0];
        }

        return executeModificationsWithSingleEvent(new SingleEventModification<int[]>() {
            int[] indizes = new int[numberToMove];

            @Override
            public boolean execute() throws CoreException {
                for (int i = 0; i < numberToMove; i++) {
                    IEnumValue currentEnumValue = enumValuesToMove.get(i);
                    int index = getIndexOfEnumValue(currentEnumValue);
                    if (index == -1) {
                        throw new NoSuchElementException();
                    }
                    indizes[i] = index;
                }
                indizes = enumValues.moveParts(indizes, up);
                return true;
            }

            @Override
            public int[] getResult() {
                return indizes;
            }
        });
    }

    public int getIndexOfEnumValue(IEnumValue enumValue) {
        ArgumentCheck.notNull(enumValue);
        return enumValues.indexOf(enumValue);
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
     * Returns whether the unique identifier validation cache contains a mapping for the given
     * unique <tt>IEnumAttribute</tt> identified by it's index.
     */
    boolean containsValidationCacheUniqueIdentifier(int uniqueEnumAttributeIndex) {
        return uniqueIdentifierValidationCache.containsKey(new Integer(uniqueEnumAttributeIndex));
    }

    /**
     * Handles the deletion of <tt>IEnumAttribute</tt>s in respect to the unique identifier
     * validation cache.
     * 
     * @param enumAttributeIndex The index of the deleted <tt>IEnumAttribute</tt>.
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
     * @param uniqueEnumAttributeIndex The index of the unique identifier <tt>IEnumAttribute</tt>.
     * @param uniqueIdentifier The value that is the unique identifier.
     * @param enumAttributeValue The <tt>IEnumAttributeValue</tt> that stores the entry.
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
     * identifier value is empty after the operation (contains no <tt>IEnumAttributeValue</tt>s
     * anymore), it will be removed from the cache, too.
     * <p>
     * This operation does nothing if the given <tt>uniqueIdentifier</tt> is <tt>null</tt> or there
     * is no such unique stored in the cache.
     * 
     * @param uniqueEnumAttributeIndex The index of the unique identifier <tt>IEnumAttribute</tt>.
     * @param uniqueIdentifier The value that is the unique identifier.
     * @param enumAttributeValue The <tt>IEnumAttributeValue</tt> that stores the entry.
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
     * unique identifier value for the given unique identifier (given as index of the corresponding
     * <tt>IEnumAttribute</tt>).
     * 
     * @param enumAttributeIndex The index of the <tt>IEnumAttribute</tt>.
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
     * Handles the movement of <tt>IEnumAttribute</tt>s in respect to the unique identifier
     * validation cache.
     * 
     * @param enumAttributeIndex The index identifying the moved <tt>IEnumAttribute</tt>.
     * @param up Flag indicating whether the <tt>IEnumAttribute</tt> was moved up or down.
     */
    void handleMoveEnumAttributeForUniqueIdentifierValidationCache(int enumAttributeIndex, boolean up) {
        int modification = up ? -1 : 1;
        int otherAffectedIndex = enumAttributeIndex + modification;

        Integer key = new Integer(enumAttributeIndex);
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
            uniqueIdentifierValidationCache.put(new Integer(enumAttributeIndex + modification), keyMap);
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

    public boolean deleteEnumValues(List<IEnumValue> enumValuesToDelete) {
        if (enumValuesToDelete == null) {
            return false;
        }

        // TODO - BROADCASTING CHANGES -
        ((IpsModel)getIpsModel()).stopBroadcastingChangesMadeByCurrentThread();

        boolean changed = false;
        for (IEnumValue currentEnumValue : enumValuesToDelete) {
            if (!(enumValues.contains(currentEnumValue))) {
                continue;
            }
            currentEnumValue.delete();
            changed = true;
        }

        // TODO - BROADCASTING CHANGES -
        ((IpsModel)getIpsModel()).resumeBroadcastingChangesMadeByCurrentThread();

        if (changed) {
            objectHasChanged();
        }
        return changed;
    }

}
