/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumTypeBuilder;
import org.faktorips.util.ArgumentCheck;

/**
 * A datatype helper for {@link IEnumType} which is an implementation of the Datatype interface.
 * This datatype helper uses the {@link EnumTypeBuilder} to get the java class name, the new
 * instance expression as well as the value of expression from it.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeHelper extends AbstractDatatypeHelper {

    private XEnumType enumType;
    private EnumTypeDatatypeAdapter enumTypeAdapter;

    /**
     * enumType must be the generator model node of enumTypeAdapter.getEnumType
     */
    public EnumTypeDatatypeHelper(XEnumType enumType, EnumTypeDatatypeAdapter enumTypeAdapter) {
        super(enumTypeAdapter);

        ArgumentCheck.notNull(enumType, this);
        ArgumentCheck.notNull(enumTypeAdapter, this);

        this.enumType = enumType;
        this.enumTypeAdapter = enumTypeAdapter;
    }

    /**
     * Returns the {@link IEnumType} of this helper.
     */
    public IEnumType getEnumType() {
        return enumTypeAdapter.getEnumType();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return new JavaCodeFragment("null");
        }
        return enumType.getNewInstanceCodeFragement(enumTypeAdapter, value, null);
    }

    @Override
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    @Override
    public String getJavaClassName() {
        return enumType.getQualifiedClassName();
    }

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return super.newInstanceFromExpression(expression, enumTypeAdapter.getEnumType().isInextensibleEnum());
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return enumType.getCallGetValueByIdentifierCodeFragment(expression, false, null);
    }

    /**
     * Generates code that retrieves the ID attribute from the given field name (enum value). As the
     * attribute may be of any datatype, that id value is converted to string. {@inheritDoc}
     */
    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        String getterName = enumType.getIdentifierAttribute().getMethodNameGetter();
        Datatype idColumnDatatype = enumType.getIdentifierAttribute().getDatatype();
        DatatypeHelper idColumnDatatypeHelper = enumType.getIpsProject().getDatatypeHelper(idColumnDatatype);

        // check for null as default value may be null and enum value set may contain null
        fragment.append("");
        fragment.append(fieldName);
        fragment.append("==null?null:(");
        fragment.append(idColumnDatatypeHelper.getToStringExpression(fieldName + "." + getterName + "()"));
        fragment.append(")");

        return fragment;
    }

    /**
     * @see XEnumType#getCallGetValueByIdentifierCodeFragment(String, JavaCodeFragment)
     */
    public JavaCodeFragment getCallGetValueByIdentifierCodeFragment(String expressionValue,
            JavaCodeFragment repositoryExp) {
        return enumType.getCallGetValueByIdentifierCodeFragment(expressionValue, repositoryExp);
    }
}
