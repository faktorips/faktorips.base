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

package org.faktorips.runtime;

/**
 * This is a special kind of a {@link SoftReferenceCache}. It uses all the functionality of the
 * SoftReferenceCache with the addition of a time stamp. With every {@link #getObject(Object)} call
 * this cache asks the {@link IModificationChecker} whether the time stamp is expired or not. If
 * time stamp is expired the cache is cleared and the time stamp is set to the new modification
 * stamp of the {@link IModificationChecker}
 * 
 * @author dirmeier
 */
public class ExpirableSoftReferenceCache<T> extends SoftReferenceCache<T> {

    private final IModificationChecker modificationChecker;

    private long timestamp;

    /**
     * This constructor needs the modification checker and the initial size of this cache
     * 
     */
    public ExpirableSoftReferenceCache(IModificationChecker modificationChecker, int initialSize) {
        super(initialSize);
        this.modificationChecker = modificationChecker;
    }

    @Override
    public T getObject(Object key) {
        if (modificationChecker.isExpired(timestamp)) {
            clear();
            timestamp = modificationChecker.getModificationStamp();
        }
        return super.getObject(key);
    }

}
