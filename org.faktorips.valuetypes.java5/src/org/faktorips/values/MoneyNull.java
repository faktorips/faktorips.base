/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.values;

/**
 * NullObject for Money.
 * <p>
 * Overrides all money methods with appropriate NullObject behavior, e.g. add() called on a
 * <code>null</code> value always returns an instance of MoneyNull.
 * <p>
 * The class is package private as the null behavior is completely defined in {@link Money}.
 */
class MoneyNull extends Money implements NullObject {

    private static final long serialVersionUID = -3546233368167459967L;
    static final String STRING_REPRESENTATION = "MoneyNull";

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
        return Money.NULL;
    }

    @Override
    public Money min(Money value) {
        return Money.NULL;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money add(Money m) {
        if (m == null) {
            throw new NullPointerException();
        }
        return this;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money subtract(Money m) {
        if (m == null) {
            throw new NullPointerException();
        }
        return this;
    }

    @Override
    public Money multiply(int factor) {
        return this;
    }

    @Override
    public Money multiply(Integer factor) {
        return this;
    }

    @Override
    public Money multiply(long factor) {
        return this;
    }

    @Override
    public Money multiply(Decimal d, int roundingMode) {
        if (d == null) {
            throw new NullPointerException();
        }
        return Money.NULL;
    }

    @Override
    public Money divide(int d, int roundingMode) {
        return this;
    }

    @Override
    public Money divide(long d, int roundingMode) {
        return this;
    }

    @Override
    public Money divide(Decimal d, int roundingMode) {
        if (d == null) {
            throw new NullPointerException();
        }
        return this;
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

    @Override
    public int compareTo(Money other) {
        throw new NullPointerException();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) {
            return false;
        }
        return ((Money)o).isNull();
    }

    @Override
    public String toString() {
        return STRING_REPRESENTATION;
    }

}
