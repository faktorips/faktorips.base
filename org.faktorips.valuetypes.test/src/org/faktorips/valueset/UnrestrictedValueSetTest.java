package org.faktorips.valueset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class UnrestrictedValueSetTest {

    @Test
    public void testIsUnrestricted_WithoutNull_includeNull() {
        UnrestrictedValueSet<String> emptyWithoutNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithoutNull_excludeNull() {
        UnrestrictedValueSet<String> emptyWithoutNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithoutNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_includeNull() {
        UnrestrictedValueSet<String> emptyWithNull = new UnrestrictedValueSet<String>(true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithNull_excludeNull() {
        UnrestrictedValueSet<String> emptyWithNull = new UnrestrictedValueSet<String>(false);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }
}
