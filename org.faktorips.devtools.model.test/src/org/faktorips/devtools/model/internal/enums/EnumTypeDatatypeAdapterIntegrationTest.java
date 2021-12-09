/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class EnumTypeDatatypeAdapterIntegrationTest extends AbstractIpsEnumPluginTest {

    private EnumTypeDatatypeAdapter genderAdapter;
    private EnumTypeDatatypeAdapter paymentModeAdapter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        genderAdapter = new EnumTypeDatatypeAdapter(genderEnumType, null);
        paymentModeAdapter = new EnumTypeDatatypeAdapter(paymentMode, null);
    }

    @Test
    public void testHasNullObject() {
        assertFalse(genderAdapter.hasNullObject());
    }

    @Test
    public void testIsPrimitive() {
        assertFalse(genderAdapter.isPrimitive());
    }

    @Test
    public void testIsValueDatatype() {
        assertTrue(genderAdapter.isValueDatatype());
    }

    @Test
    public void testIsVoid() {
        assertFalse(genderAdapter.isVoid());
    }

    @Test
    public void testCompreTo() throws Exception {
        assertEquals(0, genderAdapter.compareTo(genderAdapter));
        assertTrue(genderAdapter.compareTo(paymentModeAdapter) < 0);
        assertTrue(paymentModeAdapter.compareTo(genderAdapter) > 0);

    }

    @Test
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

        paymentModeAdapter.getEnumType().setExtensible(true);
        ids = paymentModeAdapter.getAllValueIds(true);
        idList = Arrays.asList(ids);
        assertEquals(3, ids.length);
        assertTrue(idList.contains("P1"));
        assertTrue(idList.contains("P2"));
        assertTrue(idList.contains(null));

        ids = paymentModeAdapter.getAllValueIds(false);
        assertEquals(2, ids.length);
        idList = Arrays.asList(ids);
        assertTrue(idList.contains("P1"));
        assertTrue(idList.contains("P2"));

        IEnumType color = newEnumType(ipsProject, "Color");
        color.setAbstract(false);
        color.setExtensible(false);
        color.newEnumLiteralNameAttribute();

        IEnumAttribute id = color.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("name");

        IEnumValue red = color.newEnumValue();
        red.getEnumAttributeValues().get(0).setValue(new StringValue("red"));
        red.getEnumAttributeValues().get(1).setValue(new StringValue("RED"));
        IEnumValue blue = color.newEnumValue();
        blue.getEnumAttributeValues().get(0).setValue(new StringValue("blue"));
        blue.getEnumAttributeValues().get(1).setValue(new StringValue("BLUE"));

        String[] colorIds = new EnumTypeDatatypeAdapter(color, null).getAllValueIds(false);
        /*
         * Is expected to be null because the identifier attribute is not specified for the
         * EnumType.
         */
        assertEquals(0, colorIds.length);
    }

    @Test
    public void testGetAllValueIds2() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(true);
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        String[] result = adapter.getAllValueIds(true);
        assertEquals(1, result.length);
        assertNull(result[0]);
        result = adapter.getAllValueIds(false);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetValueName() {
        paymentMode.setExtensible(true);
        assertNotNull(paymentModeAdapter.getValueName("P1"));
        assertNotNull(paymentModeAdapter.getValueName("P2"));
        assertNull(paymentModeAdapter.getValueName("quarterly"));
    }

    @Test
    public void testGetValueNameNotExtensible() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(false);
        enumType.newEnumLiteralNameAttribute();
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertNull(adapter.getValueName(null));
        assertNull(adapter.getValueName("a"));
    }

    @Test
    public void testGetValueNameExtensible() throws CoreRuntimeException {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        // EnumValue in Type
        addEnumValue(enumType, "idA", "nameA");

        enumType.setExtensible(true);
        IEnumContent content = newEnumContent(enumType, "EnumContent");
        // EnumValue in Content
        addEnumValue(content, "idB", "nameB");
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, content);

        assertEquals("nameA", adapter.getValueName("idA"));
        assertEquals("nameB", adapter.getValueName("idB"));
        assertNull(adapter.getValueName("idC"));
    }

    private void addEnumValue(IEnumValueContainer container, String id, String name) throws CoreRuntimeException {
        IEnumValue contentEnumValue = container.newEnumValue();
        List<IEnumAttributeValue> values = contentEnumValue.getEnumAttributeValues();
        values.get(0).setValue(new StringValue(id));
        values.get(1).setValue(new StringValue(name));
    }

    @Test
    public void testAreValuesEqual() {
        assertTrue(paymentModeAdapter.areValuesEqual("P1", "P1"));
        assertFalse(paymentModeAdapter.areValuesEqual("P1", "P2"));
        assertFalse(paymentModeAdapter.areValuesEqual("P1", "P3"));
    }

    @Test
    public void testCheckReadyToUse() {
        MessageList msgList = paymentModeAdapter.checkReadyToUse();
        assertFalse(msgList.containsErrorMsg());
        paymentModeAdapter.getEnumType().getEnumAttributes(true).get(0).delete();
        msgList = paymentModeAdapter.checkReadyToUse();
        /*
         * TODO pk 07.08.2009: checkReadyToUse is currently returning just an empty message list
         * since the validation of the underlying EnumType is too inperformant.
         */
        assertFalse(msgList.containsErrorMsg());
    }

    @Test
    public void testIsParsable() {
        assertTrue(paymentModeAdapter.isParsable("P1"));
        assertFalse(paymentModeAdapter.isParsable("P3"));
    }

    @Test
    public void testEquals() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(false);
        enumType.newEnumLiteralNameAttribute();

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setUnique(true);
        id.setIdentifier(true);

        IEnumAttribute name = enumType.newEnumAttribute();
        name.setName("name");
        name.setDatatype(Datatype.STRING.getQualifiedName());
        name.setUnique(true);
        name.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> values = enumValue.getEnumAttributeValues();
        values.get(0).setValue(new StringValue("a"));
        values.get(1).setValue(new StringValue("an"));
        values.get(2).setValue(new StringValue("AN"));
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);

        assertEquals(adapter, adapter);

        IEnumType enumType2 = newEnumType(ipsProject, "a.EnumType");
        enumType2.setExtensible(false);

        IEnumAttribute id2 = enumType2.newEnumAttribute();
        id2.setName("id");
        id2.setDatatype(Datatype.STRING.getQualifiedName());
        id2.setUnique(true);
        id2.setIdentifier(true);

        IEnumAttribute name2 = enumType2.newEnumAttribute();
        name2.setName("name");
        name2.setDatatype(Datatype.STRING.getQualifiedName());
        name2.setUnique(true);
        name2.setUsedAsNameInFaktorIpsUi(true);

        enumType2.newEnumLiteralNameAttribute();

        IEnumValue enumValue2 = enumType2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue(new StringValue("a"));
        values2.get(1).setValue(new StringValue("an"));
        values2.get(2).setValue(new StringValue("AN"));
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType2, null);
        assertFalse(adapter.equals(adapter2));
    }

    @Test
    public void testEquals2() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(true);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
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
        values.get(0).setValue(new StringValue("a"));
        values.get(1).setValue(new StringValue("an"));
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, content1);

        IEnumContent content2 = newEnumContent(enumType, "EnumContent2");
        IEnumValue enumValue2 = content2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue(new StringValue("b"));
        values2.get(1).setValue(new StringValue("bn"));
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, content2);

        assertFalse(adapter1.equals(adapter2));
    }

    @Test
    public void testHashCode() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(true);

        IEnumAttribute id = enumType.newEnumAttribute();
        id.setName("id");
        id.setDatatype(Datatype.STRING.getQualifiedName());
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
        values.get(0).setValue(new StringValue("a"));
        values.get(1).setValue(new StringValue("an"));
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, content1);

        IEnumContent content2 = newEnumContent(enumType, "EnumContent2");
        IEnumValue enumValue2 = content2.newEnumValue();
        List<IEnumAttributeValue> values2 = enumValue2.getEnumAttributeValues();
        values2.get(0).setValue(new StringValue("a"));
        values2.get(1).setValue(new StringValue("an"));
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, content2);

        assertEquals(adapter1.hashCode(), adapter1.hashCode());
        assertEquals(adapter1.hashCode(), adapter2.hashCode());
    }

}
