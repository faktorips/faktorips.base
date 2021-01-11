/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.math.RoundingMode;

/**
 * NullObject for Money.
 * <p>
 * Overrides all money methods with appropriate NullObject behaviour, e.g. add() called on a
 * <code>null</code> value always returns an instance of MoneyNull.
 */
public class MoneyNull extends Money implements NullObject {

    static final String STRING_REPRESENTATION = "MoneyNull";

    private static final long serialVersionUID = -3546233368167459967L;

    MoneyNull() {
        super(0, null);
    }

    @Override
    public Decimal getAmount() {
        return Decimal.NULL;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isNotNull() {
        return false;
    }

    @Override
    public Money max(Money value) {
        return NULL;
    }

    @Override
    public Money min(Money value) {
        return NULL;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money add(Money m) {
        if (m == null) {
            throw new NullPointerException();
        }
        return NULL;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money subtract(Money m) {
        if (m == null) {
            throw new NullPointerException();
        }
        return NULL;
    }

    @Override
    public Money multiply(int factor) {
        return NULL;
    }

    @Override
    public Money multiply(Integer factor) {
        return NULL;
    }

    @Override
    public Money multiply(long factor) {
        return NULL;
    }

    @Override
    public Money multiply(Decimal d, RoundingMode roundingMode) {
        if (d == null) {
            throw new NullPointerException();
        }
        return Money.NULL;
    }

    /**
     * @deprecated since 21.6.
     */
    @Override
    @Deprecated
    public Money multiply(Decimal d, int roundingMode) {
        return multiply(d, RoundingMode.valueOf(roundingMode));
    }

    @Override
    public Money divide(int d, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * @deprecated since 21.6.
     */
    @Override
    @Deprecated
    public Money divide(int d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    @Override
    public Money divide(long d, RoundingMode roundingMode) {
        return NULL;
    }

    /**
     * @deprecated since 21.6.
     */
    @Override
    @Deprecated
    public Money divide(long d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    @Override
    public Money divide(Decimal d, RoundingMode roundingMode) {
        if (d == null) {
            throw new NullPointerException();
        }
        return NULL;
    }

    /**
     * @deprecated since 21.6.
     */
    @Override
    @Deprecated
    public Money divide(Decimal d, int roundingMode) {
        return divide(d, RoundingMode.valueOf(roundingMode));
    }

    @Override
    public boolean greaterThan(Money other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return false;
    }

    @Override
    public boolean greaterThanOrEqual(Money other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return false;
    }

    @Override
    public boolean lessThan(Money other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return false;
    }

    @Override
    public boolean lessThanOrEqual(Money other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return false;
    }

    /**
     * Returns 0 if compared to Money.NULL. Returns -1 when compared to any other money value
     * (behavior analogous to ObjectUtils#compare()). Throws a {@link NullPointerException} if
     * compared to <code>null</code>.
     */
    @Override
    public int compareTo(Money other) {
        if (other == null) {
            throw new NullPointerException();
        } else if (other.isNull()) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return STRING_REPRESENTATION;
    }

}
