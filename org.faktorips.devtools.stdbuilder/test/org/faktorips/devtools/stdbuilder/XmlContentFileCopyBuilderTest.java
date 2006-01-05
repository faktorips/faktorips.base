package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class XmlContentFileCopyBuilderTest extends PluginTest {

    private IIpsProject project;
    private ITableStructure structure;
    private ITableContents contents;
    private IFolder destination;
    private String filePath;
    
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE,
            "TestTable");
        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        contents.setTableStructure(structure.getQualifiedName());
        String packageString = getPackageStructure().getPackage(
            StandardBuilderSet.KIND_TABLE_CONTENT, contents.getIpsSrcFile());
        String packagePath = packageString.replace('.', '/');
        filePath = packagePath + "/TestTable.xml";
        destination = contents.getIpsPackageFragment().getRoot().getArtefactDestination();
    }

    private IFile getContentsFile(){
        return destination.getFile(new Path(filePath));
    }
    
    public void testBuild() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
    }

    private IJavaPackageStructure getPackageStructure() throws CoreException {
        return (IJavaPackageStructure)project.getCurrentArtefactBuilderSet();
    }

    public void testDelete() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        contents.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(getContentsFile().exists());
    }
}
