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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.stdbuilder.enums.EnumClassesBuilder;

public class TableContentsEnumDatatypeHelper extends AbstractDatatypeHelper {

    private EnumClassesBuilder enumClassesBuilder;

    public TableContentsEnumDatatypeHelper(TableContentsEnumDatatypeAdapter datatype,
            EnumClassesBuilder enumClassesBuilder) {
        super(datatype);
        this.enumClassesBuilder = enumClassesBuilder;
    }

    private TableContentsEnumDatatypeAdapter getTableContentsEnumDatatypeAdapter() {
        return (TableContentsEnumDatatypeAdapter)getDatatype();
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        try {
            return enumClassesBuilder.generateCallMethodGetEnumValue(getTableContentsEnumDatatypeAdapter()
                    .getTableContents(), value, false);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
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
    public String getJavaClassName() {
        try {
            return enumClassesBuilder.getQualifiedClassName(getTableContentsEnumDatatypeAdapter().getTableContents()
                    .getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException("An exception occurred while trying to determine the java class name "
                    + "of the table content based enum type: " + getDatatype().getQualifiedName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        try {
            return enumClassesBuilder.generateCallMethodGetEnumValue(getTableContentsEnumDatatypeAdapter()
                    .getTableContents(), expression, true);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
}
