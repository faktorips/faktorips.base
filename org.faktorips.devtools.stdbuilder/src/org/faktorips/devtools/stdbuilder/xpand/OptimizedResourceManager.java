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
