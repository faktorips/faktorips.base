/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.internal.DependantObject;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class GenAssociation extends DefaultJavaGeneratorForIpsPart {

    protected IPolicyCmptTypeAssociation association;
    protected IPolicyCmptTypeAssociation reverseAssociation;
    protected IPolicyCmptType target;
    
    // the qualified name of the target's published interface.
    protected String targetInterfaceName;
    
    // the qualified name of the target's implementation class name. null if used in the interface builder!
    protected String targetImplClassName;
    
    protected String fieldName;
    
    public GenAssociation(IPolicyCmptTypeAssociation association, BasePolicyCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        
        super(association, builder, stringsSet, generateImplementation);
        this.association = association;
        this.reverseAssociation = association.findInverseAssociation(getIpsProject());
        this.target = association.findTargetPolicyCmptType(association.getIpsProject());
        this.targetInterfaceName = builder.getInterfaceBuilder().getQualifiedClassName(target);
        this.targetImplClassName = null;
        if (builder instanceof PolicyCmptImplClassBuilder) {
            targetImplClassName = builder.getQualifiedClassName(target);
        }
        this.fieldName = computeFieldName();
    }
    
    public ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return ((BasePolicyCmptTypeBuilder)getJavaSourceFileBuilder()).getInterfaceBuilder().getProductCmptInterfaceBuilder();
    }
    
    /**
     * Returns the name of the field/member variable for this association.
     */
    protected abstract String computeFieldName();
    
    public boolean isDerivedUnion() {
        return association.isDerivedUnion();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
        
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {

    }

    /**
     * {@inheritDoc}
     */
    // TODO go back to protected
    public void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {

    }

    /**
     * Gnerates a method to create a new child object if the association is a composite and
     * the target is not abstract. If the target is configurable by product a second method with the
     * product component type as argument is also generated.
     */
    protected void generateNewChildMethodsIfApplicable(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (!association.getAssociationType().isCompositionMasterToDetail()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(false, methodsBuilder);
        if (target.isConfigurableByProductCmptType() && target.findProductCmptType(getIpsProject())!=null) {
            generateMethodNewChild(true, methodsBuilder);
        }
    }
    
    private void generateMethodNewChild(
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        if (isGeneratingInterface()) {
            generateInterfaceMethodNewChild(inclProductCmptArg, builder);
        } else {
            generateImplMethodNewChild(inclProductCmptArg, builder);
        }
    }


    /**
     * Code sample without product component parameter:
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage();
     * </pre>
     * 
     * Code sample with product component parameter:
     * [Javadoc]
     * <pre>
     * public ICoverage newCoverage(ICoverageType coverageType);
     * </pre>
     */
    protected void generateInterfaceMethodNewChild(
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String targetTypeName = target.getName();
        String role = association.getTargetRoleSingular();
        if (inclProductCmptArg) { 
            String replacements[] = new String[]{targetTypeName, role, getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject()))};
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[]{targetTypeName, role}, builder);
        }
        generateSignatureNewChild(inclProductCmptArg, builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage() {
     *     ICoverage newCoverage = new Coverage();
     *     addCoverage(newCoverage); // for toMany associations, setCoverage(newCoverage) for to1
     *     newCoverage.initialize();
     *     return newCoverage;
     * }
     * </pre>
     */
    public void generateImplMethodNewChild(
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureNewChild(inclProductCmptArg, methodsBuilder);
        String addOrSetMethod = getMethodNameAddOrSetObject();
        String varName = "new" + association.getTargetRoleSingular();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append(" " + varName + " = new ");
        methodsBuilder.appendClassName(targetImplClassName);
        if (inclProductCmptArg) {
            methodsBuilder.appendln("(" + getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) + ");");  
        } else {
            methodsBuilder.appendln("();");
        }
        PolicyCmptImplClassBuilder implClassBuilder = (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        methodsBuilder.appendln(addOrSetMethod + "(" + varName + ");");
        methodsBuilder.appendln(varName + "." + implClassBuilder.getMethodNameInitialize() + "();");
        methodsBuilder.appendln("return " + varName + ";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Returns the name of the method that adds an object to a toMany association or that sets
     * the object in a to1 association respectively.
     */
    public abstract String getMethodNameAddOrSetObject();
    
    /**
     * Code sample without product component argument:
     * <pre>
     * public Coverage newCoverage()
     * </pre>
     * 
     * Code sample with product component argument:
     * <pre>
     * public Coverage newCoverage(ICoverageType coverageType)
     * </pre>
     */
    protected void generateSignatureNewChild(
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String methodName = getMethodNameNewChild(association);
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType(getIpsProject());
            argNames = new String[]{getParamNameForProductCmptInNewChildMethod(productCmptType)};
            argTypes = new String[]{getProductCmptInterfaceBuilder().getQualifiedClassName(productCmptType)};
        } else {
            argNames = EMPTY_STRING_ARRAY;
            argTypes = EMPTY_STRING_ARRAY;
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, argNames, argTypes);
    }

    /**
     * Returns the name of the method to create a new child object and add it to the parent. 
     */
    public String getMethodNameNewChild(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_NEW_CHILD_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the parameter in the new child mthod, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType) throws CoreException {
        String targetProductCmptClass = getProductCmptInterfaceBuilder().getQualifiedClassName(targetProductCmptType);
        return StringUtils.uncapitalize(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * <pre>
     * ((DependantObject)parentModelObject).setParentModelObjectInternal(this);
     * </pre>
     */
    protected JavaCodeFragment generateCodeToSynchronizeReverseComposition(
            String varName, String newValue) throws CoreException {
        
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("((");
        code.appendClassName(DependantObject.class);
        code.append(')');
        code.append(varName);
        code.append(")." + MethodNames.SET_PARENT);
        code.append('(');
        code.append(newValue);
        code.appendln(");");
        return code;
    }
    

}
