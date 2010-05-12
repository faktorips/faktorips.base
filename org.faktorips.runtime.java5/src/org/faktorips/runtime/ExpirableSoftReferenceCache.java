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

public class ExpirableSoftReferenceCache<T> extends SoftReferenceCache<T> {

    private final IModificationChecker modificationChecker;

    private long timestamp;

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
