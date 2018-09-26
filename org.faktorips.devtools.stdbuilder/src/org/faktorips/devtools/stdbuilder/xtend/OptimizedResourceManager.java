/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.xtend.expression.Resource;
import org.eclipse.xtend.expression.ResourceManagerDefaultImpl;

public class OptimizedResourceManager extends ResourceManagerDefaultImpl {

    private Set<String> invalidNames = new HashSet<String>();

    private Map<String, Resource> resourceByQName = new ConcurrentHashMap<String, Resource>(64, 0.75f, 1);

    @Override
    public Resource loadResource(String fullyQualifiedName, String extension) {
        if (invalidNames.contains(fullyQualifiedName)) {
            return null;
        }
        Resource resource = resourceByQName.get(fullyQualifiedName);
        if (resource != null) {
            return resource;
        }
        synchronized (this) {
            // double check
            resource = resourceByQName.get(fullyQualifiedName);
            if (resource != null) {
                return resource;
            }
            resource = super.loadResource(fullyQualifiedName, extension);
            if (resource == null) {
                invalidNames.add(fullyQualifiedName);
                return null;
            }
            resourceByQName.put(fullyQualifiedName, resource);
            return resource;
        }
    }

}
