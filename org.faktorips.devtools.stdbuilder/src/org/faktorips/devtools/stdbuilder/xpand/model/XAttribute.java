/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.util.StringUtil;

/**
 * Contains common behavior for product- and policy-attributes alike.
 * 
 * @author widmaier
 */
public abstract class XAttribute extends AbstractGeneratorModelNode {

    public XAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
        super(attribute, context, modelService);
    }

    @Override
    public IAttribute getIpsObjectPartContainer() {
        return (IAttribute)super.getIpsObjectPartContainer();
    }

    public IAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public DatatypeHelper getDatatypeHelper() {
        try {
            return getAttribute().getIpsProject().findDatatypeHelper(getAttribute().getDatatype());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public String getMethodNameSetterInternal() {
        return getJavaNamingConvention().getSetterMethodName(getName() + "Internal"); //$NON-NLS-1$
    }

    public String getMethodNameSetterInternalIfGenerateChangeSupport() {
        return isGenerateChangeSupport() ? getMethodNameSetterInternal() : getMethodNameSetter();
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName(), getDatatype());
    }

    public String getMethodNameWriteToXml() {
        return "write" + StringUtils.capitalize(getName());
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public String fieldPropertyNameSuffix() {
        return getContext().isGenerateSeparatedCamelCase() ? StringUtil.camelCaseToUnderscore(getName()).toUpperCase()
                : getName().toUpperCase();
    }

    /**
     * Returns the source code of the default value of the chosen attribute.
     */
    public String getDefaultValueCode() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    public final ValueDatatype getDatatype() {
        return (ValueDatatype)getDatatypeHelper().getDatatype();
    }

    /**
     * Returns a string representing the java datatype for this attribute. This datatype may however
     * be a primitive or a complex type. In case of a complex type (e.g. BigDecimal) an import is
     * added and the unqualified type name is returned. In case of a primitive the name of the type
     * is returned without adding an import.
     */
    public String getJavaClassName() {
        String javaClassName = getDatatypeHelper().getJavaClassName();
        return addImport(javaClassName);
    }

    /**
     * Return the qualified java class name of the data type for this attribute.
     * 
     * @see #getJavaClassName()
     */
    public String getQualifiedJavaClassName() {
        String javaClassName = getDatatypeHelper().getJavaClassName();
        return javaClassName;
    }

    /**
     * Returns the code to create a new instance. The expression is the code to retrieve the value
     * from, e.g. another variable. The repositoryExpression is the code for getting a repository.
     * It may be needed for enumerations with separated content.
     * 
     * @param expression The expression to get the value from
     * @param repositoryExpression the expression to get the repository
     * @return The code needed to create a new instance for a value set
     */
    public String getNewInstanceFromExpression(String expression, String repositoryExpression) {
        return getNewInstanceFromExpression(getDatatypeHelper(), expression, repositoryExpression);
    }

    /**
     * Returns the code to create a new instance. The expression is the code to retrieve the value
     * from, e.g. another variable. The repositoryExpression is the code for getting a repository.
     * It may be needed for enumerations with separated content.
     * 
     * @param datatypeHelper The data type helper of the data type you need the new instance
     *            expression for
     * @param expression The expression to get the value from
     * @param repositoryExpression the expression to get the repository
     * @return The code needed to create a new instance for a value set
     */
    protected String getNewInstanceFromExpression(DatatypeHelper datatypeHelper,
            String expression,
            String repositoryExpression) {
        JavaCodeFragment fragment = datatypeHelper.newInstanceFromExpression(expression);
        addImport(fragment.getImportDeclaration());
        String result = fragment.getSourcecode();
        if (isDatatypeExtensibleEnum()) {
            return getExpressionForEnumWithSeparatedContent(repositoryExpression, result);
        } else {
            return result;
        }
    }

    public String getExpressionForEnumWithSeparatedContent(String repositoryExpression, String newInstanceExpression) {
        return repositoryExpression + newInstanceExpression;
    }

    /**
     * Returns <code>true</code> if this attributes data type is an enumeration-type with values in
     * type and separated content.
     * 
     */
    public boolean isDatatypeExtensibleEnum() {
        return DatatypeUtil.isExtensibleEnumType(getDatatype());
    }

    public String getToStringExpression(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public boolean isPublished() {
        return getAttribute().getModifier().isPublished();
    }

    public boolean isOverwrite() {
        return getAttribute().isOverwrite();
    }

    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().referenceOrSafeCopyIfNeccessary(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getConstantNamePropertyName() {
        return "PROPERTY_" + StringUtils.upperCase(getFieldName());
    }

}
