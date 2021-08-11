/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IpsProjectPropertiesForOldVersionTest {

    private IpsProjectProperties settings;
    private IpsProjectPropertiesForOldVersion oldVersion;

    @Before
    public void setup() {
        settings = mock(IpsProjectProperties.class);
        when(settings.isValidateIpsSchema()).thenCallRealMethod();
        when(settings.getBuilderSetId()).thenCallRealMethod();
        Mockito.doCallRealMethod().when(settings).setBuilderSetId(Mockito.anyString());
        Mockito.doCallRealMethod().when(settings).setValidateIpsSchema(Mockito.anyBoolean());
        settings.setValidateIpsSchema(true);
        settings.setBuilderSetId("SomeId");
        oldVersion = new IpsProjectPropertiesForOldVersion();
    }

    @Test
    public void testPropertyNotFoundBoolean() {
        oldVersion.add(
                "ValidateIpsSchema",
                IpsProjectProperties::setValidateIpsSchema,
                false);

        assertThat(settings.isValidateIpsSchema(), is(true));

        oldVersion.checkIfFound("notFound");
        oldVersion.applyNewValue(settings);

        assertThat(settings.isValidateIpsSchema(), is(false));
    }

    @Test
    public void testPropertyFoundBoolean() {
        oldVersion.add(
                "ValidateIpsSchema",
                IpsProjectProperties::setValidateIpsSchema,
                true);

        assertThat(settings.isValidateIpsSchema(), is(true));

        oldVersion.checkIfFound("ValidateIpsSchema");
        oldVersion.applyNewValue(settings);

        assertThat(settings.isValidateIpsSchema(), is(true));
    }

    @Test
    public void testPropertyFoundStringValue() {
        oldVersion.add(
                "BuilderID",
                IpsProjectProperties::setBuilderSetId,
                "SomeNewId");

        oldVersion.checkIfFound("BuilderID");
        oldVersion.applyNewValue(settings);

        assertThat(settings.getBuilderSetId(), is("SomeId"));
    }

    @Test
    public void testPropertyNotFoundStringValue() {
        oldVersion.add(
                "BuilderID",
                IpsProjectProperties::setBuilderSetId,
                "SomeNewId");

        oldVersion.checkIfFound("notFound");
        oldVersion.applyNewValue(settings);

        assertThat(settings.getBuilderSetId(), is("SomeNewId"));
    }
}
