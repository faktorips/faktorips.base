/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnumTypeDatatypeAdapterIntegrationTest extends AbstractIpsEnumPluginTest {

    private EnumTypeDatatypeAdapter genderAdapter;
    private EnumTypeDatatypeAdapter paymentModeAdapter;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        genderAdapter = new EnumTypeDatatypeAdapter(genderEnumType, null);
        paymentModeAdapter = new EnumTypeDatatypeAdapter(paymentMode, null);
    }

    @Test
    public void testHasNullObject() {
        assertThat(genderAdapter.hasNullObject(), is(false));
    }

    @Test
    public void testIsPrimitive() {
        assertThat(genderAdapter.isPrimitive(), is(false));
    }

    @Test
    public void testIsValueDatatype() {
        assertThat(genderAdapter.isValueDatatype(), is(true));
    }

    @Test
    public void testIsVoid() {
        assertThat(genderAdapter.isVoid(), is(false));
    }

    @Test
    public void testCompareTo() throws Exception {
        assertThat(genderAdapter.compareTo(genderAdapter), is(0));
        assertThat(genderAdapter.compareTo(paymentModeAdapter), is(lessThan(0)));
        assertThat(paymentModeAdapter.compareTo(genderAdapter), is(greaterThan(0)));

    }

    @Test
    public void testGetAllValueIds() throws Exception {
        String[] ids = paymentModeAdapter.getAllValueIds(false);
        assertThat(ids.length, is(2));
        List<String> idList = Arrays.asList(ids);
        assertThat(idList, containsInAnyOrder("P1", "P2"));

        ids = paymentModeAdapter.getAllValueIds(true);
        assertThat(ids.length, is(3));
        idList = Arrays.asList(ids);
        assertThat(idList, hasItem((String)null));

        paymentModeAdapter.getEnumType().setExtensible(true);
        ids = paymentModeAdapter.getAllValueIds(true);
        idList = Arrays.asList(ids);
        assertThat(ids.length, is(3));
        assertThat(idList, containsInAnyOrder("P1", "P2", null));

        ids = paymentModeAdapter.getAllValueIds(false);
        assertThat(ids.length, is(2));
        idList = Arrays.asList(ids);
        assertThat(idList, containsInAnyOrder("P1", "P2"));

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
        assertThat(colorIds.length, is(0));
    }

    @Test
    public void testGetAllValueIds2() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(true);
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        String[] result = adapter.getAllValueIds(true);
        assertThat(result.length, is(1));
        assertThat(result[0], is(nullValue()));
        result = adapter.getAllValueIds(false);
        assertThat(result.length, is(0));
    }

    @Test
    public void testGetValueName() {
        paymentMode.setExtensible(true);
        assertThat(paymentModeAdapter.getValueName("P1"), is(notNullValue()));
        assertThat(paymentModeAdapter.getValueName("P2"), is(notNullValue()));
        assertThat(paymentModeAdapter.getValueName("quarterly"), is(nullValue()));
    }

    @Test
    public void testGetValueNameNotExtensible() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        enumType.setExtensible(false);
        enumType.newEnumLiteralNameAttribute();
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertThat(adapter.getValueName(null), is(nullValue()));
        assertThat(adapter.getValueName("a"), is(nullValue()));
    }

    @Test
    public void testGetValueNameExtensible() {
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

        assertThat(adapter.getValueName("idA"), is("nameA"));
        assertThat(adapter.getValueName("idB"), is("nameB"));
        assertThat(adapter.getValueName("idC"), is(nullValue()));
    }

    @Test
    public void testValueToString_returnsId_notPath() {
        IEnumValue value = paymentModeAdapter.getValue("P1");
        assertThat(paymentModeAdapter.valueToString(value), is("P1"));
    }

    @Test
    public void testValueToStringExtensible() {
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

        IEnumValue valueA = adapter.getValue("idA");
        IEnumValue valueB = adapter.getValue("idB");

        assertThat(adapter.valueToString(valueA), is("idA"));
        assertThat(adapter.valueToString(valueB), is("idB"));
    }

    private void addEnumValue(IEnumValueContainer container, String id, String name) {
        IEnumValue contentEnumValue = container.newEnumValue();
        List<IEnumAttributeValue> values = contentEnumValue.getEnumAttributeValues();
        values.get(0).setValue(new StringValue(id));
        values.get(1).setValue(new StringValue(name));
    }

    @Test
    public void testAreValuesEqual() {
        assertThat(paymentModeAdapter.areValuesEqual("P1", "P1"), is(true));
        assertThat(paymentModeAdapter.areValuesEqual("P1", "P2"), is(false));
        assertThat(paymentModeAdapter.areValuesEqual("P1", "P3"), is(false));
    }

    @Test
    public void testCheckReadyToUse() {
        MessageList msgList = paymentModeAdapter.checkReadyToUse();
        assertThat(msgList.containsErrorMsg(), is(false));
        paymentModeAdapter.getEnumType().getEnumAttributes(true).get(0).delete();
        msgList = paymentModeAdapter.checkReadyToUse();
        /*
         * TODO pk 07.08.2009: checkReadyToUse is currently returning just an empty message list
         * since the validation of the underlying EnumType is too inperformant.
         */
        assertThat(msgList.containsErrorMsg(), is(false));
    }

    @Test
    public void testIsParsable() {
        assertThat(paymentModeAdapter.isParsable("P1"), is(true));
        assertThat(paymentModeAdapter.isParsable("P3"), is(false));
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

        assertThat(adapter, is(adapter));

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
        assertThat(adapter.equals(adapter2), is(false));
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

        assertThat(adapter1.equals(adapter2), is(false));
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

        assertThat(adapter1.hashCode(), is(adapter1.hashCode()));
        assertThat(adapter2.hashCode(), is(adapter1.hashCode()));
    }

    @Test
    public void testGetIdByName_existing() {
        IEnumValue value = (IEnumValue)paymentModeAdapter.getValueByName("monthly");
        assertThat(value, is(paymentModeAdapter.getValue("P1")));
    }

    @Test
    public void testGetIdByName_notExisting() {
        IEnumValue value = (IEnumValue)paymentModeAdapter.getValueByName("noValue");
        assertThat(value, is(nullValue()));
    }
}
