/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertydef;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class ChangesOverTimeNamingConventionPropertyDefTest {


    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProjectProperties properties;

    private MockitoSession mockito;

    private ChangesOverTimeNamingConventionPropertyDef propertyDef = new ChangesOverTimeNamingConventionPropertyDef();

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testGetDefaultValue() throws Exception {
        when(properties.getChangesOverTimeNamingConventionIdForGeneratedCode()).thenReturn("Foo");

        assertThat(propertyDef.getDefaultValue(ipsProject), is("Foo"));
    }

    @Test
    public void testGetDisableValue() throws Exception {
        when(properties.getChangesOverTimeNamingConventionIdForGeneratedCode()).thenReturn("Bar");

        assertThat(propertyDef.getDisableValue(ipsProject), is("Bar"));
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertThat(propertyDef.isAvailable(ipsProject), is(false));
    }

    @Test
    public void testValidateValue() throws Exception {
        when(properties.getChangesOverTimeNamingConventionIdForGeneratedCode()).thenReturn("Foo");

        Message message = propertyDef.validateValue(ipsProject, "Bar");

        assertThat(message, is(notNullValue()));
        assertThat(message.getSeverity(), is(Message.WARNING));
        assertThat(message.getCode(),
                is(ChangesOverTimeNamingConventionPropertyDef.MSG_CODE_DERIVED_PROPERTY_SET_MANUALLY));
        assertThat(message.getText(), containsString("Foo"));
        assertThat(message.getText(), containsString("Bar"));
        assertThat(message.getText(), containsString(
                ChangesOverTimeNamingConventionPropertyDef.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION));
    }

}
