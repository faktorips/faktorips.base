/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.util.ArgumentCheck;

/**
 * A datatype helper for {@link IEnumType} which is an implementation of the Datatype interface.
 * This datatype helper uses the {@link EnumTypeBuilder} to get the java class name, the new
 * instance expression as well as the value of expression from it.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeHelper extends AbstractDatatypeHelper {

    private EnumTypeBuilder enumTypeBuilder;
    private EnumTypeDatatypeAdapter enumTypeAdapter;

    public EnumTypeDatatypeHelper(EnumTypeBuilder enumTypeBuilder, EnumTypeDatatypeAdapter enumTypeAdapter) {
        super(enumTypeAdapter);

        ArgumentCheck.notNull(enumTypeBuilder, this);
        ArgumentCheck.notNull(enumTypeAdapter, this);

        this.enumTypeBuilder = enumTypeBuilder;
        this.enumTypeAdapter = enumTypeAdapter;
    }

    /**
     * Returns the {@link IEnumType} of this helper.
     */
    public IEnumType getEnumType() {
        return enumTypeAdapter.getEnumType();
    }

    /**
     * Returns the {@link EnumTypeBuilder} wrapped by this helper.
     */
    public EnumTypeBuilder getEnumTypeBuilder() {
        return enumTypeBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return new JavaCodeFragment("null");
        }
        try {
            return enumTypeBuilder.getNewInstanceCodeFragement(enumTypeAdapter, value);

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavaClassName() {
        try {
            return enumTypeBuilder.getQualifiedClassName(enumTypeAdapter.getEnumType().getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException("An exception occurred while trying to determine the java class name "
                    + "of the enum type: " + enumTypeAdapter.getQualifiedName(), e);
        }
    }

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return super.newInstanceFromExpression(expression, enumTypeAdapter.getEnumType().isInextensibleEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        try {
            return enumTypeBuilder.getCallGetValueByIdentifierCodeFragment(enumTypeAdapter.getEnumType(), expression,
                    false, null);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates code that retrieves the ID attribute from the given field name (enum value). As the
     * attribute may be of any datatype, that id value is converted to string. {@inheritDoc}
     */
    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        try {
            JavaCodeFragment fragment = new JavaCodeFragment();
            IEnumType enumType = enumTypeAdapter.getEnumType();
            String getterName = enumTypeBuilder.getMethodNameGetIdentifierAttribute(enumType, enumType.getIpsProject());
            Datatype idColumnDatatype = enumTypeBuilder.getDatatypeForIdentifierAttribute(enumType,
                    enumType.getIpsProject());
            DatatypeHelper idColumnDatatypeHelper = enumType.getIpsProject().getDatatypeHelper(idColumnDatatype);

            // check for null as default value may be null and enum value set may contain null
            fragment.append("");
            fragment.append(fieldName);
            fragment.append("==null?null:(");
            fragment.append(idColumnDatatypeHelper.getToStringExpression(fieldName + "." + getterName + "()"));
            fragment.append(")");

            return fragment;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
