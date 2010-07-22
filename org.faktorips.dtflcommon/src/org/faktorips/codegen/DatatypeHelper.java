/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.codegen;

import org.faktorips.codegen.dthelpers.BooleanHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.IntegerHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.codegen.dthelpers.PrimitiveIntegerHelper;
import org.faktorips.codegen.dthelpers.StringHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 * Interface that defines functionality needed to generate Java sourcecode for datatypes.
 */
public interface DatatypeHelper {

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#BOOLEAN Datatype.BOOLEAN}.
     */
    public final static DatatypeHelper BOOLEAN = new BooleanHelper((BooleanDatatype)Datatype.BOOLEAN);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#DECIMAL Datatype.DECIMAL}.
     */
    public final static DatatypeHelper DECIMAL = new DecimalHelper((DecimalDatatype)Datatype.DECIMAL);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#INTEGER Datatype.INTEGER}.
     */
    public final static DatatypeHelper INTEGER = new IntegerHelper((IntegerDatatype)Datatype.INTEGER);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#MONEY Datatype.MONEY}.
     */
    public final static DatatypeHelper MONEY = new MoneyHelper((MoneyDatatype)Datatype.MONEY);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#PRIMITIVE_BOOLEAN
     * Datatype.PRIMITIVE_BOOLEAN}.
     */
    public final static PrimitiveDatatypeHelper PRIMITIVE_BOOLEAN = new PrimitiveBooleanHelper(
            (PrimitiveBooleanDatatype)Datatype.PRIMITIVE_BOOLEAN);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#PRIMITIVE_INT Datatype.PRIMITIVE_INTEGER}.
     */
    public final static PrimitiveDatatypeHelper PRIMITIVE_INTEGER = new PrimitiveIntegerHelper(
            (PrimitiveIntegerDatatype)Datatype.PRIMITIVE_INT);

    /**
     * Helper for {@link org.faktorips.datatype.Datatype#STRING Datatype.STRING}.
     */
    public final static DatatypeHelper STRING = new StringHelper((StringDatatype)Datatype.STRING);

    /**
     * Returns the datatype this is a helper for.
     */
    public Datatype getDatatype();

    /**
     * Sets the datatype this is a helper for. Introduced to enable setter based dependency
     * injection, needed for example for Eclipse's extension point mechanism.
     */
    public void setDatatype(Datatype datatype);

    /**
     * Returns a JavaCodeFragment with sourcecode that is either the String "null" or the sourcecode
     * to get an instance of the apropriate null object.
     */
    public JavaCodeFragment nullExpression();

    /**
     * Returns a JavaCodeFragment with sourcecode that creates an instance of the datatype's Java
     * class with the given value. If the value is null the fragment's sourcecode is either the
     * String "null" or the sourcecode to get an instance of the apropriate null object.
     */
    public JavaCodeFragment newInstance(String value);

    /**
     * Returns a JavaCodeFragment with sourcecode that creates an instance of the datatype's Java
     * class with the given expression. If the expression is null the fragment's sourcecode is
     * either the String "null" or the sourcecode to get an instance of the apropriate null object.
     * When evaluated the expression must return a string
     */
    public JavaCodeFragment newInstanceFromExpression(String expression);

    /**
     * If this is a helper for a mutable data type (like GregorianCalendar for example) this method
     * return the code fragment that creates a copy of the given expression. For immutable data
     * types this method returns the expression unmodified.
     * 
     * @param expression An expression (as Java sourcecode)
     * @return s. above
     */
    public JavaCodeFragment referenceOrSafeCopyIfNeccessary(String expression);

    /**
     * Returns the qualified Java class name the datatype represents.
     */
    public String getJavaClassName();

    /**
     * Returns the qualified Java class name of the range class of the datatype this is a helper
     * for.
     */
    public String getRangeJavaClassName(boolean useTypesafeCollections);

    /**
     * Returns a <code>JavaCodeFragment</code> containing the source code to create a new instance
     * of a type specific range.
     * 
     * @param lowerBoundExp the lower bound expression of the range. Can be <code>null</code> to
     *            indicate that the lower bound is open
     * @param upperBoundExp the upper bound expression of the range. Can be <code>null</code> to
     *            indicate that the upper bound is open.
     * @param stepExp the minimum increment expression for values within the lower and upper bounds.
     *            Can be <code>null</code> to indicate that this is a continuous range.
     * @param containsNullExp the containsNull expression
     * @return the code fragment to create a new range instance. Can be null to indicate that a
     *         range is not supported.
     */
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections);

    /**
     * Returns a <code>JavaCodeFragment</code> containing the source code to create a new enum value
     * set instance.
     * 
     * @param values the values contained in the enum value set code fragment returned by this
     *            method
     * @param containsNull indicates if <code>null</code> is also contained in the returned enum
     *            value set
     * @param useTypesafeCollections indicates if Java 5 typesafe collections and valuetypes shall
     *            be used
     */
    public JavaCodeFragment newEnumValueSetInstance(String[] values,
            boolean containsNull,
            boolean useTypesafeCollections);

    /**
     * Returns a <code>JavaCodeFragment</code> containing the source code to create a new enum value
     * set instance.
     * 
     * @param collectionExpression a JavaCodeFragment is expected that contains an expression of the
     *            type <code>java.util.Collection</code> The collection has to contain instances of
     *            the datatype of this helper.
     * 
     * @param containsNullExpression a JavaCodeFragment is expected that contains an expression of
     *            the type <code>boolean</code>
     * @param useTypesafeCollections indicates if Java 5 typesafe collections and valuetypes shall
     *            be used
     */
    public JavaCodeFragment newEnumValueSetInstance(JavaCodeFragment collectionExpression,
            JavaCodeFragment containsNullExpression,
            boolean useTypesafeCollections);
}
