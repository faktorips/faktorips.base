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
package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * A key is list of key items. Key items are either columns or ranges that belong to the same table.
 */
public interface IKey extends IIpsObjectPart {

    /**
     * Java beans property name for the property keyItems.
     */
    public final static String PROPERTY_KEY_ITEMS = "keyItems"; //$NON-NLS-1$

    /**
     * Returns the table structure the key belongs to.
     */
    public ITableStructure getTableStructure();

    /**
     * Returns the names of the key items that make up the key.
     */
    public String[] getKeyItemNames();

    /**
     * Returns the key items that make up the key. Only key items that can be resolved by name are
     * returned.
     */
    public IKeyItem[] getKeyItems();

    /**
     * Returns the key item at the indexed position.
     * 
     * @throws IndexOutOfBoundsException ff the index is not within the bounds.
     */
    public IKeyItem getKeyItemAt(int index);

    /**
     * Returns the index for the provided key item.
     * 
     * @throws IllegalArgumentException if the item is not part of this key
     */
    public int getIndexForKeyItem(IKeyItem item);

    /**
     * Returns the of the key item at the indexed position.
     * 
     * @throws IndexOutOfBoundsException ff the index is not within the bounds.
     */
    public String getNameOfKeyItemAt(int index);

    /**
     * Returns the index for the provided key item name.
     * 
     * @throws IllegalArgumentException if the item is not part of this key
     */
    public int getIndexForKeyItemName(String itemName);

    /**
     * Sets the items this key is made up of.
     * 
     * @throws IllegalArgumentException if itemNames is <code>null</code> or any of the names in the
     *             array is <code>null</code>.
     */
    public void setKeyItems(String[] itemNames);

    /**
     * Adds the key item with the given name to the key.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>
     */
    public void addKeyItem(String name);

    /**
     * Removes the key item with the given name from the key. If the key does not contain an item
     * with the given name, the key remains unchanged.
     */
    public void removeKeyItem(String name);

    /**
     * Returns the number of key items.
     */
    public int getNumOfKeyItems();

    /**
     * Returns the key items that can be added to the key. These are the table's key items that
     * aren't part of the key yet. If no candidate exists, an empty array is returned.
     */
    public IKeyItem[] getItemCandidates();

}
