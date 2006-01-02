package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.XmlTestCase;


/**
 * Abstract test case for ips objects and parts.
 */
public abstract class IpsObjectTestCase extends XmlTestCase {
    
    protected IpsSrcFile pdSrcFile;

    public IpsObjectTestCase() {
        super();
    }

    public IpsObjectTestCase(String name) {
        super(name);
    }
    
    protected void setUp(IpsObjectType type) throws Exception {
        pdSrcFile = new IpsSrcFile(null, type.getFileName("Test"));
        IpsPlugin.getDefault().getManager().putSrcFileContents(pdSrcFile, new IpsSourceFileContents(pdSrcFile, "", "UTF-8"));
        createObjectAndPart();
        pdSrcFile.markAsClean();
    }
    
    protected abstract void createObjectAndPart();

}
