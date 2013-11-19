/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.model.tablestructure.IIndex;

public class MultiMap {

    private HashMap<IIndex, Set<Row>> internalHashMap = new HashMap<IIndex, Set<Row>>();

    public Set<Row> getSet(IIndex uniqueKey) {
        return internalHashMap.get(uniqueKey);
    }

    public void addInternal(IIndex indexKey, Row row) {
        Set<Row> newSet = new HashSet<Row>();
        if (internalHashMap.containsKey(indexKey)) {
            Set<Row> oldSet = internalHashMap.get(indexKey);
            newSet = oldSet;
            internalHashMap.remove(indexKey);
        }
        newSet.add(row);
        internalHashMap.put(indexKey, newSet);
    }

    public HashMap<IIndex, Set<Row>> getInternalHashMap() {
        return internalHashMap;
    }
}
