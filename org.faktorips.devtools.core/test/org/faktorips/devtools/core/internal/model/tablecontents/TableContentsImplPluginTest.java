package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.List;

import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.util.CollectionUtil;

public class TableContentsImplPluginTest extends PluginTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'org.faktorips.plugin.internal.model.tablecontents.TableContentsImpl.dependsOn()'
     */
    public void testDependsOn() throws Exception {
        IIpsProject project = newIpsProject("TestProject");
        ITableStructure structure = (ITableStructure)newIpsObject(project,  IpsObjectType.TABLE_STRUCTURE, "Ts");
        ITableContents contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Tc");
        QualifiedNameType[] dependsOn = contents.dependsOn();
        assertEquals(0, dependsOn.length);
        
        contents.setTableStructure(structure.getQualifiedName());
        List dependsOnAsList = CollectionUtil.toArrayList(contents.dependsOn());
        assertTrue(dependsOnAsList.contains(structure.getQualifiedNameType()));

    }

}
