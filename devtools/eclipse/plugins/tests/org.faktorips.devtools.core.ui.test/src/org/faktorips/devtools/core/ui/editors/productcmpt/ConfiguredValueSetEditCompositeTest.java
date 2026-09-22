/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.junit.jupiter.api.Test;

public class ConfiguredValueSetEditCompositeTest {

    @Test
    public void testHasExtensionProperties_none() {
        IConfiguredValueSet configuredValueSet = mock(IConfiguredValueSet.class);
        when(configuredValueSet.getExtensionPropertyDefinitions()).thenReturn(Collections.emptyList());

        assertThat(ConfiguredValueSetEditComposite.hasExtensionProperties(configuredValueSet), is(false));
    }

    @Test
    public void testHasExtensionProperties_atLeastOne() {
        IConfiguredValueSet configuredValueSet = mock(IConfiguredValueSet.class);
        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(configuredValueSet.getExtensionPropertyDefinitions())
                .thenReturn(List.of(extensionPropertyDefinition));

        assertThat(ConfiguredValueSetEditComposite.hasExtensionProperties(configuredValueSet), is(true));
    }
}
