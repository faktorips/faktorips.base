package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * 
 * @author Peter Erzberger
 */
public class BuilderHelperTest extends PluginTest {

    private IIpsProject ipsProject;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
    }

//    protected void tearDown() throws Exception{
//        ((IProject)ipsProject.getCorrespondingResource()).delete(true, true, null);
//    }
    
    public void testGetFileNameWithoutExtension() throws Exception {
        IIpsObject ipsObject = newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "TableWithNoRange");
        IIpsSrcFile file = ipsObject.getIpsSrcFile();
        assertEquals("TableWithNoRange.ipstablecontents", file.getName());
        String fileNameWithoutExt = BuilderHelper.getFileNameWithoutExtension(file.getCorrespondingFile());
        assertEquals("TableWithNoRange", fileNameWithoutExt);
    }

    //TODO finish test case
    public void testCopyIpsObjectResource() throws Exception {
        IIpsObject ipsObject = newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "TableWithNoRange");
        BuilderHelper.copyIpsObjectResource(ipsObject, new MultiStatus("id", 0, "", new Exception()));
    }

}
