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
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Code generator for a derived attribute.
 * 
 * @author Jan Ortmann
 */
public class GenDerivedAttribute extends GenAttribute {

    public GenDerivedAttribute(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {

        super(a, builder, stringsSet);
        ArgumentCheck.isTrue(a.isDerived());
    }

    /**
     * Returns the policy component interface builder.
     */
    private PolicyCmptImplClassBuilder getImplClassBuilder() {
        if (getJavaSourceFileBuilder() instanceof PolicyCmptImplClassBuilder) {
            return (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        }
        return null;
    }

    /**
     * Returns the policy component interface builder.
     */
    private PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return ((PolicyCmptImplClassBuilder)getJavaSourceFileBuilder()).getInterfaceBuilder();
    }

    private ProductCmptGenImplClassBuilder getProductCmptGenImplClassBuilder() {
        return getImplClassBuilder().getProductCmptGenImplBuilder();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (isGeneratingPolicySide()) {
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
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (!generatesInterface && isGeneratingPolicySide()) {
            if (getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL
                    && !isOverwritten()) {
                generateField(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (isGeneratingPolicySide()) {
            if(generatesInterface){
                if (!isOverwritten()) {
                    generateGetterInterface(builder);
                }
            } else {
                if (getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                    generateGetterImplementationForOnTheFlyComputation(builder);
                } else {
                    if (!isOverwritten()) {
                        generateGetterImplementation(builder);
                    }
                }
            }
        }
    }

    private void generateGetterImplementationForOnTheFlyComputation(JavaCodeFragmentBuilder builder)
            throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateGetterSignature(builder);
        builder.openBracket();

        IProductCmptTypeMethod formulaSignature = getPolicyCmptTypeAttribute().findComputationMethod(ipsProject);
        if (!getPolicyCmptTypeAttribute().isProductRelevant() || formulaSignature == null
                || formulaSignature.validate(getIpsProject()).containsErrorMsg()) {
            builder.append("return ");
            builder.append(datatypeHelper.newInstance(attribute.getDefaultValue()));
            builder.appendln(";");
        } else {
            IParameter[] parameters = formulaSignature.getParameters();
            boolean resolveTypesToPublishedInterface = formulaSignature.getModifier().isPublished();
            String[] paramNames = BuilderHelper.extractParameterNames(parameters);
            String[] paramTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                    resolveTypesToPublishedInterface, ipsProject, getImplClassBuilder(),
                    getProductCmptGenImplClassBuilder());

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
            builder.appendClassName(getProductCmptGenImplClassBuilder().getQualifiedClassName(getProductCmptType()));
            builder.append(')');
            builder.append(getInterfaceBuilder().getMethodNameGetProductCmptGeneration(getProductCmptType()));
            builder.append("()).");
            builder.append(formulaSignature.getName());
            builder.append(paramFragment);
            builder.append(";");
        }
        builder.closeBracket();
    }

}
