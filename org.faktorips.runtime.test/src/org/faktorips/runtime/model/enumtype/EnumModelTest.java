package org.faktorips.runtime.model.enumtype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.faktorips.runtime.model.annotation.IpsEnum;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.junit.Test;

public class EnumModelTest {

    @Test
    public void testIsExtensible() {
        assertThat(new EnumModel(Foo.class).isExtensible(), is(false));
        assertThat(new EnumModel(Bar.class).isExtensible(), is(true));
    }

    @Test
    public void testGetEnumContentQualifiedName() {
        assertThat(new EnumModel(Foo.class).getEnumContentQualifiedName(), is(nullValue()));
        assertThat(new EnumModel(Bar.class).getEnumContentQualifiedName(), is(equalTo("my.baz")));
    }

    @Test
    public void testGetName() {
        assertThat(new EnumModel(Foo.class).getName(), is(equalTo("my.foo")));
        assertThat(new EnumModel(Bar.class).getName(), is(equalTo("my.bar")));
    }

    @Test
    public void testGetAttributes() {
        List<EnumAttributeModel> fooAttributes = new EnumModel(Foo.class).getAttributes();
        assertThat(fooAttributes.size(), is(3));
        assertThat(fooAttributes.get(0).getName(), is(equalTo("x")));
        assertThat(fooAttributes.get(1).getName(), is(equalTo("z")));
        assertThat(fooAttributes.get(2).getName(), is(equalTo("y")));
        assertThat(new EnumModel(Bar.class).getAttributes().size(), is(0));
    }

    @Test
    public void testGetAttribute() {
        assertThat(new EnumModel(Foo.class).getAttribute("x").getName(), is(equalTo("x")));
    }

    @Test
    public void testGetAttributenames() {
        List<String> fooAttributes = new EnumModel(Foo.class).getAttributenames();
        assertThat(fooAttributes.size(), is(3));
        assertThat(fooAttributes.get(0), is(equalTo("x")));
        assertThat(fooAttributes.get(1), is(equalTo("z")));
        assertThat(fooAttributes.get(2), is(equalTo("y")));
    }

    @Test
    public void testGetIdAttribute() {
        assertThat(new EnumModel(Foo.class).getIdAttribute().getName(), is(equalTo("x")));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIdAttribute_noneFound() {
        new EnumModel(Bar.class).getIdAttribute().getName();
    }

    @Test
    public void testGetDisplayNameAttribute() {
        assertThat(new EnumModel(Foo.class).getDisplayNameAttribute().getName(), is(equalTo("y")));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDisplayNameAttribute_noneFound() {
        new EnumModel(Bar.class).getDisplayNameAttribute().getName();
    }

    @IpsEnum(name = "my.foo", attributeNames = { "x", "z", "y" })
    private static class Foo {

        private Integer x;
        private String y;
        private Boolean z;

        @IpsEnumAttribute(name = "x", identifier = true, unique = true)
        public Integer getX() {
            return x;
        }

        @IpsEnumAttribute(name = "y", displayName = true)
        public String getY() {
            return y;
        }

        @IpsEnumAttribute(name = "z")
        public Boolean getZ() {
            return z;
        }

    }

    @IpsExtensibleEnum(enumContentName = "my.baz")
    @IpsEnum(name = "my.bar", attributeNames = {})
    private static class Bar {

    }

}
