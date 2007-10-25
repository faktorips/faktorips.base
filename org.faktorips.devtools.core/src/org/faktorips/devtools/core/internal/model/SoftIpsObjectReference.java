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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * A soft reference referencing an ips object. If the ips object is removed by the
 * garbage collector, you can still get the reference to the ips src file from a reference. 
 * 
 * @author Jan Ortmann
 */
public class SoftIpsObjectReference extends SoftReference {

    private IIpsSrcFile ipsSrcFile;
    
    public SoftIpsObjectReference(IpsObject ipsObject, ReferenceQueue q) {
        super(ipsObject, q);
        this.ipsSrcFile = ipsObject.getIpsSrcFile();
    }

    public SoftIpsObjectReference(IpsObject ipsObject) {
        super(ipsObject);
        this.ipsSrcFile = ipsObject.getIpsSrcFile();
    }

    /**
     * Returns the ips object referenced by this reference. Returns <code>null</code> if the garbage collector
     * has removed the ips object. Typesafe version of {@link #get()}.
     */
    public IpsObject getIpsObject() {
        return (IpsObject)get();
    }
    
    /**
     * Returns the ips source file the ips object is stored in. The ips source file is returned even when the
     * garbage collector has removed the ips object.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }
    
}
