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
    
    void putSrcFileContents(IIpsSrcFile file, IpsSourceFileContents newContent) {
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
