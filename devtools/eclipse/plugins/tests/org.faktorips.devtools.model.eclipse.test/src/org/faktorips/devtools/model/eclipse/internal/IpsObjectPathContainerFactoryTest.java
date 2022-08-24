/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal;

import static org.junit.Assert.assertNotNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainerType;
import org.faktorips.devtools.model.internal.IpsObjectPathContainerFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class IpsObjectPathContainerFactoryTest extends AbstractIpsPluginTest {

    private IpsObjectPathContainerFactory factory = new IpsObjectPathContainerFactory();

    @Category(EclipseImplementation.class)
    @Test
    public void testGetContainerType_createNewFactoryBasedOnExtensions() {
        if (Abstractions.isEclipseRunning()) {
            factory = IpsObjectPathContainerFactory.newFactoryBasedOnExtensions();

            // this is a dependency to the fact, that the container for JDT containers is defined in
            // plugin.xml, (but better this test then no test)
            assertNotNull(factory.getContainerType(IpsContainer4JdtClasspathContainerType.ID));
        }
    }

}
