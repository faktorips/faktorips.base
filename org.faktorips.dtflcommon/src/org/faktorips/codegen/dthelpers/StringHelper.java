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

package org.faktorips.codegen.dthelpers;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 *  
 */
public class StringHelper extends AbstractDatatypeHelper {

	/**
	 * Constructs a new helper for the string datatype.
	 */
	public StringHelper() {
		super();
	}

	/**
	 * Constructs a new helper for the given string datatype.
	 * 
	 * @throws IllegalArgumentException
	 *             if datatype is <code>null</code>.
	 */
	public StringHelper(StringDatatype datatype) {
		super(datatype);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
	 */
	public JavaCodeFragment newInstance(String value) {
		if (value == null) {
			return nullExpression();
		}
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendQuoted(StringUtils.escape(value));
		return fragment;
	}

	/**
     * {@inheritDoc}
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression) {
		return valueOfExpression(expression);
	}

	/**
     * {@inheritDoc}
	 */
	protected JavaCodeFragment valueOfExpression(String expression) {
		if (StringUtils.isEmpty(expression)) {
			return nullExpression();
		}
		JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.append(expression);
		return fragment;
	}

	/**
     * {@inheritDoc}
	 */
	public JavaCodeFragment nullExpression() {
		return new JavaCodeFragment("null");
	}

}
