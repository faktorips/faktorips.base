/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.datatype.classtypes.*;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * The type of data similar to a Java class. In most cases a datatype corresponds
 * directly to a Java class, e.g. a datatype that represents text data (String)
 * corresponds to the class <code>java.lang.String</code>. However a datatype can
 * exists while the corresponding Java class does not exists (yet because it's
 * being generated at a later point in time). 
 * <p>
 * If the datatype represents a value, you can safely cast the datatype to
 * <code>ValueDatatype</code>. The value datatype provides a uniform way
 * to transform values into Strings and parse Strings back into values.
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
     * Datatype representing <code>java.lang.String</code>
     */
    public final static ValueDatatype STRING = new StringDatatype();
    
    /**
     * Datatype representing <code>java.util.GregorianCalendar</code> with
     * only the date information (year, month, date) used.
     */
    public final static ValueDatatype GREGORIAN_CALENDAR_DATE = new GregorianCalendarDatatype("Date", false);

    
    /**
     * Returns the datatype's name.
     * 
     * @return datatype's name.
     */
    public abstract String getName();
    
    /**
     * Returns the datatype's qualified name. The qualified name
     * identifies the datatype.
     * 
     * @return datatype's qualified name.
     */
    public abstract String getQualifiedName();
    
    /**
     * Returns true if this is the Datatype Void, otherwise false.
     */
    public abstract boolean isVoid();
    
    /**
     * Returns true if this datatype represents one Java's primitive types.
     */
    public abstract boolean isPrimitive();
    
    /**
     * Returns true if this datatype represents values. If the method
     * returns true, the datatype can be safely casted to <code>ValueDatatype</code>.
     */
    public abstract boolean isValueDatatype();
    
    /**
     * Returns the qualified Java class name that values of this datatype are instances of. 
     */
    public abstract String getJavaClassName();
    
    /**
     * Validates the datatype and returns a message list containing appropriate error messages.
     * If the datatype is valid and empty list is returned.
     * <p>
     * Datatypes like the predefined datatypes (defined by the constants in this class) are 
     * always valid. However new datatypes can be defined as part of a model and these datatypes
     * might be invalid.
     * 
     * @throws Exception if an error occurs while validating the datatype.
     */
    public MessageList validate() throws Exception;
    
}
