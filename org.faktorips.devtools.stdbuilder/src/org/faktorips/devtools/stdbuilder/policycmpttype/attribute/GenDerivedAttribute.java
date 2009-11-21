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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
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
public class GenDerivedAttribute extends GenAttribute {

    public GenDerivedAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) throws CoreException {
        super(genPolicyCmptType, a);
        ArgumentCheck.isTrue(a.isDerived());
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            if (!isOverwritten()) {
                generateAttributeNameConstant(builder);
            }
        } else {
            if (isNotPublished()) {
                generateAttributeNameConstant(builder);
            }
        }
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            if (getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL
                    && !isOverwritten()) {
                generateField(builder);
            }
        }
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            if (!isOverwritten()) {
                generateGetterInterface(builder);
            }
        } else {
            if (getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                generateGetterImplementationForOnTheFlyComputation(builder, ipsProject);
            } else {
                if (!isOverwritten()) {
                    generateGetterImplementation(builder);
                }
            }
        }
    }

    private void generateGetterImplementationForOnTheFlyComputation(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject) throws CoreException {

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(builder, getIpsProject(), false);
        }
        generateGetterSignature(builder);
        builder.openBracket();

        IProductCmptTypeMethod formulaSignature = getPolicyCmptTypeAttribute().findComputationMethod(ipsProject);
        if (!getPolicyCmptTypeAttribute().isProductRelevant() || formulaSignature == null
                || formulaSignature.validate(ipsProject).containsErrorMsg()) {
            builder.append("return ");
            builder.append(datatypeHelper.newInstance(attribute.getDefaultValue()));
            builder.appendln(";");
        } else {
            IParameter[] parameters = formulaSignature.getParameters();
            boolean resolveTypesToPublishedInterface = formulaSignature.getModifier().isPublished();
            String[] paramNames = BuilderHelper.extractParameterNames(parameters);
            String[] paramTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                    resolveTypesToPublishedInterface, getGenPolicyCmptType().getBuilderSet(), ipsProject);

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
            builder.appendClassName(getGenPolicyCmptType().getBuilderSet().getGenerator(getProductCmptType(ipsProject))
                    .getQualifiedClassNameForProductCmptTypeGen(false));
            builder.append(')');
            builder.append(getGenPolicyCmptType().getBuilderSet().getGenerator(getProductCmptType(ipsProject))
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
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        super.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsObjectPartContainer,
                recursivelyIncludeChildren);

        addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        if (isDerivedByExplicitMethodCall()) {
            addMemberVarToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        super.getGeneratedJavaElementsForPublishedInterface(javaElements, generatedJavaType, ipsObjectPartContainer,
                recursivelyIncludeChildren);

        if (isPublished()) {
            addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

}
