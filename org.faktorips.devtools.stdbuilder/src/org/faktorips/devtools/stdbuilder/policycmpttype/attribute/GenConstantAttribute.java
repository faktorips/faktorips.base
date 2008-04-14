/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Code generator for a constant attribute.
 * 
 * @author Jan Ortmann
 */
public class GenConstantAttribute extends GenAttribute {

    public GenConstantAttribute(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder,
            LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        super(a, builder, stringsSet, generateImplementation);
        ArgumentCheck.isTrue(a.getAttributeType() == AttributeType.CONSTANT);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isGeneratingPolicySide()) {
            if (isPublished() == isGeneratingInterface()) {
                generateAttributeNameConstant(builder);
                generateConstant(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do
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
        String comment = getLocalizedText("FIELD_VALUE_JAVADOC", attributeName);
        builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        String varName = getJavaNamingConvention().getConstantClassVarName(attributeName);
        int modifier = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC;
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        builder.varDeclaration(modifier, getJavaClassName(), varName, initialValueExpression);
    }

}
