/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Status;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.junit.Test;

public class DatatypeDefinitionTest {

    @Test
    public void testDatatypeDefinition() throws CoreException {
        DatatypeHelper helper = mock(DatatypeHelper.class);
        Datatype datatype = mock(Datatype.class);
        IExtension extension = mock(IExtension.class);
        IConfigurationElement configElement = mock(IConfigurationElement.class);

        when(configElement.getName()).thenReturn(DatatypeDefinition.DATATYPE_DEFINTIION);
        when(configElement.createExecutableExtension(DatatypeDefinition.DATATYPE_CLASS)).thenReturn(datatype);
        when(configElement.createExecutableExtension(DatatypeDefinition.HELPER_CLASS)).thenReturn(helper);

        DatatypeDefinition definition = new DatatypeDefinition(extension, configElement);
        assertThat(definition.hasDatatype(), is(true));
        assertThat(definition.getDatatype(), is(datatype));
        assertThat(definition.hasHelper(), is(true));
        assertThat(definition.getHelper(), is(helper));
    }

    @Test
    public void testDatatypeDefinition_MissingHelperClass() throws CoreException {
        Datatype datatype = mock(Datatype.class);
        IExtension extension = mock(IExtension.class);
        IConfigurationElement configElement = mock(IConfigurationElement.class);

        when(configElement.getName()).thenReturn(DatatypeDefinition.DATATYPE_DEFINTIION);
        when(configElement.createExecutableExtension(DatatypeDefinition.DATATYPE_CLASS)).thenReturn(datatype);
        when(configElement.createExecutableExtension(DatatypeDefinition.HELPER_CLASS)).thenThrow(
                new CoreException(Status.CANCEL_STATUS));
        DatatypeDefinition definition = new DatatypeDefinition(extension, configElement);
        assertThat(definition.hasDatatype(), is(true));
        assertThat(definition.getDatatype(), is(datatype));
        assertThat(definition.hasHelper(), is(false));
        assertThat(definition.getHelper(), is(nullValue()));
    }
}
