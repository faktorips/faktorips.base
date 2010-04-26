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

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.valueset.DefaultEnumValueSet;

/**
 * Abstract base class for datatype helpers.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatypeHelper implements DatatypeHelper {

    private Datatype datatype;

    /**
     * Constructs a new helper.
     */
    public AbstractDatatypeHelper() {
    }

    /**
     * Constructs a new helper for the given datatype.
     */
    public AbstractDatatypeHelper(Datatype datatype) {
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }

    /**
     * This method is supposed to be overridden by subclasses.
     * <p>
     * It is used within the <code>newInstanceFromExpression(String)</code> method. It returns a
     * <code>JavaCodeFragment</code> with sourcecode that creates an instance of the datatype's Java
     * class with the given expression.
     * <p>
     * If the expression is <code>null</code> the fragment's sourcecode is either the String "null"
     * or the sourcecode to get an instance of the appropriate null object. Preconditions:
     * Expression may not be null or empty. When evaluated the expression must return a String.
     */
    protected abstract JavaCodeFragment valueOfExpression(String expression);

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        if (expression == null || expression.equals("")) {
            return nullExpression();
        }
        // ((expression==null) || (expression.equals(""))) ? nullExpression() :
        // valueOfExpression(expression)
        if (expression.startsWith("(")) {
            expression = '(' + expression + ')';
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("(");
        fragment.append(expression);
        fragment.append("==null || ");
        fragment.append(expression);
        fragment.append(".equals(\"\")");
        fragment.append(") ? ");
        fragment.append(nullExpression());
        fragment.append(" : ");
        fragment.append(valueOfExpression(expression));

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return datatype.getJavaClassName();
    }

    /**
     * {@inheritDoc}
     */
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>null</code>
     */
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Code sample
     * 
     * <pre>
     * new DefaultEnumValueSet(new GeneratedGender[] { GeneratedGender.getGeneratedGender(new Integer(1)),
     *         GeneratedGender.getGeneratedGender(new Integer(2)), GeneratedGender.getGeneratedGender(null) }, true,
     *         GeneratedGender.getGeneratedGender(null));
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     *  (EnumValueSet)new DefaultEnumValueSet&lt;GeneratedGender&gt;(
     *      true, 
     *      GeneratedGender.getGeneratedGender(null),
     *      GeneratedGender.getGeneratedGender(new Integer(1)), 
     *      GeneratedGender.getGeneratedGender(new Integer(2)));
     * </pre>
     */
    public JavaCodeFragment newEnumValueSetInstance(String[] values,
            boolean containsNull,
            boolean useTypesafeCollections) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (useTypesafeCollections) {
            frag.append("new ");
            frag.appendClassName(Java5ClassNames.OrderedValueSet_QualifiedName);
            frag.append("<");
            frag.appendClassName(getJavaClassName());
            frag.append(">(");
            frag.append(containsNull);
            frag.append(", ");
            frag.append(newInstance(null));
            for (String value : values) {
                frag.append(", ");
                frag.append(newInstance(value));
            }
            frag.appendln(")");
        } else {
            frag.append("new ");
            frag.appendClassName(DefaultEnumValueSet.class);
            frag.append("(");
            frag.append("new ");
            frag.appendClassName(getJavaClassName());
            frag.append("[] ");
            frag.appendOpenBracket();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    frag.append(", ");
                }
                frag.append(newInstance(values[i]));
            }
            frag.appendCloseBracket();
            frag.append(", ");
            frag.append(containsNull);
            frag.append(", ");
            frag.append(newInstance(null));
            frag.appendln(")");
        }
        return frag;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newEnumValueSetInstance(JavaCodeFragment valueCollection,
            JavaCodeFragment containsNullExpression,
            boolean useTypesafeCollections) {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new ");
        if (useTypesafeCollections) {
            frag.appendClassName(Java5ClassNames.OrderedValueSet_QualifiedName);
            frag.append("<");
            frag.appendClassName(getJavaClassName());
            frag.append(">");
        } else {
            frag.appendClassName(DefaultEnumValueSet.class);
        }
        frag.append("(");
        frag.append(valueCollection);
        frag.append(", ");
        frag.append(containsNullExpression);
        frag.append(", ");
        frag.append(nullExpression());
        frag.appendln(")");
        return frag;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment referenceOrSafeCopyIfNeccessary(String expression) {
        if (datatype.isValueDatatype() && ((ValueDatatype)datatype).isMutable()) {
            return newSafeCopy(expression);
        }
        return new JavaCodeFragment(expression);
    }

    /**
     * Helpers for immutable datatypes must override this method to create a copy of the value given
     * in the expression.
     */
    protected JavaCodeFragment newSafeCopy(String expression) {
        throw new RuntimeException("The DatatypeHelper for datatype " + datatype
                + " does not override the method newSafeCopy!");

    }

}
