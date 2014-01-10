/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.xtend.expression.Resource;
import org.eclipse.xtend.expression.ResourceManagerDefaultImpl;

public class OptimizedResourceManager extends ResourceManagerDefaultImpl {

    private Set<String> invalidNames = new HashSet<String>();

    private Map<String, Resource> resourceByQName = new ConcurrentHashMap<String, Resource>();

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
