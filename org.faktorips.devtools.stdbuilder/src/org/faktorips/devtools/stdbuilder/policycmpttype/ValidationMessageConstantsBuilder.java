/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class ValidationMessageConstantsBuilder extends DefaultJavaSourceFileBuilder {

    private final String CONSTANT_RESOURCE_BUNDLE_NAME = "RESOURCE_BUNDLE_NAME";
    private final ValidationMessagesPropertiesBuilder propertiesBuilder;

    public ValidationMessageConstantsBuilder(DefaultBuilderSet builderSet, String kindId,
            ValidationMessagesPropertiesBuilder propertiesBuilder) {
        super(builderSet, kindId, new LocalizedStringsSet(ValidationMessageConstantsBuilder.class));
        this.propertiesBuilder = propertiesBuilder;
        setMergeEnabled(true);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setSuperClass(MessagesHelper.class.getName());
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.setClass(true);

        // generateCodeForProductCmptTypeAttributes(mainSection);
        // generateCodeForPolicyCmptTypeAttributes(mainSection);
        // generateCodeForAssociations(mainSection.getMemberVarBuilder(),
        // mainSection.getMethodBuilder());
        // generateCodeForMethodsDefinedInModel(mainSection.getMethodBuilder());
        generateConstants(mainSection.getConstantBuilder());
        generateConstructors(mainSection.getConstructorBuilder());
        // generateTypeJavadoc(mainSection.getJavaDocForTypeBuilder());
        // generateTypeAnnotations(mainSection.getAnnotationsForTypeBuilder());
        // generateOtherCode(mainSection.getConstantBuilder(), mainSection.getMemberVarBuilder(),
        // mainSection.getMethodBuilder());
        // generateCodeForJavatype(mainSection);
    }

    private void generateConstants(JavaCodeFragmentBuilder constantBuilder) {
        generateStringConstant(constantBuilder, CONSTANT_RESOURCE_BUNDLE_NAME,
                propertiesBuilder.getResourceBundleBaseName(getIpsSrcFolderEntry()));
        String keyPrefix = getIpsObject().getQualifiedName() + "_";
        for (IValidationRule rule : ((IPolicyCmptType)getIpsObject()).getValidationRules()) {
            String key = keyPrefix + rule.getName();
            String constantName = StringUtil.camelCaseToUnderscore(key, false);
            generateStringConstant(constantBuilder, constantName, key);
        }
    }

    protected void generateStringConstant(JavaCodeFragmentBuilder constantBuilder, String varName, String value) {
        constantBuilder.append("public static final ").appendClassName(String.class).append(" ").append(varName)
                .append(" = \"").append(value).appendln("\";");
    }

    protected IIpsSrcFolderEntry getIpsSrcFolderEntry() {
        return (IIpsSrcFolderEntry)getIpsSrcFile().getIpsPackageFragment().getRoot().getIpsObjectPathEntry();
    }

    private void generateConstructors(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        String parameterClassLoader = "classLoader";
        constructorBuilder.append("public ").append(getUnqualifiedClassName()).appendln("(")
                .appendClassName(ClassLoader.class).append(" ").append(parameterClassLoader).append(")").openBracket();
        constructorBuilder.append("super(").append(CONSTANT_RESOURCE_BUNDLE_NAME).append(", ")
                .append(parameterClassLoader).appendln(");");
        constructorBuilder.closeBracket();
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        // not supported
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return "ValidationMessages";
    }

}
