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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MsgReplacementParameter;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * Generator for validation rules.
 * 
 * @author Peter Erzberger
 */
public class GenValidationRule extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenValidationRule.class);

    public GenValidationRule(GenPolicyCmptType genPolicyCmptType, IValidationRule part) {
        super(genPolicyCmptType, part, LOCALIZED_STRINGS);
    }

    @Override
    public IValidationRule getIpsPart() {
        return (IValidationRule)super.getIpsPart();
    }

    @Override
    public GenPolicyCmptType getGenType() {
        return (GenPolicyCmptType)super.getGenType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            generateFieldForMsgCode(builder);
            if (getValidationRule().isConfigurableByProductComponent()) {
                generateFieldForRuleName(builder);
            }
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
        IValidationRule rule = getIpsPart();
        String parameterValidationContext = "context";
        String javaDoc = getLocalizedText("EXEC_RULE_JAVADOC", rule.getName());
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln();
        String[] businessFunctions = rule.getBusinessFunctions();
        if (!rule.isAppliedForAllBusinessFunctions() && businessFunctions.length > 0) {
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
        if (rule.isConfigurableByProductComponent()) {
            body.append("if(getProductCmptGeneration().isValidationRuleActivated(") //
                    .append(getFieldNameForRuleName()) //
                    .append("))") //
                    .appendOpenBracket();
            if (!rule.isCheckValueAgainstValueSetRule()) {
                body.appendln("//begin-user-code");
                body.appendln(getLocalizedToDo("EXEC_RULE_IMPLEMENT", rule.getName()));
            }
        }

        String[] javaDocAnnotation = JavaSourceFileBuilder.ANNOTATION_RESTRAINED_MODIFIABLE;

        body.append("if(");
        if (rule.isCheckValueAgainstValueSetRule()) {
            javaDocAnnotation = JavaSourceFileBuilder.ANNOTATION_GENERATED;
            IPolicyCmptTypeAttribute attr = ((IPolicyCmptType)rule.getIpsObject()).getPolicyCmptTypeAttribute(rule
                    .getValidatedAttributeAt(0));
            body.append('!');

            GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute = (getGenType()).getGenerator(attr);
            if (!attr.getValueSet().isUnrestricted()) {
                body.append(genPolicyCmptTypeAttribute.getMethodNameGetSetOfAllowedValues());
            }
            body.append("(");
            body.append(parameterValidationContext);
            body.append(").contains(");
            body.append(genPolicyCmptTypeAttribute.getGetterMethodName());
            body.append("()))");
        } else {
            body.append("true) ");
        }
        body.appendOpenBracket();
        boolean generateToDo = false;
        body.append("ml.add(");
        body.append(getMethodNameCreateMessageForRule());
        Set<String> replacementParameters = getReplacementParameters(rule.getMessageText());
        body.append("(context");
        for (@SuppressWarnings("unused")
        String replacement : replacementParameters) {
            body.append(", ");
            body.append("null");
            generateToDo = true;
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
        body.appendln(" return CONTINUE_VALIDATION;");
        if (rule.isConfigurableByProductComponent()) {
            if (!rule.isCheckValueAgainstValueSetRule()) {
                body.appendln("//end-user-code");
            }
            body.appendCloseBracket();
            body.appendln(" return CONTINUE_VALIDATION;");
        }
        if (!rule.isAppliedForAllBusinessFunctions() && businessFunctions.length > 0) {
            body.appendCloseBracket();
            body.appendln(" return CONTINUE_VALIDATION;");
        }

        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
                getMethodNameExecRule(), new String[] { "ml", parameterValidationContext }, new String[] {
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
     *      MsgReplacementParameter[] replacementParameters = new MsgReplacementParameter[] { new MsgReplacementParameter(
     *                 "0", p0), new MsgReplacementParameter("1", p1), new MsgReplacementParameter("2", p2) };
     *      String message = new MessagesHelper("org.faktorips.integrationtest.internal.messages", getClass()
     *                 .getClassLoader()).getMessage("rules.TestPolicy_aRule", context.getLocale(), p0, p1, p2);
     * 
     *      return new Message(MSG_CODE_ARULE, message, Message.ERROR, objectProperties);
     *  }
     * </pre>
     */
    private void generateMethodCreateMessageForRule(JavaCodeFragmentBuilder builder, IIpsProject ipsProject)
            throws CoreException {
        IValidationRule rule = getIpsPart();
        String localVarObjectProperties = "invalidObjectProperties";
        String localVarReplacementParams = "replacementParameters";
        String localVarMessageHelper = "messageHelper";
        String localVarMessage = "message";
        String parameterContext = "context";

        Set<String> replacementParameters = getReplacementParameters(rule.getMessageText());
        // determine method parameters (name and type)
        List<String> methodParamNames = new ArrayList<String>(replacementParameters.size() + 2);
        List<String> methodParamTypes = new ArrayList<String>(replacementParameters.size() + 2);
        methodParamNames.add(parameterContext);
        methodParamTypes.add(IValidationContext.class.getName());
        methodParamNames.addAll(replacementParameters);
        methodParamTypes.addAll(getReplacementClasses(replacementParameters.size()));
        if (rule.isValidatedAttrSpecifiedInSrc()) {
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
        if (!replacementParameters.isEmpty()) {
            body.append(generateCodeForMsgReplacementParameters(localVarReplacementParams, replacementParameters));
        }

        // code to construct the message's text
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsPart().getIpsSrcFile().getIpsPackageFragment().getRoot()
                .getIpsObjectPathEntry();
        ValidationMessagesPropertiesBuilder validationMessageBuilder = getBuilderSet().getBuildersByClass(
                ValidationMessagesPropertiesBuilder.class).get(0);
        String messagesPropertiesName = validationMessageBuilder.getResourceBundleBaseName(entry);
        body.appendClassName(MessagesHelper.class).append(" ").append(localVarMessageHelper).append(" = ")//
                .append("new ").appendClassName(MessagesHelper.class).append("(\"").append(messagesPropertiesName)//
                .append("\", ").appendln("getClass().getClassLoader());");
        body.appendClassName(String.class).append(" ").append(localVarMessage).append(" = ")//
                .append(localVarMessageHelper).append(".").append(MethodNames.MESSAGE_HELPER_GET_MESSAGE).append("(\"") //
                .append(validationMessageBuilder.getMessageKey(getIpsPart())).append("\", ") //
                .append(parameterContext).append(".").append(MethodNames.VALIDATION_CONTEXT_GET_LOCALE).append("()"); //

        for (String replacementParameter : replacementParameters) {
            body.append(", ");
            body.append(replacementParameter);
        }
        body.append(");");

        // code to create the message and return it.
        body.append("return new ");
        body.appendClassName(Message.class);
        body.append('(');
        body.append(getFieldNameForMsgCode());
        body.append(", ");
        body.append(localVarMessage);
        body.append(", ");
        body.append(rule.getMessageSeverity().getJavaSourcecode());
        body.append(", ");
        body.append(localVarObjectProperties);
        if (!replacementParameters.isEmpty()) {
            body.append(", ");
            body.append(localVarReplacementParams);
        }
        body.append(");");

        String javaDoc = getLocalizedText("CREATE_MESSAGE_JAVADOC", rule.getName());
        builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                getMethodNameCreateMessageForRule(), methodParamNames.toArray(new String[methodParamNames.size()]),
                methodParamTypes.toArray(new String[methodParamTypes.size()]), body, javaDoc,
                JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    /**
     * Extracting the replacement parameters from given messageText. The replacement parameters are
     * defined curly braces. In contrast to the replacement parameters used in {@link MessageFormat}
     * , these parameters could have names and not only indices. However you could use additional
     * format information separated by comma as used by {@link MessageFormat}.
     */
    Set<String> getReplacementParameters(String messageText) {
        Set<String> result = new LinkedHashSet<String>();
        Matcher matcher = ValidationMessagesPropertiesBuilder.REPLACEMENT_PARAMETER_REGEXT.matcher(messageText);
        while (matcher.find()) {
            String parameterName = matcher.group();
            if (!Character.isJavaIdentifierStart(parameterName.charAt(0))) {
                parameterName = "p" + parameterName;
            }
            result.add(parameterName);
        }
        return result;
    }

    private List<String> getReplacementClasses(int size) {
        ArrayList<String> classes = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            classes.add(Object.class.getName());
        }
        return classes;
    }

    private JavaCodeFragment generateCodeForInvalidObjectProperties(String pObjectProperties,
            String[] validatedAttributes,
            IIpsProject ipsProject) throws CoreException {

        JavaCodeFragment code = new JavaCodeFragment();
        if (!getGenType().getIpsPart().isValid(getIpsProject())) {
            return code;
        }

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
                String propertyConstName = (getGenType()).getGenerator(attr).getStaticConstantPropertyName();
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
    private JavaCodeFragment generateCodeForMsgReplacementParameters(String localVar, Set<String> replacementParameters) {
        JavaCodeFragment code = new JavaCodeFragment();
        // MsgReplacementParameter[] replacementParameters = new
        // MsgReplacementParameter[] {
        code.appendClassName(MsgReplacementParameter.class);
        code.append("[] " + localVar + " = new ");
        code.appendClassName(MsgReplacementParameter.class);
        code.appendln("[] {");

        int i = 0;
        for (String string : replacementParameters) {

            // new MsgReplacementParameter("paramName", paramName),
            code.append("new ");
            code.appendClassName(MsgReplacementParameter.class);
            code.append("(");
            code.appendQuoted(string);
            code.append(", ");
            code.append(string);
            code.append(")");
            if (i != replacementParameters.size() - 1) {
                code.append(", ");
            }
            i++;
            code.appendln();
        }

        code.appendln("};");
        return code;
    }

    private IValidationRule getValidationRule() {
        return getIpsPart();
    }

    private void generateFieldForMsgCode(JavaCodeFragmentBuilder membersBuilder) {
        appendLocalizedJavaDoc("FIELD_MSG_CODE", getValidationRule().getName(), membersBuilder);
        membersBuilder.append("public final static ");
        membersBuilder.appendClassName(String.class);
        membersBuilder.append(' ');
        membersBuilder.append(getFieldNameForMsgCode());
        membersBuilder.append(" = \"");
        membersBuilder.append(getValidationRule().getMessageCode());
        membersBuilder.appendln("\";");
    }

    public String getFieldNameForMsgCode() {
        return getLocalizedText("FIELD_MSG_CODE_NAME", StringUtils.upperCase(getValidationRule().getName()));
    }

    private void generateFieldForRuleName(JavaCodeFragmentBuilder membersBuilder) {
        String fieldRuleName = "FIELD_RULE_NAME";
        appendLocalizedJavaDoc(fieldRuleName, getValidationRule().getName(), membersBuilder);
        membersBuilder.append("public final static ");
        membersBuilder.appendClassName(String.class);
        membersBuilder.append(' ');
        membersBuilder.append(getFieldNameForRuleName());
        membersBuilder.append(" = \"");
        membersBuilder.append(getValidationRule().getName());
        membersBuilder.appendln("\";");
    }

    public String getFieldNameForRuleName() {
        return getLocalizedText("FIELD_RULE_NAME",
                StringUtils.upperCase(StringUtil.camelCaseToUnderscore(getValidationRule().getName(), false)));
    }

    public String getMethodNameCreateMessageForRule() {
        return "createMessageForRule" + StringUtils.capitalize(getValidationRule().getName());
    }

    public String getMethodNameExecRule() {
        return StringUtils.uncapitalize(getValidationRule().getName());
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {
        addFieldToGeneratedJavaElements(javaElements, generatedJavaType, getFieldNameForMsgCode());
        addFieldToGeneratedJavaElements(javaElements, generatedJavaType, getFieldNameForRuleName());
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {
        addExecRuleMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        addCreateMessageForRuleMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    private void addCreateMessageForRuleMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {
        String[] parameters = new String[] { unresolvedParam(IValidationContext.class) };
        if (getValidationRule().isValidatedAttrSpecifiedInSrc()) {
            parameters = new String[] { unresolvedParam(IValidationContext.class),
                    unresolvedParam(ObjectProperty.class.getSimpleName() + "[]") };
        }
        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameCreateMessageForRule(),
                parameters);
    }

    private void addExecRuleMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameExecRule(),
                unresolvedParam(MessageList.class), unresolvedParam(IValidationContext.class));
    }

}
