/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFile;


/**
 *
 */
public class IpsModelManager {
    
    private IIpsModel model = new IpsModel();
    
    private Map cache = new HashMap(1000);
    
    public IIpsModel getModel() {
        return model;
    }
    
    IpsSourceFileContents getSrcFileContents(IIpsSrcFile file) {
        return (IpsSourceFileContents)cache.get(file);
    }
    
    public void putSrcFileContents(IIpsSrcFile file, IpsSourceFileContents newContent) {
        cache.put(file, newContent);
    }
    
    void removeSrcFileContents(IIpsSrcFile file) {
        cache.remove(file);
    }
    
    /**
     * Flushes the cache.
     */
    public void flushCache() {
        cache.clear();
    }
}
