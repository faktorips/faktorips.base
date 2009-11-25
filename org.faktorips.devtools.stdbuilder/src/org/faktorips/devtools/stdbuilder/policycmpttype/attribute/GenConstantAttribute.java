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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.util.ArgumentCheck;

/**
 * Code generator for a constant attribute.
 * 
 * @author Jan Ortmann
 */
public class GenConstantAttribute extends GenPolicyCmptTypeAttribute {

    public GenConstantAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) throws CoreException {
        super(genPolicyCmptType, a);
        ArgumentCheck.isTrue(a.getAttributeType() == AttributeType.CONSTANT);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (isPublished() == generatesInterface) {
            generateAttributeNameConstant(builder);
            generateConstant(builder);
        }
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        // Nothing to do.
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        // Nothing to do.
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public final static String NAME = &quot;MotorPlus&quot;;
     * </pre>
     */
    protected void generateConstant(JavaCodeFragmentBuilder builder) throws CoreException {
        String comment = getLocalizedText("FIELD_VALUE_JAVADOC", getAttribute().getName());
        builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        int modifier = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC;
        JavaCodeFragment initialValueExpression = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        builder.varDeclaration(modifier, getJavaClassName(), getConstantMemberVarName(), initialValueExpression);
    }

    public String getConstantMemberVarName() {
        return getJavaNamingConvention().getConstantClassVarName(getAttribute().getName());
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        super.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsObjectPartContainer);

        if (!(isPublished())) {
            IField constantMember = generatedJavaType.getField(getConstantMemberVarName());
            javaElements.add(constantMember);
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        super.getGeneratedJavaElementsForPublishedInterface(javaElements, generatedJavaType, ipsObjectPartContainer);

        if (isPublished()) {
            IField constantMember = generatedJavaType.getField(getConstantMemberVarName());
            javaElements.add(constantMember);
        }
    }

}
