/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.bf;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
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
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

public class BusinessFunctionBuilder extends DefaultJavaSourceFileBuilder {

    public final static String PACKAGE_STRUCTURE_KIND_ID = "BusinessFunctionBuilder.bf.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    public BusinessFunctionBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(BusinessFunctionBuilder.class));
        setMergeEnabled(true);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(BusinessFunctionIpsObjectType.getInstance())) {
            return true;
        }
        return false;
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setUnqualifiedName(getBusinessFunction().getName());
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setClass(true);
        String description = getDescriptionInGeneratorLanguage(getBusinessFunction());
        mainSection.getJavaDocForTypeBuilder().javaDoc(description, ANNOTATION_GENERATED);
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
        generateCodeForDecisions(methodBuilder);
        generateConstructor(mainSection.getConstructorBuilder());
        generateMethodCreateCallBusinessFunction(methodBuilder);
    }

    private void generateStartMethod(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, "start", new String[0], new Class[0], new JavaCodeFragment(),
                "", ANNOTATION_GENERATED);
    }

    private void generateEndMethod(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, "end", new String[0], new Class[0], new JavaCodeFragment(),
                "", ANNOTATION_GENERATED);
    }

    private String getMethodNameInlineAction(IActionBFE action) {
        return StringUtils.uncapitalize(action.getName());
    }

    private void generateCodeForInlineActions(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> bfElements = getBusinessFunction().getBFElements();
        for (IBFElement element : bfElements) {
            if (element.getType().equals(BFElementType.ACTION_INLINE) && element.isValid(getIpsProject())) {
                String description = getDescriptionInGeneratorLanguage(element);
                methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getMethodNameInlineAction((IActionBFE)element),
                        new String[0], new Class[0], new JavaCodeFragment(), description, ANNOTATION_GENERATED);
            }
        }
    }

    private void generateMethodCallMethodAction(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> bfElements = getBusinessFunction().getBFElements();
        List<String> alreadyGenerated = new ArrayList<String>();
        for (IBFElement element : bfElements) {
            if (element.getType().equals(BFElementType.ACTION_METHODCALL) && element.isValid(getIpsProject())) {
                IActionBFE actionBFE = (IActionBFE)element;
                if (alreadyGenerated.contains(actionBFE.getExecutableMethodName())) {
                    continue;
                }
                IParameterBFE parameter = actionBFE.getParameter();
                JavaCodeFragment body = new JavaCodeFragment();
                body.append(parameter.getName());
                body.append('.');
                body.append(actionBFE.getExecutableMethodName());
                body.append("();");
                String methodName = getMethodNameCallMethodAction(actionBFE);
                String javaDoc = "Calls the method " + actionBFE.getExecutableMethodName() + " on the parameter "
                        + parameter.getName() + ".";
                ArrayList<String> annotatios = new ArrayList<String>();
                annotatios.addAll(Arrays.asList(ANNOTATION_GENERATED));
                annotatios.add("see " + parameter.findDatatype().getQualifiedName() + "#"
                        + actionBFE.getExecutableMethodName());
                methodBuilder.method(Modifier.PRIVATE, Void.TYPE, methodName, new String[0], new Class[0], body,
                        javaDoc, annotatios.toArray(new String[annotatios.size()]));
                alreadyGenerated.add(actionBFE.getExecutableMethodName());
            }
        }
    }

    private String getMethodNameCallMethodAction(IActionBFE actionBFE) {
        return StringUtils.uncapitalize(actionBFE.getExecutableMethodName());
    }

    private void generateMemberVariableForCallBusinessFunctionAction(JavaCodeFragmentBuilder memberVarBuilder)
            throws CoreException {
        List<IBFElement> bfElements = getBusinessFunction().getBFElements();
        List<String> alreadyGenerated = new ArrayList<String>();
        for (IBFElement element : bfElements) {
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)
                    && element.isValid(getIpsProject())) {
                IActionBFE actionBFE = (IActionBFE)element;
                if (alreadyGenerated.contains(actionBFE.getReferencedBfQualifiedName())) {
                    continue;
                }
                memberVarBuilder.javaDoc("", ANNOTATION_GENERATED);
                IBusinessFunction bf = actionBFE.findReferencedBusinessFunction();
                memberVarBuilder.varDeclaration(Modifier.PRIVATE, getQualifiedClassName(bf),
                        getCallBusinessFunctionVarName(actionBFE));
                alreadyGenerated.add(actionBFE.getReferencedBfQualifiedName());
            }
        }
    }

    private String getCallBusinessFunctionVarName(IActionBFE actionBFE) {
        return StringUtils.uncapitalize(actionBFE.getReferencedBfUnqualifedName());
    }

    private void generateMethodCallBusinessFunctionAction(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> bfElements = getBusinessFunction().getBFElements();
        List<String> alreadyGenerated = new ArrayList<String>();
        for (IBFElement element : bfElements) {
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)
                    && element.isValid(getIpsProject())) {
                IActionBFE action = (IActionBFE)element;
                if (alreadyGenerated.contains(getMethodNameCallBusinessFunctionAction(action))) {
                    continue;
                }
                JavaCodeFragment body = new JavaCodeFragment();
                body.append(getCallBusinessFunctionVarName(action));
                body.append('.');
                body.append(getExecuteMethodName());
                body.append("();");
                StringBuilder sb = new StringBuilder();
                sb.append("Executes the business function ");
                sb.append(action.getReferencedBfUnqualifedName());
                sb.append(".");
                sb.append("\n");
                sb.append("{@link ");
                sb.append(action.getReferencedBfQualifiedName());
                sb.append("}");
                methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getMethodNameCallBusinessFunctionAction(action),
                        new String[0], new Class[0], body, sb.toString(), ANNOTATION_GENERATED);
                alreadyGenerated.add(getMethodNameCallBusinessFunctionAction(action));
            }
        }
    }

    private String getMethodNameCallBusinessFunctionAction(IActionBFE action) {
        return StringUtils.uncapitalize(action.getReferencedBfUnqualifedName());
    }

    private String getMethodNameStart() {
        return "start";
    }

    private String getMethodNameEnd() {
        return "end";
    }

    private String getExecuteMethodName() {
        return "execute";
    }

    private void generateMethodExecute(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append(getMethodNameStart());
        body.appendln("();");
        generateControlFlowMethodBody(body, getBusinessFunction().getStart(), methodBuilder);
        body.append(getMethodNameEnd());
        body.appendln("();");
        methodBuilder.method(Modifier.PUBLIC, Void.TYPE, getExecuteMethodName(), new String[0], new Class[0], body,
                "Executes this business function.", ANNOTATION_GENERATED);
    }

    private void generateMethodForMerges(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> merges = getBusinessFunction().getBFElements();
        for (IBFElement merge : merges) {
            if (merge.getType().equals(BFElementType.MERGE) && merge.isValid(getIpsProject())) {
                generateMethodForMerge(merge, methodBuilder);
            }
        }
    }

    private void generateMethodForMerge(IBFElement merge, JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        generateControlFlowMethodBody(body, merge, methodBuilder);
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getMethodNameMerge(merge), new String[0], new Class[0], body,
                "", ANNOTATION_GENERATED);
    }

    private void generateCodeForDecisions(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> decisions = getBusinessFunction().getBFElements();
        Set<String> alreadyGenerated = new HashSet<String>();
        for (IBFElement element : decisions) {
            if (element.isValid(getIpsProject())) {
                if (element.getType().equals(BFElementType.DECISION)
                        || element.getType().equals(BFElementType.DECISION_METHODCALL)) {
                    IDecisionBFE decision = (IDecisionBFE)element;
                    generateMethodForDecision(decision, methodBuilder);
                    generateMethodGetConditionValue(methodBuilder, decision, alreadyGenerated);
                }
            }
        }
    }

    private String getMethodNameForDecision(IDecisionBFE decision) {
        if (BFElementType.DECISION == decision.getType()) {
            return StringUtils.uncapitalize(decision.getName());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.uncapitalize(decision.getExecutableMethodName()));
        sb.append("ID");
        sb.append(decision.getId());
        return sb.toString();
    }

    private String getMethodNameMerge(IBFElement merge) {
        return StringUtils.uncapitalize(merge.getName());
    }

    private void appendMethodCall(IBFElement element, JavaCodeFragment body) {
        if (element.getType().equals(BFElementType.DECISION)) {
            body.append(getMethodNameForDecision((IDecisionBFE)element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.DECISION_METHODCALL)) {
            body.append(getMethodNameForDecision((IDecisionBFE)element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.MERGE)) {
            body.append(getMethodNameMerge(element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.ACTION_INLINE)) {
            body.append(getMethodNameInlineAction((IActionBFE)element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            body.append(getMethodNameCallBusinessFunctionAction((IActionBFE)element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
            body.append(getMethodNameCallMethodAction((IActionBFE)element));
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.START)) {
            body.append(getMethodNameStart());
            body.appendln("();");
            return;
        }
        if (element.getType().equals(BFElementType.END)) {
            body.append(getMethodNameEnd());
            body.appendln("();");
            return;
        }
    }

    private void generateControlFlowMethodBody(JavaCodeFragment body,
            IBFElement inputElement,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        if (inputElement == null || !inputElement.isValid(getIpsProject())) {
            body.appendln();
            return;
        }
        List<IControlFlow> outgoingFlows = inputElement.getOutgoingControlFlow();
        if (inputElement.getType().equals(BFElementType.DECISION)
                || inputElement.getType().equals(BFElementType.DECISION_METHODCALL)) {
            IDecisionBFE decision = (IDecisionBFE)inputElement;
            Datatype datatype = decision.findDatatype(getIpsProject());
            DatatypeHelper helper = getIpsProject().findDatatypeHelper(datatype.getQualifiedName());

            body.appendClassName(helper.getJavaClassName());
            body.append(" conditionValue = ");
            body.append(getMethodNameGetConditionValue(decision));
            body.appendln("();");
            boolean ifBlockExists = false;
            // the addition counter variable r is necessary to create code fragment that is still
            // parsable by jmerge. Otherwise jmerge
            // rises an exception.
            for (int r = 0, i = 0; i < outgoingFlows.size(); i++, r++) {
                IControlFlow controlFlow = outgoingFlows.get(i);
                if (!controlFlow.isValid(getIpsProject())) {
                    r--;
                    continue;
                }
                String value = controlFlow.getConditionValue();
                if (r == 0) {
                    body.append("if(conditionValue.equals(");
                    body.append(helper.newInstance(value));
                    body.append("))");
                    body.appendOpenBracket();
                    generateSingleControlFlowMethodBody(methodBuilder, body, controlFlow);
                    body.appendCloseBracket();
                    ifBlockExists = true;
                    continue;
                }
                if (i < outgoingFlows.size()) {
                    body.append("else if(conditionValue.equals(");
                    body.append(helper.newInstance(value));
                    body.append("))");
                    body.appendOpenBracket();
                    generateSingleControlFlowMethodBody(methodBuilder, body, controlFlow);
                    body.appendCloseBracket();
                }
            }
            // the ifBlockExists check is only necessary so that jmerge doesn't throw an exception
            // when it tries
            // to merge inconsistent code
            if (ifBlockExists) {
                body.append("else");
                body.appendOpenBracket();
                body.appendln("throw new RuntimeException(\"Unhandled condition value=\" + conditionValue);");
                body.appendCloseBracket();
            }
            return;
        }
        if (!outgoingFlows.isEmpty()) {
            generateSingleControlFlowMethodBody(methodBuilder, body, outgoingFlows.get(0));
        }
    }

    private String getMethodNameGetConditionValue(IDecisionBFE decision) {
        if (BFElementType.DECISION == decision.getType()) {
            return "get" + StringUtils.capitalize(decision.getName()) + "Value";
        }
        return "get" + StringUtils.capitalize(decision.getExecutableMethodName()) + "Value";
    }

    private String getKeyForMethodCallDecision(IDecisionBFE decision) {
        return decision.getTarget() + "." + decision.getExecutableMethodName();
    }

    private void generateMethodGetConditionValue(JavaCodeFragmentBuilder methodBuilder,
            IDecisionBFE decision,
            Set<String> alreadyGenerated) throws CoreException {
        if (BFElementType.DECISION_METHODCALL == decision.getType()
                && alreadyGenerated.contains(getKeyForMethodCallDecision(decision))) {
            return;
        }
        Datatype datatype = decision.findDatatype(getIpsProject());
        DatatypeHelper helper = getIpsProject().findDatatypeHelper(datatype.getQualifiedName());
        JavaCodeFragment body = new JavaCodeFragment();
        if (BFElementType.DECISION == decision.getType()) {
            body.appendln("//TODO implementation of the condition logic for the decision \"" + decision.getName()
                    + "\" goes here.");
            body.append("return ");
            body.append(helper.nullExpression());
            body.append(";");
        } else {
            IParameterBFE parameter = decision.getParameter();
            body.append("return ");
            body.append(parameter.getName());
            body.append('.');
            body.append(decision.getExecutableMethodName());
            body.append("();");
            alreadyGenerated.add(getKeyForMethodCallDecision(decision));
        }
        methodBuilder.method(Modifier.PRIVATE, helper.getJavaClassName(), getMethodNameGetConditionValue(decision),
                new String[0], new String[0], body, "", ANNOTATION_GENERATED);
    }

    private void generateSingleControlFlowMethodBody(JavaCodeFragmentBuilder methodBuilder,
            JavaCodeFragment body,
            IControlFlow controlFlow) throws CoreException {
        IBFElement element = controlFlow.getTarget();
        if (!element.isValid(getIpsProject())) {
            return;
        }
        if (element.getType().equals(BFElementType.END)) {
            return;
        }
        appendMethodCall(element, body);
        if (!(element.getType().equals(BFElementType.DECISION)
                || element.getType().equals(BFElementType.DECISION_METHODCALL) || element.getType().equals(
                        BFElementType.MERGE))) {
            generateControlFlowMethodBody(body, element, methodBuilder);
        }
    }

    private void generateMethodForDecision(IDecisionBFE decision, JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        generateControlFlowMethodBody(body, decision, methodBuilder);
        String description = getDescriptionInGeneratorLanguage(decision);
        methodBuilder.method(Modifier.PRIVATE, Void.TYPE, getMethodNameForDecision(decision), new String[0],
                new Class[0], body, description, ANNOTATION_GENERATED);
    }

    private String getParameterBFEVarName(IParameterBFE parameterBFE) {
        return StringUtils.uncapitalize(parameterBFE.getName());
    }

    private String getJavaClassName(Datatype datatype) {
        return getBuilderSet().getJavaClassName(datatype);
    }

    private void generateCodeForParameters(JavaCodeFragmentBuilder memberBuilder, JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {
        List<IParameterBFE> parameters = getBusinessFunction().getParameterBFEs();
        for (IParameterBFE parameter : parameters) {
            if (!parameter.isValid(getIpsProject())) {
                continue;
            }
            Datatype datatype = parameter.findDatatype();
            String javaClassName = getJavaClassName(datatype);
            memberBuilder.javaDoc("", ANNOTATION_GENERATED);
            memberBuilder.varDeclaration(Modifier.PRIVATE, javaClassName, getParameterBFEVarName(parameter));
            JavaCodeFragment body = new JavaCodeFragment();
            body.append("return ");
            body.append(getParameterBFEVarName(parameter));
            body.append(';');

            methodBuilder.method(Modifier.PUBLIC, javaClassName, getMethodNameForGetterParameterValue(parameter),
                    new String[0], new String[0], body, "Returns the value of the parameter " + parameter.getName()
                            + ".",
                    ANNOTATION_GENERATED);
        }
    }

    private String getMethodNameForGetterParameterValue(IParameterBFE parameter) {
        return "get" + StringUtils.capitalize(parameter.getName());
    }

    private String getMethodNameCreateCallBusinessFunction(IActionBFE action) {
        return "create" + StringUtils.capitalize(action.getReferencedBfUnqualifedName());
    }

    private void generateMethodCreateCallBusinessFunction(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IBFElement> elements = getBusinessFunction().getBFElements();
        List<String> alreadyGenerated = new ArrayList<String>();
        for (IBFElement element : elements) {
            if (!element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                continue;
            }
            if (!element.isValid(getIpsProject())) {
                continue;
            }
            IActionBFE action = (IActionBFE)element;
            if (alreadyGenerated.contains(getMethodNameCreateCallBusinessFunction(action))) {
                continue;
            }
            JavaCodeFragment body = new JavaCodeFragment();
            body.appendln("//TODO the creation of the business function \"" + action.getReferencedBfUnqualifedName()
                    + "\" needs to be implemented here.");
            body.append("return null;");

            IBusinessFunction bf = action.findReferencedBusinessFunction();
            methodBuilder
                    .method(Modifier.PRIVATE,
                            getQualifiedClassName(bf),
                            getMethodNameCreateCallBusinessFunction(action),
                            new String[0],
                            new String[0],
                            body,
                            "Factory method to create the business function \""
                                    + action.getReferencedBfUnqualifedName() + "\"",
                            ANNOTATION_GENERATED);
            alreadyGenerated.add(getMethodNameCreateCallBusinessFunction(action));
        }
    }

    private void generateConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        ArrayList<String> parameterNames = new ArrayList<String>();
        ArrayList<String> parameterTypes = new ArrayList<String>();

        List<IParameterBFE> parameters = getBusinessFunction().getParameterBFEs();
        for (IParameterBFE parameter : parameters) {
            if (!parameter.isValid(getIpsProject())) {
                continue;
            }
            parameterNames.add(getParameterBFEVarName(parameter));
            Datatype datatype = parameter.findDatatype();
            parameterTypes.add(getJavaClassName(datatype));
        }
        JavaCodeFragment body = new JavaCodeFragment();
        for (Iterator<String> it = parameterNames.iterator(); it.hasNext();) {
            String name = it.next();
            body.append("this.");
            body.append(name);
            body.append(" = ");
            body.append(name);
            body.append(';');
            if (it.hasNext()) {
                body.appendln();
            }
        }

        List<IBFElement> bfActions = getBusinessFunction().getBFElements();
        List<String> alreadyGenerated = new ArrayList<String>();
        for (Iterator<IBFElement> it = bfActions.iterator(); it.hasNext();) {
            IBFElement element = it.next();
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                IActionBFE action = (IActionBFE)element;
                if (!action.isValid(getIpsProject())) {
                    continue;
                }
                if (alreadyGenerated.contains(action.getReferencedBfQualifiedName())) {
                    continue;
                }
                body.append(getCallBusinessFunctionVarName(action));
                body.append(" = ");
                body.append(getMethodNameCreateCallBusinessFunction(action));
                body.append("();");
                if (it.hasNext()) {
                    body.appendln();
                    alreadyGenerated.add(action.getReferencedBfQualifiedName());
                }
            }
        }
        constructorBuilder.method(Modifier.PUBLIC, null, getBusinessFunction().getName(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterTypes.toArray(new String[parameterTypes.size()]), body, "Creates a new "
                        + getBusinessFunction().getName() + ".",
                ANNOTATION_GENERATED);
    }

    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getIpsObject();
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return true;
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        // not supported, yet.
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

}
