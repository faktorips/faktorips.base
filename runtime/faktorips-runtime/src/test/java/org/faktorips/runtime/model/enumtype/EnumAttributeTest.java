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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.values.Decimal;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EnumAttributeTest {

    @Mock
    private EnumType enumType;

    private EnumAttribute fooModel;

    private EnumAttribute barModel;

    private final EnumType superEnumType = IpsModel.getEnumType(SuperAbstractEnumType.class);

    private final EnumType enumType2 = IpsModel.getEnumType(AbstractEnumType.class);

    private EnumAttribute superAttr = superEnumType.getAttribute("attributeWithDescription");

    private EnumAttribute attr = enumType2.getAttribute("attributeWithDescription");

    @Before
    public void initModels() throws SecurityException, NoSuchMethodException {
        Method fooGetter = MyEnum.class.getMethod("getFoo");
        fooModel = new EnumAttribute(enumType, "foo", fooGetter);
        Method barGetter = MyEnum.class.getMethod("getBar", Locale.class);
        barModel = new EnumAttribute(enumType, "bar", barGetter);

    }

    @Test
    public void testGetDatatype() {
        assertThat(fooModel.getDatatype(), is(CoreMatchers.<Class<?>> equalTo(Integer.class)));
        assertThat(barModel.getDatatype(), is(CoreMatchers.<Class<?>> equalTo(String.class)));
    }

    @Test
    public void testIsUnique() {
        assertThat(fooModel.isUnique(), is(true));
        assertThat(barModel.isUnique(), is(false));
    }

    @Test
    public void testIsIdentifier() {
        assertThat(fooModel.isIdentifier(), is(true));
        assertThat(barModel.isIdentifier(), is(false));
    }

    @Test
    public void testIsDisplayName() {
        assertThat(fooModel.isDisplayName(), is(false));
        assertThat(barModel.isDisplayName(), is(true));
    }

    @Test
    public void testIsMandatory() {
        assertThat(fooModel.isMandatory(), is(true));
        assertThat(barModel.isMandatory(), is(false));
    }

    @Test
    public void testGetValueObject() {
        Map<Locale, String> bar = new HashMap<>();
        bar.put(Locale.GERMANY, "Hallo Welt");
        bar.put(Locale.US, "hello world");
        MyEnum myEnum = new MyEnum(42, bar);
        Locale.setDefault(Locale.US);

        assertThat((Integer)fooModel.getValue(myEnum), is(42));
        assertThat((String)barModel.getValue(myEnum), is(equalTo("hello world")));
    }

    @Test
    public void testIsMultilingual() {
        assertThat(barModel.isMultilingual(), is(true));
        assertThat(fooModel.isMultilingual(), is(false));
    }

    @Test
    public void testGetValueObjectLocale() {
        Map<Locale, String> bar = new HashMap<>();
        bar.put(Locale.GERMANY, "Hallo Welt");
        bar.put(Locale.US, "hello world");
        MyEnum myEnum = new MyEnum(42, bar);
        Locale.setDefault(Locale.US);

        assertThat((Integer)fooModel.getValue(myEnum, Locale.CANADA), is(42));
        assertThat((String)barModel.getValue(myEnum, Locale.US), is(equalTo("hello world")));
        assertThat((String)barModel.getValue(myEnum, Locale.GERMANY), is(equalTo("Hallo Welt")));
    }

    @Test
    public void testIsDeprecated() throws NoSuchMethodException, SecurityException {
        Method deprecatedGetter = MyEnum.class.getMethod("getDeprecated");
        EnumAttribute deprecatedAttribute = new EnumAttribute(enumType, "deprecated", deprecatedGetter);

        assertThat(fooModel.isDeprecated(), is(false));
        assertThat(deprecatedAttribute.isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecated() throws NoSuchMethodException, SecurityException {
        Method deprecatedGetter = MyEnum.class.getMethod("getDeprecated");
        EnumAttribute deprecatedAttribute = new EnumAttribute(enumType, "deprecated", deprecatedGetter);

        assertThat(fooModel.getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = deprecatedAttribute.getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @Test
    public void testFindSuperEnumAttribute() throws Exception {
        assertThat(superAttr.findSuperEnumAttribute(), is((Optional.empty())));
        assertThat(attr.findSuperEnumAttribute(), is(Optional.of(superAttr)));
    }

    @Test
    public void testGetDocumentation() {
        assertThat(superAttr.getDescription(Locale.GERMAN), is("Description of EnumAttributeWithDescription"));
        assertThat(attr.getDescription(Locale.GERMAN),
                is("Description of EnumAttributeWithDescription in AbstractEnumType"));
    }

    @Test
    public void testValidate_OK() {
        MyEnum myEnum = new MyEnum(1, null);
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        fooModel.validate(messages, context, myEnum);
        barModel.validate(messages, context, myEnum);
        MyEnumForMandatoryTests.mandatoryStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.optionalStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.mandatoryMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.optionalMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.mandatoryDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.mandatoryDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.mandatoryIntegerAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);
        MyEnumForMandatoryTests.mandatoryIntegerAttribute.validate(messages, context, MyEnumForMandatoryTests.allSet);

        assertThat("MessageList should be empty but is " + messages, messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingMandatory_StringNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryString")));
    }

    @Test
    public void testValidate_MissingMandatory_StringEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryString")));
    }

    @Test
    public void testValidate_MissingOptional_StringNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingOptional_StringEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalStringAttribute.validate(messages, context, MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingMandatory_MultiLingualStringNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allNulls);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryMultilingualString")));
    }

    @Test
    public void testValidate_MissingMandatory_MultiLingualStringEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryMultilingualString")));
    }

    @Test
    public void testValidate_MissingMandatory_MultiLingualStringOneLocaleEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.oneLocaleEmpty);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.oneLocaleEmpty, "mandatoryMultilingualString")));
    }

    @Test
    public void testValidate_MissingOptional_MultiLingualStringNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allNulls);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingOptional_MultiLingualStringEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingOptional_MultiLingualStringOneLocaleEmpty() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalMultilingualStringAttribute.validate(messages, context,
                MyEnumForMandatoryTests.oneLocaleEmpty);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingMandatory_DecimalNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryDecimal")));
    }

    @Test
    public void testValidate_MissingMandatory_DecimalNullObject() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryDecimal")));
    }

    @Test
    public void testValidate_MissingOptional_DecimalNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingOptional_DecimalNullObject() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalDecimalAttribute.validate(messages, context, MyEnumForMandatoryTests.allEmpty);

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testValidate_MissingMandatory_IntegerNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.mandatoryIntegerAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.containsErrorMsg(), is(true));
        Message message = messages.getMessageByCode(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY);
        assertThat(messages, is(notNullValue()));
        assertThat(message.getInvalidObjectProperties(),
                contains(new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryInteger")));
    }

    @Test
    public void testValidate_MissingOptional_IntegerNull() {
        MessageList messages = new MessageList();
        IValidationContext context = new ValidationContext();

        MyEnumForMandatoryTests.optionalIntegerAttribute.validate(messages, context, MyEnumForMandatoryTests.allNulls);

        assertThat(messages.isEmpty(), is(true));
    }

    @IpsDocumented(bundleName = "org.faktorips.runtime.model.enumtype.test", defaultLocale = "de")
    @IpsEnumType(name = "SuperAbstractEnumType", attributeNames = { "id", "name", "attributeWithDescription" })
    public interface SuperAbstractEnumType {

        @IpsEnumAttribute(name = "id", identifier = true, unique = true)
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

    private static class MyEnum {
        private final Integer foo;
        private final Map<Locale, String> bar;

        public MyEnum(Integer foo, Map<Locale, String> bar) {
            this.foo = foo;
            this.bar = bar;
        }

        @IpsEnumAttribute(name = "foo", unique = true, identifier = true, mandatory = true)
        public Integer getFoo() {
            return foo;
        }

        @IpsEnumAttribute(name = "bar", displayName = true)
        public String getBar(Locale locale) {
            return bar.get(locale);
        }

        @IpsEnumAttribute(name = "deprecated")
        @Deprecated
        public int getDeprecated() {
            return -1;
        }
    }

    @IpsEnumType(name = "enums.MyEnumForMandatoryTests", attributeNames = { "mandatoryString", "optionalString",
            "mandatoryMultilingualString", "optionalMultilingualString", "mandatoryInteger", "optionalInteger",
            "mandatoryDecimal", "optionalDecimal" })
    @IpsExtensibleEnum(enumContentName = "enums.MyEnumForMandatoryTestsContent")
    public static class MyEnumForMandatoryTests {
        private final String mandatoryString;
        private final String optionalString;
        private final InternationalString mandatoryMultilingualString;
        private final InternationalString optionalMultilingualString;
        private final Integer mandatoryInteger;
        private final Integer optionalInteger;
        private final Decimal mandatoryDecimal;
        private final Decimal optionalDecimal;
        @SuppressWarnings("unused")
        private final IRuntimeRepository productRepository = new InMemoryRuntimeRepository();

        static final EnumType enumType = new EnumType(MyEnumForMandatoryTests.class);
        static final EnumAttribute mandatoryStringAttribute = enumAttribute("mandatoryString");
        static final EnumAttribute optionalStringAttribute = enumAttribute("optionalString");
        static final EnumAttribute mandatoryMultilingualStringAttribute = mlEnumAttribute(
                "mandatoryMultilingualString");
        static final EnumAttribute optionalMultilingualStringAttribute = mlEnumAttribute(
                "optionalMultilingualString");
        static final EnumAttribute mandatoryIntegerAttribute = enumAttribute("mandatoryInteger");
        static final EnumAttribute optionalIntegerAttribute = enumAttribute("optionalInteger");
        static final EnumAttribute mandatoryDecimalAttribute = enumAttribute("mandatoryDecimal");
        static final EnumAttribute optionalDecimalAttribute = enumAttribute("optionalDecimal");

        public static MyEnumForMandatoryTests allNulls = new MyEnumForMandatoryTests(null, null, Map.of(), Map.of(),
                null,
                null, null, null);
        public static MyEnumForMandatoryTests allEmpty = new MyEnumForMandatoryTests("", "", Map.of(Locale.GERMAN, ""),
                Map.of(Locale.GERMAN, ""), null, null, Decimal.NULL, Decimal.NULL);
        public static MyEnumForMandatoryTests oneLocaleEmpty = new MyEnumForMandatoryTests("", "",
                Map.of(Locale.GERMAN, "", Locale.ENGLISH, "foo"),
                Map.of(Locale.GERMAN, "", Locale.ENGLISH, "bar"), null, null, Decimal.NULL, Decimal.NULL);
        public static MyEnumForMandatoryTests allSet = new MyEnumForMandatoryTests("hello", "world",
                Map.of(Locale.GERMAN, "eins", Locale.ENGLISH, "one"),
                Map.of(Locale.GERMAN, "zwei", Locale.ENGLISH, "two"), 3, 4, Decimal.ZERO, Decimal.valueOf(5));

        private static Method method(String name, Class<?>... parameterTypes) {
            try {
                return MyEnumForMandatoryTests.class.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException | SecurityException e) {
                fail();
                return null;
            }
        }

        private static EnumAttribute enumAttribute(String name) {
            return new EnumAttribute(enumType, name, method("get" + IpsStringUtils.toUpperFirstChar(name)));
        }

        private static EnumAttribute mlEnumAttribute(String name) {
            return new EnumAttribute(enumType, name,
                    method("get" + IpsStringUtils.toUpperFirstChar(name), Locale.class));
        }

        private static InternationalString toInternationalString(Map<Locale, String> strings) {
            if (strings == null || strings.isEmpty()) {
                return DefaultInternationalString.EMPTY;
            }
            List<LocalizedString> localizedStrings = strings.entrySet().stream()
                    .map(e -> new LocalizedString(e.getKey(), e.getValue())).toList();
            return new DefaultInternationalString(localizedStrings, Locale.GERMAN);
        }

        public MyEnumForMandatoryTests(String mandatoryString, String optionalString,
                Map<Locale, String> mandatoryMultilingualString, Map<Locale, String> optionalMultilingualString,
                Integer mandatoryInteger, Integer optionalInteger, Decimal mandatoryDecimal, Decimal optionalDecimal) {
            this.mandatoryString = mandatoryString;
            this.optionalString = optionalString;
            this.mandatoryMultilingualString = toInternationalString(mandatoryMultilingualString);
            this.optionalMultilingualString = toInternationalString(optionalMultilingualString);
            this.mandatoryInteger = mandatoryInteger;
            this.optionalInteger = optionalInteger;
            this.mandatoryDecimal = mandatoryDecimal;
            this.optionalDecimal = optionalDecimal;
        }

        @IpsEnumAttribute(name = "mandatoryString", unique = true, identifier = true, mandatory = true)
        public String getMandatoryString() {
            return mandatoryString;
        }

        @IpsEnumAttribute(name = "optionalString")
        public String getOptionalString() {
            return optionalString;
        }

        @IpsEnumAttribute(name = "mandatoryMultilingualString", displayName = true, mandatory = true)
        public String getMandatoryMultilingualString(Locale locale) {
            return mandatoryMultilingualString.get(locale);
        }

        @IpsEnumAttribute(name = "optionalMultilingualString")
        public String getOptionalMultilingualString(Locale locale) {
            return optionalMultilingualString.get(locale);
        }

        @IpsEnumAttribute(name = "mandatoryInteger", mandatory = true)
        public Integer getMandatoryInteger() {
            return mandatoryInteger;
        }

        @IpsEnumAttribute(name = "optionalInteger")
        public Integer getOptionalInteger() {
            return optionalInteger;
        }

        @IpsEnumAttribute(name = "mandatoryDecimal", mandatory = true)
        public Decimal getMandatoryDecimal() {
            return mandatoryDecimal;
        }

        @IpsEnumAttribute(name = "optionalDecimal")
        public Decimal getOptionalDecimal() {
            return optionalDecimal;
        }
    }

}
