/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * A key is list of key items. Key items are either columns or ranges that belong to the same table.
 */
public interface IKey extends IIpsObjectPart, IDescribedElement {

    /**
     * Java beans property name for the property keyItems.
     */
    String PROPERTY_KEY_ITEMS = "keyItems"; //$NON-NLS-1$

    /**
     * Returns the table structure the key belongs to.
     */
    ITableStructure getTableStructure();

    /**
     * Returns the names of the key items that make up the key.
     */
    String[] getKeyItemNames();

    /**
     * Returns the key items that make up the key. Only key items that can be resolved by name are
     * returned.
     */
    IKeyItem[] getKeyItems();

    /**
     * Returns the key item at the indexed position.
     * 
     * @throws IndexOutOfBoundsException ff the index is not within the bounds.
     */
    IKeyItem getKeyItemAt(int index);

    /**
     * Returns the index for the provided key item.
     * 
     * @throws IllegalArgumentException if the item is not part of this key
     */
    int getIndexForKeyItem(IKeyItem item);

    /**
     * Returns the of the key item at the indexed position.
     * 
     * @throws IndexOutOfBoundsException ff the index is not within the bounds.
     */
    String getNameOfKeyItemAt(int index);

    /**
     * Returns the index for the provided key item name.
     * 
     * @throws IllegalArgumentException if the item is not part of this key
     */
    int getIndexForKeyItemName(String itemName);

    /**
     * Sets the items this key is made up of.
     * 
     * @throws IllegalArgumentException if itemNames is <code>null</code> or any of the names in the
     *             array is <code>null</code>.
     */
    void setKeyItems(String[] itemNames);

    /**
     * Adds the key item with the given name to the key.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>
     */
    void addKeyItem(String name);

    /**
     * Removes the key item with the given name from the key. If the key does not contain an item
     * with the given name, the key remains unchanged.
     */
    void removeKeyItem(String name);

    /**
     * Returns the number of key items.
     */
    int getNumOfKeyItems();

    /**
     * Returns the key items that can be added to the key. These are the table's key items that
     * aren't part of the key yet. If no candidate exists, an empty array is returned.
     */
    IKeyItem[] getItemCandidates();

}
