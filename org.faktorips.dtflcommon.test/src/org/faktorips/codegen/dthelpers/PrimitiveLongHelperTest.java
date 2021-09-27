package org.faktorips.codegen.dthelpers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Before;
import org.junit.Test;

public class PrimitiveLongHelperTest {

    private PrimitiveLongHelper helper;

    @Before
    public void setUp() {
        helper = new PrimitiveLongHelper();
    }

    @Test
    public void testLongValueNeedsLIfLargerThanInt() {
        assertThat(helper.newInstance("1"), is(new JavaCodeFragment("1")));
        assertThat(helper.newInstance("123"), is(new JavaCodeFragment("123")));
        assertThat(helper.newInstance("2147483647"), is(new JavaCodeFragment("2147483647")));
        assertThat(helper.newInstance("2147483648"), is(new JavaCodeFragment("2147483648L")));
        assertThat(helper.newInstance("-2147483648"), is(new JavaCodeFragment("-2147483648")));
        assertThat(helper.newInstance("-2147483649"), is(new JavaCodeFragment("-2147483649L")));
        assertThat(helper.newInstance("+2147483649"), is(new JavaCodeFragment("+2147483649L")));
    }

    @Test
    public void testLongValuesNeedsLEdgeCases() {
        assertThat(helper.newInstance(null), is(new JavaCodeFragment((String)null)));
        assertThat(helper.newInstance(""), is(new JavaCodeFragment("")));
        assertThat(helper.newInstance(" "), is(new JavaCodeFragment(" ")));
        assertThat(helper.newInstance("A"), is(new JavaCodeFragment("A")));
        assertThat(helper.newInstance("AB"), is(new JavaCodeFragment("AB")));
        // _ not allowed in Long#parseLong
        assertThat(helper.newInstance("60_000_000_000"), is(new JavaCodeFragment("60_000_000_000")));
    }
}
