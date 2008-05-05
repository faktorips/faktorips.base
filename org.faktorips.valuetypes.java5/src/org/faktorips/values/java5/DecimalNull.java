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

package org.faktorips.values.java5;

import java.math.BigDecimal;

import org.faktorips.values.NullObject;

/**
 * NullObject for Decimal. 
 * <p>
 * Overrides all Decimal methods with appropriate NullObject behaviour, e.g.
 * add() called on a null value always returns an instance of DecimalNull.
 * <p>
 * The class is package private as the null behaviour is completly defined
 * in {@link Decimal}.   
 */
public class DecimalNull extends Decimal implements NullObject {

	private static final long serialVersionUID = -2216601951904037094L;

	DecimalNull() {
        super(null);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isNull() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isNotNull() {
        return false;
    }

    /**
     * @throws NullPointerException
     */
    public int scale() {
        throw newNullPointerException();
    }
    
    /**
     * @throws NullPointerException
     */
    public int signum() {
        throw newNullPointerException();
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal add(Decimal d) {
        return this;
    }
    
    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal add(Integer i) {
        return this;
    }
    
    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal add(int i) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Decimal max(Decimal value){
        return Decimal.NULL;
    }

    /**
     * {@inheritDoc}
     */
    public Decimal min(Decimal value){
        return Decimal.NULL;
    }
    
    /**
     * Returns the special case DECIMAL.NULL.
     */
	public Decimal subtract(Decimal d) {
	    return this;
	}
    
    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal subtract(int i) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
	public Decimal multiply(Decimal d) {
	    return this;
	}
    
    /**
     * Returns the special case Money.NULL.
     */
    public Money multiply(Money m, int roundingMode) {
        return Money.NULL;
    }
    
	
    /**
     * Returns the special case DECIMAL.NULL.
     */
	public Decimal multiply(Integer i) {
	    return this;
	}
	
    /**
     * Returns the special case DECIMAL.NULL.
     */
	public Decimal multiply(int i) {
	    return Decimal.NULL;
	}
	
    /**
     * Returns the special case DECIMAL.NULL.
     */
	public Decimal multiply(long l) {
	    return this;
	}
	
    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal divide(Decimal d, int scale, int roundingMode) {
        return this;
    }

    /**
     * Returns the special case DECIMAL.NULL.
     */
    public Decimal divide(int value, int scale, int roundingMode) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Decimal divide(long value, int scale, int roundingMode) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Decimal setScale(int scale, int roundingMode) {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public Decimal round(int precision, int roundingMode) {
        return this;
    }

    /**
     * @throws NullPointerException
     */
    public double doubleValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    public float floatValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    public int intValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    public long longValue() {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public BigDecimal bigDecimalValue() {
        return null;
    }

    /**
     * @throws NullPointerException
     */
    public byte byteValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    public short shortValue() {
        throw newNullPointerException();
    }

    /**
     * @throws NullPointerException
     */
    public int compareTo(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equalsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean greaterThan(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean greaterThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean lessThan(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean lessThanOrEqual(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean notEqualsIgnoreScale(Decimal d) {
        throw newNullPointerException();
    }

    /**
     * {@inheritDoc}
     */
    public Decimal abs() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Decimal negate() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
	public int hashCode() {
	    return 0;
	}
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "DecimalNull";
    }
    
    /**
     * Returns <code>true</code> if the given object is also Decimal.NULL,
     * otherwise false.
     */
    public boolean equals(Object o) {
        if (!(o instanceof Decimal)) {
            return false;
        }
        return ((Decimal)o).isNull();
    }
    
    private NullPointerException newNullPointerException() throws NullPointerException {
        return new NullPointerException("Method not applicable to null.");
    }
}
