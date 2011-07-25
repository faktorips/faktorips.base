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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.util.LocalizedStringsSet;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    public PolicyCmptInterfaceBuilder(DefaultBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class));
        setMergeEnabled(true);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    protected String getSuperclass() throws CoreException {
        return null;
    }

    /**
     * Returns the <code>GenPolicyCmptType</code> for this builder.
     */
    private GenPolicyCmptType getGenerator() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType());
    }

    @Override
    protected String[] getExtendedInterfaces() throws CoreException {
        List<String> interfaces = new ArrayList<String>();
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
            if (isGenerateDeltaSupport()) {
                interfaces.add(IDeltaSupport.class.getName());
            }
            if (isGenerateCopySupport()) {
                interfaces.add(ICopySupport.class.getName());
            }
            if (isGenerateVisitorSupport()) {
                interfaces.add(IVisitorSupport.class.getName());
            }
            String notificationSupportInterfaceName = getGenerator().getNotificationSupportInterfaceName();
            if (notificationSupportInterfaceName != null) {
                interfaces.add(notificationSupportInterfaceName);
            }
        }
        if (isFirstDependantTypeInHierarchy(type)) {
            interfaces.add(IDependantObject.class.getName());
        }

        return interfaces.toArray(new String[interfaces.size()]);
    }

    @Override
    protected boolean generatesInterface() {
        return true;
    }

    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do
    }

    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        String description = getDescriptionInGeneratorLanguage(getIpsObject());
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), description, getIpsObject(), builder);
    }

    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (getProductCmptType() != null) {
            if (hasValidProductCmptTypeName()) {
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodSetProductCmpt(methodsBuilder);
                getGenProductCmptType().generateMethodGetProductCmptGeneration(getIpsProject(), methodsBuilder);
            }
        }
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
        getGenProductCmptType().generateSignatureGetProductCmpt(methodsBuilder);
        methodsBuilder.append(";");
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
        getGenProductCmptType().generateSignatureSetProductComponent(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    @Override
    protected void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // Empty implementation.
    }

    /**
     * Empty implementation.
     * 
     * Overridden
     */
    @Override
    protected void generateCodeForContainerAssociationImplementation(IPolicyCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return true;
    }

}
