/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.descriptorOf;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasNoOverlay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.devtools.model.internal.ipsproject.ChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class IpsObjectGenerationDecoratorTest {

    private final IpsObjectGenerationDecorator ipsObjectGenerationDecorator = new IpsObjectGenerationDecorator();

    private static IIpsModelPreferences modelPreferencesWithNamingConvention(String namingConventionId) {
        IIpsModelPreferences modelPreferences = mock(IIpsModelPreferences.class);
        when(modelPreferences.getChangesOverTimeNamingConvention())
                .thenReturn(new ChangesOverTimeNamingConvention(namingConventionId));
        return modelPreferences;
    }

    @Test
    public void testGetDefaultImageDescriptor_FIPS() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention(IChangesOverTimeNamingConvention.FAKTOR_IPS))) {

            ImageDescriptor defaultImageDescriptor = ipsObjectGenerationDecorator.getDefaultImageDescriptor();

            assertThat(defaultImageDescriptor, is(descriptorOf("FIPS_Generation.gif")));
            assertThat(defaultImageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetDefaultImageDescriptor_PM() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention(IChangesOverTimeNamingConvention.PM))) {

            ImageDescriptor defaultImageDescriptor = ipsObjectGenerationDecorator.getDefaultImageDescriptor();

            assertThat(defaultImageDescriptor, is(descriptorOf("PM_Generation.gif")));
            assertThat(defaultImageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetDefaultImageDescriptor_VAA() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention(IChangesOverTimeNamingConvention.VAA))) {

            ImageDescriptor defaultImageDescriptor = ipsObjectGenerationDecorator.getDefaultImageDescriptor();

            assertThat(defaultImageDescriptor, is(descriptorOf("VAA_Generation.gif")));
            assertThat(defaultImageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetDefaultImageDescriptor_UnknownNamingConvention() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention("FooBar"))) {

            ImageDescriptor defaultImageDescriptor = ipsObjectGenerationDecorator.getDefaultImageDescriptor();

            assertThat(defaultImageDescriptor, is(descriptorOf("Generation.gif")));
            assertThat(defaultImageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetImageDescriptor_Null() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention(IChangesOverTimeNamingConvention.VAA))) {

            ImageDescriptor imageDescriptor = ipsObjectGenerationDecorator.getImageDescriptor(null);

            assertThat(imageDescriptor, is(descriptorOf("VAA_Generation.gif")));
            assertThat(imageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetImageDescriptor() throws Exception {
        try (TestIpsModelExtensions x = TestIpsModelExtensions
                .using(modelPreferencesWithNamingConvention(IChangesOverTimeNamingConvention.FAKTOR_IPS))) {

            ImageDescriptor imageDescriptor = ipsObjectGenerationDecorator
                    .getImageDescriptor(mock(IIpsObjectGeneration.class));

            assertThat(imageDescriptor, is(descriptorOf("FIPS_Generation.gif")));
            assertThat(imageDescriptor, hasNoOverlay());
        }
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(ipsObjectGenerationDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_NoValidFrom() throws Exception {
        IIpsObjectGeneration ipsObjectGeneration = mock(IIpsObjectGeneration.class);

        IIpsModelPreferences modelPreferences = mock(IIpsModelPreferences.class);
        when(modelPreferences.getNullPresentation()).thenReturn("--");
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.using(modelPreferences)) {

            String label = ipsObjectGenerationDecorator.getLabel(ipsObjectGeneration);

            assertThat(label, is("--"));
        }
    }

    @Test
    public void testGetLabel_FormattedValidFrom() throws Exception {
        IIpsObjectGeneration ipsObjectGeneration = mock(IIpsObjectGeneration.class);
        GregorianCalendar validFrom = new GregorianCalendar(2021, Calendar.MARCH, 3);
        when(ipsObjectGeneration.getValidFrom()).thenReturn(validFrom);

        IIpsModelPreferences modelPreferences = mock(IIpsModelPreferences.class);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINESE);
        when(modelPreferences.getDateFormat()).thenReturn(dateFormat);
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.using(modelPreferences)) {

            String label = ipsObjectGenerationDecorator.getLabel(ipsObjectGeneration);

            assertThat(label, is(dateFormat.format(validFrom.getTime())));
        }
    }

}
