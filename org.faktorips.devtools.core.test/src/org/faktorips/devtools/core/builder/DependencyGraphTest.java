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

package org.faktorips.devtools.core.builder;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.util.CollectionUtil;

public class DependencyGraphTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;
    private DependencyGraph graph;
    private IPolicyCmptType a;
    private IPolicyCmptType b;
    private IPolicyCmptType c;
    private IPolicyCmptType d;
    
    
    public void setUp() throws Exception{
        System.out.println("Start setUp()");
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        a = newPolicyCmptType(root, "A");
        b = newPolicyCmptType(root, "B");
        c = newPolicyCmptType(root, "C");
        d = newPolicyCmptType(root, "D");
        a.newRelation().setTarget(d.getQualifiedName());
        c.setSupertype(a.getQualifiedName());
        c.newRelation().setTarget(b.getQualifiedName());
        
        a.getIpsSrcFile().save(true, null);
        c.getIpsSrcFile().save(true, null);
        graph = new DependencyGraph(ipsProject);
        System.out.println("Finished setUp()");
    }
    
    /*
     * Test method for 'org.faktorips.plugin.builder.DependencyGraph.getDependants(String)'
     */
    public void testGetDependants() throws CoreException {
        System.out.println("Start testGetDependants()");

        QualifiedNameType[] dependants = graph.getDependants(a.getQualifiedNameType());
        List dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(c.getQualifiedNameType()));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(c.getQualifiedNameType()));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(a.getQualifiedNameType()));
        assertEquals(1, dependants.length);

        System.out.println("Finished testGetDependants()");
    }

    /*
     * Test method for 'org.faktorips.plugin.builder.DependencyGraph.update(String)'
     */
    public void testUpdate() throws Exception {
        System.out.println("Start testUpdate()");
        
        a.getRelations()[0].delete();
        a.getIpsSrcFile().save(true, null);
        
        QualifiedNameType[] dependants = graph.getDependants(a.getQualifiedNameType());
        //not only the changed IpsObject has to be updated in the dependency graph but also all dependants of it
        graph.update(a.getQualifiedNameType());
        for (int i = 0; i < dependants.length; i++) {
            graph.update(dependants[i]);
        }
        
        List dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(c.getQualifiedNameType()));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(b.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertTrue(dependsOnList.contains(c.getQualifiedNameType()));
        assertEquals(1, dependants.length);

        dependants = graph.getDependants(c.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);

        dependants = graph.getDependants(d.getQualifiedNameType());
        dependsOnList = CollectionUtil.toArrayList(dependants);
        assertEquals(0, dependants.length);
    }

}
