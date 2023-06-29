/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.enumtype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.devtools.stdbuilder.util.LocaleGeneratorUtil;
import org.faktorips.devtools.stdbuilder.xmodel.MethodParameter;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.util.ArgumentCheck;

public class XEnumType extends XClass {

    static final String VAR_NAME_PRODUCT_REPOSITORY = "productRepository";

    private final IJavaClassNameProvider javaClassNameProvider;

    public XEnumType(IEnumType enumtype, GeneratorModelContext context, ModelService modelService) {
        super(enumtype, context, modelService);
        javaClassNameProvider = createEnumJavaClassNameProvider(
                getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()));
    }

    /* private */boolean isMarkerEnum() {
        return getIpsProject().getMarkerEnums().contains(getEnumType().getIpsSrcFile());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    public static IJavaClassNameProvider createEnumJavaClassNameProvider(boolean isGeneratePublishedInterfaces) {
        return new EnumJavaClassNameProvider(isGeneratePublishedInterfaces);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        return getIpsObjectPartContainer().isValid(getIpsProject());
    }

    @Override
    protected String getBaseSuperclassName() {
        return "";
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        if (isAbstract()) {
            return getExtendedOrImplementedInterfaces();
        } else {
            return new LinkedHashSet<>();
        }
    }

    @Override
    public LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        LinkedHashSet<String> interfaces = new LinkedHashSet<>();
        if (hasSuperEnumType()) {
            interfaces.add(addImport(getSuperEnumType().getQualifiedClassName()));
        }

        if (isClass()) {
            interfaces.add(addImport(Serializable.class));
            interfaces.add(addImport(Comparable.class) + "<" + getName() + ">");
        }

        if (isMarkerEnum()) {
            interfaces.add(addImport(IMarker.class));
        }
        // is not abstract needed because of FIPS-10047
        if (getGeneratorConfig().isGenerateToXmlSupport() && !isAbstract() && isExtensible()) {
            interfaces.add(addImport(IToXmlSupport.class));
        }

        return interfaces;
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        return getExtendedOrImplementedInterfaces();
    }

    public IEnumType getEnumType() {
        return (IEnumType)getIpsObjectPartContainer().getIpsObject();
    }

    public String getQualifiedIpsObjectName() {
        return getEnumType().getQualifiedName();
    }

    public List<XEnumAttribute> getAttributes(boolean includeSupertypeCopies, boolean includeLiteralName) {
        List<IEnumAttribute> enumAttributes;
        if (includeSupertypeCopies) {
            if (isAbstract()) {
                enumAttributes = getEnumType().findAllEnumAttributes(includeLiteralName, getIpsProject());
            } else {
                enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
            }
        } else {
            enumAttributes = getEnumType().getEnumAttributes(includeLiteralName);
        }
        return new ArrayList<>(initNodesForParts(enumAttributes, XEnumAttribute.class));
    }

    public List<XEnumAttribute> getDeclaredAttributesWithoutLiteralName() {
        return getAttributes(false, false);
    }

    public List<XEnumAttribute> getAllAttributesWithoutLiteralName() {
        return getAttributes(true, false);
    }

    public List<XEnumAttribute> getAllAttributesWithField() {
        List<XEnumAttribute> results = new ArrayList<>();
        List<XEnumAttribute> attributeModelNodes = getAttributes(true, false);
        for (XEnumAttribute attribute : attributeModelNodes) {
            if (attribute.isGenerateField()) {
                results.add(attribute);
            }
        }
        return results;
    }

    public List<XEnumAttribute> getAllUniqueAttributesWithoutLiteralName() {
        List<XEnumAttribute> attributeModelNodes = getAttributes(true, false);
        List<XEnumAttribute> results = new ArrayList<>();

        for (XEnumAttribute attribute : attributeModelNodes) {
            if (attribute.isUnique()) {
                results.add(attribute);
            }
        }
        return results;
    }

    public List<XEnumAttribute> getAllAttributes() {
        return getAttributes(true, true);
    }

    public String getEnumContentQualifiedName() {
        return getEnumType().getEnumContentName();
    }

    public boolean isExtensible() {
        return getEnumType().isExtensible();
    }

    public boolean isAbstract() {
        return getEnumType().isAbstract();
    }

    /**
     * Returns whether to generate a class.
     */
    public boolean isClass() {
        return isInterface() ? false : isExtensible();
    }

    /**
     * Returns whether to generate an interface.
     */
    public boolean isInterface() {
        return getEnumType().isAbstract();
    }

    /* This method is public because it is used in enumXmlAdapterBuilder */
    public String getMethodNameGetIdentifierAttribute() {
        return getIdentifierAttribute().getMethodNameGetter();
    }

    public XEnumAttribute getIdentifierAttribute() {
        return getModelNode(getEnumType().findIdentiferAttribute(getIpsProject()), XEnumAttribute.class);
    }

    public XEnumAttribute getDisplayNameAttribute() {
        return getModelNode(getEnumType().findUsedAsNameInFaktorIpsUiAttribute(getIpsProject()), XEnumAttribute.class);
    }

    public XEnumAttribute getEnumLiteralNameAttribute() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();

        if (literalNameAttribute != null) {
            return getModelNode(literalNameAttribute, XEnumAttribute.class);
        } else {
            throw new IllegalStateException("Literalname attribute is null for the enum type " + getName());
        }
    }

    public String getQualifiedClassName() {
        return getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

    public String getUnqualifiedClassName() {
        return addImport(getQualifiedName(BuilderAspect.IMPLEMENTATION));
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
     * @throws IpsException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumType</code> or <code>enumValue</code> is
     *             <code>null</code>.
     */
    public JavaCodeFragment getNewInstanceCodeFragement(EnumTypeDatatypeAdapter enumTypeAdapter,
            String value,
            ExtendedExprCompiler exprCompiler) {

        ArgumentCheck.notNull(enumTypeAdapter, this);

        ExtendedExprCompiler expressionCompiler = getExpressionCompiler(exprCompiler);
        JavaCodeFragment repositoryExp = expressionCompiler.getRuntimeRepositoryExpression();

        JavaCodeFragment fragment = new JavaCodeFragment();
        if (getEnumType().isInextensibleEnum() || enumTypeAdapter.getEnumContent() == null) {
            IEnumValue enumValue = enumTypeAdapter.getEnumValueContainer().findEnumValue(value,
                    enumTypeAdapter.getEnumValueContainer().getIpsProject());
            if (enumValue == null) {
                return fragment;
            }
            IEnumAttributeValue literalNameAttributeValue = enumValue
                    .getEnumAttributeValue(getEnumType().getEnumLiteralNameAttribute());
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

    private ExtendedExprCompiler getExpressionCompiler(ExtendedExprCompiler exprCompiler) {
        if (exprCompiler == null) {
            return getIpsProject().newExpressionCompiler();
        }
        return exprCompiler;
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
        fragment.append("getExistingEnumValue("); //$NON-NLS-1$
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
     * Returns the code fragment for a <code>getValueByXXX()</code> method call expression.<br>
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
     * Returns the code fragment for a <code>getValueByXXX()</code> method call expression.<br>
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
                DatatypeHelper idAttrDatatypeHelper = getIpsProject()
                        .findDatatypeHelper(enumAttribute.getDatatype().getQualifiedName());
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

    public boolean hasSuperEnumType() {
        return getSuperEnumType() != null;
    }

    public XEnumType getSuperEnumType() {
        if (getEnumType().findSuperEnumType(getIpsProject()) != null) {
            return getModelNode(getEnumType().findSuperEnumType(getIpsProject()), XEnumType.class);
        } else {
            return null;
        }
    }

    public boolean isMessageHelperNeeded() {
        if (getEnumType().isCapableOfContainingValues()
                && (getEnumType().isInextensibleEnum() || getEnumType().containsValues())) {
            List<IEnumAttribute> enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(false);
            for (IEnumAttribute enumAttribute : enumAttributes) {
                if (enumAttribute.isMultilingual()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGenerateMethodCompareTo() {
        return getEnumType().isExtensible() && !isInterface();
    }

    public boolean isIndexFieldRequired() {
        return getEnumType().isExtensible() || isGenerateMethodCompareTo();
    }

    public String getDefaultLocale() {
        JavaCodeFragment defaultLocaleExpr = LocaleGeneratorUtil
                .getLocaleCodeFragment(getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale());
        addImport(defaultLocaleExpr.getImportDeclaration());
        return defaultLocaleExpr.getSourcecode();
    }

    public List<XEnumValue> getEnumValues() {
        return new ArrayList<>(initNodesForParts(getEnumType().getEnumValues(), XEnumValue.class));
    }

    public List<MethodParameter> getConstructorParameters() {
        List<MethodParameter> parameters = new ArrayList<>();
        if (isIndexFieldRequired()) {
            parameters.add(new MethodParameter("int", getVarNameIndex()));
        }

        for (XEnumAttribute attribute : getAllAttributesWithField()) {
            parameters
                    .add(new MethodParameter(attribute.getDatatypeNameForConstructor(), attribute.getMemberVarName()));
        }

        return parameters;
    }

    public List<MethodParameter> getStringConstructorParameters() {
        List<MethodParameter> parameters = new ArrayList<>();
        if (isIndexFieldRequired()) {
            parameters.add(new MethodParameter("int", getVarNameIndex()));
        }

        for (XEnumAttribute attribute : getAllAttributesWithField()) {
            if (attribute.isMultilingual()) {
                parameters.add(new MethodParameter(addImport(InternationalString.class),
                        attribute.getStringConstructorParamName()));
            } else {
                parameters.add(new MethodParameter(addImport(String.class), attribute.getStringConstructorParamName()));
            }
        }

        parameters.add(new MethodParameter(addImport(IRuntimeRepository.class), getVarnameProductRepository()));

        return parameters;
    }

    public String getVarNameMessageHelper() {
        return "MESSAGE_HELPER";
    }

    public String getVarNameIdMap() {
        return "ID_MAP";
    }

    public String getVarnameProductRepository() {
        return VAR_NAME_PRODUCT_REPOSITORY;
    }

    public String getVarNameIndex() {
        return "index";
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
