/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.enumtype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepositoryMockTest.ExtensibleEnum;
import org.faktorips.runtime.internal.AbstractRuntimeRepositoryMockTest.RealEnum;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.model.enumtype.EnumAttributeTest.MyEnumForMandatoryTests;
import org.faktorips.runtime.model.type.Deprecation;
import org.junit.Test;

public class EnumTypeTest {

    @Test
    public void testIsExtensible() {
        assertThat(new EnumType(Foo.class).isExtensible(), is(false));
        assertThat(new EnumType(Bar.class).isExtensible(), is(true));
    }

    @Test
    public void testGetEnumContentQualifiedName() {
        assertThat(new EnumType(Foo.class).getEnumContentQualifiedName(), is(nullValue()));
        assertThat(new EnumType(Bar.class).getEnumContentQualifiedName(), is(equalTo("my.baz")));
    }

    @Test
    public void testGetName() {
        assertThat(new EnumType(Foo.class).getName(), is(equalTo("my.foo")));
        assertThat(new EnumType(Bar.class).getName(), is(equalTo("my.bar")));
    }

    @Test
    public void testGetAttributes() {
        List<EnumAttribute> fooAttributes = new EnumType(Foo.class).getAttributes();
        assertThat(fooAttributes.size(), is(3));
        assertThat(fooAttributes.get(0).getName(), is(equalTo("x")));
        assertThat(fooAttributes.get(1).getName(), is(equalTo("Z")));
        assertThat(fooAttributes.get(2).getName(), is(equalTo("y")));
        assertThat(new EnumType(Bar.class).getAttributes().size(), is(0));
    }

    @Test
    public void testGetAttribute() {
        assertThat(new EnumType(Foo.class).getAttribute("x").getName(), is(equalTo("x")));
        assertThat(new EnumType(Foo.class).getAttribute("Z").getName(), is(equalTo("Z")));
    }

