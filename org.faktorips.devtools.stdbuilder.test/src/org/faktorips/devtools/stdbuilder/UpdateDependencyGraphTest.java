/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IDependencyGraph;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class UpdateDependencyGraphTest extends AbstractStdBuilderTest {

    public UpdateDependencyGraphTest() {
        super();
    }

    /**
     * Tests if the build updates the dependency graph correctly.
     */
    @Test
    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "A");
        IPolicyCmptType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "B");
        typeB.setSupertype(typeA.getQualifiedName());
        typeB.getIpsSrcFile().save(true, null);

        // B depends on A
        fullBuild();
        IDependencyGraph graph = project.getDependencyGraph();
        IDependency[] dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, dependency.length);
        assertEquals(
                IpsObjectDependency.createSubtypeDependency(typeB.getQualifiedNameType(), typeA.getQualifiedNameType()),
                dependency[0]);

        // delete the dependency
        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, dependency.length);

        // recreate the dependency
        typeB.setSupertype(typeA.getQualifiedName());
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(1, dependency.length);
        assertEquals(
                IpsObjectDependency.createSubtypeDependency(typeB.getQualifiedNameType(), typeA.getQualifiedNameType()),
                dependency[0]);

        typeB.setSupertype("");
        typeB.getIpsSrcFile().save(true, null);
        incrementalBuild();
        dependency = graph.getDependants(typeA.getQualifiedNameType());
        assertEquals(0, dependency.length);
    }

}
