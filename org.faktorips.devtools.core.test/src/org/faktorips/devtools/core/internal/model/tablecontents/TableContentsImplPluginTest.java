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

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.List;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.util.CollectionUtil;

public class TableContentsImplPluginTest extends AbstractIpsPluginTest {

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
