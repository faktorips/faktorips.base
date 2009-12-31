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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Memory sensitive cache for the contents of ips source files.
 * <p>
 * This cache caches the ips source file contents and, as the ips source file content helds a
 * reference to the ips object stored in the file, also ips objects. The cache is designed to allow
 * the garbage collector to reclaim the cached objects. This is archived by using soft references.
 * <p>
 * The following fact has to be considered in the implementation:
 * <p>
 * IpsSrcFileContent objects are an internal implementation detail of the model. They are not
 * visible to the client (like the UI and the builder) using the published API. In fact the only
 * object holding a reference to them, is this cache. On the other hand, clients work directly with
 * ips objects and thus are allowed to hold (strong) references to them. So this cache can't hold
 * soft references to ips source file contents objects only. What would happen (and actually this
 * has been a bug) is, that the garbage collector removes an ips source file content object held in
 * the cache, while a client holds a reference to the ips object referenced by the content object.
 * The garbage collector is allowed to remove the contents object as this cache is the only object
 * holding a reference to it and this reference is a soft one. So what happens is that the next time
 * another client (not the one already holding a reference to the ips object), the ips source file
 * content is not found in the cache. Thus a new one including an ips object is created and we end
 * up having two instances for the same logical ips object (which is pretty bad).
 * <p>
 * 
 * @see SoftReference
 * 
 * @author Joerg Ortmann
 */
@SuppressWarnings("unchecked")
// This class isn't used at all, we might delete it, so no need for warnings
public class IpsSrcFileContentsCache {

    // the number of hard references to hold internally,
    // these last recent used entries are not removed by the garbage collector
    private final int HARD_REFERENCE_SIZE;

    private Map contentsMap;

    // ReferenceQueue used to clear SoftReferences
    private final ReferenceQueue queue = new ReferenceQueue();

    // the list of hard references, order of last access
    private final LinkedList hardCache = new LinkedList();

    /**
     * Creates a new cache with the given initial size.
     */
    public IpsSrcFileContentsCache(int initialSize) {
        contentsMap = new HashMap(initialSize);
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
    public IpsSrcFileContentsCache(int initialSize, int hardReferenceSize) {
        contentsMap = new HashMap(initialSize);
        HARD_REFERENCE_SIZE = hardReferenceSize;
    }

    public ReferenceQueue getReferenceQueue() {
        return queue;
    }

    /*
     * Go through the ReferenceQueue and remove content for garbage collected ips objects.
     */
    private void processQueue() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.processQueue() - start"); //$NON-NLS-1$
        }
        // to be done
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.processQueue() - finished"); //$NON-NLS-1$
        }
    }

    public void clear() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.clear() - start"); //$NON-NLS-1$
        }
        hardCache.clear();
        processQueue();
        contentsMap.clear();
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.clear() - finished"); //$NON-NLS-1$
        }
    }

    public IpsSrcFileContent getContents(IIpsSrcFile ipsSrcFile) {
        Object result = null;
        IpsSrcFileContent contents = (IpsSrcFileContent)contentsMap.get(ipsSrcFile);
        if (contents != null) {
            if (contents.getIpsObject() == null) {
                // the ips object has been garbage collected, remove the contents from the map
                remove(ipsSrcFile);
                contents = null;
            } else {
                if (HARD_REFERENCE_SIZE != 0) {
                    hardCache.addFirst(result);
                    if (HARD_REFERENCE_SIZE > 0 && hardCache.size() > HARD_REFERENCE_SIZE) {
                        hardCache.removeLast();
                    }
                }
            }
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.get(): IpsSrcFile=" + ipsSrcFile + ", Returned object=" + contents); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return contents;
    }

    /**
     * {@inheritDoc}
     */
    public void putContents(IpsSrcFileContent ipsSrcFileContents) {
        IIpsSrcFile ipsSrcFile = ipsSrcFileContents.getIpsSrcFile();
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.put(): IpsSrcFile=" + ipsSrcFile); //$NON-NLS-1$
        }
        processQueue();
        contentsMap.put(ipsSrcFile, ipsSrcFileContents);
    }

    /**
     * Removes the object indentified by the given key from the cache.
     * 
     * @return previous object associated with specified key, or <tt>null</tt> if there was no
     *         object for key.
     */
    public Object remove(IIpsSrcFile file) {
        System.out.println("remove " + file); //$NON-NLS-1$
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsObjectCache.remove(): IpsSrcFile= " + file); //$NON-NLS-1$
        }
        return contentsMap.remove(file);
    }
}
