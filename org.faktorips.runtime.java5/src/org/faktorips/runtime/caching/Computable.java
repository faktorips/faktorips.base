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

package org.faktorips.runtime.caching;

/**
 * Interface to compute objects of type V identified by a key of type K
 * 
 * @author dirmeier
 */
public interface Computable<K, V> {

    /**
     * Compute an object of type V identified by the key of type K
     * 
     * @param key the key to identify the object
     * @return the computed Object of type V
     * @throws InterruptedException When computation was interrupted
     */
    public V compute(K key) throws InterruptedException;

}
