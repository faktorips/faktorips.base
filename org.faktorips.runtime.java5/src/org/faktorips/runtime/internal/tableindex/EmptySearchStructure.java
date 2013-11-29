/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.tableindex;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A {@link SearchStructure} containing no values. Represents a "nothing found"-result of a search.
 */
class EmptySearchStructure<R> extends SearchStructure<R> {

    @Override
    public SearchStructure<R> get(Object key) {
        return this;
    }

    @Override
    public Set<R> get() {
        return Collections.emptySet();
    }

    @Override
    public R getUnique() {
        throw new NoSuchElementException();
    }

    @Override
    public R getUnique(R defaultValue) {
        return defaultValue;
    }
}