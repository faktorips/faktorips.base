package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.TocEntry;

public class TableXmlFileBuilderTest extends PluginTest {

    private IIpsProject ipsProject;
    private ITableStructure tableStructure;

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        tableStructure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
            "RateTable");
        tableStructure.getIpsSrcFile().save(true, null);

        ITableContents contents = (ITableContents)newIpsObject(ipsProject,
            IpsObjectType.TABLE_CONTENTS, tableStructure.getQualifiedName());
        contents.setTableStructure(tableStructure.getQualifiedName());
        contents.getIpsSrcFile().save(true, null);
    }

    private TableImplBuilder getTableImplBuilder(IIpsArtefactBuilderSet artefactBuilderSet) {
        IIpsArtefactBuilder[] builders = artefactBuilderSet.getArtefactBuilders();
        for (int i = 0; i < builders.length; i++) {
            if (builders[i] instanceof TableImplBuilder) {
                return (TableImplBuilder)builders[i];
            }
        }
        return null;
    }

    /**
     * Tests if the product component registry's toc file is written.
     */
    public void testTocFile() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        IFile tocFile = entry.getIpsPackageFragmentRoot(ipsProject).getTocFileInOutputFolder();
        assertTrue(tocFile.exists());

        IIpsArtefactBuilderSet builderSet = ipsProject.getCurrentArtefactBuilderSet();
        String tableQualifiedName = getTableImplBuilder(builderSet).getQualifiedClassName(
            tableStructure.getIpsSrcFile());

        TocEntry tocEntry = entry.getIpsPackageFragmentRoot(ipsProject)
                .getRuntimeRepositoryToc().getTableTocEntry(tableQualifiedName);
        assertNotNull(tocEntry);

        assertTrue(tocEntry.isTableTocEntry());
        assertEquals(tableStructure.getQualifiedName(), tocEntry.getIpsObjectName());
    }

}