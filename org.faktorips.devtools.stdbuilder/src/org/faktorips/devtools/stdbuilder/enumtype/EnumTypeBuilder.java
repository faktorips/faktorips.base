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

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Builder that generates the java source for <code>EnumType</code> ips objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeBuilder extends DefaultJavaSourceFileBuilder {

    /** The package id identifiying the builder */
    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumTypeBuilder.enumtype.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    /** The builder config property name that indicates whether to use java 5 enum types. */
    private final static String USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY = "useJavaEnumTypes";

    /**
     * Creates a new <code>EnumTypeBuilder</code> that will belong to the given ips artefact builder
     * set.
     * 
     * @param builderSet The ips artefact builder set this builder shall be a part of.
     */
    public EnumTypeBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumTypeBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            return true;
        }

        return false;
    }

    /** Returns whether to generate an enum. */
    private boolean isEnum() {
        IEnumType enumType = getEnumType();
        if (!(enumType.isAbstract())) {
            if (ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())
                    && getIpsProject().getIpsArtefactBuilderSet().getConfig().getPropertyValueAsBoolean(
                            USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY) && enumType.getValuesArePartOfModel()) {
                return true;
            }
        }

        return false;
    }

    /** Returns whether to generate a class. */
    private boolean isClass() {
        if (!(getEnumType().getValuesArePartOfModel())) {
            return true;
        }
        if (!(ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject()))
                || !(getIpsProject().getIpsArtefactBuilderSet().getConfig()
                        .getPropertyValueAsBoolean(USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY))) {
            return true;
        }

        return false;
    }

    /** Returns whether to generate an interface. */
    private boolean isInterface() {
        if (!(isEnum()) && !(isClass())) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForJavatype() throws CoreException {
        IEnumType enumType = getEnumType();

        // Set general properties
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClass(isClass());
        mainSection.setEnum(isEnum());
        int classModifier = (isClass() && enumType.isAbstract() ? Modifier.PUBLIC | Modifier.ABSTRACT : Modifier.PUBLIC);
        mainSection.setClassModifier(classModifier);
        mainSection.setUnqualifiedName(getJavaNamingConvention().getTypeName(enumType.getName()));
        mainSection.getJavaDocForTypeBuilder().javaDoc(enumType.getDescription(), ANNOTATION_GENERATED);

        // Set supertype / implemented interface
        if (enumType.hasSuperEnumType()) {
            IEnumType superEnumType = enumType.findSuperEnumType();
            if (superEnumType != null) {
                if (isEnum() || isInterface()) {
                    mainSection.setExtendedInterfaces(new String[] { getQualifiedClassName(superEnumType) });
                } else {
                    mainSection.setSuperClass(getQualifiedClassName(superEnumType));
                }
            }
        }

        generateEnumValues(mainSection, isEnum());

        // Generate the attributes and the constructor
        if (isEnum() || isClass()) {
            generateAttributes(mainSection.getMemberVarBuilder());
            generateConstructor(mainSection.getConstructorBuilder());
        }

        // Generate the methods
        generateMethods(mainSection.getMethodBuilder());
    }

    /** Generates the java code for the enum values. */
    private void generateEnumValues(TypeSection mainSection, boolean javaAtLeast5) throws CoreException {
        IEnumType enumType = getEnumType();
        /*
         * If no enum value definition is added we need to make the semicolon anyway telling the
         * compiler that now the attribute section begins.
         */
        boolean appendSemicolon = javaAtLeast5;
        boolean lastEnumValueGenerated = false;

        /*
         * Generate the enum values if they are part of the model and if the enum type is not
         * abstract.
         */
        if (enumType.getValuesArePartOfModel() && !(enumType.isAbstract())) {
            // Go over all model side defined enum values
            List<IEnumValue> enumValues = enumType.getEnumValues();
            for (int i = 0; i < enumValues.size(); i++) {

                IEnumValue currentEnumValue = enumValues.get(i);
                // Generate only for valid enum values
                if (currentEnumValue.isValid()) {
                    IEnumAttributeValue currentIdentifierEnumAttributeValue = enumValues.get(i)
                            .findIdentifierEnumAttributeValue();
                    List<IEnumAttributeValue> currentEnumAttributeValues = currentEnumValue.getEnumAttributeValues();
                    if (javaAtLeast5) {
                        lastEnumValueGenerated = (i == enumValues.size() - 1);
                        generateEnumValueAsEnumDefinition(currentEnumAttributeValues,
                                currentIdentifierEnumAttributeValue, lastEnumValueGenerated, mainSection
                                        .getEnumDefinitionBuilder());
                        appendSemicolon = false;
                    } else {
                        generateEnumValueAsConstant(currentEnumAttributeValues, currentIdentifierEnumAttributeValue,
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
     * Generates the java source code for an enum value as enum definition (java at least 5).
     * 
     * @see #generateConstant(IEnumType, List, IEnumAttributeValue, JavaCodeFragmentBuilder)
     * 
     * @param enumAttributeValues The enum attribute values of the enum value to create.
     * @param identifierEnumAttributeValue The enum attribute value that is the identifier.
     * @param lastEnumDefinition Flag indicating whether this enum definition will be the last one.
     * @param enumDefinitionBuilder The java source code builder to use for creating enum
     *            definitions.
     */
    private void generateEnumValueAsEnumDefinition(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue identifierEnumAttributeValue,
            boolean lastEnumDefinition,
            JavaCodeFragmentBuilder enumDefinitionBuilder) throws CoreException {

        // Create enum definition source fragment
        appendLocalizedJavaDoc("ENUMVALUE", getEnumType(), enumDefinitionBuilder);
        JavaCodeFragment enumDefinitionFragment = new JavaCodeFragment();
        enumDefinitionFragment.append(identifierEnumAttributeValue.getValue().toUpperCase());
        enumDefinitionFragment.append(" (");
        appendEnumValueParameters(enumAttributeValues, enumDefinitionFragment);
        enumDefinitionFragment.append(')');
        if (!(lastEnumDefinition)) {
            enumDefinitionFragment.append(", ");
            enumDefinitionFragment.appendln(' ');
            enumDefinitionFragment.appendln(' ');
        } else {
            enumDefinitionFragment.append(';');
        }

        enumDefinitionBuilder.append(enumDefinitionFragment);
        enumDefinitionBuilder.appendln();
    }

    /**
     * Generates the java source code for an enum value as constant (java less than 5).
     * 
     * @see #generateEnumDefinition(IEnumType, List, IEnumAttributeValue, boolean,
     *      JavaCodeFragmentBuilder)
     * 
     * @param enumAttributeValues The enum attribute values of the enum value to create.
     * @param identifierEnumAttributeValue The enum attribute value that is the identifier.
     * @param constantBuilder The java source code builder to use for creating constants.
     */
    private void generateEnumValueAsConstant(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue identifierEnumAttributeValue,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        IEnumType enumType = getEnumType();

        // Build constant source
        appendLocalizedJavaDoc("ENUMVALUE", enumType, constantBuilder);
        JavaCodeFragment initExpression = new JavaCodeFragment();
        initExpression.append("new ");
        initExpression.append(enumType.getName());
        initExpression.append('(');
        appendEnumValueParameters(enumAttributeValues, initExpression);
        initExpression.append(')');

        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, enumType.getName(),
                identifierEnumAttributeValue.getValue().toUpperCase(), initExpression);
        constantBuilder.appendLn(' ');
    }

    /** Appends the parameter values to an enum value creation code fragment. */
    private void appendEnumValueParameters(List<IEnumAttributeValue> enumAttributeValues,
            JavaCodeFragment javaCodeFragment) throws CoreException {

        int numberEnumAttributeValues = enumAttributeValues.size();
        for (int i = 0; i < numberEnumAttributeValues; i++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
            IEnumAttribute referencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
            DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(referencedEnumAttribute.getDatatype());
            if (datatypeHelper != null) {
                javaCodeFragment.append(datatypeHelper.newInstance(currentEnumAttributeValue.getValue()));
            }
            if (i < numberEnumAttributeValues - 1) {
                javaCodeFragment.append(", ");
            }
        }
    }

    /** Generates the java code for the attributes. */
    private void generateAttributes(JavaCodeFragmentBuilder attributeBuilder) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumType().findAllEnumAttributes()) {
            String attributeName = currentEnumAttribute.getName();
            // The first character will be made lower case
            String codeName = getJavaNamingConvention().getMemberVarName(attributeName);

            if (currentEnumAttribute.isValid()) {
                /*
                 * If the generation artefact is a class and the attribute is inherited do not
                 * generate it for this class because it is also inherited in the source code.
                 */
                if (!(isClass() && currentEnumAttribute.isInherited())) {
                    appendLocalizedJavaDoc("ATTRIBUTE", attributeName, currentEnumAttribute, attributeBuilder);
                    attributeBuilder.varDeclaration(Modifier.PRIVATE | Modifier.FINAL, currentEnumAttribute
                            .getDatatype(), codeName);
                    attributeBuilder.appendLn(' ');
                }
            }
        }
    }

    /** Generates the java code for the constructor. */
    private void generateConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.findAllEnumAttributes();
        List<IEnumAttribute> validEnumAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid()) {
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
            argumentClasses[i] = currentEnumAttribute.getDatatype();
        }

        // Build method body
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (isClass() && enumType.hasSuperEnumType()) {
            createSuperConstructorCall(methodBody);
        }
        createAttributeInitialization(methodBody);

        appendLocalizedJavaDoc("CONSTRUCTOR", enumType.getName(), enumType, constructorBuilder);
        int constructorVisibility = (isClass() && enumType.isAbstract()) ? Modifier.PROTECTED : Modifier.PRIVATE;
        constructorBuilder.methodBegin(constructorVisibility, null, getJavaNamingConvention().getTypeName(
                enumType.getName()), argumentNames, argumentClasses);
        constructorBuilder.append(methodBody);
        constructorBuilder.methodEnd();
    }

    /** Creates the method call of the super constructor. */
    private void createSuperConstructorCall(JavaCodeFragment constructorMethodBody) throws CoreException {
        IEnumType enumType = getEnumType();

        constructorMethodBody.append("super(");

        List<IEnumAttribute> validSupertypeAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumType.findSuperEnumType().findAllEnumAttributes()) {
            if (currentEnumAttribute.isValid()) {
                validSupertypeAttributes.add(currentEnumAttribute);
            }
        }

        for (int i = 0; i < validSupertypeAttributes.size(); i++) {
            String name = validSupertypeAttributes.get(i).getName();
            constructorMethodBody.append(getJavaNamingConvention().getMemberVarName(name));
            if (i < validSupertypeAttributes.size() - 1) {
                constructorMethodBody.append(", ");
            }
        }

        constructorMethodBody.append(");");
        constructorMethodBody.appendln(' ');
        constructorMethodBody.appendln(' ');
    }

    /** Creates the attribute initialization code for the constructor. */
    private void createAttributeInitialization(JavaCodeFragment constructorMethodBody) throws CoreException {
        /*
         * If an enum is being generated we need to initialize all attributes, on the other hand, if
         * a class is being generated we only initialize the attributes that are not inherited.
         * Every attribute that shall be initialized must be valid.
         */
        List<IEnumAttribute> attributesToInit = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : getEnumType().findAllEnumAttributes()) {
            if (currentEnumAttribute.isValid()) {
                if (isEnum() || !(currentEnumAttribute.isInherited())) {
                    attributesToInit.add(currentEnumAttribute);
                }
            }
        }

        for (IEnumAttribute currentEnumAttribute : attributesToInit) {
            String currentName = getJavaNamingConvention().getMemberVarName(currentEnumAttribute.getName());
            constructorMethodBody.append("this.");
            constructorMethodBody.append(currentName);
            constructorMethodBody.append(" = ");
            constructorMethodBody.append(currentName);
            constructorMethodBody.append(';');
            constructorMethodBody.appendln();
        }
    }

    /** Generates the java code for the methods. */
    private void generateMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.findAllEnumAttributes();

        // Create getters for each attribute
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {

            if (currentEnumAttribute.isValid()) {
                String attributeName = currentEnumAttribute.getName();
                String description = currentEnumAttribute.getDescription();

                Datatype datatype = getIpsProject().findDatatype(currentEnumAttribute.getDatatype());
                String methodName = getJavaNamingConvention().getGetterMethodName(attributeName, datatype);

                // If an interface is to be generated then only generate the method signatures.
                if (isInterface()) {
                    if (!(currentEnumAttribute.isInherited())) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute,
                                methodBuilder);
                        methodBuilder.signature(Modifier.PUBLIC, currentEnumAttribute.getDatatype(), methodName, null,
                                null);
                        methodBuilder.appendLn(';');
                    }

                } else {
                    if (isEnum() || !(isClass() && currentEnumAttribute.isInherited())) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute,
                                methodBuilder);

                        // Build method body
                        JavaCodeFragment methodBody = new JavaCodeFragment();
                        methodBody.append("return ");
                        methodBody.append(getJavaNamingConvention().getMemberVarName(attributeName));
                        methodBody.append(';');

                        methodBuilder.methodBegin(Modifier.PUBLIC, currentEnumAttribute.getDatatype(), methodName,
                                null, null);
                        methodBuilder.append(methodBody);
                        methodBuilder.methodEnd();
                    }
                }

            }
        }
    }

    /** Returns the enum type for that code is being generated. */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
