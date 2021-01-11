/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Before;

public abstract class PersistenceIpsTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;
    protected PolicyCmptType policyCmptType;

    public PersistenceIpsTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newPersistentIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "Policy1");
    }

    public IIpsProject newPersistentIpsProject() throws CoreException {
        ipsProject = newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPersistenceSupport(true);
        ipsProject.setProperties(props);
        return ipsProject;
    }

}
