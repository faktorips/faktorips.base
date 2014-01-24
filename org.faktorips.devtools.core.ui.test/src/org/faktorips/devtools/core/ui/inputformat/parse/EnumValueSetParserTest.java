/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.inputformat.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.DefaultInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueSetParserTest {

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private ConfigElement configElement;

    @Mock
    private ValueDatatype datatype;

    private IEnumValueSet enumValueSet;

    private EnumValueSetParser enumParser;

    @Before
    public void setUp() throws Exception {
        enumParser = new EnumValueSetParser(configElement, uiPlugin);
        enumValueSet = new EnumValueSet(configElement, "ID");

        when(uiPlugin.getInputFormat(Mockito.any(ValueDatatype.class), Mockito.any(IIpsProject.class))).thenReturn(
                new DefaultInputFormat());
        when(configElement.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configElement.getIpsProject()).thenReturn(ipsProject);
        when(configElement.getIpsModel()).thenReturn(ipsModel);
        when(configElement.getIpsObject()).thenReturn(ipsObject);
        when(configElement.getValueSet()).thenReturn(enumValueSet);
    }

    @Test
    public void testParseInternalNewEnumValueSet() throws Exception {
        IValueSet parseInternal = enumParser.parseValueSet("test | test2");
        enumValueSet.addValue("test | test1");
        EnumValueSet enumVS = (EnumValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof EnumValueSet);
        assertEquals(configElement, enumVS.getParent());
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test2", enumVS.getValue(1));
    }

    @Test
    public void testParseInternalOldEnumValueSet() throws Exception {
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IValueSet parseInternal = enumParser.parseValueSet("test | test1");
        EnumValueSet enumVS = (EnumValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof EnumValueSet);
        assertEquals(configElement, enumVS.getParent());
        assertTrue(("ID").equals(parseInternal.getId()));
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test1", enumVS.getValue(1));
    }

}
