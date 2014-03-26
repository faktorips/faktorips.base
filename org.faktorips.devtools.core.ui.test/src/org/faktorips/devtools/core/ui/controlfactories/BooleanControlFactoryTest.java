/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controlfactories;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.LinkedHashMap;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BooleanControlFactoryTest extends AbstractIpsPluginTest {

    @Mock
    private IEnumValueSet enumValueSet;

    @Mock
    private BooleanControlFactory booleanControlFactory;

    @Before
    public void setup() {
        booleanControlFactory = new BooleanControlFactory();
    }

    @Test
    public void testRadioOptions_EnumValueSetWithoutNull() throws Exception {
        LinkedHashMap<String, String> options = booleanControlFactory.initOptions(enumValueSet, true);

        assertThat(options.keySet(), hasItem("true"));
        assertThat(options.keySet(), hasItem("false"));
    }

    @Test
    public void testRadioOptions_EnumValueSetNullIncluded() throws Exception {
        LinkedHashMap<String, String> options = booleanControlFactory.initOptions(enumValueSet, false);

        assertThat(options.keySet(), hasItem("true"));
        assertThat(options.keySet(), hasItem("false"));
        assertTrue(options.keySet().contains(null));
    }
}
