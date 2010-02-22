/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        IDependency[] dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, dependency.length);
        assertEquals(IpsObjectDependency.createSubtypeDependency(typeB.getQualifiedNameType(), typeA
                .getQualifiedNameType()), dependency[0]);

        // delete the dependency
        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, dependency.length);

        // recreate the dependeny
        typeB.setSupertype(typeA.getQualifiedName());
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, dependency.length);
        assertEquals(IpsObjectDependency.createSubtypeDependency(typeB.getQualifiedNameType(), typeA
                .getQualifiedNameType()), dependency[0]);

        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, dependency.length);
    }

}
