/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.jdtcontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsContainer4JdtClasspathContainerTypeTest {

    @Test
    public void testNewContainer() {
        IpsContainer4JdtClasspathContainerType type = new IpsContainer4JdtClasspathContainerType();
        IIpsProject ipsProject = mock(IpsProject.class);

        IIpsObjectPathContainer container = type.newContainer(ipsProject, "path");

        assertEquals(IpsContainer4JdtClasspathContainerType.ID, container.getContainerId());
        assertEquals(new Path("path"), container.getOptionalPath());
        assertSame(ipsProject, container.getIpsProject());
    }
}
