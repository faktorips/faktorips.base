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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.util.message.MessageList;

public class EnumTypeDatatypeAdapterTest extends AbstractIpsEnumPluginTest {

    private EnumTypeDatatypeAdapter genderAdapter;
    private EnumTypeDatatypeAdapter paymentModeAdapter;

    public void setUp() throws Exception {
        super.setUp();
        genderAdapter = new EnumTypeDatatypeAdapter(genderEnumType, null);
        paymentModeAdapter = new EnumTypeDatatypeAdapter(paymentMode, null);
    }

    public void testGetJavaClassName() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(true);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setLiteralName(true);
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("a");
        values.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertEquals("org.faktorips.sample.model.EnumType", adapter.getJavaClassName());
    }

    public void testHasNullObject() {
        assertFalse(genderAdapter.hasNullObject());
    }

    public void testIsPrimitive() {
        assertFalse(genderAdapter.isPrimitive());
    }

    public void testIsValueDatatype() {
        assertTrue(genderAdapter.isValueDatatype());
    }

    public void testIsVoid() {
        assertFalse(genderAdapter.isVoid());
    }

    public void testCompreTo() throws Exception {
        try {
            genderAdapter.compareTo(new Object());
            fail("ClassCastException is expected.");
        } catch (ClassCastException e) {
        }
        assertEquals(0, genderAdapter.compareTo(genderAdapter));
        assertTrue(genderAdapter.compareTo(paymentModeAdapter) < 0);
        assertTrue(paymentModeAdapter.compareTo(genderAdapter) > 0);

    }

    public void testGetAllValueIds() throws Exception {
        String[] ids = paymentModeAdapter.getAllValueIds(false);
        assertEquals(2, ids.length);
        List<String> idList = Arrays.asList(ids);
        assertTrue(idList.contains("P1"));
        assertTrue(idList.contains("P2"));

        ids = paymentModeAdapter.getAllValueIds(true);
        assertEquals(3, ids.length);
        idList = Arrays.asList(ids);
        assertTrue(idList.contains(null));

        paymentModeAdapter.getEnumType().setContainingValues(false);
        ids = paymentModeAdapter.getAllValueIds(true);
        idList = Arrays.asList(ids);
        assertEquals(1, ids.length);
        assertTrue(idList.contains(null));

        ids = paymentModeAdapter.getAllValueIds(false);
        assertEquals(0, ids.length);

        IEnumType color = newEnumType(ipsProject, "Color");
        color.setAbstract(false);
        color.setContainingValues(true);

        IEnumAttribute id = color.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setLiteralName(false);
        id.setName("name");
        IEnumValue red = color.newEnumValue();
        IEnumAttributeValue redN = red.getEnumAttributeValues().get(0);
        redN.setValue("red");
        IEnumValue blue = color.newEnumValue();
        IEnumAttributeValue blueN = blue.getEnumAttributeValues().get(0);
        blueN.setValue("blue");

        String[] colorIds = new EnumTypeDatatypeAdapter(color, null).getAllValueIds(false);
        // is expected to be null because the literal name attribute is not specified for the enum
        // type
        assertEquals(0, colorIds.length);
    }

    public void testGetAllValueIds2() throws Exception {

        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(false);
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        String[] result = adapter.getAllValueIds(true);
        assertEquals(1, result.length);
        assertNull(result[0]);
        result = adapter.getAllValueIds(false);
        assertEquals(0, result.length);
    }

    public void testgGetValueName() {
        assertNotNull(paymentModeAdapter.getValueName("P1"));
        assertNotNull(paymentModeAdapter.getValueName("P2"));
        assertNull(paymentModeAdapter.getValueName("quarterly"));
    }

    public void testgGetValueName2() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(false);
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertNull(adapter.getValueName(null));
        assertNull(adapter.getValueName("a"));

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setLiteralName(true);
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumContent content = newEnumContent(enumType, "EnumContent");
        IEnumValue enumValue = content.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("a");
        values.get(1).setValue("an");
        adapter = new EnumTypeDatatypeAdapter(enumType, content);

        assertEquals("a", adapter.getValueName("a"));
        assertNull(adapter.getValueName("b"));

    }

    public void testAreValuesEqual() {
        assertTrue(paymentModeAdapter.areValuesEqual("P1", "P1"));
        assertFalse(paymentModeAdapter.areValuesEqual("P1", "P2"));
        try {
            paymentModeAdapter.areValuesEqual("P1", "P3");
            fail("");
        } catch (Exception e) {
        }
    }

    public void testCheckReadyToUse() {
        MessageList msgList = paymentModeAdapter.checkReadyToUse();
        assertFalse(msgList.containsErrorMsg());
        paymentModeAdapter.getEnumType().getEnumAttributes().get(0).delete();
        msgList = paymentModeAdapter.checkReadyToUse();
        assertTrue(msgList.containsErrorMsg());
    }

    public void testIsParsable() {
        assertTrue(paymentModeAdapter.isParsable("P1"));
        assertFalse(paymentModeAdapter.isParsable("P3"));
    }

    public void testEquals() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(true);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setLiteralName(true);
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("a");
        values.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);

        assertEquals(adapter, adapter);
        
        IEnumType enumType2 = newEnumType(ipsProject, "a.EnumType");
        enumType2.setContainingValues(true);

        IEnumAttribute id2 = enumType2.newEnumAttribute();
        id2.setName("id");
        id2.setDatatype(Datatype.STRING.getQualifiedName());
        id2.setLiteralName(true);
        id2.setUnique(true);
        id2.setIdentifier(true);

        IEnumAttribute name2 = enumType2.newEnumAttribute();
        name2.setName("name");
        name2.setDatatype(Datatype.STRING.getQualifiedName());
        name2.setUnique(true);
        name2.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue enumValue2 = enumType2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue("a");
        values2.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType2, null);
        assertFalse(adapter.equals(adapter2));
    }
    
    public void testEquals2() throws Exception{
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(false);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setLiteralName(true);
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumContent content1 = newEnumContent(enumType, "EnumContent1");
        IEnumValue enumValue = content1.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("a");
        values.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, content1);

        IEnumContent content2 = newEnumContent(enumType, "EnumContent2");
        IEnumValue enumValue2 = content2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue("a");
        values2.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, content2);

        assertFalse(adapter1.equals(adapter2));
    }
    
    public void testHashCode() throws Exception{
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setContainingValues(false);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setLiteralName(true);
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumContent content1 = newEnumContent(enumType, "EnumContent1");
        IEnumValue enumValue = content1.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue("a");
        values.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, content1);

        IEnumContent content2 = newEnumContent(enumType, "EnumContent2");
        IEnumValue enumValue2 = content2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue("a");
        values2.get(1).setValue("an");
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, content2);
        
        assertEquals(adapter1.hashCode(), adapter1.hashCode());
        assertEquals(adapter1.hashCode(), adapter2.hashCode());
    }
}
