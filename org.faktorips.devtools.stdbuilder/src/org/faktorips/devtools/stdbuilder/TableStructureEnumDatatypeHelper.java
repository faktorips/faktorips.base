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
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.TableStructureEnumDatatypeAdapter;
import org.faktorips.util.ArgumentCheck;

/**
 * TODO real implementation
 * 
 * @author Thorsten Guenther, Peter Erzberger
 */
public class TableStructureEnumDatatypeHelper extends AbstractDatatypeHelper {

    /**
     * 
     */
    public TableStructureEnumDatatypeHelper(TableStructureEnumDatatypeAdapter datatype) {
        super(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(Datatype datatype) {
        ArgumentCheck.isInstanceOf(datatype, TableStructureEnumDatatypeAdapter.class);
        super.setDatatype(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
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
        fragment.append(".valueOf("); //$NON-NLS-1$
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
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "org.faktorips.integrationtest.tables.TableAsEnumTypeDatatype"; //$NON-NLS-1$
    }
}
