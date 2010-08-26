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

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.message.MessageList;

/**
 * A cache for the results of the validation.
 * 
 * @author Jan Ortmann
 */
public class ValidationResultCache {

    private HashMap<IIpsObjectPartContainer, MessageList> data = new HashMap<IIpsObjectPartContainer, MessageList>(1000);

    public ValidationResultCache() {
        super();
    }

    /**
     * Puts a copy of the given the validation result for the given IPS object part container into
     * the cache. Overwrites any old data for the given container. If result is <code>null</code>,
     * any cached data for the container is removed.
     * 
     * @param container The container to that the result belongs
     * @param result The validation result to put into the cache.
     * 
     * @throws NullPointerException if container is <code>null</code>.
     */
    synchronized public void putResult(IIpsObjectPartContainer container, MessageList result) {
        if (result == null) {
            data.remove(container);
            return;
        }
        // Cache a defensive copy.
        MessageList copy = new MessageList();
        copy.add(result);
        data.put(container, copy);
    }

    /**
     * Returns the cached validation result for the given container or <code>null</code> if the
     * cache does not contain a result for the container.
     */
    synchronized public MessageList getResult(IIpsObjectPartContainer c) {
        MessageList cached = data.get(c);
        if (cached == null) {
            return null;
        }
        // Return a defensive copy.
        MessageList result = new MessageList();
        result.add(cached);
        return result;
    }

    /**
     * Removes the data from the cache that is stale because the given IPS source file has changed.
     * Does nothing if the given file is <code>null</code>.
     * <p>
     * Implementation note: At the moment we clear the whole cache if a file changes, as due to the
     * dependencies between objects the validation result of other objects can also change if one
     * object is changed. We might use the exact dependencies between objects to solve this more
     * efficiently in a later version.
     * 
     * @param file The ips source file that has changed.
     */
    synchronized public void removeStaleData(IIpsSrcFile file) {
        data.clear();
    }

    /**
     * Clears the whole cache.
     */
    synchronized public void clear() {
        data.clear();
    }

}
