/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.util.ArgumentCheck;

/**
 * Code generator for a derived attribute.
 * 
 * @author Jan Ortmann
 */
public class GenDerivedAttribute extends GenPolicyCmptTypeAttribute {

    public GenDerivedAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) {
        super(genPolicyCmptType, a);
        ArgumentCheck.isTrue(a.isDerived());
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            if (!getAttribute().isOverwrite()) {
                generateAttributeNameConstant(builder);
            }
        } else {
            if (!(isPublished())) {
                generateAttributeNameConstant(builder);
            }
        }
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            if ((getAttribute()).getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL
                    && !getAttribute().isOverwrite()) {
                generateField(builder);
            }
        }
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            if (!getAttribute().isOverwrite()) {
                generateGetterInterface(builder);
            }
        } else {
            if ((getAttribute()).getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                generateGetterImplementationForOnTheFlyComputation(builder, ipsProject);
            } else {
                if (!getAttribute().isOverwrite()) {
                    generateGetterImplementation(builder);
                }
            }
        }
    }

    private void generateGetterImplementationForOnTheFlyComputation(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject) throws CoreException {

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getAttribute().isOverwrite()) {
            appendOverrideAnnotation(builder, getIpsProject(), false);
        } else {
            if (getAttribute().getModifier().isPublished()) {
                appendOverrideAnnotation(builder, getIpsProject(), true);
            }
        }
        generateGetterSignature(builder);
        builder.openBracket();

        IProductCmptTypeMethod formulaSignature = (getAttribute()).findComputationMethod(ipsProject);
        if (!(getAttribute()).isProductRelevant() || formulaSignature == null
                || formulaSignature.validate(ipsProject).containsErrorMsg()) {
            builder.append("return ");
            builder.append(getDatatypeHelper().newInstance(getAttribute().getDefaultValue()));
            builder.appendln(";");
        } else {
            IParameter[] parameters = formulaSignature.getParameters();
            boolean resolveTypesToPublishedInterface = formulaSignature.getModifier().isPublished();
            String[] paramNames = BuilderHelper.extractParameterNames(parameters);
            String[] paramTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                    resolveTypesToPublishedInterface, getGenType().getBuilderSet(), ipsProject);

            builder.appendln("// TODO Belegung der Berechnungsparameter implementieren");
            JavaCodeFragment paramFragment = new JavaCodeFragment();
            paramFragment.append('(');
            for (int i = 0; i < paramNames.length; i++) {
                builder.appendClassName(paramTypes[i]);
                builder.append(' ');
                builder.append(paramNames[i]);
                builder.append(" = ");
                Datatype paramDataype = ipsProject.findDatatype(parameters[i].getDatatype());
                DatatypeHelper helper = ipsProject.getDatatypeHelper(paramDataype);
                if (paramDataype.isPrimitive()) {
                    builder.append(((ValueDatatype)paramDataype).getDefaultValue());
                } else {
                    if (helper != null) {
                        JavaCodeFragment nullExpressionFragment = helper.nullExpression();
                        builder.append(nullExpressionFragment);
                    } else {
                        builder.append("null");
                    }
                }
                builder.appendln(";");
                if (i > 0) {
                    paramFragment.append(", ");
                }
                paramFragment.append(paramNames[i]);
            }
            paramFragment.append(")");
            builder.append(" return ((");
            builder.appendClassName(getGenType().getBuilderSet().getGenerator(getProductCmptType(ipsProject))
                    .getQualifiedClassNameForProductCmptTypeGen(false));
            builder.append(')');
            builder.append(getGenType().getBuilderSet().getGenerator(getProductCmptType(ipsProject))
                    .getMethodNameGetProductCmptGeneration());
            builder.append("()).");
            builder.append(formulaSignature.getName());
            builder.append(paramFragment);
            builder.append(";");
        }

        builder.closeBracket();
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isDerivedByExplicitMethodCall()) {
            if (!(isPublished()) || !(isOverwrite())) {
                super.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsElement);
            }
            if (!(isOverwrite())) {
                addMemberVarToGeneratedJavaElements(javaElements, generatedJavaType);
                addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            }

        } else {
            super.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsElement);
            addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isOverwrite()) {
            return;
        }

        super.getGeneratedJavaElementsForPublishedInterface(javaElements, generatedJavaType, ipsElement);

        if (isPublished()) {
            addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

}
