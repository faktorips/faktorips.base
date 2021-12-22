/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IKey;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.util.CollectionUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Key extends AtomicIpsObjectPart implements IKey {

    static final String KEY_ITEM_TAG_NAME = "Item"; //$NON-NLS-1$

    private List<String> items = new ArrayList<>(0);

    // caching the keyItems for better performance
    private List<IKeyItem> keyItems = new ArrayList<>();

    public Key(TableStructure tableStructure, String id) {
        super(tableStructure, id);
    }

    protected Key() {
        super();
    }

    @Override
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    @Override
    public String[] getKeyItemNames() {
        return items.toArray(new String[items.size()]);
    }

    @Override
    public IKeyItem[] getKeyItems() {
        return keyItems.toArray(new IKeyItem[keyItems.size()]);
    }

    private void updateKeyItems() {
        keyItems = new ArrayList<>();
        for (String item : items) {
            IKeyItem keyItem = getKeyItem(item);
            if (keyItem != null) {
                keyItems.add(keyItem);
            }
        }
    }

    private IKeyItem getKeyItem(String item) {
        IKeyItem keyItem = null;
        if (getTableStructure() != null) {
            IColumn c = getTableStructure().getColumn(item);
            if (c != null) {
                keyItem = c;
            } else {
                IColumnRange range = getTableStructure().getRange(item);
                if (range != null) {
                    keyItem = range;
                }
            }
            return keyItem;
        }
        return null;
    }

    @Override
    public void setKeyItems(String[] itemNames) {
        items = CollectionUtil.toArrayList(itemNames);
        updateKeyItems();
        objectHasChanged();
    }

    @Override
    public void addKeyItem(String name) {
        items.add(name);
        IKeyItem keyItem = getKeyItem(name);
        if (keyItem != null) {
            keyItems.add(keyItem);
        }
        objectHasChanged();
    }

    @Override
    public void removeKeyItem(String name) {
        items.remove(name);
        IKeyItem keyItem = getKeyItem(name);
        if (keyItem != null) {
            keyItems.remove(keyItem);
        }
        objectHasChanged();
    }

    @Override
    public int getNumOfKeyItems() {
        return items.size();
    }

    @Override
    public IKeyItem[] getItemCandidates() {
        return getItemCandidates(getTableStructure());
    }

    private IKeyItem[] getItemCandidates(ITableStructure tableStructure) {
        List<IKeyItem> result = new ArrayList<>();
        addCandidates(result, tableStructure.getColumns());
        addCandidates(result, tableStructure.getRanges());
        return result.toArray(new IKeyItem[result.size()]);
    }

    /**
     * Adds the items that are candidates to the result list.
     */
    private void addCandidates(List<IKeyItem> result, IKeyItem[] items) {
        for (IKeyItem item : items) {
            if (isCandidate(item)) {
                result.add(item);
            }
        }
    }

    /**
     * Returns true if the item is a candidate to be added to the key.
     */
    private boolean isCandidate(IKeyItem candidateItem) {
        for (String keyItem : items) {
            if (keyItem.equals(candidateItem.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        NodeList nl = element.getElementsByTagName(KEY_ITEM_TAG_NAME);
        items = new ArrayList<>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Element itemElement = (Element)nl.item(i);
            String item = itemElement.getAttribute("name"); //$NON-NLS-1$
            items.add(item);
        }
        updateKeyItems();
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        for (String item : items) {
            Element itemElement = element.getOwnerDocument().createElement(KEY_ITEM_TAG_NAME);
            itemElement.setAttribute("name", item); //$NON-NLS-1$
            element.appendChild(itemElement);
        }
    }

    @Override
    public int getIndexForKeyItem(IKeyItem item) {

        int indexOfKeyItem = keyItems.indexOf(item);

        if (indexOfKeyItem >= 0) {
            return indexOfKeyItem;
        }

        throw new IllegalArgumentException(
                "The provided item: " + item + " is not part of the list of items hold by this key."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int getIndexForKeyItemName(String itemName) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(itemName)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "The provided item name: " + itemName + " doesn't match with one of the items" //$NON-NLS-1$ //$NON-NLS-2$
                        + " in the itme list of this key."); //$NON-NLS-1$
    }

    @Override
    public IKeyItem getKeyItemAt(int index) {
        return getKeyItems()[index];
    }

    @Override
    public String getNameOfKeyItemAt(int index) {
        return items.get(index);
    }

}
