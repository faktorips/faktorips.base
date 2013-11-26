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

package org.faktorips.runtime.internal.indexstructure;

import java.util.TreeMap;

public class TreeStructure<K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> extends
        AbstractMapStructure<K, V, R> {

    public TreeStructure() {
        super(new TreeMap<K, V>());
    }

    public static <K extends Comparable<K>, V extends Structure<R> & Mergeable<? super V>, R> TreeStructure<K, V, R> create() {
        return new TreeStructure<K, V, R>();
    }

    @Override
    public Structure<R> get(Object key) {
        V result = getMap().get(key);
        // TODO tree traversal
        if (result == null) {
            return ResultStructure.create();
        }
        return result;
    }

}