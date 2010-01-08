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

package org.faktorips.datatype;

import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
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
public interface Datatype extends Comparable {

    /**
     * Datatype Void.
     */
    public final static Datatype VOID = new Void();

    /**
     * Datatype representing {@link java.lang.Boolean}.
     */
    public final static ValueDatatype BOOLEAN = new BooleanDatatype();

    /**
     * Datatype representing {@link Decimal}.
     */
    public final static ValueDatatype DECIMAL = new DecimalDatatype();

    /**
     * Datatype representing {@link java.lang.Double}.
     */
    public static final ValueDatatype DOUBLE = new DoubleDatatype();

    /**
     * Datatype representing {@link Integer}.
     */
    public final static ValueDatatype INTEGER = new IntegerDatatype();

    /**
     * Datatype representing {@link Long}.
     */
    public final static ValueDatatype LONG = new LongDatatype();

    /**
     * Datatype representing {@link Money}.
     */
    public final static ValueDatatype MONEY = new MoneyDatatype();

    /**
     * Datatype representing the primitive <code>boolean</code>.
     */
    public final static ValueDatatype PRIMITIVE_BOOLEAN = new PrimitiveBooleanDatatype();

    /**
     * Datatype representing the primitive <code>int</code>.
     */
    public final static ValueDatatype PRIMITIVE_INT = new PrimitiveIntegerDatatype();

    /**
     * Datatype representing the primitive <code>long</code>.
     */
    public final static ValueDatatype PRIMITIVE_LONG = new PrimitiveLongDatatype();

    /**
     * Datatype representing <code>java.lang.String</code>
     */
    public final static ValueDatatype STRING = new StringDatatype();

    /**
     * Datatype representing <code>java.util.GregorianCalendar</code>. Note that in Faktor-IPS
     * values of that datatype only contain the information about the date, not the time.
     * <p>
     * We have to solve the problem, that conceptually we have more like three datatypes for date,
     * time and datetime, but in Java it is messed up with the gregorian calendar.
     */
    public final static ValueDatatype GREGORIAN_CALENDAR = new GregorianCalendarAsDateDatatype();

    /**
     * Datatype representing <code>java.util.GregorianCalendar</code> with only the date information
     * (year, month, date) used.
     * 
     * @see GregorianCalendarDatatype
     */
    @Deprecated
    public final static ValueDatatype GREGORIAN_CALENDAR_DATE = new GregorianCalendarDatatype("Date", false);

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
     * Returns the qualified Java class name that values of this datatype are instances of.
     */
    public String getJavaClassName();

    /**
     * Returns <code>true</code> if the datatype has a special instance representing
     * <code>null</code>, otherwise <code>false</code>. The design pattern is called the
     * <em>NullObject</em> pattern.
     */
    public boolean hasNullObject();

}
