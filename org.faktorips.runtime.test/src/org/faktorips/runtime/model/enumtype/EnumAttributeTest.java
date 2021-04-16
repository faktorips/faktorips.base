package org.faktorips.runtime.model.enumtype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumAttributeTest {

    @Mock
    private EnumType enumType;

    private EnumAttribute fooModel;

    private EnumAttribute barModel;

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
    }

}
