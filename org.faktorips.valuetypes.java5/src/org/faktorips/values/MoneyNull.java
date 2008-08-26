/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

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

    MoneyNull() {
        super(0, null);
    }

    /**
     * Returns Decimal.NULL.
     */
    @Override
    public Decimal getAmount() {
        return Decimal.NULL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNull() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNotNull() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Money max(Money value){
        return Money.NULL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Money min(Money value){
        return Money.NULL;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money add(Money m) {
        if (m==null) {
            throw new NullPointerException();
        }
        return this;
    }

    /**
     * Returns the special case MONEY.NULL.
     */
    @Override
    public Money subtract(Money m) {
        if (m==null) {
            throw new NullPointerException();
        }
        return this;
    }

    /**
     * Overridden method.
     * @see org.faktorips.values.Money#multiply(int)
     */
    @Override
    public Money multiply(int factor) {
        return this;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#multiply(java.lang.Integer)
     */
    @Override
    public Money multiply(Integer factor) {
        return this;
    }

    /**
     * Overridden method.
     * @see org.faktorips.values.Money#multiply(long)
     */
    @Override
    public Money multiply(long factor) {
        return this;
    }

    /**
     * Overridden method.
     * @see org.faktorips.values.Money#multiply(org.openips.datatype.Decimal, int)
     */
    @Override
    public Money multiply(Decimal d, int roundingMode) {
        if (d==null) {
            throw new NullPointerException();
        }
        return Money.NULL;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#divide(int, int)
     */
    @Override
    public Money divide(int d, int roundingMode) {
        return this;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#divide(long, int)
     */
    @Override
    public Money divide(long d, int roundingMode) {
        return this;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#divide(org.faktorips.values.Decimal, int)
     */
    @Override
    public Money divide(Decimal d, int roundingMode) {
        if (d==null) {
            throw new NullPointerException();
        }
        return this;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#greaterThan(org.faktorips.values.Money)
     */
    @Override
    public boolean greaterThan(Money other) {
        if (other==null) {
            throw new NullPointerException();
        }
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#greaterThanOrEqual(org.faktorips.values.Money)
     */
    @Override
    public boolean greaterThanOrEqual(Money other) {
        if (other==null) {
            throw new NullPointerException();
        }
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#lessThan(org.faktorips.values.Money)
     */
    @Override
    public boolean lessThan(Money other) {
        if (other==null) {
            throw new NullPointerException();
        }
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#lessThanOrEqual(org.faktorips.values.Money)
     */
    @Override
    public boolean lessThanOrEqual(Money other) {
        if (other==null) {
            throw new NullPointerException();
        }
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.values.Money#compareTo(org.faktorips.values.Money)
     */
    @Override
    public int compareTo(Money other) {
        throw new NullPointerException();
    }

    /**
     * Overridden Method.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) {
            return false;
        }
        return ((Money)o).isNull();
    }


    @Override
    public String toString() {
        return "MoneyNull";
    }


}
