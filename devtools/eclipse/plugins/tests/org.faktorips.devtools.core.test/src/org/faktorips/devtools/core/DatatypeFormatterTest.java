/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Test;

public class DatatypeFormatterTest extends AbstractIpsPluginTest {

    @Test
    public void testFormatValueIEnumTypeIEnumContentString() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestProject");

        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setExtensible(true);
        enum1.newEnumLiteralNameAttribute();

        IEnumAttribute attr1 = enum1.newEnumAttribute();
        attr1.setDatatype(Datatype.STRING.getQualifiedName());
        attr1.setIdentifier(true);
        attr1.setName("id");
        attr1.setUnique(true);
        attr1.setIdentifier(true);

        IEnumAttribute attr2 = enum1.newEnumAttribute();
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        attr2.setName("name");
        attr2.setUnique(true);
        attr2.setUsedAsNameInFaktorIpsUi(true);

        IEnumAttribute attr3 = enum1.newEnumAttribute();
        attr3.setDatatype(Datatype.STRING.getQualifiedName());
        attr3.setName("description");
        attr3.setUnique(false);

        IEnumValue enumValue = enum1.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue(ValueFactory.createStringValue("A"));
        values.get(1).setValue(ValueFactory.createStringValue("a"));
        values.get(2).setValue(ValueFactory.createStringValue("aname"));
        values.get(3).setValue(ValueFactory.createStringValue("adesc"));

        IEnumValue enumValue2 = enum1.newEnumValue();
        values = enumValue2.getEnumAttributeValues();
        values.get(0).setValue(ValueFactory.createStringValue("B"));
        values.get(1).setValue(ValueFactory.createStringValue("b"));
        values.get(2).setValue(ValueFactory.createStringValue("bname"));
        values.get(3).setValue(ValueFactory.createStringValue("bdesc"));

        IEnumValue enumValue3 = enum1.newEnumValue();
        values = enumValue3.getEnumAttributeValues();
        values.get(0).setValue(ValueFactory.createStringValue("C"));
        values.get(1).setValue(ValueFactory.createStringValue("c"));
        values.get(2).setValue(ValueFactory.createStringValue("cname"));
        values.get(3).setValue(ValueFactory.createStringValue("cdesc"));

        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        ipsPreferences.setNamedDataTypeDisplay(NamedDataTypeDisplay.NAME);
        DatatypeFormatter formatter = new DatatypeFormatter(ipsPreferences);

        String text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("aname", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "b");
        assertEquals("bname", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "c");
        assertEquals("cname", text);

        attr1.setIdentifier(false);
        // Returns the unformatted value since the value a cannot be identified as an id value of
        // the enum1
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr1.setIdentifier(true);

        attr2.setUsedAsNameInFaktorIpsUi(false);
        // since the display setting is name and no name ui attribute exists the default is to
        // return the input value
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr2.setUsedAsNameInFaktorIpsUi(true);

        ipsPreferences.setNamedDataTypeDisplay(NamedDataTypeDisplay.ID);
        formatter = new DatatypeFormatter(ipsPreferences);

        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("a", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "b");
        assertEquals("b", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "c");
        assertEquals("c", text);

        attr1.setIdentifier(false);
        // since the display setting is id and no id ui attribute exists the default is to
        // return the input value
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr1.setIdentifier(true);

        attr2.setUsedAsNameInFaktorIpsUi(false);
        // should not have any influence
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr2.setUsedAsNameInFaktorIpsUi(true);

        ipsPreferences.setNamedDataTypeDisplay(NamedDataTypeDisplay.NAME_AND_ID);
        formatter = new DatatypeFormatter(ipsPreferences);

        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("aname (a)", text);

        attr1.setIdentifier(false);
        // Returns the unformatted value since the value a cannot be identified as an id value of
        // the enum1
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("a", text);
        attr1.setIdentifier(true);

        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "k");
        assertEquals("k", text);
    }

}
