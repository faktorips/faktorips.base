/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;

public class DependencyGraphPersistenceManagerTest extends AbstractIpsPluginTest {

    public final void testGetDependencyGraph() throws Exception {

        IIpsProject ipsProject = newIpsProject("Testproject");
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IAssociation bToA = b.newAssociation();
        bToA.setTarget(a.getQualifiedName());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        DependencyGraphPersistenceManager persistenceManager = IpsPlugin.getDefault()
                .getDependencyGraphPersistenceManager();
        assertNull(persistenceManager.getDependencyGraph(ipsProject));
        persistenceManager.saving(new TestSaveContext());
        DependencyGraph graph = persistenceManager.getDependencyGraph(ipsProject);
        assertNotNull(graph);
        IDependency[] dependencies = graph.getDependants(a.getQualifiedNameType());
        assertEquals(1, dependencies.length);
        assertEquals(b.getQualifiedNameType(), dependencies[0].getSource());
    }

    private static class TestSaveContext implements ISaveContext {

        @Override
        public IPath[] getFiles() {
            return null;
        }

        @Override
        public int getKind() {
            return ISaveContext.FULL_SAVE;
        }

        @Override
        public int getPreviousSaveNumber() {
            return 0;
        }

        @Override
        public IProject getProject() {
            return null;
        }

        @Override
        public int getSaveNumber() {
            return 0;
        }

        @Override
        public IPath lookup(IPath file) {
            return null;
        }

        @Override
        public void map(IPath file, IPath location) {
        }

        @Override
        public void needDelta() {
        }

        @Override
        public void needSaveNumber() {
        }

    }
}