    @Test
    public void testGetAttributenames() {
        List<String> fooAttributes = new EnumType(Foo.class).getAttributenames();
        assertThat(fooAttributes.size(), is(3));
        assertThat(fooAttributes.get(0), is(equalTo("x")));
        assertThat(fooAttributes.get(1), is(equalTo("Z")));
        assertThat(fooAttributes.get(2), is(equalTo("y")));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetEnumClass() {
        EnumType type = new EnumType(Foo.class);

        // is(Class) is deprecated and raw cast is required due to assertThat
        assertThat(type.getEnumClass(), is(equalTo((Class)Foo.class)));
    }

    @Test
    public void testGetIdAttribute() {
        assertThat(new EnumType(Foo.class).getIdAttribute().getName(), is(equalTo("x")));
    }

    @Test
    public void testGetValuesFromType() {
        List<ExtensibleEnum> enumValues = EnumType.getValuesFromType(ExtensibleEnum.class);

        assertEquals(ExtensibleEnum.VALUES, enumValues);
    }

    @Test
    public void testGetValuesFromType_Cache() {
        List<ExtensibleEnum> expectedValues = ExtensibleEnum.VALUES;
        List<ExtensibleEnum> firstCall = AbstractRuntimeRepository.getEnumValuesDefinedInType(ExtensibleEnum.class);
        assertEquals(expectedValues, firstCall);
        ExtensibleEnum.VALUES = List.of(ExtensibleEnum.VALUE2);
        List<ExtensibleEnum> secondCall = AbstractRuntimeRepository.getEnumValuesDefinedInType(ExtensibleEnum.class);
        assertEquals(expectedValues, secondCall);
        assertEquals(firstCall, secondCall);
        ExtensibleEnum.VALUES = List.of(ExtensibleEnum.VALUE1, ExtensibleEnum.VALUE2);
    }

    @Test
    public void testGetValuesFromType_JavaEnum() {
        List<RealEnum> enumValues = EnumType.getValuesFromType(RealEnum.class);

        assertEquals(List.of(RealEnum.values()), enumValues);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIdAttribute_noneFound() {
        new EnumType(Bar.class).getIdAttribute().getName();
    }

    @Test
    public void testGetDisplayNameAttribute() {
        assertThat(new EnumType(Foo.class).getDisplayNameAttribute().getName(), is(equalTo("y")));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDisplayNameAttribute_noneFound() {
        new EnumType(Bar.class).getDisplayNameAttribute().getName();
    }

    @Test
    public void testIsDeprecated() {
        assertThat(IpsModel.getEnumType(Foo.class).isDeprecated(), is(false));
        assertThat(IpsModel.getEnumType(DeprecatedEnum.class).isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecated() {
        assertThat(IpsModel.getEnumType(Foo.class).getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = IpsModel.getEnumType(DeprecatedEnum.class).getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @Test
    public void testGetDocumentation() {
        EnumType superEnumType = IpsModel.getEnumType(SuperAbstractEnumType.class);
        EnumType enumType = IpsModel.getEnumType(AbstractEnumType.class);
        assertThat(superEnumType.getDescription(Locale.GERMAN), is("Description of super source"));
        assertThat(enumType.getDescription(Locale.GERMAN), is("Description of super source"));
    }

    @Test
    public void testValidate_OK() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.enumType.validate(messages, context, List.of(MyEnumForMandatoryTests.allSet));

        assertThat("MessageList should be empty but is " + messages, messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_CallsMandatoryValidations() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.enumType.validate(messages, context,
                List.of(MyEnumForMandatoryTests.allSet, MyEnumForMandatoryTests.allNulls,
                        MyEnumForMandatoryTests.allEmpty));

        assertThat(messages.containsErrorMsg(), is(true));
        assertThat(messages.stream()
                .filter(m -> m.getCode().equals(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY))
                .map(Message::getInvalidObjectProperties)
                .flatMap(List::stream).toList(),
                contains(
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryMultilingualString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryInteger"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryDecimal"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryMultilingualString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryInteger"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryDecimal")));
    }

    @Test
    public void testValidate_NotUnique() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        Foo e1a = new Foo(1, "Eins", true);
        Foo e1b = new Foo(1, "Eins", true);
        Foo e2 = new Foo(2, "Eins", true);
        Foo.enumType.validate(messages, context, List.of(e1a, e1b, e2));

        assertThat(messages.containsErrorMsg(), is(true));
        assertThat(messages.stream()
                .filter(m -> m.getCode().equals(EnumAttribute.MSGCODE_NOT_UNIQUE))
                .map(Message::getInvalidObjectProperties)
                .flatMap(List::stream).toList(),
                contains(
                        new ObjectProperty(e1a, "x"),
                        new ObjectProperty(e1b, "x")));
    }

    @Test
    public void testValidate_WrongType() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Foo.enumType.validate(messages, context, List.of(new Foo(1, "Eins", true), new Bar())));
        assertThat(exception.getMessage(), containsString("Bar"));
        assertThat(exception.getMessage(), containsString("my.foo"));
    }

    @IpsEnumType(name = "my.foo", attributeNames = { "x", "Z", "y" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "de")
    public static class Foo {

        static final EnumType enumType = new EnumType(Foo.class);

        private Integer x;
        private String y;
        private Boolean z;

        public Foo(Integer x, String y, Boolean z) {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @IpsEnumAttribute(name = "x", identifier = true, unique = true, mandatory = true)
        public Integer getX() {
            return x;
        }

        @IpsEnumAttribute(name = "y", displayName = true)
        public String getY() {
            return y;
        }

        @IpsEnumAttribute(name = "Z")
        public Boolean getZ() {
            return z;
        }

    }

    @IpsExtensibleEnum(enumContentName = "my.baz")
    @IpsEnumType(name = "my.bar", attributeNames = {})
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "de")
    private static class Bar {
        // en extensible enum
    }

    @IpsExtensibleEnum(enumContentName = "deprecated.content")
    @IpsEnumType(name = "deprecated.structure", attributeNames = {})
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "de")
    @Deprecated
    private static class DeprecatedEnum {
        // a dreprecated enum
    }

    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "de")
    @IpsEnumType(name = "SuperAbstractEnumType", attributeNames = { "id", "name", "attributeWithDescription" })
    public interface SuperAbstractEnumType {

        @IpsEnumAttribute(name = "id", identifier = true, unique = true, mandatory = true)
        Integer getId();

        @IpsEnumAttribute(name = "name", displayName = true)
        String getName();

        @IpsEnumAttribute(name = "attributeWithDescription")
        String getAttributeWithDescription();
    }

    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "en")
    @IpsEnumType(name = "AbstractEnumType", attributeNames = { "id", "name", "attributeWithDescription" })
    public interface AbstractEnumType extends SuperAbstractEnumType {
        // an abstract enumType
    }
}
