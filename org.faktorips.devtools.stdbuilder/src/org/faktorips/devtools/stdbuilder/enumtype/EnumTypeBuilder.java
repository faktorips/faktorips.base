/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Builder that generates the java source for <code>EnumType</code>s.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeBuilder extends DefaultJavaSourceFileBuilder {

    /** The package id identifying the builder */
    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumTypeBuilder.enumtype.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    /** The builder configuration property name that indicates whether to use Java 5 enum types. */
    private final static String USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY = "useJavaEnumTypes"; //$NON-NLS-1$

    /**
     * Creates a new <code>EnumTypeBuilder</code> that will belong to the given IPS artefact builder
     * set.
     * 
     * @param builderSet The IPS artefact builder set this builder shall be a part of.
     */
    public EnumTypeBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumTypeBuilder.class));
        setMergeEnabled(true);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if Java 5 enums are available.
     */
    private boolean isJava5EnumsAvailable() {
        return ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())
                && getIpsProject().getIpsArtefactBuilderSet().getConfig()
                        .getPropertyValueAsBoolean(USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY);
    }

    /**
     * Returns whether to generate an enum.
     */
    private boolean useEnumGeneration() {
        IEnumType enumType = getEnumType();

        if (!(isJava5EnumsAvailable())) {
            return false;
        }

        if (enumType.isAbstract()) {
            return false;
        }

        if (!(enumType.isContainingValues())) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether to generate a class.
     */
    private boolean useClassGeneration() {
        IEnumType enumType = getEnumType();

        if (!(isJava5EnumsAvailable())) {
            return true;
        }

        if (enumType.isAbstract()) {
            return false;
        }

        if (!(enumType.isContainingValues())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether to generate an interface.
     */
    private boolean useInterfaceGeneration() {
        if (!(useEnumGeneration()) && !(useClassGeneration())) {
            return true;
        }

        return false;
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        IEnumType enumType = getEnumType();

        // Set general properties
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClass(useClassGeneration());
        mainSection.setEnum(useEnumGeneration());
        int classModifier = Modifier.PUBLIC;
        if (useClassGeneration()) {
            if (enumType.isAbstract()) {
                classModifier = Modifier.PUBLIC | Modifier.ABSTRACT;
            } else {
                classModifier = Modifier.PUBLIC | Modifier.FINAL;
            }
        }
        mainSection.setClassModifier(classModifier);
        mainSection.setUnqualifiedName(getJavaNamingConvention().getTypeName(enumType.getName()));
        String description = getDescriptionInGeneratorLanguage(enumType);
        mainSection.getJavaDocForTypeBuilder().javaDoc(description, ANNOTATION_GENERATED);

        if (((StandardBuilderSet)getBuilderSet()).isGenerateJaxbSupport() && !enumType.isContainingValues()
                && !enumType.isAbstract()) {
            EnumXmlAdapterBuilder xmlAdapterBuilder = getBuilderSet().getBuildersByClass(EnumXmlAdapterBuilder.class)
                    .get(0);
            mainSection.getAnnotationsForTypeBuilder().annotationClassValueLn(
                    "javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter", "value", //$NON-NLS-1$ //$NON-NLS-2$
                    xmlAdapterBuilder.getQualifiedClassName(enumType));
        }

        // Set super type / implemented interface and ensure serialization
        List<String> implementedInterfaces = new ArrayList<String>(5);

        if (enumType.hasSuperEnumType()) {
            IEnumType superEnumType = enumType.findSuperEnumType(getIpsProject());
            if (superEnumType != null) {
                if (useEnumGeneration() || useInterfaceGeneration()
                        || (useClassGeneration() && isJava5EnumsAvailable())) {
                    implementedInterfaces.add(getQualifiedClassName(superEnumType));
                } else {
                    mainSection.setSuperClass(getQualifiedClassName(superEnumType));
                }
            }
        }
        if (useClassGeneration()) {
            if ((enumType.isAbstract() && !(enumType.hasSuperEnumType()))
                    || (useClassGeneration() && isJava5EnumsAvailable())) {
                implementedInterfaces.add(Serializable.class.getName());
            }
            generateConstantForSerialVersionNumber(mainSection.getConstantBuilder());
        }
        mainSection.setExtendedInterfaces(implementedInterfaces.toArray(new String[implementedInterfaces.size()]));

        // Generate enumeration values
        generateCodeForEnumValues(mainSection, useEnumGeneration());

        // Generate the attributes and the constructor
        if (useEnumGeneration() || useClassGeneration()) {
            generateCodeForEnumAttributes(mainSection.getMemberVarBuilder());
            generateCodeForConstructor(mainSection.getConstructorBuilder());
        }

        // Generate the methods
        generateCodeForMethods(mainSection.getMethodBuilder());
    }

    private void generateMethodGetEnumValueId(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        if (useInterfaceGeneration()) {
            return;
        }
        // the method getEnumValueId is only needed for enumerations with separated content
        if (getEnumType().isContainingValues()) {
            return;
        }
        IEnumAttribute identifierAttribute = getEnumType().findIdentiferAttribute(getIpsProject());
        if (identifierAttribute == null || !identifierAttribute.isValid(getIpsProject())) {
            return;
        }
        if (getEnumType().hasSuperEnumType() && identifierAttribute.getEnumType() != getEnumType()) {
            return;
        }
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return "); //$NON-NLS-1$
        body.append(getJavaNamingConvention().getMemberVarName(identifierAttribute.getName()));
        body.append(";"); //$NON-NLS-1$
        appendLocalizedJavaDoc("METHOD_GET_ENUM_VALUE_BY_ID", getEnumType(), methodBuilder);
        if (isJava5EnumsAvailable()) {
            methodBuilder.annotationLn(ANNOTATION_SUPPRESS_WARNINGS_UNUSED);
        }
        methodBuilder.method(Modifier.PRIVATE, Object.class, getMethodNameGetEnumValueId(), new String[0],
                new Class[0], body, null);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * private static final long serialVersionUID =
     * 7932454078331259392L;
     * </pre>
     */
    private void generateConstantForSerialVersionNumber(JavaCodeFragmentBuilder constantBuilder) {
        IEnumType enumType = getEnumType();
        appendLocalizedJavaDoc("SERIALVERSIONUID", enumType, constantBuilder); //$NON-NLS-1$
        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Long.TYPE,
                "serialVersionUID", new JavaCodeFragment("1L")); //$NON-NLS-1$ //$NON-NLS-2$
        constantBuilder.appendln(' ');
    }

    private void generateCodeForEnumValues(TypeSection mainSection, boolean javaAtLeast5) throws CoreException {
        IEnumType enumType = getEnumType();
        /*
         * If no enumeration value definition is added we need to make the semicolon anyway telling
         * the compiler that now the attribute section begins.
         */
        boolean appendSemicolon = javaAtLeast5;
        boolean lastEnumValueGenerated = false;

        /*
         * Generate the enumeration values if they are part of the model and if the enumeration type
         * is not abstract.
         */
        IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
        if (enumType.isContainingValues() && !(enumType.isAbstract()) && literalNameAttribute != null) {
            // Go over all model side defined enum values
            List<IEnumValue> enumValues = enumType.getEnumValues();
            for (int i = 0; i < enumValues.size(); i++) {
                IEnumValue currentEnumValue = enumValues.get(i);
                // Generate only for valid enum values
                if (currentEnumValue.isValid(getIpsProject())) {
                    List<IEnumAttributeValue> currentEnumAttributeValues = currentEnumValue.getEnumAttributeValues();
                    IEnumAttributeValue currentLiteralNameEnumAttributeValue = currentEnumAttributeValues.get(enumType
                            .getIndexOfEnumAttribute(literalNameAttribute));
                    currentEnumAttributeValues.remove(currentLiteralNameEnumAttributeValue);
                    if (javaAtLeast5) {
                        lastEnumValueGenerated = (i == enumValues.size() - 1);
                        createEnumValueAsEnumDefinition(currentEnumAttributeValues,
                                currentLiteralNameEnumAttributeValue, lastEnumValueGenerated,
                                mainSection.getEnumDefinitionBuilder());
                        appendSemicolon = false;
                    } else {
                        createEnumValueAsConstant(currentEnumAttributeValues, currentLiteralNameEnumAttributeValue,
                                mainSection.getConstantBuilder());
                    }
                }
            }
        }

        if (appendSemicolon || (!lastEnumValueGenerated && javaAtLeast5)) {
            mainSection.getEnumDefinitionBuilder().append(';');
        }
    }

    /**
     * Returns a java code fragment that contains the code for the instantiation of the provided
     * enumeration value of the provided enumeration type. Code snippet:
     * 
     * <pre>
     *  [Javadoc]
     *  Gender.MALE
     * </pre>
     * 
     * <pre>
     *  [Javadoc]
     *  repository.getEnumValue(Gender.class, &quot;m&quot;)
     * </pre>
     * 
     * @throws CoreException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumType</code> or <code>enumValue</code> is
     *             <code>null</code>.
     */
    public JavaCodeFragment getNewInstanceCodeFragement(EnumTypeDatatypeAdapter enumTypeAdapter,
            String value,
            JavaCodeFragment repositoryExp) throws CoreException {

        ArgumentCheck.notNull(enumTypeAdapter, this);

        JavaCodeFragment fragment = new JavaCodeFragment();
        if (enumTypeAdapter.getEnumType().isContainingValues()) {
            // TODO pk is this the right ips project that is provided to the finder method??
            IEnumValue enumValue = enumTypeAdapter.getEnumValueContainer().findEnumValue(value,
                    enumTypeAdapter.getEnumValueContainer().getIpsProject());
            if (enumValue == null) {
                return fragment;
            }
            IEnumAttributeValue literalNameAttributeValue = enumValue.getEnumAttributeValue(enumTypeAdapter
                    .getEnumType().getEnumLiteralNameAttribute());
            if (literalNameAttributeValue == null) {
                return fragment;
            }
            fragment.appendClassName(getQualifiedClassName(enumTypeAdapter.getEnumType()));
            fragment.append('.');
            fragment.append(getConstantNameForEnumAttributeValue(literalNameAttributeValue));
            return fragment;
        }
        return getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(enumTypeAdapter.getEnumType(), value, false,
                repositoryExp);
    }

    /**
     * @param enumType The enumeration type the new instance is a value of.
     * @param valueOrExpression Either an unquoted value in String format like <code>male</code> or
     *            an expression to get this value like <code>values.getValue(i)</code>
     * @param isExpression <code>true</code> if the parameter <code>valueOrExpression</code>is an
     *            expression, <code>false</code> otherwise.
     * @param repositoryExp Expression to get the runtime repository instance that contains the
     *            enumeration type, e.g. <code>getRepository()</code>
     */
    private JavaCodeFragment getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(IEnumType enumType,
            String valueOrExpression,
            boolean isExpression,
            JavaCodeFragment repositoryExp) throws CoreException {

        IEnumAttribute attribute = enumType.findIdentiferAttribute(getIpsProject());
        DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(attribute.findDatatype(getIpsProject()));
        JavaCodeFragment fragment = new JavaCodeFragment();

        if (!isJava5EnumsAvailable()) {
            fragment.append("("); //$NON-NLS-1$
            fragment.appendClassName(getQualifiedClassName(enumType));
            fragment.append(")"); //$NON-NLS-1$
        }
        if (repositoryExp != null) {
            fragment.append(repositoryExp);
        }
        fragment.append('.');
        fragment.append("getEnumValue("); //$NON-NLS-1$
        fragment.appendClassName(getQualifiedClassName(enumType));
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
     *  [Javadoc]
     *  Gender.getValueById(&quot;male&quot;)
     * </pre>
     * 
     * <pre>
     *  [Javadoc]
     *  repository.getEnumValue(Gender.class, &quot;m&quot;)
     * </pre>
     * 
     * 
     * @throws CoreException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public JavaCodeFragment getCallGetValueByIdentifierCodeFragment(IEnumType enumType,
            String expressionValue,
            JavaCodeFragment repositoryExp) throws CoreException {

        ArgumentCheck.notNull(enumType);

        if (enumType.isContainingValues()) {
            IEnumAttribute enumAttribute = enumType.findIdentiferAttribute(getIpsProject());
            DatatypeHelper idAttrDatatypeHelper = getIpsProject().findDatatypeHelper(
                    enumAttribute.findDatatype(getIpsProject()).getQualifiedName());
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.appendClassName(getQualifiedClassName(enumType));
            fragment.append('.');
            fragment.append(getMethodNameGetValueBy(enumAttribute));
            fragment.append("("); //$NON-NLS-1$
            fragment.append(idAttrDatatypeHelper.newInstanceFromExpression(expressionValue));
            fragment.append(")"); //$NON-NLS-1$
            return fragment;
        }
        return getNewInstanceCodeFragmentForEnumTypesWithDeferredContent(enumType, expressionValue, true, repositoryExp);
    }

    public String getMethodNameGetValueBy(IEnumAttribute uniqueEnumAttribute) {
        String attributeName = uniqueEnumAttribute.getName();
        char[] charArray = attributeName.toCharArray();
        charArray[0] = Character.toUpperCase(attributeName.charAt(0));
        return "getValueBy" + String.copyValueOf(charArray); //$NON-NLS-1$
    }

    public String getMethodNameIsValueBy(IEnumAttribute uniqueEnumAttribute) {
        return "isValueBy" + StringUtils.capitalize(uniqueEnumAttribute.getName()); //$NON-NLS-1$
    }

    public String getMethodNameGetIdentifierAttribute(IEnumType enumType, IIpsProject ipsProject) throws CoreException {
        IEnumAttribute idAttribute = enumType.findIdentiferAttribute(ipsProject);
        return getMethodNameGetter(idAttribute);
    }

    /**
     * This method expects the literal name attribute value of an enumeration value as parameter to
     * provide the accurate constant name for it.
     */
    private String getConstantNameForEnumAttributeValue(IEnumAttributeValue literalNameAttributeValue) {
        if (literalNameAttributeValue.getValue() == null) {
            return ""; //$NON-NLS-1$
        }

        return literalNameAttributeValue.getValue().toUpperCase();
    }

    /**
     * Generates the Java source code for an enumeration value as enum definition (Java at least 5).
     * 
     * @param enumAttributeValues The enumeration attribute values of the enumeration value to
     *            create.
     * @param literalEnumAttributeValue The enumeration attribute value that refers to the literal
     *            name enumeration attribute.
     * @param lastEnumDefinition Flag indicating whether this enum definition will be the last one.
     * @param enumDefinitionBuilder The java source code builder to use for creating enum
     *            definitions.
     */
    private void createEnumValueAsEnumDefinition(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue literalEnumAttributeValue,
            boolean lastEnumDefinition,
            JavaCodeFragmentBuilder enumDefinitionBuilder) throws CoreException {

        // Create enumeration definition source fragment
        appendLocalizedJavaDoc("ENUMVALUE", getEnumType(), enumDefinitionBuilder); //$NON-NLS-1$
        JavaCodeFragment enumDefinitionFragment = new JavaCodeFragment();
        enumDefinitionFragment.append(literalEnumAttributeValue.getValue().toUpperCase());
        enumDefinitionFragment.append(" ("); //$NON-NLS-1$
        appendEnumValueParameters(enumAttributeValues, enumDefinitionFragment);
        enumDefinitionFragment.append(')');
        if (!(lastEnumDefinition)) {
            enumDefinitionFragment.append(", "); //$NON-NLS-1$
            enumDefinitionFragment.appendln(' ');
            enumDefinitionFragment.appendln(' ');
        } else {
            enumDefinitionFragment.append(';');
        }

        enumDefinitionBuilder.append(enumDefinitionFragment);
        enumDefinitionBuilder.appendln();
    }

    /**
     * Generates the Java source code for an enumeration value as constant (Java less than 5).
     * 
     * @param enumAttributeValues The enumeration attribute values of the enumeration value to
     *            create.
     * @param literalEnumAttributeValue The enumeration attribute value that refers to the literal
     *            name enumeration attribute.
     * @param constantBuilder The Java source code builder to use for creating constants.
     */
    private void createEnumValueAsConstant(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue literalEnumAttributeValue,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        IEnumType enumType = getEnumType();

        // Build constant source
        appendLocalizedJavaDoc("ENUMVALUE", enumType, constantBuilder); //$NON-NLS-1$
        JavaCodeFragment initExpression = new JavaCodeFragment();
        initExpression.append("new "); //$NON-NLS-1$
        initExpression.append(enumType.getName());
        initExpression.append('(');
        appendEnumValueParameters(enumAttributeValues, initExpression);
        initExpression.append(')');

        DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(enumType.getQualifiedName());
        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                datatypeHelper.getJavaClassName(), literalEnumAttributeValue.getValue().toUpperCase(), initExpression);
        constantBuilder.appendln(' ');
    }

    /** Appends the parameter values to an enumeration value creation code fragment. */
    private void appendEnumValueParameters(List<IEnumAttributeValue> enumAttributeValues,
            JavaCodeFragment javaCodeFragment) throws CoreException {

        int numberEnumAttributeValues = enumAttributeValues.size();
        for (int i = 0; i < numberEnumAttributeValues; i++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
            IEnumAttribute referencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute(getIpsProject());
            IIpsProject ipsProject = getIpsProject();
            Datatype datatype = referencedEnumAttribute.findDatatype(ipsProject);
            if (datatype == null) {
                continue;
            }
            DatatypeHelper datatypeHelper = ipsProject.findDatatypeHelper(datatype.getQualifiedName());
            if (datatypeHelper != null) {
                javaCodeFragment.append(datatypeHelper.newInstance(currentEnumAttributeValue.getValue()));
            }
            // TODO pk handle missing datatypeHelper. Write error to the buildStatus
            if (i < numberEnumAttributeValues - 1) {
                javaCodeFragment.append(", "); //$NON-NLS-1$
            }
        }
    }

    private void generateCodeForEnumAttributes(JavaCodeFragmentBuilder attributeBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        int modifier = enumType.isAbstract() ? Modifier.PROTECTED : Modifier.PRIVATE | Modifier.FINAL;

        for (IEnumAttribute currentEnumAttribute : getEnumType().getEnumAttributesIncludeSupertypeCopies(false)) {
            String attributeName = currentEnumAttribute.getName();
            String codeName = getMemberVarName(attributeName);

            if (currentEnumAttribute.isValid(getIpsProject())) {
                /*
                 * If the generation artifact is a class and the attribute is inherited do not
                 * generate source code for this attribute because it is also inherited in the
                 * source code.
                 */
                if (!(useClassGeneration() && currentEnumAttribute.isInherited())
                        || (useClassGeneration() && !(enumType.isContainingValues()))) {
                    IIpsProject ipsProject = getIpsProject();
                    DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(currentEnumAttribute
                            .findDatatype(ipsProject));
                    if (datatypeHelper != null) {
                        /*
                         * happens if the attribute is inherited from the supertype, but the
                         * supertype can't be found
                         */
                        String description = getDescriptionInGeneratorLanguage(currentEnumAttribute);
                        attributeBuilder.javaDoc(description, ANNOTATION_GENERATED);
                        attributeBuilder.varDeclaration(modifier, datatypeHelper.getJavaClassName(), codeName);
                        attributeBuilder.appendln(' ');
                    }
                }
            }
        }
    }

    /** The first character will be made lower case. */
    public String getMemberVarName(String attributeName) {
        return getJavaNamingConvention().getMemberVarName(attributeName);
    }

    private void generateCodeForConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        generateConstructorForEnumsWithSeparateContent(constructorBuilder);
        generateConstructurForEnumsWithContent(constructorBuilder);
        generatePublicConstructorForEnumsWithSeparateContent(constructorBuilder);
    }

    private String getNameForConstructor(IEnumType enumType) {
        return getJavaNamingConvention().getTypeName(enumType.getName());
    }

    private void generateConstructurForEnumsWithContent(JavaCodeFragmentBuilder constructorBuilder)
            throws CoreException {

        IEnumType enumType = getEnumType();
        if (!enumType.isContainingValues() || enumType.isAbstract()) {
            return;
        }
        int constructorVisibility = (useClassGeneration() && enumType.isAbstract()) ? Modifier.PROTECTED
                : Modifier.PRIVATE;
        generatePublicConstructor(constructorBuilder, constructorVisibility);
    }

    private void generatePublicConstructor(JavaCodeFragmentBuilder constructorBuilder, int modifier)
            throws CoreException {

        IEnumType enumType = getEnumType();
        if (!enumType.isValid(getIpsProject())) {
            return; // can't get datatypes for inherited attributes, if supertype is missing
        }
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        List<IEnumAttribute> validEnumAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                validEnumAttributes.add(currentEnumAttribute);
            }
        }

        // Build method arguments
        String[] argumentNames = new String[validEnumAttributes.size()];
        String[] argumentClasses = new String[validEnumAttributes.size()];
        JavaNamingConvention javaNamingConvention = getJavaNamingConvention();
        for (int i = 0; i < validEnumAttributes.size(); i++) {
            IEnumAttribute currentEnumAttribute = validEnumAttributes.get(i);
            String attributeName = currentEnumAttribute.getName();
            argumentNames[i] = javaNamingConvention.getMemberVarName(attributeName);
            argumentClasses[i] = currentEnumAttribute.findDatatype(getIpsProject()).getJavaClassName();
        }

        // Build method body
        JavaCodeFragment methodBody = new JavaCodeFragment();
        createAttributeInitialization(methodBody);

        appendLocalizedJavaDoc("CONSTRUCTOR", enumType.getName(), enumType, constructorBuilder); //$NON-NLS-1$
        constructorBuilder.methodBegin(modifier, null, getNameForConstructor(enumType), argumentNames, argumentClasses);
        constructorBuilder.append(methodBody);
        constructorBuilder.methodEnd();

    }

    private void generatePublicConstructorForEnumsWithSeparateContent(JavaCodeFragmentBuilder constructorBuilder)
            throws CoreException {

        IEnumType enumType = getEnumType();
        if (enumType.isContainingValues() || enumType.isAbstract()) {
            return;
        }
        generatePublicConstructor(constructorBuilder, Modifier.PUBLIC);
    }

    private void generateConstructorForEnumsWithSeparateContent(JavaCodeFragmentBuilder constructorBuilder)
            throws CoreException {

        IEnumType enumType = getEnumType();
        if (!enumType.isValid(getIpsProject()) || enumType.isContainingValues() || enumType.isAbstract()) {
            return;
        }

        String enumValuesListName = "propertyValues"; //$NON-NLS-1$
        JavaCodeFragment body = new JavaCodeFragment();
        int i = 0;
        for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                String currentName = getJavaNamingConvention().getMemberVarName(currentEnumAttribute.getName());
                body.append("this."); //$NON-NLS-1$
                body.append(currentName);
                body.append(" = "); //$NON-NLS-1$

                Datatype datatype = currentEnumAttribute.findDatatype(getIpsProject());
                DatatypeHelper helper = getIpsProject().findDatatypeHelper(datatype.getQualifiedName());
                String expression = enumValuesListName + ".get(" + i + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                expression = isJava5EnumsAvailable() ? expression : "(String)" + expression; //$NON-NLS-1$
                if (helper instanceof EnumTypeDatatypeHelper) {
                    EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
                    if (!enumHelper.getEnumType().isContainingValues()) {
                        body.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(
                                enumHelper.getEnumType(), expression, new JavaCodeFragment("productRepository"))); //$NON-NLS-1$
                        body.append(';');
                        body.appendln();
                        i++;
                        continue;
                    }
                }
                body.append(helper.newInstanceFromExpression(expression));
                body.append(';');
                body.appendln();
            }
            i++;
        }
        if (!isJava5EnumsAvailable()) {
            body.appendln("// This method is only called to avoid the compiler warning for an " //$NON-NLS-1$
                    + "unused method. see the java doc of this method for further explanation."); //$NON-NLS-1$
            body.append(getMethodNameGetEnumValueId());
            body.appendln("();"); //$NON-NLS-1$
        }
        String[] argClasses = isJava5EnumsAvailable() ? new String[] { List.class.getName() + "<String>", //$NON-NLS-1$
                IRuntimeRepository.class.getName() } : new String[] { List.class.getName(),
                IRuntimeRepository.class.getName() };

        appendLocalizedJavaDoc("CONSTRUCTOR", enumType.getName(), enumType, constructorBuilder); //$NON-NLS-1$
        constructorBuilder.methodBegin(Modifier.PROTECTED, null, getNameForConstructor(enumType), new String[] {
                enumValuesListName, "productRepository" }, argClasses); //$NON-NLS-1$
        constructorBuilder.append(body);
        constructorBuilder.methodEnd();

    }

    private String getMethodNameGetEnumValueId() {
        return "getEnumValueId"; //$NON-NLS-1$
    }

    /** Creates the attribute initialization code for the constructor. */
    private void createAttributeInitialization(JavaCodeFragment constructorMethodBody) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumType().getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                String currentName = getJavaNamingConvention().getMemberVarName(currentEnumAttribute.getName());
                constructorMethodBody.append("this."); //$NON-NLS-1$
                constructorMethodBody.append(currentName);
                constructorMethodBody.append(" = "); //$NON-NLS-1$
                constructorMethodBody.append(currentName);
                constructorMethodBody.append(';');
                constructorMethodBody.appendln();
            }
        }
    }

    private void generateCodeForMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateMethodGetValueBy(methodBuilder);
        generateMethodIsValueBy(methodBuilder);
        generateMethodGetterMethods(methodBuilder);
        generateMethodValues(methodBuilder);
        generateMethodReadResolve(methodBuilder);
        generateMethodToString(methodBuilder);
        generateMethodEquals(methodBuilder);
        generateMethodHashCode(methodBuilder);
        generateMethodGetEnumValueId(methodBuilder);
    }

    private void generateMethodGetterMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        IIpsProject ipsProject = getIpsProject();

        // Create getters for each attribute.
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(currentEnumAttribute
                        .findDatatype(ipsProject));
                if (datatypeHelper == null) {
                    // Can happen, if attribute is inherited, but supertype can't be found.
                    continue;
                }
                String attributeName = currentEnumAttribute.getName();
                String description = getDescriptionInGeneratorLanguage(currentEnumAttribute);
                String methodName = getMethodNameGetter(currentEnumAttribute);

                // If an interface is to be generated then only generate the method signatures.
                if (useInterfaceGeneration()) {
                    if (!(currentEnumAttribute.isInherited())) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute, //$NON-NLS-1$
                                methodBuilder);
                        methodBuilder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, null,
                                null);
                        methodBuilder.appendln(';');
                    }

                } else {
                    if (useEnumGeneration() || !(useClassGeneration() && currentEnumAttribute.isInherited())
                            || (useClassGeneration() && !(enumType.isContainingValues()))) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute, //$NON-NLS-1$
                                methodBuilder);

                        // Build method body
                        JavaCodeFragment methodBody = new JavaCodeFragment();
                        methodBody.append("return "); //$NON-NLS-1$
                        methodBody.append(getMemberVarName(attributeName));
                        methodBody.append(';');

                        methodBuilder.methodBegin(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, null,
                                null);
                        methodBuilder.append(methodBody);
                        methodBuilder.methodEnd();
                    }
                }

            }
        }
    }

    /**
     * Generates the Java code for <code>getValueByXXX()</code> methods for each enumeration value.
     * 
     * Code snippet:
     * 
     * <pre>
     *  [Javadoc]
     *  public final static Gender getValueById(String id) {
     *      if (id == null) {
     *          return null;
     *      }
     *      
     *      if (id.equals(&quot;male&quot;)) {
     *          return MALE;
     *      }
     *      if (id.equals(&quot;female&quot;)) {
     *          return FEMALE;
     *      }
     *      
     *      return null;
     *  }
     * </pre>
     */
    private void generateMethodGetValueBy(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        if (!(enumType.isContainingValues()) || enumType.isAbstract()) {
            return;
        }

        List<IEnumAttribute> uniqueAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        List<IEnumValue> enumValues = enumType.getEnumValues();
        IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
        if (literalNameAttribute == null) {
            return;
        }

        for (IEnumAttribute currentEnumAttribute : uniqueAttributes) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                if (EnumUtil.findEnumAttributeIsUnique(currentEnumAttribute, getIpsProject())) {
                    JavaCodeFragment body = new JavaCodeFragment();
                    String parameterName = currentEnumAttribute.getName();

                    ValueDatatype datatype = currentEnumAttribute.findDatatype(getIpsProject());
                    DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);

                    boolean primitiveType = datatype.isPrimitive();
                    if (!(primitiveType)) {
                        body.append("if("); //$NON-NLS-1$
                        body.append(parameterName);
                        body.append(" == null)"); //$NON-NLS-1$
                        body.appendOpenBracket();
                        body.appendln("return null;"); //$NON-NLS-1$
                        body.appendCloseBracket();
                    }

                    for (IEnumValue currentEnumValue : enumValues) {
                        if (!(currentEnumValue.isValid(getIpsProject()))) {
                            continue;
                        }

                        IEnumAttributeValue attributeValue = currentEnumValue
                                .getEnumAttributeValue(currentEnumAttribute);
                        if (primitiveType) {
                            body.append("if ("); //$NON-NLS-1$
                            body.append(parameterName);
                            body.append(" == "); //$NON-NLS-1$
                            body.append(datatypeHelper.newInstance(attributeValue.getValue()));
                            body.append(")"); //$NON-NLS-1$
                            body.appendOpenBracket();
                            body.append("return "); //$NON-NLS-1$
                            body.append(getConstantNameForEnumAttributeValue(currentEnumValue
                                    .getEnumAttributeValue(literalNameAttribute)));
                            body.append(";"); //$NON-NLS-1$
                            body.appendCloseBracket();
                        } else {
                            body.append("if ("); //$NON-NLS-1$
                            body.append(parameterName);
                            body.append(".equals("); //$NON-NLS-1$
                            body.append(datatypeHelper.newInstance(attributeValue.getValue()));
                            body.append("))"); //$NON-NLS-1$
                            body.appendOpenBracket();
                            body.append("return "); //$NON-NLS-1$
                            body.append(getConstantNameForEnumAttributeValue(currentEnumValue
                                    .getEnumAttributeValue(literalNameAttribute)));
                            body.append(";"); //$NON-NLS-1$
                            body.appendCloseBracket();
                        }
                    }

                    body.append("return null;"); //$NON-NLS-1$

                    appendLocalizedJavaDoc("METHOD_GET_VALUE_BY_XXX", parameterName, enumType, methodBuilder); //$NON-NLS-1$
                    methodBuilder.methodBegin(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                            getQualifiedClassName(enumType), getMethodNameGetValueBy(currentEnumAttribute),
                            new String[] { parameterName }, new String[] { datatypeHelper.getJavaClassName() });
                    methodBuilder.append(body);
                    methodBuilder.methodEnd();
                }
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public final static boolean isValueById(Integer id) {
     *     return getGenderById(id) != null;
     * }
     * </pre>
     */
    private void generateMethodIsValueBy(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        if (!(enumType.isContainingValues()) || enumType.isAbstract()) {
            return;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid(getIpsProject())) {
                if (EnumUtil.findEnumAttributeIsUnique(currentEnumAttribute, getIpsProject())) {
                    JavaCodeFragment methodBody = new JavaCodeFragment();
                    methodBody.append("return "); //$NON-NLS-1$
                    methodBody.append(getMethodNameGetValueBy(currentEnumAttribute));
                    methodBody.append("("); //$NON-NLS-1$
                    methodBody.append(currentEnumAttribute.getName());
                    methodBody.append(")"); //$NON-NLS-1$
                    methodBody.append(" != null;"); //$NON-NLS-1$

                    String[] parameterNames = new String[] { currentEnumAttribute.getName() };
                    String[] parameterClasses = new String[] { currentEnumAttribute.findDatatype(getIpsProject())
                            .getJavaClassName() };

                    appendLocalizedJavaDoc("METHOD_IS_VALUE_BY_XXX", currentEnumAttribute.getName(), enumType, //$NON-NLS-1$
                            methodBuilder);
                    methodBuilder.method(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Boolean.TYPE.getName(),
                            getMethodNameIsValueBy(currentEnumAttribute), parameterNames, parameterClasses, methodBody,
                            null);
                }
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public static final GeneratedGender[] values() {
     *      return new GeneratedGender[] { MALE, FEMALE };
     * }
     * </pre>
     * 
     * Not generated for Java 5 enumeration generation because for Java 5 enumerations the method is
     * provided by Java itself.
     */
    private void generateMethodValues(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        if (useEnumGeneration() || enumType.isAbstract() || !(enumType.isContainingValues())) {
            return;
        }

        String methodName = "values"; //$NON-NLS-1$
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return "); //$NON-NLS-1$
        methodBody.append("new "); //$NON-NLS-1$
        methodBody.append(enumType.getName());
        methodBody.append("[] {"); //$NON-NLS-1$

        List<IEnumValue> enumValues = enumType.getEnumValues();
        for (int i = 0; i < enumValues.size(); i++) {
            IEnumValue currentEnumValue = enumValues.get(i);
            if (!(currentEnumValue.isValid(getIpsProject()))) {
                continue;
            }

            IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
            if (literalNameAttribute == null) {
                continue;
            }

            IEnumAttributeValue literalNameAttributeValue = currentEnumValue
                    .getEnumAttributeValue(literalNameAttribute);
            if (literalNameAttributeValue == null) {
                continue;
            }

            methodBody.append(getConstantNameForEnumAttributeValue(literalNameAttributeValue));
            if (i < enumValues.size() - 1) {
                methodBody.append(", "); //$NON-NLS-1$
            }
        }

        methodBody.append("};"); //$NON-NLS-1$

        appendLocalizedJavaDoc("METHOD_VALUES", enumType, methodBuilder); //$NON-NLS-1$
        DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(enumType.getQualifiedName());
        methodBuilder.method(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, datatypeHelper.getJavaClassName()
                + "[]", methodName, new String[0], new String[0], methodBody, null); //$NON-NLS-1$
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * private Object readResolve() throws ObjectStreamException {
     *     return getGender(id);
     * }
     * </pre>
     * 
     * Only generated for class generation because Java 5 enumerations are serializable out of the
     * box.
     */
    private void generateMethodReadResolve(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        if (!(useClassGeneration()) || enumType.isAbstract() || !enumType.isContainingValues()) {
            return;
        }

        IEnumAttribute identifierAttribute = getEnumType().findIdentiferAttribute(getIpsProject());
        if (identifierAttribute == null) {
            return;
        }

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return "); //$NON-NLS-1$
        if (enumType.isContainingValues()) {
            methodBody.append(getMethodNameGetValueBy(identifierAttribute));
            methodBody.append('(');
            if (identifierAttribute.isInherited()) {
                methodBody.append(getJavaNamingConvention().getGetterMethodName(identifierAttribute.getName(),
                        identifierAttribute.findDatatype(getIpsProject())));
                methodBody.append('(');
                methodBody.append(')');
            } else {
                methodBody.append(identifierAttribute.getName());
            }
            methodBody.append(')');
        } else {
            methodBody.append("null"); //$NON-NLS-1$
        }
        methodBody.append(';');
        methodBuilder.javaDoc(null, ANNOTATION_GENERATED);
        methodBuilder.method(Modifier.PRIVATE, Object.class, "readResolve", new String[0], new Class[0], //$NON-NLS-1$
                new Class[] { ObjectStreamException.class }, methodBody, null);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public boolean equals(Object obj) {
     *     if (obj instanceof PaymentOption) {
     *         return this.getId().equals(((PaymentOption)obj).getId());
     *     }
     *     return false;
     * }
     * </pre>
     */
    private void generateMethodEquals(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        if (!useClassGeneration()) {
            return;
        }
        IEnumAttribute idAttr = getEnumType().findIdentiferAttribute(getIpsProject());
        if (idAttr == null || !idAttr.isValid(getIpsProject())) {
            return;
        }
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if(obj instanceof "); //$NON-NLS-1$
        body.appendClassName(getQualifiedClassName());
        body.append(")"); //$NON-NLS-1$
        body.appendOpenBracket();
        body.append("return this."); //$NON-NLS-1$
        body.append(getMethodNameGetIdentifierAttribute(getEnumType(), getIpsProject()));
        body.append("().equals((("); //$NON-NLS-1$
        body.appendClassName(getQualifiedClassName());
        body.append(")obj)."); //$NON-NLS-1$
        body.append(getMethodNameGetIdentifierAttribute(getEnumType(), getIpsProject()));
        body.append("());"); //$NON-NLS-1$
        body.appendCloseBracket();
        body.appendln("return false;"); //$NON-NLS-1$

        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodBuilder, false);
        methodBuilder.methodBegin(Modifier.PUBLIC, Boolean.TYPE.getName(), "equals", new String[] { "obj" }, //$NON-NLS-1$ //$NON-NLS-2$
                new String[] { Object.class.getName() });
        methodBuilder.append(body);
        methodBuilder.methodEnd();
    }

    private void generateMethodHashCode(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        if (!useClassGeneration()) {
            return;
        }
        IEnumAttribute idAttr = getEnumType().findIdentiferAttribute(getIpsProject());
        if (idAttr == null || !idAttr.isValid(getIpsProject())) {
            return;
        }
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return "); //$NON-NLS-1$
        body.append(getMethodNameGetIdentifierAttribute(getEnumType(), getIpsProject()));
        body.appendln("().hashCode();"); //$NON-NLS-1$

        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodBuilder, false);
        methodBuilder.methodBegin(Modifier.PUBLIC, Integer.TYPE.getName(), "hashCode", new String[0], new String[0]); //$NON-NLS-1$
        methodBuilder.append(body);
        methodBuilder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public String toString() {
     *     return &quot;Gender: &quot; + getName();
     * }
     * </pre>
     */
    private void generateMethodToString(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        if (useInterfaceGeneration()) {
            return;
        }

        IEnumType enumType = getEnumType();
        if (enumType.isAbstract()) {
            return;
        }
        IEnumAttribute idAttribute = enumType.findIdentiferAttribute(getIpsProject());
        if (idAttribute == null || !(idAttribute.isValid(getIpsProject()))) {
            return;
        }

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return "); //$NON-NLS-1$
        methodBody.append("\""); //$NON-NLS-1$
        methodBody.append(enumType.getName());
        methodBody.append(": \" + "); //$NON-NLS-1$
        methodBody.append(getJavaNamingConvention().getMemberVarName(idAttribute.getName()));
        IEnumAttribute displayName = enumType.findUsedAsNameInFaktorIpsUiAttribute(getIpsProject());
        if (displayName == null || !(displayName.isValid(getIpsProject()))) {
            methodBody.append(";"); //$NON-NLS-1$
        } else {
            methodBody.append(" + '(' + "); //$NON-NLS-1$
            methodBody.append(getJavaNamingConvention().getMemberVarName(displayName.getName()));
            methodBody.append(" + ')';"); //$NON-NLS-1$
        }
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodBuilder, false);
        methodBuilder.method(Modifier.PUBLIC, String.class, "toString", new String[0], new Class[0], methodBody, null); //$NON-NLS-1$
    }

    /**
     * Returns the enumeration type for that code is being generated.
     */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return true;
    }

    @Override
    protected IType getGeneratedJavaImplementationTypeThis(IIpsObject ipsObject) {
        return getGeneratedJavaTypes(ipsObject).get(0);
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        if (ipsObjectPartContainer instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)ipsObjectPartContainer;
            IEnumType enumType = enumAttribute.getEnumType();
            IType javaType = getGeneratedJavaTypes(enumType).get(0);
            if (isJava5EnumsAvailable()) {
                getGeneratedJavaElementsForAttributeJava5EnumsAvailable(javaElements, enumAttribute, javaType);
            } else {
                getGeneratedJavaElementsForAttributeJava5EnumsNotAvailable(javaElements, enumAttribute, javaType);
            }

        } else if (ipsObjectPartContainer instanceof IEnumLiteralNameAttributeValue) {
            IEnumLiteralNameAttributeValue literalNameValue = (IEnumLiteralNameAttributeValue)ipsObjectPartContainer;
            IIpsObject parentIpsObject = literalNameValue.getEnumValue().getEnumValueContainer();
            IType javaType = getGeneratedJavaTypes(parentIpsObject).get(0);
            IField javaEnumLiteral = javaType.getField(literalNameValue.getValue());
            javaElements.add(javaEnumLiteral);
        }
    }

    private void getGeneratedJavaElementsForAttributeJava5EnumsAvailable(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) {

        if (enumAttribute.getEnumType().isAbstract()) {
            if (!(enumAttribute.isInherited())) {
                addGetterMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
            }
        } else {
            addMemberVarToGeneratedJavaElements(javaElements, enumAttribute, javaType);
            addGetterMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
            try {
                if (EnumUtil.findEnumAttributeIsUnique(enumAttribute, enumAttribute.getIpsProject())) {
                    addGetValueByMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                    addIsValueByMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getGeneratedJavaElementsForAttributeJava5EnumsNotAvailable(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) {

        if (enumAttribute.getEnumType().isAbstract()) {
            if (!(enumAttribute.isInherited())) {
                addMemberVarToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                addGetterMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
            }
        } else {
            if (!(enumAttribute.isInherited())) {
                addMemberVarToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                addGetterMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
            }
            try {
                if (EnumUtil.findEnumAttributeIsUnique(enumAttribute, enumAttribute.getIpsProject())) {
                    addGetValueByMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                    addIsValueByMethodToGeneratedJavaElements(javaElements, enumAttribute, javaType);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addGetterMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) {

        String methodName = getMethodNameGetter(enumAttribute);
        String[] parameterTypeSignature = new String[0];
        IMethod getterMethod = javaType.getMethod(methodName, parameterTypeSignature);
        javaElements.add(getterMethod);
    }

    private void addMemberVarToGeneratedJavaElements(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) {

        String fieldName = getMemberVarName(enumAttribute);
        IField memberVar = javaType.getField(fieldName);
        javaElements.add(memberVar);
    }

    private void addGetValueByMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) throws CoreException {

        String methodName = getMethodNameGetValueBy(enumAttribute);
        String[] parameterTypeSignature = new String[] { "Q"
                + enumAttribute.findDatatype(enumAttribute.getIpsProject()) + ";" };
        IMethod getValueByMethod = javaType.getMethod(methodName, parameterTypeSignature);
        javaElements.add(getValueByMethod);
    }

    private void addIsValueByMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IEnumAttribute enumAttribute,
            IType javaType) throws CoreException {

        String methodName = getMethodNameIsValueBy(enumAttribute);
        String[] parameterTypeSignature = new String[] { "Q"
                + enumAttribute.findDatatype(enumAttribute.getIpsProject()) + ";" };
        IMethod isValueByMethod = javaType.getMethod(methodName, parameterTypeSignature);
        javaElements.add(isValueByMethod);
    }

    public String getMethodNameGetter(IEnumAttribute enumAttribute) {
        Datatype datatype = getDatatypeHelper(enumAttribute).getDatatype();
        return getJavaNamingConvention().getGetterMethodName(enumAttribute.getName(), datatype);
    }

    private String getMemberVarName(IEnumAttribute enumAttribute) {
        return getJavaNamingConvention().getMemberVarName(enumAttribute.getName());
    }

    private DatatypeHelper getDatatypeHelper(IEnumAttribute enumAttribute) {
        try {
            return getIpsProject().getDatatypeHelper(enumAttribute.findDatatype(getIpsProject()));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
