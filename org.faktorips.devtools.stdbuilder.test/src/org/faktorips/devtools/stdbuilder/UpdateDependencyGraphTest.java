/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class UpdateDependencyGraphTest extends AbstractIpsPluginTest {

    public UpdateDependencyGraphTest() {
        super();
    }

    /**
     * Tests if the build updates the dependencygraph correctly.
     * 
     * @throws CoreException
     */
    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "A");
        IPolicyCmptType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "B");
        typeB.setSupertype(typeA.getQualifiedName());
        typeB.getIpsSrcFile().save(true, null);
        
        // B depends on A
        fullBuild();
        DependencyGraph graph = ((IpsModel)project.getIpsModel()).getDependencyGraph(project);
        QualifiedNameType[] qnt = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, qnt.length);
        assertEquals(typeB.getQualifiedNameType(), qnt[0]);

        // delete the dependency
        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        qnt = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, qnt.length);
        
        // recreate the dependeny
        typeB.setSupertype(typeA.getQualifiedName());
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        qnt = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, qnt.length);
        assertEquals(typeB.getQualifiedNameType(), qnt[0]);
        
        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        qnt = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, qnt.length);
    }

}
