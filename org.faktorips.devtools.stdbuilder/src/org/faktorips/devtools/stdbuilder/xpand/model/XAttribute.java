/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

    private DatatypeHelper datatypeHelper;

    public XAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
        super(attribute, context, modelService);
        try {
            datatypeHelper = attribute.getIpsProject().findDatatypeHelper(attribute.getDatatype());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IAttribute getIpsObjectPartContainer() {
        return (IAttribute)super.getIpsObjectPartContainer();
    }

    public IAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName(), getDatatype());
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public String fieldPropertyNameSuffix() {
        return getContext().isGenerateSeparatedCamelCase() ? StringUtil.camelCaseToUnderscore(getName()).toUpperCase()
                : getName().toUpperCase();
    }

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
        if (isDatatypeContentSeparatedEnum()) {
            return getExpressionForEnumWithSeparatedContent(repositoryExpression, result);
        } else {
            return result;
        }
    }

    public String getExpressionForEnumWithSeparatedContent(String repositoryExpression, String newInstanceExpression) {
        return repositoryExpression + newInstanceExpression;
    }

    /**
     * Returns <code>true</code> if this attributes data type is an enumeration-type with separate
     * content. <code>false</code> else.
     * 
     */
    public boolean isDatatypeContentSeparatedEnum() {
        return DatatypeUtil.isEnumTypeWithSeparateContent(getDatatype());
    }

    public String getToStringExpression(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public boolean isPublished() {
        return getAttribute().getModifier().isPublished();
    }

    public final boolean isOverwrite() {
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
