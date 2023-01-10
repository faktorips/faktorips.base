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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.type.Deprecation;
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

        @IpsEnumAttribute(name = "foo", unique = true, identifier = true)
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

}
