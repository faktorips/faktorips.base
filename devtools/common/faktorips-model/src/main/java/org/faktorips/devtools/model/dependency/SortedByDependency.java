/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.dependency;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.util.MultiMap;

public class SortedByDependency<T extends IIpsObject> {

    private SortedByDependency() {
        // only static access
    }

    public static <T extends IIpsObject> Set<T> sortByInstanceOf(Collection<T> objectsToFix) {
        return new SortedByDependency<T>().sortedFixDifferences(objectsToFix);
    }

    private Set<T> sortedFixDifferences(Collection<T> objectsToFix) {
        MultiMap<T, T> dependencies = getDependencyMap(objectsToFix);

        LinkedHashSet<T> result = new LinkedHashSet<>();

        Set<T> roots = new HashSet<>(dependencies.keySet());
        Collection<T> values = dependencies.values();
        roots.removeAll(values);
        result.addAll(roots);
        addChildren(roots, dependencies, result);
        // for those not yet added, for example because of cyclic dependencies
        result.addAll(dependencies.keySet());

        return result;
    }

    private MultiMap<T, T> getDependencyMap(Collection<T> objectsToFix) {
        MultiMap<T, T> dependencies = MultiMap.createWithLinkedSetAsValues();
        HashMap<QualifiedNameType, T> qNameToSrcFile = map(objectsToFix);
        for (T ipsObject : objectsToFix) {
            dependencies.put(ipsObject);
            IDependency[] dependsOn = ipsObject.dependsOn();
            for (IDependency dependency : dependsOn) {
                if (dependency.getType().equals(DependencyType.INSTANCEOF)) {
                    T dependentSrcFile = qNameToSrcFile.get(dependency.getTarget());
                    if (dependentSrcFile != null) {
                        dependencies.put(dependentSrcFile, ipsObject);
                    }
                    // else not part of the selected objects. Transitive dependency may be missing.
                }
            }
        }
        return dependencies;
    }

    private HashMap<QualifiedNameType, T> map(Collection<T> objectsToFix) {
        HashMap<QualifiedNameType, T> map = new HashMap<>();
        for (T ipsObject : objectsToFix) {
            map.put(ipsObject.getQualifiedNameType(), ipsObject);
        }
        return map;
    }

    private void addChildren(Collection<T> roots, MultiMap<T, T> dependencies, LinkedHashSet<T> result) {
        for (T root : roots) {
            Collection<T> children = dependencies.get(root);
            result.addAll(children);
            addChildren(children, dependencies, result);
        }
    }

}
