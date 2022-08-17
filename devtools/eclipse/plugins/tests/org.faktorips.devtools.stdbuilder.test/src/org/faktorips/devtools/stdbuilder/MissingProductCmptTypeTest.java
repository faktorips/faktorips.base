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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingProductCmptTypeTest extends AbstractStdBuilderTest {

    public MissingProductCmptTypeTest() {
        super();
    }

    /**
     * Tests if the build finishes without throwing an exception if the product component type name
     * is missing
     */
    @Test
    public void test() throws Exception {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(project, "Policy");
        type.setConfigurableByProductCmptType(true);
        type.setProductCmptType("Product"); // missing product component type!

        type.getIpsSrcFile().save(null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
