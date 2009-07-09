/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class DatatypeFormatterTest extends AbstractIpsPluginTest {

    public void testFormatValueIEnumTypeIEnumContentString() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestProject");

        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setContainingValues(true);

        IEnumAttribute attr1 = enum1.newEnumAttribute();
        attr1.setDatatype(Datatype.STRING.getQualifiedName());
        attr1.setLiteralName(true);
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
        values.get(0).setValue("a");
        values.get(1).setValue("aname");
        values.get(2).setValue("adesc");

        IEnumValue enumValue2 = enum1.newEnumValue();
        values = enumValue2.getEnumAttributeValues();
        values.get(0).setValue("b");
        values.get(1).setValue("bname");
        values.get(2).setValue("bdesc");

        IEnumValue enumValue3 = enum1.newEnumValue();
        values = enumValue3.getEnumAttributeValues();
        values.get(0).setValue("c");
        values.get(1).setValue("cname");
        values.get(2).setValue("cdesc");
        
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        ipsPreferences.setEnumTypeDisplay(EnumTypeDisplay.NAME);
        DatatypeFormatter formatter = new DatatypeFormatter(ipsPreferences);

        String text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("aname", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "b");
        assertEquals("bname", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "c");
        assertEquals("cname", text);

        attr1.setIdentifier(false);
        //Returns the unformatted value since the value a cannot be identified as an id value of
        //the enum1
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr1.setIdentifier(true);

        attr2.setUsedAsNameInFaktorIpsUi(false);
        //since the display setting is name and no name ui attribute exists the default is to 
        //return the input value
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr2.setUsedAsNameInFaktorIpsUi(true);

        ipsPreferences.setEnumTypeDisplay(EnumTypeDisplay.ID);
        formatter = new DatatypeFormatter(ipsPreferences);
        
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("a", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "b");
        assertEquals("b", text);
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "c");
        assertEquals("c", text);
        
        attr1.setIdentifier(false);
        //since the display setting is id and no id ui attribute exists the default is to 
        //return the input value
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr1.setIdentifier(true);

        attr2.setUsedAsNameInFaktorIpsUi(false);
        //should not have any influence 
        assertEquals("a", formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a"));
        attr2.setUsedAsNameInFaktorIpsUi(true);
        
        ipsPreferences.setEnumTypeDisplay(EnumTypeDisplay.NAME_AND_ID);
        formatter = new DatatypeFormatter(ipsPreferences);
        
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("aname (a)", text);

        attr1.setIdentifier(false);
        //Returns the unformatted value since the value a cannot be identified as an id value of
        //the enum1
        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "a");
        assertEquals("a", text);
        attr1.setIdentifier(true);

        text = formatter.formatValue(new EnumTypeDatatypeAdapter(enum1, null), "k");
        assertEquals("k", text);

    }

}
