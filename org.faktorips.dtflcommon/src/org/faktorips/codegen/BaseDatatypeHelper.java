/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.datatype.Datatype;

/**
 * Interface that defines functionality needed to generate source code for data types.
 */
// Should be renamed to DatatypeHelper, but that name is already taken and can't be changed without
// breaking API
public interface BaseDatatypeHelper<T extends CodeFragment> {

    /**
     * Returns the data type this is a helper for.
     */
    public Datatype getDatatype();

    /**
     * Sets the data type this is a helper for. Introduced to enable setter based dependency
     * injection, needed for example for Eclipse's extension point mechanism.
     */
    public void setDatatype(Datatype datatype);

    /**
     * Returns a CodeFragment with source code that is either the String "null" or the source code
     * to get an instance of the appropriate null object.
     */
    public T nullExpression();

    /**
     * Returns a CodeFragment with source code that creates an instance of the data type with the
     * given value. If the value is null the fragment's source code is either the String "null" or
     * the source code to get an instance of the appropriate null object.
     */
    public T newInstance(String value);

    /**
     * Returns a CodeFragment with source code that creates an instance of the data type with the
     * given expression. If the expression is null the fragment's source code is either the String
     * "null" or the source code to get an instance of the appropriate null object. When evaluated
     * the expression must return a string.
     * 
     * @param expression A source code expression that yields a String. Examples are a constant
     *            String like <code>"FOO"</code>, a variable like <code>foo</code> or a method call
     *            like <code>getÍd()</code>.
     */
    public T newInstanceFromExpression(String expression);

    /**
     * Returns a CodeFragment with source code that creates an instance of the data type with the
     * given expression. If the expression is null the fragment's source code is either the String
     * "null" or the source code to get an instance of the appropriate null object. When evaluated
     * the expression must return a string
     * 
     * @param expression A source code expression that yields a String. Examples are a constant
     *            String like <code>"FOO"</code>, a variable like <code>foo</code> or a method call
     *            like <code>getÍd()</code>.
     * @param checkForNull <code>true</code> if this helper has to assume that the given expression
     *            can yield <code>null</code> or the empty string. Can be used to generate simpler
     *            code, if the null check is not necessary.
     */
    public T newInstanceFromExpression(String expression, boolean checkForNull);

    /**
     * Returns a {@link CodeFragment} containing the code for converting the value (of the given
     * field) to a string representation with respect to its data type. The String must be built so
     * that it can be read using the valueOf-Expression. If the value is <code>null</code>, the
     * toString-code will yield <code>null</code> as a result.
     * <p>
     * The default implementation will call the values toString() method or return <code>null</code>
     * . The default implementation for generic (extensible) data types will call the method defined
     * using setToStringMethodName(). Custom {@link BaseDatatypeHelper}s may override.
     * 
     * 
     * @param fieldName the name of the field in the generated class that should be converted to a
     *            string
     * @return a {@link CodeFragment} containing the toString() code.
     */
    public T getToStringExpression(String fieldName);
}
