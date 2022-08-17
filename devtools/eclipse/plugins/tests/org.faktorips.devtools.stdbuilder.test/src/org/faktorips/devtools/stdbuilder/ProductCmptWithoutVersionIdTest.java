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
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptWithoutVersionIdTest extends AbstractStdBuilderTest {

    public ProductCmptWithoutVersionIdTest() {
        super();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IIpsProjectProperties props = project.getProperties();
        props.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false));
        project.setProperties(props);

        IPolicyCmptType type = newPolicyAndProductCmptType(project, "Policy", "Product");
        IProductCmpt cmpt = newProductCmpt(type.findProductCmptType(project), "Product-A");
        cmpt.getIpsSrcFile().save(null);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
