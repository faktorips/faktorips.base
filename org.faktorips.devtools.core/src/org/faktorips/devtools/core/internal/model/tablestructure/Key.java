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
package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Key extends AtomicIpsObjectPart implements IKey {

    final static String KEY_ITEM_TAG_NAME = "Item"; //$NON-NLS-1$

    private List<String> items = new ArrayList<String>(0);

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
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        for (String item : items) {
            IColumn c = getTableStructure().getColumn(item);
            if (c != null) {
                keyItems.add(c);
            } else {
                IColumnRange range = getTableStructure().getRange(item);
                if (range != null) {
                    keyItems.add(range);
                }
            }
        }
        return keyItems.toArray(new IKeyItem[keyItems.size()]);
    }

    @Override
    public void setKeyItems(String[] itemNames) {
        items = CollectionUtil.toArrayList(itemNames);
        objectHasChanged();
    }

    @Override
    public void addKeyItem(String name) {
        items.add(name);

    }

    @Override
    public void removeKeyItem(String name) {
        items.remove(name);
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
        List<IKeyItem> result = new ArrayList<IKeyItem>();
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
        items = new ArrayList<String>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Element itemElement = (Element)nl.item(i);
            String item = itemElement.getAttribute("name"); //$NON-NLS-1$
            items.add(item);
        }
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

        IKeyItem[] keyItems = getKeyItems();
        for (int i = 0; i < keyItems.length; i++) {
            if (keyItems[i].equals(item)) {
                return i;
            }
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
                "The provided item name: " + itemName + " doesn't match with one of the items" + //$NON-NLS-1$ //$NON-NLS-2$
                        " in the itme list of this key."); //$NON-NLS-1$
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
