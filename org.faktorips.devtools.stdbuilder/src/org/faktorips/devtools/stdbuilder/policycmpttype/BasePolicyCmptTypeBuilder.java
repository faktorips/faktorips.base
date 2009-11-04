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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.method.GenMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet)
            throws CoreException {
        super(builderSet, kindId, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (attribute.isProductRelevant() && getProductCmptType() == null) {
            return;
        }
        GenAttribute generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                .getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        GenMethod generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(method);
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns the GenProductCmptType for this builder.
     */
    public GenProductCmptType getGenProductCmptType() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType());
    }

    /**
     * Returns the GenProductCmptType for this builder.
     */
    public GenPolicyCmptType getGenPolicyCmptType() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType());
    }

    // XXX AW: - REFACOTRING SUPPORT PROTOTYPE -
    @Override
    // TODO AW: this code is likely needed to be moved elsewhere
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        IIpsProject ipsProject = ipsObjectPartContainer.getIpsProject();
        try {
            IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
            IPackageFragmentRoot root = ipsProject.getJavaProject().getPackageFragmentRoot(
                    ipsObjectPath.getOutputFolderForMergableSources());
            String internalPackageSeparator = isBuildingPublishedSourceFile() ? "" : ".internal";
            IPackageFragment fragment = root.getPackageFragment(ipsObjectPath
                    .getBasePackageNameForMergableJavaClasses()
                    + internalPackageSeparator
                    + "."
                    + ipsObjectPartContainer.getIpsObject().getIpsPackageFragment().getName());
            String unitName = isBuildingPublishedSourceFile() ? getJavaNamingConvention().getPublishedInterfaceName(
                    ipsObjectPartContainer.getIpsObject().getName())
                    + ".java" : getJavaNamingConvention().getImplementationClassName(
                    ipsObjectPartContainer.getIpsObject().getName())
                    + ".java";
            ICompilationUnit compilationUnit = fragment.getCompilationUnit(unitName);
            String interfaceIdentificator = isBuildingPublishedSourceFile() ? "I" : "";
            IType javaType = compilationUnit.getType(interfaceIdentificator
                    + ipsObjectPartContainer.getIpsObject().getName());
            getGeneratedJavaElementsThis(javaElements, javaType, ipsObjectPartContainer);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        return javaElements;
    }

    protected abstract void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IType javaType,
            IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException;

}
