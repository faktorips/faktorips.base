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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * A collection of ips object parts. This class is used together with BaseIpsObject and
 * BaseIpsObjectPart to ease the development of new ips object subclasses. An ips object part
 * collection is a collection of parts of the same type. E.g. a collection holds only methods or
 * only attributes but not both. As opposed to an IpsObjectPartContainer which is a container for
 * ips object parts of any kind.
 * 
 * @see IpsObjectPartContainer
 * @see BaseIpsObject
 * @see BaseIpsObjectPart
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartCollection<T extends IIpsObjectPart> implements Iterable<T> {

    private IpsObjectPartContainer parent;
    private String xmlTag;
    private Class<? extends T> partsBaseClass;
    // TODO CD das Interface muss von Typ T oder dadrueber sein
    // Beispiel: T ist BusinessFunction --> partsPublishedInterface = IBusinessFunction.class
    // --> ? super T
    private Class<T> partsPublishedInterface;

    private List<T> parts = new ArrayList<T>();

    public IpsObjectPartCollection(BaseIpsObject ipsObject, Class<? extends T> partsClazz, Class<T> publishedInterface,
            String xmlTag) {
        this(partsClazz, publishedInterface, xmlTag);
        ArgumentCheck.notNull(ipsObject);
        this.parent = ipsObject;
        ipsObject.addPartCollection(this);
    }

    public IpsObjectPartCollection(BaseIpsObjectPart ipsObjectPart, Class<? extends T> partsClazz,
            Class<T> publishedInterface, String xmlTag) {
        this(partsClazz, publishedInterface, xmlTag);
        ArgumentCheck.notNull(ipsObjectPart);
        this.parent = ipsObjectPart;
        ipsObjectPart.addPartCollection(this);
    }

    private IpsObjectPartCollection(Class<? extends T> partsClazz, Class<T> publishedInterface, String xmlTag) {
        ArgumentCheck.notNull(partsClazz);
        ArgumentCheck.notNull(publishedInterface);
        ArgumentCheck.notNull(xmlTag);
        this.partsBaseClass = partsClazz;
        this.partsPublishedInterface = publishedInterface;
        this.xmlTag = xmlTag;
    }

    private Constructor<T> getConstructor(Class<? extends T> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length != 2) {
                continue;
            }
            if (params[1].equals(String.class)) {
                if (IIpsObjectPartContainer.class.isAssignableFrom(params[0])) {
                    @SuppressWarnings("unchecked")
                    // neccessary as Class.getDeclaredConstructors() is of type Constructor<?>[]
                    // while returning Contructor<T>[]
                    // The Javaoc Class.getDeclaredConstructors() for more information
                    Constructor<T> castedConstructor = (Constructor<T>)constructor;
                    return castedConstructor;
                }
            }
        }
        throw new RuntimeException(this + ", Part class hasn't got an appropriate constructor."); //$NON-NLS-1$
    }

    public void clear() {
        parts.clear();
    }

    public int size() {
        return parts.size();
    }

    public int indexOf(T part) {
        return parts.indexOf(part);
    }

    public boolean contains(T part) {
        return parts.contains(part);
    }

    public T getPart(int index) {
        return parts.get(index);
    }

    @Override
    public Iterator<T> iterator() {
        return parts.iterator();
    }

    public Object[] toArray(Object[] emptyArray) {
        return parts.toArray(emptyArray);
    }

    public T[] toArray(T[] emptyArray) {
        return parts.toArray(emptyArray);
    }

    /**
     * Returns the underlying list that stores the parts.
     */
    public List<T> getBackingList() {
        return parts;
    }

    public IIpsObjectPart[] getParts() {
        return parts.toArray(new IIpsObjectPart[parts.size()]);
    }

    /**
     * Returns the part with the given name contained in this collection. If more than one part with
     * the name exist, the first part with the name is returned. Returns <code>null</code> if no
     * part with the given name exists or name is <code>null</code>.
     */
    public T getPartByName(String name) {
        if (name == null) {
            return null;
        }
        for (T part : parts) {
            if (name.equals(part.getName())) {
                return part;
            }
        }
        return null;

    }

    /**
     * Returns the part with the given id contained in this collection. Returns <code>null</code> if
     * no part with the given id exists.
     */
    public T getPartById(String id) {
        for (T part : parts) {
            if (id.equals(part.getId())) {
                return part;
            }
        }
        return null;
    }

    /**
     * This method creates a new <code>IpsObjectPart</code> according to the configuration of this
     * object. The provided initializer can set the new <code>IpsObjectPart</code> into a valid
     * state before a <code>ContentChangeEvent.TYPE_PART_ADDED</code> is fired. This method is not
     * part of the published interface. Subclasses that want to provide a factory method for a
     * specific <code>IpsObjectPart</code> can use this method and add their method to the published
     * interface if desired.
     * 
     * @return the new IpsPart
     */
    protected T newPart(IpsObjectPartInitializer<T> initializer) {
        T part = newPartInternal(parent.getNextPartId(), getConstructor(partsBaseClass));
        initializer.initialize(part);
        parent.partWasAdded(part);
        return part;
    }

    public T newPart() {
        T newPart = newPartInternal(parent.getNextPartId(), getConstructor(partsBaseClass));
        parent.partWasAdded(newPart);
        return newPart;
    }

    public T newPart(Element el, String id) {
        if (xmlTag.equals(el.getNodeName())) {
            return newPartInternal(id, getConstructor(partsBaseClass));
        }
        return null;
    }

    /**
     * Creates and returns a new part. The concrete type of the new part depends on the given class.
     * <p>
     * The given class must be a subclass of the class this <tt>IpsObjectPartCollection</tt> is
     * based upon. If that is not the case, <tt>null</tt> will be returned.
     * <p>
     * This operation can be used if you want to create an <tt>IpsObjectPartCollection</tt> of a
     * certain type and you also want to store subclasses of that type in the collection.
     * 
     * @param clazz
     */
    @SuppressWarnings("unchecked")
    public T newPart(Class<?> clazz) {
        if (partsPublishedInterface.isAssignableFrom(clazz)) {
            T newPart = newPartInternal(parent.getNextPartId(), getConstructor((Class<? extends T>)clazz));
            return newPart;
        }
        return null;
    }

    /**
     * The part is added to the collection if the type is assignable form the partsBaseClass. Return
     * true if the part is of correct type or fals if it is not.
     * 
     * @param part
     * @return Return true if the type of the part is supported by this collection
     */
    @SuppressWarnings("unchecked")
    public boolean addPart(IIpsObjectPart part) {
        if (this.partsBaseClass.isAssignableFrom(part.getClass())) {
            parts.add((T)part);
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the part was contained in this collection (before this call) and
     * was removed. Returns <code>false</code> otherwise.
     */
    public boolean removePart(IIpsObjectPart part) {
        return parts.remove(part);
    }

    /**
     * Creates a new part without updating the src file. Subclasses have to instantiate a new object
     * of the concrete subclass of IpsObjectPart.
     */
    private T newPartInternal(String id, Constructor<T> constructor) {
        try {
            T newPart = constructor.newInstance(new Object[] { parent, id });
            parts.add(newPart);
            return newPart;
        } catch (Exception e) {
            throw new RuntimeException(this + ", Error creating new instance via constructor " + constructor, e); //$NON-NLS-1$
        }
    }

    /**
     * Moves the parts indicated by the given indexes one position up or down in their containing
     * list.
     * 
     * @param indexes The indexes that specify the elements to move.
     * @param up Flag indicating whether to move the elements one position up (<code>true</code>) or
     *            down (<code>false</code>).
     * 
     * @return The new indexes of the moved elements.
     */
    public int[] moveParts(int[] indexes, boolean up) {
        ListElementMover<T> mover = new ListElementMover<T>(parts);
        int[] newIndexes = mover.move(indexes, up);
        parent.partsMoved(getParts());
        return newIndexes;
    }

    @Override
    public String toString() {
        return "Part collection for " + partsBaseClass.getName(); //$NON-NLS-1$
    }

    /**
     * An implementations of this interface is required by the newPart(IIpsObjectPartCollection)
     * method. It is supposed to be used for additional initialization of a new IpsObjectPart before
     * its creation and addition to the according IpsObject is communicated to
     * ContentChangeListeners.
     * 
     * @author Peter Erzberger
     */
    public interface IpsObjectPartInitializer<T extends IIpsObjectPart> {

        /**
         * Initializes the provided IpsObjectPart.
         */
        public void initialize(T part);
    }
}
