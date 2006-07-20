/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.TableStructureEnumDatatypeAdapter;

/**
 * TODO real implementation
 * 
 * @author Thorsten Guenther
 */
public class TableStructureEnumDatatypeHelper implements DatatypeHelper {

    TableStructureEnumDatatypeAdapter datatype;
    
    /**
     * 
     */
    public TableStructureEnumDatatypeHelper(TableStructureEnumDatatypeAdapter datatype) {
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
        if (!(datatype instanceof TableStructureEnumDatatypeAdapter)) {
            throw new IllegalArgumentException("Datatype " + datatype + " is not an instance of " + this.datatype.getClass());
        }
        this.datatype = (TableStructureEnumDatatypeAdapter)datatype;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getJavaClassName());        
        fragment.append(".valueOf(");
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getJavaClassName());        
        fragment.append(".valueOf(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "org.faktorips.integrationtest.tables.TableAsEnumTypeDatatype";
    }

    /**
     * {@inheritDoc}
     */
    public String getRangeJavaClassName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newRangeInstance(String lowerBoundExp,
            String upperBoundExp,
            String stepExp,
            String containsNullExp) {
        return null;
    }

}
