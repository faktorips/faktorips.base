/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.junit.Test;

public class DatatypeHelperFactoryDefinitionTest {

    @Test
    public void testDatatypeDefinition() throws CoreRuntimeException {
        DatatypeHelperFactory factory = mock(DatatypeHelperFactory.class);
        Datatype datatype = mock(Datatype.class);
        IExtension extension = mock(IExtension.class);
        IConfigurationElement configElement = mock(IConfigurationElement.class);

        when(configElement.getName()).thenReturn(DatatypeHelperFactoryDefinition.FACTORY_DEFINTIION);
        when(configElement.createExecutableExtension(DatatypeHelperFactoryDefinition.DATATYPE_CLASS)).thenReturn(
                datatype);
        when(configElement.createExecutableExtension(DatatypeHelperFactoryDefinition.FACTORY_CLASS))
                .thenReturn(factory);

        DatatypeHelperFactoryDefinition definition = new DatatypeHelperFactoryDefinition(extension, configElement);
        assertThat(definition.getDatatype(), is(datatype));
        assertThat(definition.getFactory(), is(factory));
    }

    @Test(expected = IllegalStateException.class)
    public void testDatatypeDefinition_WrongConfigElement() {
        IExtension extension = mock(IExtension.class);
        IConfigurationElement configElement = mock(IConfigurationElement.class);

        when(configElement.getName()).thenReturn("That's Not My Name");

        new DatatypeHelperFactoryDefinition(extension, configElement);
    }

}
