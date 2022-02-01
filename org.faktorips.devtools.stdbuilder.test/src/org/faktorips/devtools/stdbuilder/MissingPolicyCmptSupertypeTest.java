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
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Test;

public class MissingPolicyCmptSupertypeTest extends AbstractStdBuilderTest {

    @Test
    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(project, "Type1");
        type.setSupertype("MissingSupertype");

        type.getIpsSrcFile().save(true, null);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
