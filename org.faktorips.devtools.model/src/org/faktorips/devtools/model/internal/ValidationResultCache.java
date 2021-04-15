/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.util.HashMap;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.runtime.MessageList;

/**
 * A cache for the results of the validation.
 * 
 * @author Jan Ortmann
 */
public class ValidationResultCache {

    private HashMap<IIpsObjectPartContainer, MessageList> data = new HashMap<>(
            1000);

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
    public synchronized void putResult(IIpsObjectPartContainer container, MessageList result) {
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
    public synchronized MessageList getResult(IIpsObjectPartContainer c) {
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
     * @param file The IPS source file that has changed.
     */
    public synchronized void removeStaleData(IIpsSrcFile file) {
        clear();
    }

    /**
     * Clears the whole cache.
     */
    public synchronized void clear() {
        data.clear();
    }

}
