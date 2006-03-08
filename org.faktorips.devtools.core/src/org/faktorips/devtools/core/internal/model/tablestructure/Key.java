/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 */
public abstract class Key extends IpsObjectPart implements IKey {
    
    final static String KEY_ITEM_TAG_NAME = "Item"; //$NON-NLS-1$
    
    private List items = new ArrayList(0);

    /**
     * @param parent
     */
    public Key(TableStructure tableStructure, int id) {
        super(tableStructure, id);
    }

    protected Key() {
        super();
    }

    /**
     * Overridden.
     */ 
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }
    
    /** 
     * Overridden.
     */
    public String[] getKeyItemNames() {
        return (String[])items.toArray(new String[items.size()]);
    }
    
    /**
     * Overridden.
     */
    public IKeyItem[] getKeyItems() {
        List keyItems = new ArrayList();
        for (Iterator it=items.iterator(); it.hasNext(); ) {
            String item = (String)it.next();
            IColumn c = getTableStructure().getColumn(item);
            if (c!=null) {
                keyItems.add(c);
            } else {
                IColumnRange range = getTableStructure().getRange(item);
                if (range!=null) {
                    keyItems.add(range);
                }
            }
        }
        return (IKeyItem[])keyItems.toArray(new IKeyItem[keyItems.size()]);
    }

    /** 
     * Overridden.
     */
    public void setKeyItems(String[] itemNames) {
        items = CollectionUtil.toArrayList(itemNames);
        updateSrcFile();
    }

    /** 
     * Overridden.
     */
    public void addKeyItem(String name) {
        // TODO Auto-generated method stub

    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IKey#removeKeyItem(java.lang.String)
     */
    public void removeKeyItem(String name) {
        // TODO Auto-generated method stub

    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IKey#getNumOfKeyItems()
     */
    public int getNumOfKeyItems() {
        return items.size();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableKey.gif"); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IKey#getItemCandidates()
     */
    public IKeyItem[] getItemCandidates() {
        return getItemCandidates(getTableStructure());
    }

    private IKeyItem[] getItemCandidates(ITableStructure tableStructure) {
        List result = new ArrayList();
        addCandidates(result, tableStructure.getColumns());
        addCandidates(result, tableStructure.getRanges());
        return (IKeyItem[])result.toArray(new IKeyItem[result.size()]);
    }
    
    /*
     * Adds the items that are candidates to the result list. 
     */
    private void addCandidates(List result, IKeyItem[] items) {
        for (int i=0; i<items.length; i++) {
            if (isCandidate(items[i])) {
                result.add(items[i]);
            }
        }
    }
    
    /*
     * Returns true if the item is a candidate to be added to the key. 
     */
    private boolean isCandidate (IKeyItem candidateItem) {
        for (Iterator it=items.iterator(); it.hasNext(); ) {
            String keyItem = (String)it.next();
            if (keyItem.equals(candidateItem.getName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        NodeList nl = element.getElementsByTagName(KEY_ITEM_TAG_NAME);
        items = new ArrayList(nl.getLength());
        for (int i=0; i<nl.getLength(); i++) {
            Element itemElement = (Element)nl.item(i);
            String item = itemElement.getAttribute("name"); //$NON-NLS-1$
            items.add(item);
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        for (Iterator it=items.iterator(); it.hasNext(); ) {
            String item = (String)it.next();
            Element itemElement = element.getOwnerDocument().createElement(KEY_ITEM_TAG_NAME);
            itemElement.setAttribute("name", item); //$NON-NLS-1$
            element.appendChild(itemElement);
        }
    }

}
