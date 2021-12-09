/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Test;

/**
 * 
 * 
 * @author Jan Ortmann
 */
public class InvalidPcTypeHierarchyTest extends AbstractStdBuilderTest {

    @Test
    public void test() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type1 = newPolicyCmptType(project, "Type1");
        IPolicyCmptType type2 = newPolicyCmptType(project, "Type2");
        IPolicyCmptType type3 = newPolicyCmptType(project, "Type3");

        type3.setSupertype(type2.getQualifiedName());
        type2.setSupertype(type1.getQualifiedName());
        type1.setSupertype(type3.getQualifiedName());

        type1.getIpsSrcFile().save(true, null);
        type2.getIpsSrcFile().save(true, null);
        type3.getIpsSrcFile().save(true, null);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
