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

package org.faktorips.valueset;

import java.math.BigDecimal;

import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * A range implementation where the upper and lower bounds are of the type
 * <code>org.faktorips.datatype.Money</code>.
 * 
 * @author Jan Ortmann
 * @see org.faktorips.values.Money
 */
public class MoneyRange extends DefaultRange<Money> {

    private static final long serialVersionUID = 4750295893441094927L;

    /**
     * Creates a new MoneyRange with the provided lower and upper bounds parsed using the
     * Money.valueOf(String s) method.
     */
    public final static MoneyRange valueOf(String lower, String upper) {
        return new MoneyRange(Money.valueOf(lower), Money.valueOf(upper));
    }

    /**
     * Creates a new MoneyRange with the provided lower and upper bounds, the step increment and an
     * indicator saying if the null value is contained. The values are determined by parsing the
     * strings using the Money.valueOf(String s) method.
     */
    public final static MoneyRange valueOf(String lower, String upper, String step, boolean containsNull) {
        return new MoneyRange(Money.valueOf(lower), Money.valueOf(upper), Money.valueOf(step), containsNull);
    }

    public final static MoneyRange valueOf(Money lower, Money upper, Money step) {
        return valueOf(lower, upper, step, false);
    }

    public final static MoneyRange valueOf(Money lower, Money upper, Money step, boolean containsNull) {
        MoneyRange range = new MoneyRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates a new MoneyRange with the provided lower bound and upper bound.
     */
    public MoneyRange(Money lowerBound, Money upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new MoneyRange with the provided lower bound, upper bound and step.
     */
    private MoneyRange(Money lowerBound, Money upperBound, Money step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Money value, Money bound) {

        Decimal step = getStep().getAmount();
        Decimal zero = Decimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }

        Decimal diff = bound.subtract(value).getAmount().abs();
        try {
            // throws an ArithmeticException if rounding is necessary. If the value is contained in
            // the range no rounding is necessary since this division must return an integer value
            diff.divide(getStep().getAmount(), 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Money getNextValue(Money currentValue) {
        return currentValue.add(getStep());
    }

    /**
     * Returns true if the value is in the range. The borders are considered to be in the range.
     * Returns false if the passed value is null.
     */
    @Override
    public boolean contains(Money value) {
        if (value == null) {
            return false;
        }
        if (value.isNull()) {
            if (containsNull()) {
                return true;
            }
            return false;
        }
        return super.contains(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        Decimal upperAmount = getUpperBound().getAmount();
        Decimal lowerAmount = getLowerBound().getAmount();
        Decimal stepAmount = getStep().getAmount();

        return upperAmount.subtract(lowerAmount).abs().divide(stepAmount, 0, BigDecimal.ROUND_UNNECESSARY).intValue() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Money getNullValue() {
        return Money.NULL;
    }
}
