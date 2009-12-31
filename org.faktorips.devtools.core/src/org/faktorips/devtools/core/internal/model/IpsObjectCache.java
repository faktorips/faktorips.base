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

package org.faktorips.devtools.core.internal.model;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Cache for ips objects. Uses a HashMap to cache objects. The objects will be releases by the
 * garbage colector via soft references.
 * 
 * @author Joerg Ortmann
 */
@SuppressWarnings("unchecked")
// This class isn't used at all, we might delete it, so no need for warnings
public class IpsObjectCache {
    // the number of hard references to hold internally,
    // these last recent used entries are not removed by the garbage collector
    private final int HARD_REFERENCE_SIZE;

    // the internal hash that holds all SoftReferences
    private Map hash;
    // ReferenceQueue used to clear SoftReferences
    private final ReferenceQueue queue = new ReferenceQueue();

    // the list of hard references, order of last access
    private final LinkedList hardCache = new LinkedList();

    /*
     * Inner subclass of SoftReference which contains additional the key to make it easier to find
     * the entry in the map, after it has been garbage collected
     */
    private static class SoftValue extends SoftReference {
        private final Object key;

        private SoftValue(Object referent, Object key, ReferenceQueue q) {
            super(referent, q);
            this.key = key;
        }
    }

    /**
     * Created a new SoftReferencesCache with the given initial size.
     */
    public IpsObjectCache(int initialSize) {
        hash = new HashMap(initialSize);
        HARD_REFERENCE_SIZE = 0;
    }

    /**
     * Creates a new SoftReferencesCache with the given initial size and hard reference size. The
     * hard reference size ensures that the last recent entries are not removed by the garbage
     * collector, thus all other entries in the cache will be removed first.<br>
     * If the hard refence size is 0 then no hard references are stored, and therefore all entries
     * could be removed by the garbage collector in undefined order. If the size is -1 then the
     * garbage collector never removes entries from the cache. If the size is greater than 0 then
     * this size of entries will be stored as hard reference (order of last access) and never
     * deleted by the garbage collector
     */
    public IpsObjectCache(int initialSize, int hardReferenceSize) {
        hash = new HashMap(initialSize);
        HARD_REFERENCE_SIZE = hardReferenceSize;
    }

    /*
     * Go through the ReferenceQueue and remove garbage collected SoftValue objects.
     */
    private void processQueue() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.processQueue - start"); //$NON-NLS-1$
        }
        SoftValue sv;
        while ((sv = (SoftValue)queue.poll()) != null) {
            remove(sv.key);
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.processQueue - finish"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.clear - start"); //$NON-NLS-1$
        }
        hardCache.clear();
        processQueue();
        hash.clear();
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.clear - finished"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject(Object key) {
        Object result = null;
        Reference ref = (Reference)hash.get(key);
        if (ref != null) {
            // check if the value from the soft reference isn't null
            result = ref.get();
            if (result == null) {
                // the value has been garbage collected, remove the entry from the map
                hash.remove(key);
            } else {
                if (HARD_REFERENCE_SIZE != 0) {
                    hardCache.addFirst(result);
                    if (HARD_REFERENCE_SIZE > 0 && hardCache.size() > HARD_REFERENCE_SIZE) {
                        hardCache.removeLast();
                    }
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void put(Object key, Object value) {
        processQueue();
        hash.put(key, new SoftValue(value, key, queue));
    }

    /**
     * Removes the object indentified by the given key from the cache.
     * 
     * @return previous object associated with specified key, or <tt>null</tt> if there was no
     *         object for key.
     */
    public Object remove(Object key) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.remove " + key); //$NON-NLS-1$
        }
        return hash.remove(key);
    }
}
