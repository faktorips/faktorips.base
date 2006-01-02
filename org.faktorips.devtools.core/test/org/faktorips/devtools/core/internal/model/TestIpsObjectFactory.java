package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;


/**
 *
 */
public class TestIpsObjectFactory {

    public final static IIpsObject createPdObject(IpsObjectType type, String name) throws CoreException {
        IIpsSrcFile file = new IpsSrcFile(null, type.getFileName(name));
        IpsPlugin.getDefault().getManager().putSrcFileContents(file, new IpsSourceFileContents(file, "", "UTF-8"));
        file.markAsClean();
        return type.newObject(file); 
    }
    
    /**
     * 
     */
    private TestIpsObjectFactory() {
    }
    

}
