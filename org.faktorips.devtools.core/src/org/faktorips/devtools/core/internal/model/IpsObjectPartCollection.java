/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * A collection of ips object parts. This class is used togehter with BaseIpsObject and BaseIpsObjectPart
 * to ease the development of new ips object subclasses. An ips object part collection is 
 * a collection of parts of the same type. E.g. a collection holds only methods or only attributes
 * but not both. In constrast an IpsObjectPartContainer is a container for ips object parts of
 * any kind.
 * 
 * @see IpsObjectPartContainer
 * @see BaseIpsObject
 * @see BaseIpsObjectPart
 *  
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartCollection {

    private IpsObjectPartContainer parent;
    private String xmlTag;
    private Class partsBaseClass;
    private Constructor constructor;
    
    private List parts = new ArrayList();
    
    public IpsObjectPartCollection(BaseIpsObject ipsObject, Class partsClazz, String xmlTag) {
        this(partsClazz, xmlTag);
        ArgumentCheck.notNull(ipsObject);
        this.parent = ipsObject;
        ipsObject.addPartCollection(this);
    }
    
    public IpsObjectPartCollection(BaseIpsObjectPart ipsObjectPart, Class partsClazz, String xmlTag) {
        this(partsClazz, xmlTag);
        ArgumentCheck.notNull(ipsObjectPart);
        this.parent = ipsObjectPart;
        ipsObjectPart.addPartCollection(this);
    }
    
    private IpsObjectPartCollection(Class partsClazz, String xmlTag) {
        ArgumentCheck.notNull(partsClazz);
        ArgumentCheck.notNull(xmlTag);
        this.partsBaseClass = partsClazz;
        this.xmlTag = xmlTag;
        constructor = getConstructor();
    }
    
    private Constructor getConstructor() {
        Constructor[] constructors = partsBaseClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Class[] params = constructors[i].getParameterTypes();
            if (params.length!=2) {
                continue;
            }
            if (params[1].equals(Integer.TYPE)) {
                if (IIpsObjectPartContainer.class.isAssignableFrom(params[0])) {
                    return constructors[i];
                }
                if (IIpsObject.class.isAssignableFrom(params[0])) {
                    return constructors[i];
                }
            }
        }
        throw new RuntimeException("Part class hasn't got an appropriate constructor.");
    }
    
    public void clear() {
        parts.clear();
    }
    
    public int size() {
        return parts.size();
    }
    
    public IIpsObjectPart getPart(int index) {
        return (IIpsObjectPart)parts.get(index);
    }
    
    public Iterator iterator() {
        return parts.iterator();
    }
    
    public Object[] toArray(Object[] emptyArray) {
        return parts.toArray(emptyArray);
    }
    
    public IIpsObjectPart[] getParts() {
        return (IIpsObjectPart[])parts.toArray(new IIpsObjectPart[parts.size()]);
    }
    
    
    /**
     * Returns the part with the given name contained in this collection.
     * If more than one part with the name exist, the first part with the name is returned.
     * Returns <code>null</code> if no part with the given name exists or name is <code>null</code>.
     */
    public IIpsObjectPart getPartByName(String name) {
        if (name==null) {
            return null;
        }
        for (Iterator it=parts.iterator(); it.hasNext(); ) {
            IIpsObjectPart part = (IIpsObjectPart)it.next();
            if (name.equals(part.getName())) {
                return part;
            }
        }
        return null;
        
    }
    /**
     * {@inheritDoc}
     */
    public IpsObjectPart newPart() {
        IpsObjectPart newPart = newPartInternal(parent.getNextPartId());
        parent.partWasAdded(newPart);
        return newPart;
    }

    public IpsObjectPart newPart(Element el, int id) {
        if (xmlTag.equals(el.getNodeName())) {
            return newPartInternal(id);
        }
        return null;
    }

    public IpsObjectPart newPart(Class clazz) {
        if (this.partsBaseClass.isAssignableFrom(clazz)) {
            return newPart();
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the part was contained in this collection (before this call)
     * and was removed. Returns <code>false</code> otherwise.
     */
    public boolean readdPart(IIpsObjectPart part) {
        if (this.partsBaseClass.isAssignableFrom(part.getClass())) {
            parts.add(part);
            return true;
        }
        return false;
    }

    public boolean removePart(IIpsObjectPart part) {
        return parts.remove(part);
    }

    /**
     * Creates a new part without updating the src file.
     * Subclasses have to instantiate a new object of the concrete
     * subclass of IpsObjectPart.
     */
    private IpsObjectPart newPartInternal(int id) {
        try {
            IpsObjectPart newPart = (IpsObjectPart)constructor.newInstance(new Object[]{parent, new Integer(id)});
            parts.add(newPart);
            return newPart;
        } catch (Exception e) {
            throw new RuntimeException("Error creating new instance via constructor " + constructor);
        }
    }
    
    public int[] moveParts(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(parts);
        int[] newIndexes = mover.move(indexes, up);
        parent.partsMoved(getParts());
        return newIndexes;
    }

    public String toString() {
        return "Part collection for " + partsBaseClass.getName();
    }
}
