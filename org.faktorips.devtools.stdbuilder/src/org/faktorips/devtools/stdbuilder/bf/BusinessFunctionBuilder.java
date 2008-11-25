/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.bf;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

public class BusinessFunctionBuilder extends DefaultJavaSourceFileBuilder {

    public final static String PACKAGE_STRUCTURE_KIND_ID = "BusinessFunctionBuilder.bf.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    public BusinessFunctionBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(BusinessFunctionBuilder.class));
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(BusinessFunctionIpsObjectType.getInstance())) {
            return true;
        }
        return false;
    }

    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setUnqualifiedName(getBusinessFunction().getName());
        mainSection.setClassModifier(Modifier.PUBLIC | Modifier.FINAL);
        mainSection.setClass(true);
        // TODO add IBusinessFunction interface to runtime
        // mainSection.setExtendedInterfaces(new String[] { IBusinessFunction.class.getName()});

        JavaCodeFragmentBuilder memberBuilder = getMainTypeSection().getMemberVarBuilder();
        JavaCodeFragmentBuilder methodBuilder = getMainTypeSection().getMethodBuilder();
        generateMethodExecute(methodBuilder);
        generateStartMethod(methodBuilder);
        generateEndMethod(methodBuilder);
        generateCodeForParameters(memberBuilder, methodBuilder);
        generateMemberVariableForCallBusinessFunctionAction(memberBuilder);
        generateMethodCallMethodAction(methodBuilder);
        generateCodeForInlineActions(methodBuilder);
        generateMethodCallBusinessFunctionAction(methodBuilder);
        generateMethodForMerges(methodBuilder);
        generateMethodForDecisions(methodBuilder);
        generateConstructor(mainSection.getConstructorBuilder());
        generateMethodCreateCallBusinessFunction(methodBuilder);
    }

    private void generateStartMethod(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.method(Modifier.PUBLIC, Void.TYPE, "start", new String[0], new Class[0], new JavaCodeFragment(),
                "JavaDoc");
    }

    private void generateEndMethod(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.method(Modifier.PUBLIC, Void.TYPE, "end", new String[0], new Class[0], new JavaCodeFragment(),
                "JavaDoc");
    }

    private void generateCodeForInlineActions(JavaCodeFragmentBuilder methodBuilder) {
        List bfElements = getBusinessFunction().getBFElements();
        // TODO call validation
        for (Iterator it = bfElements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if (element.getType().equals(BFElementType.ACTION_INLINE)) {
                methodBuilder.method(Modifier.PUBLIC, Void.TYPE, element.getName(), new String[0], new Class[0],
                        new JavaCodeFragment(), "JavaDoc");
            }
        }
    }

    private void generateMethodCallMethodAction(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List bfElements = getBusinessFunction().getBFElements();
        // TODO call validation
        for (Iterator it = bfElements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
                IActionBFE actionBFE = (IActionBFE)element;
                IParameterBFE parameter = actionBFE.getParameter();
                JavaCodeFragment body = new JavaCodeFragment();
                body.append(parameter.getName());
                body.append('.');
                body.append(actionBFE.getExecutableMethodName());
                body.append("();");
                String methodName = getCallMethodName(actionBFE);
                methodBuilder.method(Modifier.PUBLIC, Void.TYPE, methodName, new String[0], new Class[0], body,
                        "JavaDoc");
            }
        }
    }

    private String getCallMethodName(IActionBFE actionBFE) {
        return StringUtils.uncapitalize(actionBFE.getExecutableMethodName());
    }

    private void generateMemberVariableForCallBusinessFunctionAction(JavaCodeFragmentBuilder memberVarBuilder) {
        List bfElements = getBusinessFunction().getBFElements();
        // TODO call validation
        for (Iterator it = bfElements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                IActionBFE actionBFE = (IActionBFE)element;
                memberVarBuilder.varDeclaration(Modifier.PRIVATE, actionBFE.getReferencedBfQualifiedName(),
                        getCallBusinessFunctionVarName(actionBFE));
            }
        }
    }

    private String getCallBusinessFunctionVarName(IActionBFE actionBFE) {
        return StringUtils.uncapitalize(actionBFE.getReferencedBfUnqualifedName());
    }

    private void generateMethodCallBusinessFunctionAction(JavaCodeFragmentBuilder methodBuilder) {
        List bfElements = getBusinessFunction().getBFElements();
        // TODO call validation
        for (Iterator it = bfElements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                IActionBFE action = (IActionBFE)element;
                JavaCodeFragment body = new JavaCodeFragment();
                body.append(getCallBusinessFunctionVarName(action));
                body.append('.');
                // TODO define method name in model
                body.append(getExecuteMethodName());
                body.append("();");
                methodBuilder.method(Modifier.PUBLIC, Void.TYPE, getMethodNameCallBusinessFunctionAction(action), new String[0], new Class[0], body,
                        "JavaDoc");
            }
        }
    }

    private String getMethodNameCallBusinessFunctionAction(IActionBFE action){
        return StringUtils.uncapitalize(action.getTarget()); 
    }
    
    private String getStartMethodName() {
        return "start";
    }

    private String getEndMethodName() {
        return "end";
    }

    private String getExecuteMethodName() {
        return "execute";
    }

    private void generateMethodExecute(JavaCodeFragmentBuilder methodBuilder) {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append(getStartMethodName());
        body.append("();");
        generateControlFlowMethodBody(body, getBusinessFunction().getStart(), methodBuilder);
        body.append(getEndMethodName());
        body.append("();");
        methodBuilder.method(Modifier.PUBLIC, Void.TYPE, getExecuteMethodName(), new String[0], new Class[0], body,
                "javadoc");
    }

    private void generateMethodForMerges(JavaCodeFragmentBuilder methodBuilder) {
        List merges = getBusinessFunction().getBFElements();
        for (Iterator it = merges.iterator(); it.hasNext();) {
            IBFElement merge = (IBFElement)it.next();
            if (merge.getType().equals(BFElementType.MERGE)) {
                generateMethodForMerge(merge, methodBuilder);
            }
        }
    }

    private void generateMethodForMerge(IBFElement merge, JavaCodeFragmentBuilder methodBuilder) {
        JavaCodeFragment body = new JavaCodeFragment();
        generateControlFlowMethodBody(body, merge, methodBuilder);
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getDecisionBFEMethodName(merge), new String[0], new Class[0],
                body, "javadoc");
    }

    private void generateMethodForDecisions(JavaCodeFragmentBuilder methodBuilder) {
        List decisions = getBusinessFunction().getBFElements();
        for (Iterator it = decisions.iterator(); it.hasNext();) {
            IBFElement decision = (IBFElement)it.next();
            if (decision.getType().equals(BFElementType.DECISION)) {
                generateMethodForDecision(decision, methodBuilder);
            }
        }
    }

    // TODO search for better method name since usage is for merge and decision
    private String getDecisionBFEMethodName(IBFElement decision) {
        return StringUtils.uncapitalize(decision.getName());
    }

    private void appendMethodCall(IBFElement element, JavaCodeFragment body) {
        if (element.getType().equals(BFElementType.DECISION) || element.getType().equals(BFElementType.MERGE)
                || element.getType().equals(BFElementType.ACTION_INLINE)) {
            body.append(StringUtils.uncapitalize(element.getName()));
            body.append("();");
            return;
        }
        if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            body.append(getCallBusinessFunctionVarName((IActionBFE)element));
            body.append('.');
            body.append(getExecuteMethodName());
            body.append("();");
        }
        if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
            body.append(getCallMethodName((IActionBFE)element));
            body.append("();");
        }
        if (element.getType().equals(BFElementType.START)) {
            body.append(getStartMethodName());
            body.append("();");
        }
        if (element.getType().equals(BFElementType.END)) {
            body.append(getEndMethodName());
            body.append("();");
        }
    }

    // TODO if-else blocks for decisions are missing
    private void generateControlFlowMethodBody(JavaCodeFragment body,
            IBFElement inputElement,
            JavaCodeFragmentBuilder methodBuilder) {
        List outgoingFlows = inputElement.getOutgoingControlFlow();
        if (inputElement.getType().equals(BFElementType.DECISION)) {
            body.append("if(true)");
            body.appendOpenBracket();
            for (Iterator it = outgoingFlows.iterator(); it.hasNext();) {
                IControlFlow controlFlow = (IControlFlow)it.next();
                generateSingleControlFlowMethodBody(methodBuilder, body, controlFlow);
                body.appendCloseBracket();
                if (it.hasNext()) {
                    body.append("else if(true)");
                    body.appendOpenBracket();
                }
            }
            return;
        }
        if (!outgoingFlows.isEmpty()) {
            generateSingleControlFlowMethodBody(methodBuilder, body, (IControlFlow)outgoingFlows.get(0));
        }
    }

    private void generateSingleControlFlowMethodBody(JavaCodeFragmentBuilder methodBuilder,
            JavaCodeFragment body,
            IControlFlow controlFlow) {
        IBFElement element = controlFlow.getTarget();
        if (element.getType().equals(BFElementType.END)) {
            return;
        }
        appendMethodCall(element, body);
        if (!(element.getType().equals(BFElementType.DECISION) || element.getType().equals(BFElementType.MERGE))) {
            body.appendln();
            generateControlFlowMethodBody(body, element, methodBuilder);
        }
    }

    private void generateMethodForDecision(IBFElement decision, JavaCodeFragmentBuilder methodBuilder) {
        JavaCodeFragment body = new JavaCodeFragment();
        generateControlFlowMethodBody(body, decision, methodBuilder);
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getDecisionBFEMethodName(decision), new String[0],
                new Class[0], body, "javadoc");
    }

    private String getParameterBFEVarName(IParameterBFE parameterBFE) {
        return StringUtils.uncapitalize(parameterBFE.getName());
    }

    private String getJavaClassName(Datatype datatype) throws CoreException {
        if (datatype instanceof IPolicyCmptType) {
            return ((StandardBuilderSet)getBuilderSet()).getGenerator((IPolicyCmptType)datatype).getQualifiedName(true);
        } else if (datatype instanceof IProductCmptType) {
            return ((StandardBuilderSet)getBuilderSet()).getGenerator((IProductCmptType)datatype)
                    .getQualifiedName(true);
        } else {
            DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);
            return helper.getJavaClassName();
        }
    }

    private void generateCodeForParameters(JavaCodeFragmentBuilder memberBuilder, JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {
        List parameters = getBusinessFunction().getParameterBFEs();
        for (Iterator it = parameters.iterator(); it.hasNext();) {
            IParameterBFE parameter = (IParameterBFE)it.next();
            Datatype datatype = parameter.findDatatype();
            String javaClassName = getJavaClassName(datatype);
            memberBuilder.varDeclaration(Modifier.PRIVATE, javaClassName, getParameterBFEVarName(parameter));
            JavaCodeFragment body = new JavaCodeFragment();
            body.append("return ");
            body.append(parameter.getName());
            body.append(';');
            methodBuilder.method(Modifier.PUBLIC, javaClassName, StringUtils.capitalize(parameter.getName()),
                    new String[0], new String[0], body, "JavaDoc");
        }
    }

    private String getMethodNameCreateCallBusinessFunction(IActionBFE action) {
        return "create" + StringUtils.capitalize(action.getReferencedBfUnqualifedName());
    }

    private void generateMethodCreateCallBusinessFunction(JavaCodeFragmentBuilder methodBuilder) {
        List elements = getBusinessFunction().getBFElements();
        for (Iterator it = elements.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if(!element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)){
                continue;
            }
            IActionBFE action = (IActionBFE)element;
            if (action.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                methodBuilder.method(Modifier.PRIVATE, action.getReferencedBfQualifiedName(), getMethodNameCreateCallBusinessFunction(action),
                        new String[0], new String[0], new JavaCodeFragment("return null;"), "javadoc");
            }
        }
    }

    private void generateConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        ArrayList parameterNames = new ArrayList();
        ArrayList parameterTypes = new ArrayList();

        List parameters = getBusinessFunction().getParameterBFEs();
        for (Iterator it = parameters.iterator(); it.hasNext();) {
            IParameterBFE parameter = (IParameterBFE)it.next();
            parameterNames.add(getParameterBFEVarName(parameter));
            Datatype datatype = parameter.findDatatype();
            parameterTypes.add(getJavaClassName(datatype));
        }
        JavaCodeFragment body = new JavaCodeFragment();
        for (Iterator it = parameterNames.iterator(); it.hasNext();) {
            String name = (String)it.next();
            body.append("this.");
            body.append(name);
            body.append(" = ");
            body.append(name);
            body.append(';');
            if (it.hasNext()) {
                body.appendln();
            }
        }

        List bfActions = getBusinessFunction().getBFElements();
        for (Iterator it = bfActions.iterator(); it.hasNext();) {
            IBFElement element = (IBFElement)it.next();
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                IActionBFE action = (IActionBFE)element;
                body.append(getCallBusinessFunctionVarName(action));
                body.append(" = ");
                body.append(getMethodNameCreateCallBusinessFunction(action));
                body.append("();");
                if(it.hasNext()){
                    body.appendln();
                }
            }
        }
        constructorBuilder.method(Modifier.PUBLIC, null, getBusinessFunction().getName(), (String[])parameterNames
                .toArray(new String[parameterNames.size()]), (String[])parameterTypes.toArray(new String[parameterTypes
                .size()]), body, "javadoc");
    }

    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getIpsObject();
    }
}
