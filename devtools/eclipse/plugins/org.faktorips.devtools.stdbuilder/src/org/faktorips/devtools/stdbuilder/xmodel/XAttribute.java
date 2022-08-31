/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.util.DatatypeHelperUtil;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
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
        return getDatatypeHelper(getAttribute().getDatatype());
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public String getMethodNameSetterInternal() {
        return getJavaNamingConvention().getSetterMethodName(getName() + "Internal"); //$NON-NLS-1$
    }

    public String getMethodNameSetterInternalIfGenerateChangeSupport() {
        return getGeneratorConfig().isGenerateChangeSupport() ? getMethodNameSetterInternal() : getMethodNameSetter();
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName(), getDatatype());
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public String fieldPropertyNameSuffix() {
        return getGeneratorConfig().isGenerateSeparatedCamelCase()
                ? StringUtil.camelCaseToUnderscore(getName()).toUpperCase()
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
        if (getDatatypeHelper() == null) {
            IAttribute attribute = getAttribute();
            throw new IpsException("When building \"" + getIpsProject().getName() + "\", the datatype \""
                    + attribute.getDatatype() + "\" of the attribute \"" + attribute.getIpsObject().getName()
                    + "#" + attribute.getName()
                    + "\" could not be found.");
        }
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
        return getDatatypeHelper().getJavaClassName();
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
        JavaCodeFragment fragment = DatatypeHelperUtil.getNewInstanceFromExpression(datatypeHelper, expression,
                repositoryExpression);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
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

    public XAttribute getOverwrittenAttribute() {
        if (isOverwrite()) {
            IAttribute overwrittenAttribute = getAttribute().findOverwrittenAttribute(getIpsProject());
            return getModelNode(overwrittenAttribute, getClass());
        } else {
            throw new RuntimeException(
                    "Attribute is not overwritten so there is no overwritten attribute for " + getAttribute());
        }
    }

    public boolean isChangingOverTime() {
        return getAttribute().isChangingOverTime();
    }

    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().referenceOrSafeCopyIfNeccessary(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getConstantNamePropertyName() {
        return "PROPERTY_" + StringUtils.upperCase(getFieldName());
    }

    /**
     * Extension to {@link #getAnnotationsForPublishedInterface(AnnotatedJavaElementType, boolean)}
     * for attribute methods that are generated in either published interface or implementation
     * class depending on the {@link Modifier}. For published attributes, this method inherits the
     * behavior of {@link #getAnnotationsForPublishedInterface(AnnotatedJavaElementType, boolean)}.
     * If the attribute is not published, this method only returns annotations if the builder is
     * currently generating an implementation class.
     */
    public String getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType type,
            boolean isGeneratingInterface) {
        if (isPublished()) {
            return getAnnotationsForPublishedInterface(type, isGeneratingInterface);
        } else {
            if (!isGeneratingInterface) {
                return getAnnotations(type);
            } else {
                return "";
            }
        }
    }

    /**
     * Checks whether the attribute is abstract or not. The attribute is abstract if its datatype is
     * an abstract datatype.
     */
    public boolean isAbstract() {
        return getDatatype().isAbstract();
    }

    /**
     * Returns <code>true</code> if abstract getters should be generated for this attribute.
     */
    public boolean isGenerateAbstractMethods() {
        return !getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()) && isAbstract() && !isOverwrite();
    }
}
