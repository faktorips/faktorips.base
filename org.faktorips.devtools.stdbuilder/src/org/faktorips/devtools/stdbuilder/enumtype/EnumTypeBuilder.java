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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
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

    /**
     * Creates a new <code>EnumTypeBuilder</code> that will belong to the given ips artefact builder
     * set.
     * 
     * @param builderSet The ips artefact builder set this builder shall be a part of.
     */
    public EnumTypeBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumTypeBuilder.class));
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
        if (!(getEnumType().isAbstract())) {
            if (ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())) {
                return true;
            }
        }

        return false;
    }

    /** Returns whether to generate a class. */
    private boolean isClass() {
        if (!(getEnumType().isAbstract())) {
            if (!(ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForJavatype() throws CoreException {
        IEnumType enumType = getEnumType();

        TypeSection mainSection = getMainTypeSection();
        mainSection.setClass(isClass());
        mainSection.setEnum(isEnum());
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getJavaNamingConvention().getTypeName(enumType.getName()));
        mainSection.getJavaDocForTypeBuilder().javaDoc(enumType.getDescription(), ANNOTATION_GENERATED);

        if (enumType.hasSuperEnumType()) {
            IEnumType superEnumType = enumType.findSuperEnumType();
            if (superEnumType != null) {
                mainSection.setExtendedInterfaces(new String[] { getQualifiedClassName(enumType.findSuperEnumType()) });
            }
        }

        generateEnumDefinitions(mainSection.getEnumDefinitionBuilder());
        generateAttributes(mainSection.getMemberVarBuilder());

        if (isEnum() || isClass()) {
            generateConstructor(mainSection.getConstructorBuilder());
        }

        generateMethods(mainSection.getMethodBuilder());
    }

    /** Generates the java code for the constants. */
    private void generateEnumDefinitions(JavaCodeFragmentBuilder enumDefinitionBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();

        if (enumType.getValuesArePartOfModel()) {

            // Go over all model side defined enum values
            List<IEnumValue> enumValues = enumType.getEnumValues();
            for (int i = 0; i < enumValues.size(); i++) {

                // Search for the enum attribute value that refers to the identifier enum attribute
                List<IEnumAttributeValue> currentEnumAttributeValues = enumValues.get(i).getEnumAttributeValues();
                for (IEnumAttributeValue currentEnumAttributeValue : currentEnumAttributeValues) {

                    IEnumAttribute currentReferencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
                    if (currentReferencedEnumAttribute.isIdentifier()) {
                        // Create enum constant source fragment
                        appendLocalizedJavaDoc("ENUMVALUE", enumType, enumDefinitionBuilder);
                        JavaCodeFragment constantsFragment = new JavaCodeFragment();
                        constantsFragment.append(currentEnumAttributeValue.getValue().toUpperCase());
                        constantsFragment.append(" (");

                        int numberEnumAttributeValues = currentEnumAttributeValues.size();
                        for (int k = 0; k < numberEnumAttributeValues; k++) {
                            IEnumAttribute referencedEnumAttribute = enumAttributes.get(k);
                            DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(
                                    referencedEnumAttribute.getDatatype());
                            if (datatypeHelper != null) {
                                constantsFragment.append(datatypeHelper.newInstance(currentEnumAttributeValues.get(k)
                                        .getValue()));
                            }
                            if (k < numberEnumAttributeValues - 1) {
                                constantsFragment.append(", ");
                            }
                        }

                        constantsFragment.append(')');
                        if (i < enumValues.size() - 1) {
                            constantsFragment.append(", ");
                        } else {
                            constantsFragment.append(';');
                        }
                        enumDefinitionBuilder.append(constantsFragment);

                        break;
                    }
                }
            }

            enumDefinitionBuilder.appendln();
        }
    }

    /** Generates the java code for the attributes. */
    private void generateAttributes(JavaCodeFragmentBuilder attributeBuilder) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumType().getEnumAttributes()) {
            // The first character will be made lower case
            String attributeName = currentEnumAttribute.getName();
            if (currentEnumAttribute.isValid()) {
                appendLocalizedJavaDoc("ATTRIBUTE", attributeName, currentEnumAttribute, attributeBuilder);
                attributeBuilder.varDeclaration(Modifier.PRIVATE | Modifier.FINAL, currentEnumAttribute.getDatatype(),
                        getJavaNamingConvention().getMemberVarName(attributeName));
            }
        }
    }

    /** Generates the java code for the constructor. */
    private void generateConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();
        int validAttributesCount = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid()) {
                validAttributesCount++;
            }
        }

        // Build method arguments
        String[] argumentNames = new String[validAttributesCount];
        String[] argumentClasses = new String[validAttributesCount];
        JavaNamingConvention javaNamingConvention = getJavaNamingConvention();
        int j = 0;
        for (int i = 0; i < enumAttributes.size(); i++) {
            IEnumAttribute currentEnumAttribute = enumAttributes.get(i);
            if (currentEnumAttribute.isValid()) {
                String attributeName = currentEnumAttribute.getName();
                argumentNames[j] = javaNamingConvention.getMemberVarName(attributeName);
                argumentClasses[j] = currentEnumAttribute.getDatatype();
                j++;
            }
        }

        // Build method body
        JavaCodeFragment methodBody = new JavaCodeFragment();
        for (String currentArgumentName : argumentNames) {
            methodBody.append("this.");
            methodBody.append(currentArgumentName);
            methodBody.append(" = ");
            methodBody.append(currentArgumentName);
            methodBody.append(';');
            methodBody.appendln();
        }

        appendLocalizedJavaDoc("CONSTRUCTOR", enumType.getName(), enumType, constructorBuilder);
        constructorBuilder.methodBegin(Modifier.PRIVATE, null, getJavaNamingConvention()
                .getTypeName(enumType.getName()), argumentNames, argumentClasses);
        constructorBuilder.append(methodBody);
        constructorBuilder.methodEnd();
    }

    /** Generates the java code for the methods. */
    private void generateMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();

        // Create getters for each attribute
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {

            if (currentEnumAttribute.isValid()) {
                String attributeName = currentEnumAttribute.getName();

                // Build method body
                JavaCodeFragment methodBody = new JavaCodeFragment();
                methodBody.append("return ");
                methodBody.append(getJavaNamingConvention().getMemberVarName(attributeName));
                methodBody.append(';');

                String description = currentEnumAttribute.getDescription();
                String name = currentEnumAttribute.getName();
                appendLocalizedJavaDoc("GETTER", name, description, currentEnumAttribute, methodBuilder);
                methodBuilder.methodBegin(Modifier.PUBLIC, currentEnumAttribute.getDatatype(),
                        getJavaNamingConvention().getGetterMethodName(attributeName,
                                getIpsProject().findDatatype(currentEnumAttribute.getDatatype())), null, null);
                methodBuilder.append(methodBody);
                methodBuilder.methodEnd();
            }
        }
    }

    /** Returns the enum type for that code is being generated. */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
