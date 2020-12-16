/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import java.math.BigDecimal;

import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * The type of data similar to a Java class. In most cases a datatype corresponds directly to a Java
 * class, e.g. a datatype that represents text data (String) corresponds to the class
 * <code>java.lang.String</code>. However a datatype can exists while the corresponding Java class
 * does not exists (yet because it's being generated at a later point in time).
 * <p>
 * If the datatype represents a value, you can safely cast the datatype to
 * <code>ValueDatatype</code>. The value datatype provides a uniform way to transform values into
 * Strings and parse Strings back into values.
 * 
 * @author Jan Ortmann
 */
public interface Datatype extends Comparable<Datatype> {

    /**
     * Datatype Void.
     */
    public static final Void VOID = new Void();

    /**
     * Datatype representing {@link java.lang.Boolean}.
     */
    public static final BooleanDatatype BOOLEAN = new BooleanDatatype();

    /**
     * Datatype representing {@link Decimal}.
     */
    public static final DecimalDatatype DECIMAL = new DecimalDatatype();

    /**
     * Datatype representing {@link BigDecimal}.
     */
    public static final BigDecimalDatatype BIG_DECIMAL = new BigDecimalDatatype();

    /**
     * Datatype representing {@link java.lang.Double}.
     */
    public static final DoubleDatatype DOUBLE = new DoubleDatatype();

    /**
     * Datatype representing {@link Integer}.
     */
    public static final IntegerDatatype INTEGER = new IntegerDatatype();

    /**
     * Datatype representing {@link Long}.
     */
    public static final LongDatatype LONG = new LongDatatype();

    /**
     * Datatype representing {@link Money}.
     */
    public static final MoneyDatatype MONEY = new MoneyDatatype();

    /**
     * Datatype representing the primitive <code>boolean</code>.
     */
    public static final PrimitiveBooleanDatatype PRIMITIVE_BOOLEAN = new PrimitiveBooleanDatatype();

    /**
     * Datatype representing the primitive <code>int</code>.
     */
    public static final PrimitiveIntegerDatatype PRIMITIVE_INT = new PrimitiveIntegerDatatype();

    /**
     * Datatype representing the primitive <code>long</code>.
     */
    public static final PrimitiveLongDatatype PRIMITIVE_LONG = new PrimitiveLongDatatype();

    /**
     * Datatype representing <code>java.lang.String</code>
     */
    public static final StringDatatype STRING = new StringDatatype();

    /**
     * Datatype representing <code>java.util.GregorianCalendar</code>. Note that in Faktor-IPS
     * values of that datatype only contain the information about the date, not the time.
     */
    public static final GregorianCalendarAsDateDatatype GREGORIAN_CALENDAR = new GregorianCalendarAsDateDatatype();

    /**
     * Returns the datatype's name.
     */
    public String getName();

    /**
     * Returns the datatype's qualified name.
     * <p>
     * The qualified name identifies the datatype.
     */
    public String getQualifiedName();

    /**
     * Returns <code>true</code> if this is the Datatype Void, otherwise <code>false</code>.
     */
    public boolean isVoid();

    /**
     * Returns <code>true</code> if this datatype represents one Java's primitive types.
     */
    public boolean isPrimitive();

    /**
     * Returns <code>true</code> if this datatype is an abstract datatype in means of the object
     * oriented paradigm.
     */
    public boolean isAbstract();

    /**
     * Returns <code>true</code> if this datatype represents values.
     * <p>
     * If the method returns true, the datatype can be safely casted to <code>ValueDatatype</code>.
     */
    public boolean isValueDatatype();

    /**
     * Returns <code>true</code> if this value datatype is an enum datatype. In this case the
     * instance be be casted to {@link EnumDatatype}. Returns <code>false</code> otherwise.
     */
    public boolean isEnum();

    /**
     * Returns <code>true</code> if the datatype has a special instance representing
     * <code>null</code>, otherwise <code>false</code>. The design pattern is called the
     * <em>NullObject</em> pattern.
     */
    public boolean hasNullObject();

}
