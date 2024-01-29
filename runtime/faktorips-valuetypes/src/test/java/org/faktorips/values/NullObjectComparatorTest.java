package org.faktorips.values;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Comparator;
import java.util.Currency;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class NullObjectComparatorTest {

    private static final Matcher<Integer> negative = new TypeSafeMatcher<>() {

        @Override
        public void describeTo(Description description) {
            description.appendText("a negative value");
        }

        @Override
        protected boolean matchesSafely(Integer i) {
            return i < 0;
        }
    };

    private static final Matcher<Integer> positive = new TypeSafeMatcher<>() {

        @Override
        public void describeTo(Description description) {
            description.appendText("a positive value");
        }

        @Override
        protected boolean matchesSafely(Integer i) {
            return i > 0;
        }
    };

    @Test
    public void testCompare_NullsFirst() {
        Comparator<Decimal> nullsFirst = NullObjectComparator.nullsFirst();

        assertThat(nullsFirst.compare(null, null), is(0));
        assertThat(nullsFirst.compare(null, Decimal.NULL), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, null), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, Decimal.NULL), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, Decimal.ZERO), is(negative));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.NULL), is(positive));
        assertThat(nullsFirst.compare(Decimal.valueOf(-1), Decimal.ZERO), is(negative));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.valueOf(-1)), is(positive));
        assertThat(nullsFirst.compare(Decimal.valueOf(1), Decimal.ZERO), is(positive));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.valueOf(1)), is(negative));
    }

    @Test
    public void testCompare_NullsLast() {
        Comparator<Decimal> nullsLast = NullObjectComparator.nullsLast();

        assertThat(nullsLast.compare(null, null), is(0));
        assertThat(nullsLast.compare(null, Decimal.NULL), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, null), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, Decimal.NULL), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, Decimal.ZERO), is(positive));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.NULL), is(negative));
        assertThat(nullsLast.compare(Decimal.valueOf(-1), Decimal.ZERO), is(negative));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.valueOf(-1)), is(positive));
        assertThat(nullsLast.compare(Decimal.valueOf(1), Decimal.ZERO), is(positive));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.valueOf(1)), is(negative));
    }

    @Test
    public void testReversed_NullsFirst() {
        Comparator<Decimal> nullsFirst = NullObjectComparator.nullsFirst();

        nullsFirst = nullsFirst.reversed();

        assertThat(nullsFirst.compare(null, null), is(0));
        assertThat(nullsFirst.compare(null, Decimal.NULL), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, null), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, Decimal.NULL), is(0));
        assertThat(nullsFirst.compare(Decimal.NULL, Decimal.ZERO), is(positive));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.NULL), is(negative));
        assertThat(nullsFirst.compare(Decimal.valueOf(-1), Decimal.ZERO), is(positive));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.valueOf(-1)), is(negative));
        assertThat(nullsFirst.compare(Decimal.valueOf(1), Decimal.ZERO), is(negative));
        assertThat(nullsFirst.compare(Decimal.ZERO, Decimal.valueOf(1)), is(positive));
    }

    @Test
    public void testReversed_NullsLast() {
        Comparator<Decimal> nullsLast = NullObjectComparator.nullsLast();

        nullsLast = nullsLast.reversed();

        assertThat(nullsLast.compare(null, null), is(0));
        assertThat(nullsLast.compare(null, Decimal.NULL), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, null), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, Decimal.NULL), is(0));
        assertThat(nullsLast.compare(Decimal.NULL, Decimal.ZERO), is(negative));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.NULL), is(positive));
        assertThat(nullsLast.compare(Decimal.valueOf(-1), Decimal.ZERO), is(positive));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.valueOf(-1)), is(negative));
        assertThat(nullsLast.compare(Decimal.valueOf(1), Decimal.ZERO), is(negative));
        assertThat(nullsLast.compare(Decimal.ZERO, Decimal.valueOf(1)), is(positive));
    }

    @Test
    public void testThenComparing() {
        Comparator<Money> currencyThenAmount = new NullObjectComparator<>(true,
                Comparator.comparing(Money::getCurrency, Comparator.comparing(Currency::getCurrencyCode)))
                        .thenComparing(Comparator.comparing(Money::getAmount));

        assertThat(currencyThenAmount.compare(null, null), is(0));
        assertThat(currencyThenAmount.compare(null, Money.NULL), is(0));
        assertThat(currencyThenAmount.compare(Money.NULL, null), is(0));
        assertThat(currencyThenAmount.compare(Money.NULL, Money.NULL), is(0));
        assertThat(currencyThenAmount.compare(Money.NULL, Money.euro(0)), is(negative));
        assertThat(currencyThenAmount.compare(Money.euro(0), Money.NULL), is(positive));
        assertThat(currencyThenAmount.compare(Money.euro(0), Money.usd(0)), is(negative));
        assertThat(currencyThenAmount.compare(Money.usd(0), Money.euro(0)), is(positive));
        assertThat(currencyThenAmount.compare(Money.euro(1000), Money.usd(0)), is(negative));
        assertThat(currencyThenAmount.compare(Money.euro(1000), Money.euro(0)), is(positive));
        assertThat(currencyThenAmount.compare(Money.euro(0), Money.euro(1000)), is(negative));
    }

}
