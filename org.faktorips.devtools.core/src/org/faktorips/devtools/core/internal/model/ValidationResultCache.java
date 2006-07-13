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

import java.util.HashMap;

import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.util.message.MessageList;

/**
 * A cache for the results of the validation.
 * 
 * @author Jan Ortmann
 */
public class ValidationResultCache {

	private HashMap data = new HashMap(1000);
	
	public ValidationResultCache() {
		super();
	}
	
	/**
	 * Puts a copy of the given the validation result for the given ips object part container 
	 * into the chache. Overwrittes any old data for the given container. If result is <code>null</code>,
	 * any cached data for the container is removed. 
	 * 
	 * @param container  The container to that the result belongs
	 * @param result	 The validation result to put into the cache.
	 * 
	 * @throws NullPointerException if container is <code>null</code>.
	 */
	public void putResult(IIpsObjectPartContainer container, MessageList result) {
		if (result==null) {
			data.remove(container);
			return;
		}
		// cache a defensive copy
		MessageList copy = new MessageList(); 
		copy.add(result);
		data.put(container, copy);
	}

	/**
	 * Returns the cached validation result for the given container or <code>null</code>
	 * if the cache does not contain a result for the container.
	 */
	public MessageList getResult(IIpsObjectPartContainer c) {
		MessageList cached = (MessageList )data.get(c);
		if (cached==null) {
			return null;
		}
		// return a defensive copy
		MessageList result = new MessageList(); 
		result.add(result);
		return result;
	}
	
	/**
	 * Removes the data from the cache that is stale because the given container
	 * has changed. Does nothing if the given container is <code>null</code>.
	 * <p>
	 * Implementation note: At the moment we clear the whole cache if an object part
	 * container changes as due to the dependencies between objects other the validation 
	 * result of other objects can also change if one object is changed. We might use
	 * the exact dependencies between objects to solve this more efficiently.  
	 */
	public void removeStaleData(IIpsObjectPartContainer changedContainer) {
		data = new HashMap(1000);
	}

}
