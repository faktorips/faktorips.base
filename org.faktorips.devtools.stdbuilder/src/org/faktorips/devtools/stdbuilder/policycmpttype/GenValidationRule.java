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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.MessageFragment;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MsgReplacementParameter;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for validation rules.
 * 
 * @author Peter Erzberger
 */
public class GenValidationRule extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenValidationRule.class);

    public GenValidationRule(GenPolicyCmptType genPolicyCmptType, IIpsObjectPartContainer part) throws CoreException {
        super(genPolicyCmptType, part, LOCALIZED_STRINGS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            generateFieldForMsgCode(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (!generatesInterface) {
            generateMethodExecRule(builder);
            generateMethodCreateMessageForRule(builder, ipsProject);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *   if (&quot;rules.businessProcess1&quot;.equals(businessFunction) || &quot;rules.businessProcess2&quot;.equals(businessFunction)) {
     *      //begin-user-code
     *      boolean condition = getA().equals(new Integer(1));
     *      if (condition) {
     *          ml.add(createMessageForRuleARule(String.valueOf(getA()), String.valueOf(getB()), String.valueOf(getHallo())));
     *          return false;
     *      }
     *      return true;
     *      //end-user-code
     *  }
     *  return true;
     * </pre>
     */
    private void generateMethodExecRule(JavaCodeFragmentBuilder builder) throws CoreException {
        IValidationRule rule = (IValidationRule)getIpsPart();
        String parameterValidationContext = "context";
        String javaDoc = getLocalizedText("EXEC_RULE_JAVADOC", rule.getName());
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln();
        String[] businessFunctions = rule.getBusinessFunctions();
        if (!rule.isAppliedForAllBusinessFunctions()) {
            if (businessFunctions.length > 0) {
                body.append("if(");
                for (int j = 0; j < businessFunctions.length; j++) {
                    body.append("\"");
                    body.append(businessFunctions[j]);
                    body.append("\"");
                    body.append(".equals(");
                    body.append(parameterValidationContext);
                    body.append(".getValue(\"businessFunction\"))");
                    if (j < businessFunctions.length - 1) {
                        body.appendln(" || ");
                    }
                }
                body.append(")");
                body.appendOpenBracket();
            }
        }
        if (!rule.isCheckValueAgainstValueSetRule()) {
            body.appendln("//begin-user-code");
            body.appendln(getLocalizedToDo("EXEC_RULE_IMPLEMENT", rule.getName()));
        }

        body.append("if(");
        String[] javaDocAnnotation = JavaSourceFileBuilder.ANNOTATION_RESTRAINED_MODIFIABLE;
        if (rule.isCheckValueAgainstValueSetRule()) {
            javaDocAnnotation = JavaSourceFileBuilder.ANNOTATION_GENERATED;
            IPolicyCmptTypeAttribute attr = ((IPolicyCmptType)rule.getIpsObject()).getPolicyCmptTypeAttribute(rule
                    .getValidatedAttributeAt(0));
            body.append('!');

            GenAttribute genAttribute = ((GenPolicyCmptType)getGenType()).getGenerator(attr);
            if (!attr.getValueSet().isUnrestricted()) {
                body.append(genAttribute.getMethodNameGetSetOfAllowedValues());
            }
            body.append("(");
            body.append(parameterValidationContext);
            body.append(").contains(");
            body.append(genAttribute.getGetterMethodName());
            body.append("()))");
        } else {
            body.append("true) ");
        }
        body.appendOpenBracket();
        boolean generateToDo = false;
        body.append("ml.add(");
        body.append(getMethodNameCreateMessageForRule(rule));
        MessageFragment msgFrag = MessageFragment.createMessageFragment(rule.getMessageText(),
                MessageFragment.VALUES_AS_PARAMETER_NAMES);
        body.append("(context");
        if (msgFrag.hasParameters()) {
            body.append(", ");
            String[] parameterNames = msgFrag.getParameterNames();
            for (int j = 0; j < parameterNames.length; j++) {
                body.append("null");
                generateToDo = true;
                if (j < parameterNames.length - 1) {
                    body.append(", ");
                }
            }
        }

        if (rule.isValidatedAttrSpecifiedInSrc()) {
            generateToDo = true;
            body.append(", ");
            body.append("new ");
            body.appendClassName(ObjectProperty.class);
            body.append("[0]");
        }

        body.append("));");
        if (generateToDo) {
            body.append(getLocalizedToDo("EXEC_RULE_COMPLETE_CALL_CREATE_MSG", rule.getName()));
        }
        body.appendln();
        body.appendCloseBracket();
        body.appendln(" return true;");
        if (!rule.isCheckValueAgainstValueSetRule()) {
            body.appendln("//end-user-code");
        }
        if (!rule.isAppliedForAllBusinessFunctions()) {
            if (businessFunctions.length > 0) {
                body.appendCloseBracket();
                body.appendln(" return true;");
            }
        }

        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
                getMethodNameExecRule(rule), new String[] { "ml", parameterValidationContext }, new String[] {
                        MessageList.class.getName(), IValidationContext.class.getName() }, body, javaDoc,
                javaDocAnnotation);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *   protected Message createMessageForRuleARule(String p0, String p1, String p2) {
     *      ObjectProperty[] objectProperties = new ObjectProperty[] { new ObjectProperty(this, PROPERTY_NAME_A),
     *              new ObjectProperty(this, PROPERTY_NAME_B) };
     *      StringBuffer text = new StringBuffer();
     *      text.append(&quot;Check parameters &quot;);
     *      text.append(p0);
     *      text.append(&quot;, check if line break works in generated code\n&quot;);
     *      text.append(p1);
     *      text.append(&quot; and &quot;);
     *      text.append(p2);
     *      return new Message(MSG_CODE_ARULE, text.toString(), Message.ERROR, objectProperties);
     *  }
     * </pre>
     */
    private void generateMethodCreateMessageForRule(JavaCodeFragmentBuilder builder, IIpsProject ipsProject)
            throws CoreException {
        IValidationRule rule = (IValidationRule)getIpsPart();
        String localVarObjectProperties = "invalidObjectProperties";
        String localVarReplacementParams = "replacementParameters";
        MessageFragment msgFrag = MessageFragment.createMessageFragment(rule.getMessageText(),
                MessageFragment.VALUES_AS_PARAMETER_NAMES);

        // determine method parameters (name and type)
        List<String> methodParamNames = new ArrayList<String>(msgFrag.getNumberOfParameters() + 2);
        List<String> methodParamTypes = new ArrayList<String>(msgFrag.getNumberOfParameters() + 2);
        methodParamNames.add("context");
        methodParamTypes.add(IValidationContext.class.getName());
        methodParamNames.addAll(Arrays.asList(msgFrag.getParameterNames()));
        methodParamTypes.addAll(Arrays.asList(msgFrag.getParameterClasses()));
        if (!rule.isValidatedAttrSpecifiedInSrc()) {
        } else {
            methodParamNames.add(localVarObjectProperties);
            methodParamTypes.add(ObjectProperty.class.getName() + "[]");
        }

        // code for objectProperties
        JavaCodeFragment body = new JavaCodeFragment();
        String[] validatedAttributes = rule.getValidatedAttributes();
        if (!rule.isValidatedAttrSpecifiedInSrc()) {
            body.append(generateCodeForInvalidObjectProperties(localVarObjectProperties, validatedAttributes,
                    ipsProject));
        }
        // code for replacement parameters
        if (msgFrag.hasParameters()) {
            body
                    .append(generateCodeForMsgReplacementParameters(localVarReplacementParams, msgFrag
                            .getParameterNames()));
        }

        // code to construct the message's text
        body.append(msgFrag.getFrag());

        // code to create the message and return it.
        body.append("return new ");
        body.appendClassName(Message.class);
        body.append('(');
        body.append(getFieldNameForMsgCode(rule));
        body.append(", ");
        body.append(msgFrag.getMsgTextExpression());
        body.append(", ");
        body.append(rule.getMessageSeverity().getJavaSourcecode());
        body.append(", ");
        body.append(localVarObjectProperties);
        if (msgFrag.hasParameters()) {
            body.append(", ");
            body.append(localVarReplacementParams);
        }
        body.append(");");

        String javaDoc = getLocalizedText("CREATE_MESSAGE_JAVADOC", rule.getName());
        builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                getMethodNameCreateMessageForRule(rule), methodParamNames.toArray(new String[methodParamNames.size()]),
                methodParamTypes.toArray(new String[methodParamTypes.size()]), body, javaDoc,
                JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private JavaCodeFragment generateCodeForInvalidObjectProperties(String pObjectProperties,
            String[] validatedAttributes,
            IIpsProject ipsProject) throws CoreException {

        JavaCodeFragment code = new JavaCodeFragment();
        if (validatedAttributes.length > 0) {
            code.appendClassName(ObjectProperty.class);
            code.append("[] ");
            code.append(pObjectProperties);
            code.append(" = new ");
            code.appendClassName(ObjectProperty.class);
            code.append("[]{");
            for (int j = 0; j < validatedAttributes.length; j++) {
                IPolicyCmptTypeAttribute attr = ((IPolicyCmptType)getIpsPart().getIpsObject())
                        .findPolicyCmptTypeAttribute(validatedAttributes[j], ipsProject);
                String propertyConstName = ((GenPolicyCmptType)getGenType()).getGenerator(attr)
                        .getStaticConstantPropertyName();
                code.append(" new ");
                code.appendClassName(ObjectProperty.class);
                code.append("(this, ");
                code.append(propertyConstName);
                code.append(")");
                if (j < validatedAttributes.length - 1) {
                    code.append(',');
                }
            }
            code.appendln("};");
        } else {
            code.appendClassName(ObjectProperty.class);
            code.append(" ");
            code.append(pObjectProperties);
            code.append(" = new ");
            code.appendClassName(ObjectProperty.class);
            code.appendln("(this);");
        }
        return code;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * MsgReplacementParameter[] replacementParameters = new MsgReplacementParameter[] { new MsgReplacementParameter(&quot;maxVs&quot;,
     *         maxVs), };
     * 
     * </pre>
     */
    private JavaCodeFragment generateCodeForMsgReplacementParameters(String localVar, String[] parameterNames) {
        JavaCodeFragment code = new JavaCodeFragment();
        // MsgReplacementParameter[] replacementParameters = new
        // MsgReplacementParameter[] {
        code.appendClassName(MsgReplacementParameter.class);
        code.append("[] " + localVar + " = new ");
        code.appendClassName(MsgReplacementParameter.class);
        code.appendln("[] {");

        for (int i = 0; i < parameterNames.length; i++) {

            // new MsgReplacementParameter("paramName", paramName),
            code.append("new ");
            code.appendClassName(MsgReplacementParameter.class);
            code.append("(");
            code.appendQuoted(parameterNames[i]);
            code.append(", ");
            code.append(parameterNames[i]);
            code.append(")");
            if (i != parameterNames.length - 1) {
                code.append(", ");
            }
            code.appendln();
        }

        code.appendln("};");
        return code;
    }

    private IValidationRule getValidationRule() {
        return (IValidationRule)getIpsPart();
    }

    private void generateFieldForMsgCode(JavaCodeFragmentBuilder membersBuilder) {
        appendLocalizedJavaDoc("FIELD_MSG_CODE", getValidationRule().getName(), membersBuilder);
        membersBuilder.append("public final static ");
        membersBuilder.appendClassName(String.class);
        membersBuilder.append(' ');
        membersBuilder.append(getFieldNameForMsgCode(getValidationRule()));
        membersBuilder.append(" = \"");
        membersBuilder.append(getValidationRule().getMessageCode());
        membersBuilder.appendln("\";");
    }

    public String getFieldNameForMsgCode(IValidationRule rule) {
        return getLocalizedText("FIELD_MSG_CODE_NAME", StringUtils.upperCase(rule.getName()));
    }

    private String getMethodNameCreateMessageForRule(IValidationRule rule) {
        return "createMessageForRule" + StringUtils.capitalize(rule.getName());
    }

    private String getMethodNameExecRule(IValidationRule r) {
        return StringUtils.uncapitalize(r.getName());
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

}
