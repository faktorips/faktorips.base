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

import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
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
        assertThat(fooAttributes.get(1).getName(), is(equalTo("z")));
        assertThat(fooAttributes.get(2).getName(), is(equalTo("y")));
        assertThat(new EnumType(Bar.class).getAttributes().size(), is(0));
    }

    @Test
    public void testGetAttribute() {
        assertThat(new EnumType(Foo.class).getAttribute("x").getName(), is(equalTo("x")));
    }

    @Test
    public void testGetAttributenames() {
        List<String> fooAttributes = new EnumType(Foo.class).getAttributenames();
        assertThat(fooAttributes.size(), is(3));
        assertThat(fooAttributes.get(0), is(equalTo("x")));
        assertThat(fooAttributes.get(1), is(equalTo("z")));
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

    @IpsEnumType(name = "my.foo", attributeNames = { "x", "z", "y" })
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
    @IpsEnumType(name = "my.bar", attributeNames = {})
    private static class Bar {

    }

    @IpsExtensibleEnum(enumContentName = "deprecated.content")
    @IpsEnumType(name = "deprecated.structure", attributeNames = {})
    @Deprecated
    private static class DeprecatedEnum {

    }

}
