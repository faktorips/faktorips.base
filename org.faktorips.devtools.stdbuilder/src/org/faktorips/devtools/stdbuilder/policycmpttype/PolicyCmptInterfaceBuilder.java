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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private boolean generateDeltaSupport = false;
    private boolean generateCopySupport = false;

    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            boolean changeListenerSupportActive) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class),
                changeListenerSupportActive);
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return this;
    }

    public boolean isGenerateDeltaSupport() {
        return generateDeltaSupport;
    }

    public void setGenerateDeltaSupport(boolean generateDeltaSupport) {
        this.generateDeltaSupport = generateDeltaSupport;
    }

    public boolean isGenerateCopySupport() {
        return generateCopySupport;
    }

    public void setGenerateCopySupport(boolean generateCopySupport) {
        this.generateCopySupport = generateCopySupport;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptTypeName(ipsSrcFile));
    }

    public String getPolicyCmptTypeName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return StringUtils.capitalize(name);
    }

    protected void assertConditionsBeforeGenerating() {
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        List interfaces = new ArrayList();
        IPolicyCmptType type = getPcType();
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype != null) {
            interfaces.add(getQualifiedClassName(supertype));
        } else {
            if (type.isConfigurableByProductCmptType()) {
                interfaces.add(IConfigurableModelObject.class.getName());
            } else {
                interfaces.add(IModelObject.class.getName());
            }
            if (generateDeltaSupport) {
                interfaces.add(IDeltaSupport.class.getName());
            }
            if (generateCopySupport) {
                interfaces.add(ICopySupport.class.getName());
            }
        }
        if (isFirstDependantTypeInHierarchy(type)) {
            interfaces.add(IDependantObject.class.getName());
        }
        return (String[])interfaces.toArray(new String[interfaces.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), getIpsObject().getDescription(), getIpsObject(),
                builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (getProductCmptType() != null) {
            if (hasValidProductCmptTypeName()) {
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodSetProductCmpt(methodsBuilder);
                ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                        .generateMethodGetProductCmptGeneration(getIpsProject(), methodsBuilder);
            }
        }
        // TODO remove
        // generateCodeForValidationRules(memberVarsBuilder);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorProduct getMotorProduct();
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[] { getProductCmptType().getName(), getPcType().getName() };
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT", replacements, getPcType(), methodsBuilder);
        generateSignatureGetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.append(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProduct getMotorProduct()
     * </pre>
     */
    public void generateSignatureGetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String returnType = getQualifiedClassName(type);
        String methodName = getMethodNameGetProductCmpt(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    String getMethodNameGetProductCmpt(IProductCmptType type) throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(type).getMethodNameGetProductCmpt();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults);
     * </pre>
     */
    protected void generateMethodSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[] { getProductCmptType().getName(),
                StringUtils.uncapitalize(getProductCmptType().getName()), "initPropertiesWithConfiguratedDefaults" };
        appendLocalizedJavaDoc("METHOD_SET_PRODUCTCMPT", replacements, getProductCmptType(), methodsBuilder);
        generateSignatureSetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults)
     * </pre>
     */
    public void generateSignatureSetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameSetProductCmpt(type);
        String[] paramTypes = new String[] { getQualifiedClassName(type), "boolean" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName,
                getMethodParamNamesSetProductCmpt(type), paramTypes);
    }

    /**
     * Returns the name of the method to set the product component, e.g. setMotorProduct
     */
    public String getMethodNameSetProductCmpt(IProductCmptType type) throws CoreException {
        return getLocalizedText(type, "METHOD_SET_PRODUCTCMPT_NAME", type.getName());
    }

    /**
     * Returns the method parameters for the method: setProductCmpt.
     */
    public String[] getMethodParamNamesSetProductCmpt(IProductCmptType type) throws CoreException {
        return new String[] { StringUtils.uncapitalize(type.getName()), "initPropertiesWithConfiguratedDefaults" };
    }

    /**
     * Code samples:
     * 
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    public void generateSignatureForMethodDefinedInModel(IMethod method,
            int javaModifier,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String[] paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < paramClassNames.length; i++) {
            if (paramTypes[i] instanceof IPolicyCmptType) {
                paramClassNames[i] = getQualifiedClassName((IPolicyCmptType)paramTypes[i]);
            } else {
                paramClassNames[i] = paramTypes[i].getJavaClassName();
            }
        }
        String returnClassName;
        if (returnType instanceof IPolicyCmptType) {
            returnClassName = getQualifiedClassName((IPolicyCmptType)returnType);
        } else {
            returnClassName = returnType.getJavaClassName();
        }
        methodsBuilder.signature(javaModifier, returnClassName, method.getName(), method.getParameterNames(),
                paramClassNames);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // empty implementation
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    public void generateSignatureGetPropertyValue(String propName,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(propName, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the getter method to access a property/attribute value.
     */
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    public String getMethodNametSetPropertyValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper) {
        return getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
    }

    public void generateCallToMethodSetPropertyValue(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragment value,
            JavaCodeFragmentBuilder builder) {
        builder.append(getMethodNametSetPropertyValue(a, datatypeHelper));
        builder.append('(');
        builder.append(value);
        builder.append(");");
    }

    /**
     * Returns the name of the parameter in the setter method for a property, e.g. newValue.
     */
    public String getParamNameForSetPropertyValue(IPolicyCmptTypeAttribute a) {
        return getLocalizedText(a, "PARAM_NEWVALUE_NAME", a.getName());
    }

    /**
     * Returns the name of the parameter in the new child mthod, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType)
            throws CoreException {
        String targetProductCmptClass = getQualifiedClassName(targetProductCmptType);
        return StringUtils.uncapitalize(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * @param targetProductCmptType
     * @return
     * @throws CoreException
     */
    private String getQualifiedClassName(IProductCmptType targetProductCmptType) throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(targetProductCmptType).getQualifiedName(true);
    }

    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerAssociationImplementation(IPolicyCmptTypeAssociation containerAssociation,
            List subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }

    public String getFieldNameForMsgCode(IValidationRule rule) {
        return getLocalizedText(rule, "FIELD_MSG_CODE_NAME", StringUtils.upperCase(rule.getName()));
    }
}