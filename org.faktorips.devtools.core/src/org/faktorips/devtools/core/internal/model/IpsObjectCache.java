/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectCache {

    private HashMap objects = new HashMap(1000);
    
    /**
     * 
     */
    public IpsObjectCache() {
        super();
    }
    
    /**
     * Returns the object cached for the given file and the file's corresponding resource
     * modification stamp. Returns <code>null</code> if the cache does not contain an ips object
     * for the given file and it's modification stamp.
     */
    public IpsObject get(IpsSrcFile file) {
        CachedObject co = (CachedObject)objects.get(file);
        if (co==null) {
            return null;
        }
        if (co.modStamp==file.getCorrespondingResource().getModificationStamp()) {
            return co.object;
        }
        return null;
    }
    
    public void put(IpsSrcFile file, IpsObject object) {
        objects.put(file, new CachedObject(object, file.getCorrespondingResource().getModificationStamp()));
    }

    class CachedObject {
        private IpsObject object;
        private long modStamp;
        
        CachedObject(IpsObject o, long modStamp) {
            this.object = o;
            this.modStamp = modStamp;
        }
    }
}
