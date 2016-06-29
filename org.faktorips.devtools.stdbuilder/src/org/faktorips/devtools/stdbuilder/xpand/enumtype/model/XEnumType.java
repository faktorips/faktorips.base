/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.util.ArgumentCheck;

public class XEnumType extends XClass {

    /** The builder configuration property name that indicates whether to use Java 5 enum types. */
    private static final String USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY = "useJavaEnumTypes"; //$NON-NLS-1$

    private ExtendedExprCompiler expressionCompilerLazy;

    private EnumJavaClassNameProvider javaClassNameProvider;

    public XEnumType(IEnumType enumtype, GeneratorModelContext context, ModelService modelService) {
        super(enumtype, context, modelService);
        javaClassNameProvider = createEnumJavaClassNameProvider(isGeneratePublishedInterfaces());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    public static EnumJavaClassNameProvider createEnumJavaClassNameProvider(boolean isGeneratePublishedInterfaces) {
        return new EnumJavaClassNameProvider(isGeneratePublishedInterfaces);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            return getIpsObjectPartContainer().isValid(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected String getBaseSuperclassName() {
        // TODO
        return "";
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        // TODO
        return new LinkedHashSet<String>();
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        // TODO Auto-generated method stub
        return new LinkedHashSet<String>();
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        // TODO Auto-generated method stub
        return new LinkedHashSet<String>();
    }

    public IEnumType getEnumType() {
        return (IEnumType)getIpsObjectPartContainer().getIpsObject();
    }

    public String getQualifiedIpsObjectName() {
        return getEnumType().getQualifiedName();
    }

    public List<XEnumAttribute> getAttributeModelNodes(boolean includeSupertypeCopies, boolean includeLiteralName) {
        List<IEnumAttribute> enumAttributes;
        if (includeSupertypeCopies) {
            enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        } else {
            enumAttributes = getEnumType().getEnumAttributes(includeLiteralName);
        }
        return new ArrayList<XEnumAttribute>(initNodesForParts(enumAttributes, XEnumAttribute.class));
    }

    public List<XEnumAttribute> getAllAttributeModelNodes() {
        return getAttributeModelNodes(true, true);
    }

    public String getEnumContentQualifiedName() {
        return getEnumType().getEnumContentName();
    }

    public boolean isExtensible() {
        return getEnumType().isExtensible();
    }

    /**
     * Returns whether to generate an enum.
     */
    protected boolean isEnum() {
        return isInterface() ? false : getEnumType().isInextensibleEnum();
    }

    /**
     * Returns whether to generate a class.
     */
    protected boolean isClass() {
        return isInterface() ? false : getEnumType().isExtensible();
    }

    /**
     * Returns whether to generate an interface.
     */
    protected boolean isInterface() {
        return getEnumType().isAbstract();
    }

    /**
     * Returns <code>true</code> if Java 5 enums are available.
     */
    protected boolean isJava5EnumsAvailable() {
        return ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())
                && getIpsProject().getIpsArtefactBuilderSet().getConfig()
                        .getPropertyValueAsBoolean(USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY);
    }

    /* This method is public because it is used in enumXmlAdapterBuilder */
    public String getMethodNameGetIdentifierAttribute() {
        return getIdentifierAttribute().getMethodNameGetter();
    }

    public XEnumAttribute getIdentifierAttribute() {
        return getModelNode(getEnumType().findIdentiferAttribute(getIpsProject()), XEnumAttribute.class);
    }

    public String getQualifiedClassName() {
        return getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

    /**
     * Returns a java code fragment that contains the code for the instantiation of the provided
     * enumeration value of the provided enumeration type. Code snippet:
     *
     * <pre>
     * [Javadoc]
     * Gender.MALE
     * </pre>
     *
     * <pre>
     * [Javadoc]
     * repository.getEnumValue(Gender.class, &quot;m&quot;)
     * </pre>
     *
     * @throws CoreException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumType</code> or <code>enumValue</code> is
     *             <code>null</code>.
     */
    public JavaCodeFragment getNewInstanceCodeFragement(EnumTypeDatatypeAdapter enumTypeAdapter, String value)
            throws CoreException {

        ArgumentCheck.notNull(enumTypeAdapter, this);

        ExtendedExprCompiler expressionCompiler = getExpressionCompiler();
        JavaCodeFragment repositoryExp = expressionCompiler.getRuntimeRepositoryExpression();

        JavaCodeFragment fragment = new JavaCodeFragment();
        if (getEnumType().isInextensibleEnum() || enumTypeAdapter.getEnumContent() == null) {
            IEnumValue enumValue = enumTypeAdapter.getEnumValueContainer().findEnumValue(value,
                    enumTypeAdapter.getEnumValueContainer().getIpsProject());
            if (enumValue == null) {
                return fragment;
            }
            IEnumAttributeValue literalNameAttributeValue = enumValue.getEnumAttributeValue(getEnumType()
                    .getEnumLiteralNameAttribute());
            if (literalNameAttributeValue == null) {
                return fragment;
            }
            fragment.appendClassName(getQualifiedClassName());
            fragment.append('.');
            fragment.append(getConstantNameForEnumAttributeValue(literalNameAttributeValue));
            return fragment;
        }
        return getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(value, false, repositoryExp);
    }

    private ExtendedExprCompiler getExpressionCompiler() {
        if (expressionCompilerLazy == null) {
            expressionCompilerLazy = getIpsProject().newExpressionCompiler();
        }
        return expressionCompilerLazy;
    }

    /**
     * Sets the {@link ExtendedExprCompiler} used to create code fragments containing repository
     * access.
     *
     * @param expressionCompiler the {@link ExtendedExprCompiler} used to create code fragments
     *            containing repository access
     */
    public void setExtendedExprCompiler(ExtendedExprCompiler expressionCompiler) {
        this.expressionCompilerLazy = expressionCompiler;
    }

    /**
     * Expects the literal name attribute value of an enumeration value as parameter to provide the
     * accurate constant name for it.
     */
    private String getConstantNameForEnumAttributeValue(IEnumAttributeValue literalNameAttributeValue) {
        if (literalNameAttributeValue.getValue() == null) {
            return ""; //$NON-NLS-1$
        }
        return literalNameAttributeValue.getValue().getLocalizedContent(getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * @param valueOrExpression Either an unquoted value in String format like <code>male</code> or
     *            an expression to get this value like <code>values.getValue(i)</code>
     * @param isExpression <code>true</code> if the parameter <code>valueOrExpression</code>is an
     *            expression, <code>false</code> otherwise.
     * @param repositoryExp Expression to get the runtime repository instance that contains the
     *            enumeration type, e.g. <code>getRepository()</code>
     */
    private JavaCodeFragment getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(String valueOrExpression,
            boolean isExpression,
            JavaCodeFragment repositoryExp) {
        XEnumAttribute attribute = getIdentifierAttribute();
        DatatypeHelper datatypeHelper = attribute.getDatatypeHelper(true);
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (repositoryExp != null) {
            fragment.append(repositoryExp);
        }
        fragment.append('.');
        fragment.append("getEnumValue("); //$NON-NLS-1$
        fragment.appendClassName(getQualifiedClassName());
        fragment.append(".class, "); //$NON-NLS-1$
        String expression = valueOrExpression;
        if (!isExpression) {
            expression = "\"" + valueOrExpression + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }
        /*
         * As the data type of the identifier attribute needn't be a String, we have to convert the
         * String expression to an instance of the appropriate data type. (see bug #1586)
         */
        fragment.append(datatypeHelper.newInstanceFromExpression(expression));
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

    /**
     * Returns the code fragment for a <code>getValueByXXX()</code> method call expression.<br />
     * Code snippet:
     *
     * <pre>
     * [Javadoc]
     * Gender.getValueById(&quot;male&quot;)
     * </pre>
     *
     * <pre>
     * [Javadoc]
     * repository.getEnumValue(Gender.class, &quot;m&quot;)
     * </pre>
     *
     * @param expressionValue A Java source code expression that yields a String. Examples are a
     *            constant String like <code>"FOO"</code>, a variable like <code>foo</code> or a
     *            method call like <code>getÍd()</code>.
     * @param repositoryExp An expression to access the FAKTOR-IPS runtime repository. Needed for
     *            enum types with deferred contents.
     *
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public JavaCodeFragment getCallGetValueByIdentifierCodeFragment(String expressionValue,
            JavaCodeFragment repositoryExp) {
        return getCallGetValueByIdentifierCodeFragment(expressionValue, true, repositoryExp);
    }

    /**
     * Returns the code fragment for a <code>getValueByXXX()</code> method call expression.<br />
     * Code snippet:
     *
     * <pre>
     * [Javadoc]
     * Gender.getValueById(&quot;male&quot;)
     * </pre>
     *
     * <pre>
     * [Javadoc]
     * repository.getEnumValue(Gender.class, &quot;m&quot;)
     * </pre>
     *
     * @param expressionValue A Java source code expression that yields a String. Examples are a
     *            constant String like <code>"FOO"</code>, a variable like <code>foo</code> or a
     *            method call like <code>getÍd()</code>.
     * @param checkExpressionForNullAndEmptyString <code>true</code> if this helper must check that
     *            the given expression can yield <code>null</code> or the empty string. Can be used
     *            for simpler code.
     * @param repositoryExp An expression to access the Faktor-IPS runtime repository. Needed for
     *            enum types with deferred contents.
     *
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public JavaCodeFragment getCallGetValueByIdentifierCodeFragment(String expressionValue,
            boolean checkExpressionForNullAndEmptyString,
            JavaCodeFragment repositoryExp) {
        if (!isExtensible()) {
            XEnumAttribute enumAttribute = getIdentifierAttribute();
            if (enumAttribute != null) {
                DatatypeHelper idAttrDatatypeHelper = getIpsProject().findDatatypeHelper(
                        enumAttribute.getDatatype().getQualifiedName());
                JavaCodeFragment fragment = new JavaCodeFragment();
                fragment.appendClassName(getQualifiedClassName());
                fragment.append('.');
                fragment.append(getMethodNameGetValueByIdentifier());
                fragment.append("("); //$NON-NLS-1$
                fragment.append(idAttrDatatypeHelper.newInstanceFromExpression(expressionValue,
                        checkExpressionForNullAndEmptyString));
                fragment.append(")"); //$NON-NLS-1$
                return fragment;
            } else {
                return new JavaCodeFragment("null");
            }
        } else {
            return getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(expressionValue, true, repositoryExp);
        }

    }

    public String getMethodNameGetValueByIdentifier() {
        String attributeName = getIdentifierAttribute().getName();
        char[] charArray = attributeName.toCharArray();
        charArray[0] = Character.toUpperCase(attributeName.charAt(0));
        return "getValueBy" + String.copyValueOf(charArray); //$NON-NLS-1$
    }

    private static final class EnumJavaClassNameProvider extends DefaultJavaClassNameProvider {
        private EnumJavaClassNameProvider(boolean isGeneratePublishedInterface) {
            super(isGeneratePublishedInterface);
        }

        @Override
        public boolean isImplClassInternalArtifact() {
            return false;
        }
    }
}
