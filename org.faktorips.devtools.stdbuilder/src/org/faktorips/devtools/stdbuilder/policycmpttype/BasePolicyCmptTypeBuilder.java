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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.method.GenPolicyCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * Abstract base class for the policy component type interface and policy component type
 * implementation builders.
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet)
            throws CoreException {

        super(builderSet, kindId, stringsSet);
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
    }

    public boolean isGenerateDeltaSupport() {
        return getBuilderSet().getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT).booleanValue();
    }

    public boolean isGenerateCopySupport() {
        return getBuilderSet().getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_COPY_SUPPORT).booleanValue();
    }

    public boolean isGenerateVisitorSupport() {
        return getBuilderSet().getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT).booleanValue();
    }

    /**
     * This validation is necessary because otherwise a java class file is created with a wrong java
     * class name this causes jmerge to throw an exception
     */
    protected boolean hasValidProductCmptTypeName() throws CoreException {
        IProductCmptType type = getProductCmptType();
        MessageList msgList = type.validate(getIpsProject());
        return !msgList.getMessagesFor(type, IIpsElement.PROPERTY_NAME).containsErrorMsg();
    }

    @Override
    protected void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (attribute.isProductRelevant() && getProductCmptType() == null) {
            return;
        }
        GenPolicyCmptTypeAttribute generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                .getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        GenPolicyCmptTypeMethod generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                .getGenerator(method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForValidationRule(IValidationRule validationRule) throws CoreException {
        GenValidationRule generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(
                validationRule);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenAssociation generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(
                association);
        generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
    }

    boolean isFirstDependantTypeInHierarchy(IPolicyCmptType type) throws CoreException {
        if (!type.isDependantType()) {
            return false;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype == null) {
            return true;
        }
        return !supertype.isDependantType();
    }

    protected boolean isUseTypesafeCollections() {
        return ((StandardBuilderSet)getBuilderSet()).isUseTypesafeCollections();
    }

    protected boolean isGenerateJaxbSuppert() {
        return ((StandardBuilderSet)getBuilderSet()).isGenerateJaxbSupport();
    }

    /** Returns the GenProductCmptType for this builder. */
    public GenProductCmptType getGenProductCmptType() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType());
    }

    /** Returns the GenProductCmptType for this builder. */
    public GenPolicyCmptType getGenPolicyCmptType() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType());
    }

    private GenType getGenType(IType type) {
        try {
            return ((StandardBuilderSet)getBuilderSet()).getGenerator(type);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO AW: This code could be on a higher abstraction level but the hierarchy doesn't allow it.
    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement) {
        IType type = null;
        if (ipsElement instanceof IType) {
            type = (IType)ipsElement;
        } else if (ipsElement instanceof IAttribute) {
            type = ((IAttribute)ipsElement).getType();
        } else if (ipsElement instanceof IMethod) {
            type = ((IMethod)ipsElement).getType();
        } else {
            return;
        }

        GenType genType = getGenType(type);
        org.eclipse.jdt.core.IType javaType = getGeneratedJavaType(type);
        if (isBuildingPublishedSourceFile()) {
            genType.getGeneratedJavaElementsForPublishedInterface(javaElements, javaType, ipsElement);
        } else {
            genType.getGeneratedJavaElementsForImplementation(javaElements, javaType, ipsElement);
        }
    }

}
