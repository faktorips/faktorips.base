package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.LocalizedStringsSet;

public class JavaSourceFileBuilderTest extends IpsPluginTest {

    private DumyJavaSourceFileBuilder builder;
    private IIpsProject project;
    private IIpsSrcFile ipsSrcFile;

    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ipsSrcFile = newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy").getIpsSrcFile();
        builder = new DumyJavaSourceFileBuilder(new DumyPackageStructure(), "dumy",
                  new LocalizedStringsSet(JavaSourceFileBuilderTest.class));
    }

    public void testBeforeBuild() throws Exception {

        builder.beforeBuild(ipsSrcFile, null);
        assertEquals(ipsSrcFile, builder.getIpsSrcFile());
    }

    public void testAfterBuild() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.afterBuild(ipsSrcFile);
        assertNull(builder.getIpsObject());
    }

    public void testBuild() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        assertTrue(builder.generateCalled);

        builder.reset();
        builder.build(ipsSrcFile);
        assertFalse(builder.generateCalled);
        
        //check file creation
        IFile file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination().getFile("TestPolicy.java");
        assertTrue(file.exists());
        
        //this checks if the merge.xml has been found since it will try to merge the content because
        //the java file exists already
        builder.setMergeEnabled(true);
        builder.reset();
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
    }

    public void testDelete() throws Exception{
        builder.beforeBuild(ipsSrcFile, null);
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        //check file creation
        IFile file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination().getFile("TestPolicy.java");
        assertTrue(file.exists());
        
        //check file deletion
        builder.delete(ipsSrcFile);
        file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination().getFile("TestPolicy.java");
        assertFalse(file.exists());
    }
    
    public void testGetLocalizedText() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        String value = builder.getLocalizedText(ipsSrcFile, "key");
        assertNotNull(value);
        builder.afterBuild(ipsSrcFile);
    }
}
