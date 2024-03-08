/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.enumtype;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.InternationalStringDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.InternationalStringDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.builder.java.util.EnumTypeDatatypeHelper;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;

public class XEnumAttribute extends AbstractGeneratorModelNode {

    public XEnumAttribute(IEnumAttribute enumAttribute, GeneratorModelContext context, ModelService modelService) {
        super(enumAttribute, context, modelService);
    }

    protected IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsObjectPartContainer();
    }

    public boolean isUnique() {
        return getEnumAttribute().findIsUnique(getIpsProject());
    }

    public boolean isDisplayName() {
        return getEnumAttribute().findIsUsedAsNameInFaktorIpsUi(getIpsProject());
    }

    public boolean isInherited() {
        return getEnumAttribute().isInherited();
    }

    public boolean isIdentifier() {
        return getEnumAttribute().findIsIdentifier(getIpsProject());
    }

    public boolean isLiteralName() {
        return getEnumAttribute().isEnumLiteralNameAttribute();
    }

    public boolean isMultilingualSupported() {
        return getEnumAttribute().isMultilingualSupported();
    }

    public boolean isMultilingual() {
        return getEnumAttribute().isMultilingual();
    }

    public boolean isDeclaredIn(XEnumType xEnumType) {
        return !xEnumType.hasSuperEnumType() || getEnumAttribute().getEnumType() == xEnumType.getEnumType();
    }

    public XEnumType getEnumType() {
        return getModelNode(getEnumAttribute().getEnumType(), XEnumType.class);
    }

    protected DatatypeHelper getDatatypeHelper(boolean mapMultilingual) {
        IEnumAttribute enumAttribute = getEnumAttribute();
        if (enumAttribute == null) {
            return getIpsProject().getDatatypeHelper(Datatype.STRING);
        } else if (mapMultilingual && enumAttribute.isMultilingual()) {
            return new InternationalStringDatatypeHelper(true);
        } else {
            return getDatatypeHelper(getDatatype());
        }
    }

    public ValueDatatype getDatatype() {
        return getEnumAttribute().findDatatype(getIpsProject());
    }

    /**
     * Returns the name of the Java class for the datatype used in the constructor and add's a
     * matching import. May differ from the datatype returned by the corresponding getter (for
     * example {@link InternationalString} vs. {@link String}).
     */
    public String getDatatypeNameForConstructor() {
        return addImport(getDatatypeHelper(true));
    }

    public Datatype getDatatypeUseWrappers() {
        ValueDatatype datatype = getDatatype();
        if (datatype.isPrimitive()) {
            datatype = datatype.getWrapperType();
        }
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        return datatypeHelper.getDatatype();
    }

    public String getDatatypeNameUseWrappers() {
        return addImport(getDatatypeUseWrappers());
    }

    private String addImport(DatatypeHelper datatypeHelper) {
        return addImport(datatypeHelper.getJavaClassName());
    }

    public String getDatatypeName() {
        return addImport(getDatatype());
    }

    private String addImport(Datatype datatype) {
        return addImport(getDatatypeHelper(datatype));
    }

    /**
     * @see IEnumAttribute#findDatatypeIgnoreEnumContents(org.faktorips.devtools.model.ipsproject.IIpsProject)
     */
    protected ValueDatatype getDatatypeIgnoreEnumContents() {
        return getEnumAttribute().findDatatypeIgnoreEnumContents(getIpsProject());
    }

    public boolean isGenerateField() {
        return (getEnumAttribute().getEnumType().isExtensible() || !isMultilingual()) && !isLiteralName();
    }

    public String getMemberVarName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    /**
     * Returns the assignment from a (International)String parameter (as used in the constructor
     * used by the {@link IRuntimeRepository} to create extended enum contents) to the member
     * variable.
     */
    public String getMemberVarAssignmentFromStringParameter() {
        ValueDatatype datatype = getDatatype();
        String paramName = getStringConstructorParamName();
        if (Datatype.STRING.equals(datatype) || datatype instanceof InternationalStringDatatype) {
            return paramName;
        }
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        JavaCodeFragment newInstanceFromExpression;
        if (isExtensibleEnumDatatype(datatypeHelper)) {
            newInstanceFromExpression = ((EnumTypeDatatypeHelper)datatypeHelper)
                    .getCallGetValueByIdentifierCodeFragment(paramName,
                            new JavaCodeFragment(XEnumType.VAR_NAME_PRODUCT_REPOSITORY));
        } else {
            newInstanceFromExpression = datatypeHelper.newInstanceFromExpression(paramName);
        }
        addImport(newInstanceFromExpression.getImportDeclaration());
        return newInstanceFromExpression.getSourcecode();
    }

    public String getStringConstructorParamName() {
        return getMemberVarName() + "String";
    }

    private boolean isExtensibleEnumDatatype(DatatypeHelper helper) {
        if (helper instanceof EnumTypeDatatypeHelper enumHelper) {
            if (enumHelper.getEnumType().isExtensible()) {
                return true;
            }
        }
        return false;
    }

    public String getMethodNameGetter() {
        Datatype datatype = getDatatypeHelper(false).getDatatype();
        return getJavaNamingConvention().getGetterMethodName(getName(), datatype);
    }

    public String getMethodNameGetValueBy() {
        return "getValueBy" + IpsStringUtils.toUpperFirstChar(getName());
    }

    public String getMethodNameGetExistingValueBy() {
        return "getExistingValueBy" + IpsStringUtils.toUpperFirstChar(getName());
    }

    public String getMethodNameIsValueBy() {
        return "isValueBy" + IpsStringUtils.toUpperFirstChar(getName());
    }

    public String getToStringExpression(String memberVarName) {
        DatatypeHelper datatypeHelper = getDatatypeHelper(true);
        JavaCodeFragment fragment = datatypeHelper.getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        if (datatypeHelper instanceof InternationalStringDatatypeHelper) {
            JavaCodeFragment cast = new JavaCodeFragment().append("(").appendClassName(DefaultInternationalString.class)
                    .append(")");
            addImport(cast.getImportDeclaration());
            return cast.append(fragment).getSourcecode();
        }
        return fragment.getSourcecode();
    }

}
