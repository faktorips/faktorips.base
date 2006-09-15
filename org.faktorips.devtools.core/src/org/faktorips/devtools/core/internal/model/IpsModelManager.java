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
    
    public IpsModelManager() {
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("ModelManager created.");
    	}
    }
    public IIpsModel getModel() {
        return model;
    }
    
    IpsSourceFileContents getSrcFileContents(IIpsSrcFile file) {
    	IpsSourceFileContents contents = getSrcFileContentsInternal(file);
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("ModelCache.get() for " + file + " returns " + contents);
    	}
        return contents;
    }
    
    IpsSourceFileContents getSrcFileContentsInternal(IIpsSrcFile file) {
        return (IpsSourceFileContents)cache.get(file);
    }
    
    public void putSrcFileContents(IIpsSrcFile file, String newContent, String encoding) {
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("ModelCache.put() for " + file);
    	}
    	IpsSourceFileContents contents = getSrcFileContentsInternal(file);
    	if (contents==null) {
    		contents = new IpsSourceFileContents(file, newContent, encoding);
    		cache.put(file, contents);
    	} else {
    		contents.setSourceTextInternal(newContent);
    	}
    }
    
    /**
     * Flushes the cache.
     */
    public void flushCache() {
        cache.clear();
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("ModelCached flushed.");
    	}
    }

	/**
	 * @param srcFile
	 */
	public void removeSrcFileContents(IIpsSrcFile srcFile) {
    	if (IpsModel.TRACE_MODEL_MANAGEMENT) {
    		System.out.println("ModelCache.remove() for " + srcFile);
    	}
		cache.remove(srcFile);
	}
}
