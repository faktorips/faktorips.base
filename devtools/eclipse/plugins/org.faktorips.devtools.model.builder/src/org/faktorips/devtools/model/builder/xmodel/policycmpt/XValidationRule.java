/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.policycmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.java.util.LocaleGeneratorUtil;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.MethodParameter;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

public class XValidationRule extends AbstractGeneratorModelNode {

    public XValidationRule(IValidationRule validationRule, GeneratorModelContext context, ModelService modelService) {
        super(validationRule, context, modelService);
    }

    @Override
    public IValidationRule getIpsObjectPartContainer() {
        return (IValidationRule)super.getIpsObjectPartContainer();
    }

    public IValidationRule getValidationRule() {
        return getIpsObjectPartContainer();
    }

    public boolean isConfigured() {
        return getValidationRule().isConfigurableByProductComponent();
    }

    public boolean isChangingOverTime() {
        return getValidationRule().isChangingOverTime();
    }

    public boolean isValidatedAttrSpecifiedInSrc() {
        return getValidationRule().isValidatedAttrSpecifiedInSrc();
    }

    public boolean isCheckValueAgainstValueSetRule() {
        return getValidationRule().isCheckValueAgainstValueSetRule();
    }

    public XPolicyAttribute getCheckedAttribute() {
        IAttribute attr = getValidationRule().getType().getAttribute(getValidationRule().getValidatedAttributeAt(0));
        return getModelNode(attr, XPolicyAttribute.class);
    }

    public boolean isNeedTodoCompleteCallCreateMsg() {
        return isContainsReplacementParameters() || isValidatedAttrSpecifiedInSrc();
    }

    public boolean isContainsReplacementParameters() {
        return !getValidationRule().getMessageText().getReplacementParameters().isEmpty();
    }

    public LinkedHashSet<String> getReplacementParameters() {
        return convertToJavaParameters(getValidationRule().getMessageText().getReplacementParameters());
    }

    public String getDefaultLocale() {
        ISupportedLanguage defaultLanguage = getIpsProject().getReadOnlyProperties().getDefaultLanguage();
        JavaCodeFragment localeCodeFragment = LocaleGeneratorUtil.getLocaleCodeFragment(defaultLanguage.getLocale());
        addImport(localeCodeFragment.getImportDeclaration());
        return localeCodeFragment.getSourcecode();
    }

    public boolean isValidateAttributes() {
        return getValidationRule().getValidatedAttributes().length > 0;
    }

    public List<String> getValidatedAttributeConstants() {
        List<String> result = new ArrayList<>();
        String[] attributes = getValidationRule().getValidatedAttributes();
        for (String attributeName : attributes) {
            IAttribute attr = getValidationRule().getType().findAttribute(attributeName, getIpsProject());
            XPolicyAttribute xPolicyAttribute = getModelNode(attr, XPolicyAttribute.class);
            result.add(xPolicyAttribute.getConstantNamePropertyName());
        }
        return result;
    }

    public String getMethodNameExecRule() {
        return IpsStringUtils.toLowerFirstChar(getName());
    }

    public String getMethodNameCreateMessage() {
        return "createMessageForRule" + IpsStringUtils.toUpperFirstChar(getName());
    }

    public List<MethodParameter> getCreateMessageParameters() {
        List<MethodParameter> result = new ArrayList<>();
        result.add(new MethodParameter(addImport(IValidationContext.class), "context"));
        for (String replacementParameter : getReplacementParameters()) {
            result.add(new MethodParameter(addImport(Object.class), replacementParameter));
        }
        if (isValidatedAttrSpecifiedInSrc()) {
            result.add(new MethodParameter(addImport(ObjectProperty.class) + "[]", "invalidObjectProperties"));
        }
        return result;
    }

    public String getValidateMessageBundleName() {
        IIpsObjectPathEntry entry = getIpsObjectPartContainer().getIpsSrcFile().getIpsPackageFragment().getRoot()
                .getIpsObjectPathEntry();
        if (entry instanceof IIpsSrcFolderEntry) {
            return getContext().getValidationMessageBundleBaseName((IIpsSrcFolderEntry)entry);
        }
        return IIpsObjectPathEntry.ERROR_CAST_EXCEPTION_PATH;
    }

    public String getValidationMessageKey() {
        return getIpsObjectPartContainer().getQualifiedRuleName();
    }

    public String getConstantNameMessageCode() {
        String upperCaseName = getName();
        if (getGeneratorConfig().isGenerateSeparatedCamelCase()) {
            upperCaseName = StringUtil.camelCaseToUnderscore(upperCaseName, false);
        }
        upperCaseName = upperCaseName.toUpperCase();
        return "MSG_CODE_" + upperCaseName;
    }

    public String getMessageCode() {
        return getValidationRule().getMessageCode();
    }

    public String getSeverityConstant() {
        JavaCodeFragment codeFragment = getValidationRule().getMessageSeverity().getJavaSourcecode();
        addImport(codeFragment.getImportDeclaration());
        return codeFragment.getSourcecode();
    }

    public String getConstantNameRuleName() {
        return getLocalizedText("FIELD_RULE_NAME",
                StringUtil.camelCaseToUnderscore(getValidationRule().getName(), false)).toUpperCase();
    }

    /**
     * Converts the parameters found in the list of parameters to be java compatible variable names.
     * If any parameter is no valid java identifier we add the prefix 'p' (for parameter). If there
     * is already another parameter with this name we add as much 'p' as needed to be unique.
     *
     */
    LinkedHashSet<String> convertToJavaParameters(LinkedHashSet<String> parameters) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final String parameterName : parameters) {
            if (!Character.isJavaIdentifierStart(parameterName.charAt(0))) {
                String javaIdent = "p" + parameterName;
                while (parameters.contains(javaIdent)) {
                    javaIdent = "p" + javaIdent;
                }
                result.add(javaIdent);
            } else {
                result.add(parameterName);
            }
        }
        return result;
    }

    public List<String> getMarkers() {
        List<String> result = new ArrayList<>();
        Set<String> markerIds = new LinkedHashSet<>(getValidationRule().getMarkers());
        LinkedHashSet<IIpsSrcFile> markerEnums = getIpsProject().getMarkerEnums();
        for (IIpsSrcFile markerEnumSrcFile : markerEnums) {
            IEnumType enumType = (IEnumType)markerEnumSrcFile.getIpsObject();
            Set<String> ids = new HashSet<>(enumType.findAllIdentifierAttributeValues(getIpsProject()));
            for (Iterator<String> iterator = markerIds.iterator(); iterator.hasNext();) {
                String id = iterator.next();
                if (ids.contains(id)) {
                    result.add(getMarkerSourceCode(enumType, id));
                    iterator.remove();
                }
            }
        }
        if (!markerIds.isEmpty()) {
            throw new IllegalStateException("No marker enum values found for id(s) " + markerIds.toString());
        }
        return result;
    }

    public String getMarkerSourceCode(IEnumType enumType, String id) {
        DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(enumType.getQualifiedName());
        JavaCodeFragment newInstance = datatypeHelper.newInstance(id);
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    public boolean isContainingMarkers() {
        return !getMarkers().isEmpty();
    }

}
