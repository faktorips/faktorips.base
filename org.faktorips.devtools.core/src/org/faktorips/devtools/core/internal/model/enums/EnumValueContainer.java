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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
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
     * associates them with a list of all <tt>IEnumAttributeValue</tt>s referring to unique
     * identifiers in this row. This list is intended to be used to validate unique identifiers.
     * Each entry of the outer map stands for another unique identifier. The number that is used as
     * the key for each unique identifier is meant to be the index of the <tt>IEnumAttribute</tt>
     * being the unique identifier.
     */
    private Map<Integer, Map<String, List<IEnumAttributeValue>>> uniqueIdentifierCache;

    /**
     * The index of the default identifier (can be used to query the {@link #uniqueIdentifierCache}
     * for the default identifier value. This is a list because there can be multiple default
     * identifiers (although this state is invalid).
     */
    private List<Integer> cachedDefaultIdentifierIndices = new ArrayList<Integer>();

    /** Flag indicating whether the unique identifier cache has been already initialized. */
    private boolean uniqueIdentifierCacheInitialized;

    /**
     * Creates a new <tt>EnumValueContainer</tt>.
     * 
     * @param file The IPS source file in which this IPS object will be stored in.
     */
    protected EnumValueContainer(IIpsSrcFile file) {
        super(file);

        enumValues = new IpsObjectPartCollection<IEnumValue>(this, EnumValue.class, IEnumValue.class,
                IEnumValue.XML_TAG);
        uniqueIdentifierCache = new HashMap<Integer, Map<String, List<IEnumAttributeValue>>>();
    }

    @Override
    public List<IEnumValue> getEnumValues() {
        List<IEnumValue> valuesList = new ArrayList<IEnumValue>();
        valuesList.addAll(enumValues.asList());
        return valuesList;
    }

    @Override
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
            throw new RuntimeException("Unable to determine the value ids of this enum type.", e); //$NON-NLS-1$
        }
    }

    @Override
    public IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject) {
        if (identifierAttributeValue == null) {
            return null;
        }

        initUniqueIdentifierCache(ipsProject);

        if (cachedDefaultIdentifierIndices.isEmpty()) {
            return null;
        }

        Integer defaultIdentifierIndex = cachedDefaultIdentifierIndices.get(0);
        Map<String, List<IEnumAttributeValue>> cachedValues = uniqueIdentifierCache.get(defaultIdentifierIndex);
        if (cachedValues == null) {
            return null;
        }

        List<IEnumAttributeValue> cachedAttributeValues = cachedValues.get(identifierAttributeValue);
        if (cachedAttributeValues == null || cachedAttributeValues.isEmpty()) {
            return null;
        }

        return cachedAttributeValues.get(0).getEnumValue();
    }

    @Override
    public IEnumValue newEnumValue() throws CoreException {
        final IEnumType enumType = findEnumType(getIpsProject());

        // Creation not possible if enumeration type can't be found.
        if (enumType == null) {
            return null;
        }

        return getIpsModel().executeModificationsWithSingleEvent(
                new SingleEventModification<IEnumValue>(getIpsSrcFile()) {

                    IEnumValue newEnumValue;

                    @Override
                    public boolean execute() throws CoreException {
                        // Create new enumeration value.
                        newEnumValue = (IEnumValue)newPart(EnumValue.class);

                        /*
                         * Add as many enumeration attribute values as there are enumeration
                         * attributes in the enumeration type.
                         */
                        boolean includeLiteralNames = EnumValueContainer.this instanceof IEnumType;
                        for (IEnumAttribute enumAttribute : enumType
                                .getEnumAttributesIncludeSupertypeCopies(includeLiteralNames)) {
                            if (enumAttribute.isEnumLiteralNameAttribute()) {
                                newEnumValue.newEnumLiteralNameAttributeValue();
                            } else {
                                newEnumValue.newEnumAttributeValue();
                            }
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

    @Override
    public int getEnumValuesCount() {
        return enumValues.size();
    }

    @Override
    public int[] moveEnumValues(final List<IEnumValue> enumValuesToMove, final boolean up) throws CoreException {
        ArgumentCheck.notNull(enumValuesToMove);
        final int numberToMove = enumValuesToMove.size();
        if (numberToMove == 0) {
            return new int[0];
        }

        return getIpsModel().executeModificationsWithSingleEvent(new SingleEventModification<int[]>(getIpsSrcFile()) {
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

    @Override
    public int getIndexOfEnumValue(IEnumValue enumValue) {
        ArgumentCheck.notNull(enumValue);
        return enumValues.indexOf(enumValue);
    }

    @Override
    public void clear() {
        clearUniqueIdentifierCache();
        enumValues.clear();
        objectHasChanged();
    }

    /**
     * Adds a new map of values to {@link IEnumAttributeValue}s for a unique key to the unique
     * identifier cache.
     */
    void addUniqueIdentifierToCache(int uniqueEnumAttributeIndex, boolean defaultIdentifier) {
        uniqueIdentifierCache.put(new Integer(uniqueEnumAttributeIndex),
                new HashMap<String, List<IEnumAttributeValue>>());
        if (defaultIdentifier) {
            cachedDefaultIdentifierIndices.add(uniqueEnumAttributeIndex);
        }
    }

    /**
     * Removes the map for the given unique key identified by the index of its enum attribute from
     * the unique identifier validation cache.
     * <p>
     * Does nothing if there exists no entry for this unique identifier.
     */
    void removeUniqueIdentifierFromCache(int uniqueEnumAttributeIndex) {
        uniqueIdentifierCache.remove(new Integer(uniqueEnumAttributeIndex));
        cachedDefaultIdentifierIndices.remove(Integer.valueOf(uniqueEnumAttributeIndex));
    }

    void addDefaultIdentifierToCache(int identifierIndex) {
        cachedDefaultIdentifierIndices.add(identifierIndex);
    }

    void removeDefaultIdentifierFromCache(int identifierIndex) {
        cachedDefaultIdentifierIndices.remove(Integer.valueOf(identifierIndex));
    }

    /**
     * Returns whether the unique identifier validation cache contains a mapping for the given
     * unique <tt>IEnumAttribute</tt> identified by it's index.
     */
    boolean containsCacheUniqueIdentifier(int uniqueEnumAttributeIndex) {
        return uniqueIdentifierCache.containsKey(new Integer(uniqueEnumAttributeIndex));
    }

    /**
     * Handles the deletion of <tt>IEnumAttribute</tt>s with respect to the unique identifier cache.
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
                movingMaps.add(uniqueIdentifierCache.get(new Integer(keyValue)));
                movingKeys.add(new Integer(keyValue));
            }
        }
        for (int i = 0; i < movingMaps.size(); i++) {
            int newKeyValue = movingKeys.get(i) - 1;
            uniqueIdentifierCache.put(new Integer(newKeyValue), movingMaps.get(i));
        }
    }

    /**
     * Registers a unique identifier value to the unique identifier cache.
     * <p>
     * If the given <tt>uniqueIdentifier</tt> is <tt>null</tt> this operation will do nothing.
     * 
     * @param uniqueEnumAttributeIndex The index of the unique identifier <tt>IEnumAttribute</tt>.
     * @param uniqueIdentifier The value that is the unique identifier.
     * @param enumAttributeValue The <tt>IEnumAttributeValue</tt> that stores the entry.
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException If there is no unique identifier in the cache corresponding
     *             to the <tt>uniqueEnumAttributeIndex</tt>.
     */
    void addCacheEntry(int uniqueEnumAttributeIndex, String uniqueIdentifier, IEnumAttributeValue enumAttributeValue) {
        ArgumentCheck.notNull(enumAttributeValue);
        ArgumentCheck.isTrue(uniqueIdentifierCache.containsKey(uniqueEnumAttributeIndex));

        if (uniqueIdentifier == null) {
            return;
        }

        Map<String, List<IEnumAttributeValue>> identifierMap = uniqueIdentifierCache.get(uniqueEnumAttributeIndex);
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
     * Removes a unique identifier from the cache.
     * <p>
     * If the map for the given unique identifier value is empty after the operation (contains no
     * <tt>IEnumAttributeValue</tt>s anymore), it will be removed from the cache, too.
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
    void removeCacheEntry(int uniqueEnumAttributeIndex, String uniqueIdentifier, IEnumAttributeValue enumAttributeValue) {
        ArgumentCheck.notNull(enumAttributeValue);
        Integer outerKey = new Integer(uniqueEnumAttributeIndex);

        if (uniqueIdentifier == null || !(uniqueIdentifierCache.containsKey(outerKey))) {
            return;
        }

        Map<String, List<IEnumAttributeValue>> identifierMap = uniqueIdentifierCache.get(outerKey);
        List<IEnumAttributeValue> enumAttributeValues = identifierMap.get(uniqueIdentifier);
        if (enumAttributeValues != null) {
            enumAttributeValues.remove(enumAttributeValue);
            if (enumAttributeValues.size() == 0) {
                identifierMap.remove(uniqueIdentifier);
            }
        }
    }

    /**
     * Returns whether the unique identifier cache has already been initialized.
     */
    boolean isUniqueIdentifierCacheInitialized() {
        return uniqueIdentifierCacheInitialized;
    }

    /**
     * Initializes the unique identifier validation cache.
     * <p>
     * The operation might also fail. Returns <tt>true</tt> on success (or if the cache was already
     * initialized), <tt>false</tt> on failure.
     * 
     * @param ipsProject The {@link IIpsProject} to use for the search of the referenced enumeration
     *            type and it's super types
     */
    boolean initUniqueIdentifierCache(IIpsProject ipsProject) {
        if (uniqueIdentifierCacheInitialized) {
            return true;
        }
        clearUniqueIdentifierCache();
        try {
            uniqueIdentifierCacheInitialized = initUniqueIdentifierCacheImpl(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            uniqueIdentifierCacheInitialized = false;
        }
        return uniqueIdentifierCacheInitialized;
    }

    /**
     * Subclass implementation needs to initialize the unique identifier cache.
     * <p>
     * Must return <tt>true</tt> if the operation was successful, <tt>false</tt> if not.
     * 
     * @param ipsProject The {@link IIpsProject} to use for the search of the referenced enumeration
     *            type and it's super types
     * 
     * @throws CoreException May throw this kind of exception if the need arises.
     */
    abstract boolean initUniqueIdentifierCacheImpl(IIpsProject ipsProject) throws CoreException;

    /**
     * Initializes the unique identifier entries.
     */
    void initCacheEntries(List<IEnumAttribute> uniqueEnumAttributes, IEnumType enumType) throws CoreException {
        ArgumentCheck.notNull(uniqueEnumAttributes);

        for (IEnumValue currentEnumValue : getEnumValues()) {
            List<IEnumAttributeValue> uniqueEnumAttributeValues = currentEnumValue.findUniqueEnumAttributeValues(
                    uniqueEnumAttributes, getIpsProject());
            for (int i = 0; i < uniqueEnumAttributeValues.size(); i++) {
                IEnumAttributeValue currentUniqueAttributeValue = uniqueEnumAttributeValues.get(i);
                int currentReferencedAttributeIndex = enumType.getIndexOfEnumAttribute(uniqueEnumAttributes.get(i));
                addCacheEntry(currentReferencedAttributeIndex, currentUniqueAttributeValue.getValue(),
                        currentUniqueAttributeValue);
            }
        }
    }

    /**
     * Returns the list from the unique identifier cache corresponding to the given unique
     * identifier value for the given unique identifier (given as index of the corresponding
     * <tt>IEnumAttribute</tt>).
     * 
     * @param enumAttributeIndex The index of the <tt>IEnumAttribute</tt>.
     * @param uniqueIdentifierValue The value of the unique identifier.
     * 
     * @throws NullPointerException If <tt>uniqueIdentifierValue</tt> is <tt>null</tt>.
     */
    List<IEnumAttributeValue> getCacheListForUniqueIdentifier(int enumAttributeIndex, String uniqueIdentifierValue) {
        ArgumentCheck.notNull(uniqueIdentifierValue);
        Integer outerKey = new Integer(enumAttributeIndex);
        ArgumentCheck.isTrue(uniqueIdentifierCache.containsKey(outerKey));
        return uniqueIdentifierCache.get(outerKey).get(uniqueIdentifierValue);
    }

    /**
     * Handles the movement of <tt>IEnumAttribute</tt>s in respect to the unique identifier cache.
     * 
     * @param enumAttributeIndex The index identifying the moved <tt>IEnumAttribute</tt>.
     * @param up Flag indicating whether the <tt>IEnumAttribute</tt> was moved up or down.
     */
    void handleMoveEnumAttribute(int enumAttributeIndex, boolean up) {
        int modification = up ? -1 : 1;
        int otherAffectedIndex = enumAttributeIndex + modification;
        if (cachedDefaultIdentifierIndices.contains(enumAttributeIndex)) {
            int listIndex = cachedDefaultIdentifierIndices.indexOf(enumAttributeIndex);
            cachedDefaultIdentifierIndices.set(listIndex, enumAttributeIndex + modification);
        }
        if (cachedDefaultIdentifierIndices.contains(otherAffectedIndex)) {
            int listIndex = cachedDefaultIdentifierIndices.indexOf(otherAffectedIndex);
            cachedDefaultIdentifierIndices.set(listIndex, otherAffectedIndex - modification);
        }

        Integer key = new Integer(enumAttributeIndex);
        Integer otherKey = new Integer(otherAffectedIndex);
        Map<String, List<IEnumAttributeValue>> keyMap = uniqueIdentifierCache.get(key);
        Map<String, List<IEnumAttributeValue>> otherKeyMap = uniqueIdentifierCache.get(otherKey);

        // At least one of the two affected keys must be existent in the validation cache.
        boolean keyAffected = keyMap != null;
        boolean otherKeyAffected = otherKeyMap != null;
        if (!(keyAffected || otherKeyAffected)) {
            return;
        }

        if (keyAffected) {
            removeUniqueIdentifierFromCache(key);
        }
        if (otherKeyAffected) {
            removeUniqueIdentifierFromCache(otherKey);
        }
        if (keyAffected) {
            uniqueIdentifierCache.put(new Integer(enumAttributeIndex + modification), keyMap);
        }
        if (otherKeyAffected) {
            uniqueIdentifierCache.put(new Integer(otherAffectedIndex - modification), otherKeyMap);
        }
    }

    Integer[] getCachedUniqueIdentifierKeys() {
        Set<Integer> keys = uniqueIdentifierCache.keySet();
        return keys.toArray(new Integer[keys.size()]);
    }

    @Override
    public void clearUniqueIdentifierCache() {
        uniqueIdentifierCache.clear();
        cachedDefaultIdentifierIndices.clear();
        uniqueIdentifierCacheInitialized = false;
    }

    @Override
    public void initFromXml(Element element) {
        super.initFromXml(element);
        clearUniqueIdentifierCache();
    }

    @Override
    public boolean deleteEnumValues(final List<IEnumValue> enumValuesToDelete) {
        if (enumValuesToDelete == null) {
            return false;
        }

        try {
            return getIpsModel().executeModificationsWithSingleEvent(
                    new SingleEventModification<Boolean>(getIpsSrcFile()) {

                        private Boolean changed;

                        @Override
                        protected boolean execute() throws CoreException {
                            changed = false;
                            for (IEnumValue currentEnumValue : enumValuesToDelete) {
                                if (!(enumValues.contains(currentEnumValue))) {
                                    continue;
                                }
                                currentEnumValue.delete();
                                changed = true;
                            }
                            return changed;
                        }

                        @Override
                        protected Boolean getResult() {
                            return changed;
                        }
                    });
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
