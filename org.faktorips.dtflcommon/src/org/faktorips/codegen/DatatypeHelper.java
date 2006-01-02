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
 * Interface that defines functionality needed to generate Java sourcecode for
 * datatypes.
 */
public interface DatatypeHelper {

	/**
	 * Helper for
	 * {@link org.faktorips.datatype.Datatype#BOOLEAN Datatype.BOOLEAN}.
	 */
	public final static DatatypeHelper BOOLEAN = new BooleanHelper(
			(BooleanDatatype) Datatype.BOOLEAN);

	/**
	 * Helper for
	 * {@link org.faktorips.datatype.Datatype#DECIMAL Datatype.DECIMAL}.
	 */
	public final static DatatypeHelper DECIMAL = new DecimalHelper(
			(DecimalDatatype) Datatype.DECIMAL);

	/**
	 * Helper for
	 * {@link org.faktorips.datatype.Datatype#INTEGER Datatype.INTEGER}.
	 */
	public final static DatatypeHelper INTEGER = new IntegerHelper(
			(IntegerDatatype) Datatype.INTEGER);

	/**
	 * Helper for {@link org.faktorips.datatype.Datatype#MONEY Datatype.MONEY}.
	 */
	public final static DatatypeHelper MONEY = new MoneyHelper(
			(MoneyDatatype) Datatype.MONEY);

	/**
	 * Helper for
	 * {@link org.faktorips.datatype.Datatype#PRIMITIVE_BOOLEAN Datatype.PRIMITIVE_BOOLEAN}.
	 */
	public final static PrimitiveDatatypeHelper PRIMITIVE_BOOLEAN = new PrimitiveBooleanHelper(
			(PrimitiveBooleanDatatype) Datatype.PRIMITIVE_BOOLEAN);

	/**
	 * Helper for
	 * {@link org.faktorips.datatype.Datatype#PRIMITIVE_INTEGER Datatype.PRIMITIVE_INTEGER}.
	 */
	public final static PrimitiveDatatypeHelper PRIMITIVE_INTEGER = new PrimitiveIntegerHelper(
			(PrimitiveIntegerDatatype) Datatype.PRIMITIVE_INT);

	/**
	 * Helper for {@link org.faktorips.datatype.Datatype#STRING Datatype.STRING}.
	 */
	public final static DatatypeHelper STRING = new StringHelper(
			(StringDatatype) Datatype.STRING);

	/**
	 * Returns the datatype this is a helper for.
	 */
	public Datatype getDatatype();
	
	/**
	 * Sets the datatype this is a helper for. Introduced to enable setter based
	 * dependency injection, needed for example for Eclipse's extension point
	 * mechanism.
	 */
	public void setDatatype(Datatype datatype);

	/**
	 * Returns a JavaCodeFragment with sourcecode that is either the String
	 * "null" or the sourcecode to get an instance of the apropriate null
	 * object.
	 */
	public JavaCodeFragment nullExpression();

	/**
	 * Returns a JavaCodeFragment with sourcecode that creates an instance of
	 * the datatype's Java class with the given value. If the value is null the
	 * fragment's sourcecode is either the String "null" or the sourcecode to
	 * get an instance of the apropriate null object.
	 */
	public JavaCodeFragment newInstance(String value);

	/**
	 * Returns a JavaCodeFragment with sourcecode that creates an instance of
	 * the datatype's Java class with the given expression. If the expression is
	 * null the fragment's sourcecode is either the String "null" or the
	 * sourcecode to get an instance of the apropriate null object. When
	 * evaluated the expression must return a string
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression);
	
    /**
     * Returns the qualified Java class name of the range class of the datatype this is a helper for. 
     */
    public abstract String getRangeJavaClassName();    	

}
