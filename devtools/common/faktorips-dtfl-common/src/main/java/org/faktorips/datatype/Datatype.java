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
import org.faktorips.values.NullObject;

/**
 * The type of data similar to a Java class. In most cases a datatype corresponds directly to a Java
 * class, e.g. a datatype that represents text data (String) corresponds to the class
 * {@code java.lang.String}. However a datatype can exist while the corresponding Java class does
 * not exist (yet, because it's being generated at a later point in time).
 * <p>
 * If the datatype represents a value, you can safely cast the datatype to {@code ValueDatatype}.
 * The value datatype provides a uniform way to transform values into Strings and parse Strings back
 * into values.
 * 
 * @author Jan Ortmann
 */
public interface Datatype extends Comparable<Datatype> {

    /**
     * Datatype representing {@link java.lang.Void}.
     */
    Void VOID = new Void();

    /**
     * Datatype representing {@link java.lang.Boolean}.
     */
    BooleanDatatype BOOLEAN = new BooleanDatatype();

    /**
     * Datatype representing {@link Decimal}.
     */
    DecimalDatatype DECIMAL = new DecimalDatatype();

    /**
     * Datatype representing {@link BigDecimal}.
     */
    BigDecimalDatatype BIG_DECIMAL = new BigDecimalDatatype();

    /**
     * Datatype representing {@link java.lang.Double}.
     */
    DoubleDatatype DOUBLE = new DoubleDatatype();

    /**
     * Datatype representing {@link Integer}.
     */
    IntegerDatatype INTEGER = new IntegerDatatype();

    /**
     * Datatype representing {@link Long}.
     */
    LongDatatype LONG = new LongDatatype();

    /**
     * Datatype representing {@link Money}.
     */
    MoneyDatatype MONEY = new MoneyDatatype();

    /**
     * Datatype representing the primitive {@code boolean}.
     */
    PrimitiveBooleanDatatype PRIMITIVE_BOOLEAN = new PrimitiveBooleanDatatype();

    /**
     * Datatype representing the primitive {@code int}.
     */
    PrimitiveIntegerDatatype PRIMITIVE_INT = new PrimitiveIntegerDatatype();

    /**
     * Datatype representing the primitive {@code long}.
     */
    PrimitiveLongDatatype PRIMITIVE_LONG = new PrimitiveLongDatatype();

    /**
     * Datatype representing {@code java.lang.String}
     */
    StringDatatype STRING = new StringDatatype();

    /**
     * Datatype representing {@link java.util.GregorianCalendar}. Note that in Faktor-IPS values of
     * that datatype only contain the information about the date, not the time.
     */
    GregorianCalendarAsDateDatatype GREGORIAN_CALENDAR = new GregorianCalendarAsDateDatatype();

    /**
     * Returns the datatype's name.
     */
    String getName();

    /**
     * Returns the datatype's qualified name.
     * <p>
     * The qualified name identifies the datatype.
     */
    String getQualifiedName();

    /**
     * Returns {@code true} if this is the Datatype {@link Void}, otherwise {@code false}.
     */
    boolean isVoid();

    /**
     * Returns {@code true} if this datatype represents one of Java's primitive types.
     */
    boolean isPrimitive();

    /**
     * Returns {@code true} if this datatype is an abstract datatype in means of the object oriented
     * paradigm.
     */
    boolean isAbstract();

    /**
     * Returns {@code true} if this datatype represents values.
     * <p>
     * If the method returns {@code true}, the datatype can be safely casted to
     * {@code ValueDatatype}.
     */
    boolean isValueDatatype();

    /**
     * Returns {@code true} if this value datatype is an enum datatype. In this case the instance
     * can be cast to {@link EnumDatatype}. Returns {@code false} otherwise.
     */
    boolean isEnum();

    /**
     * Returns {@code true} if the datatype has a special instance representing {@code null},
     * otherwise {@code false}. The design pattern is called the null-object pattern.
     *
     * @see NullObject
     */
    boolean hasNullObject();

}
