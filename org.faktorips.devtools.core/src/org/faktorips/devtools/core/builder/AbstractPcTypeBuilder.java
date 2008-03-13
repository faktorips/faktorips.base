/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class that can be used for builders generating Java sourcecode for a policy
 * component type.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPcTypeBuilder extends DefaultJavaSourceFileBuilder {
 
    public AbstractPcTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet stringsSet) {
        super(builderSet, kindId, stringsSet);
    }

    /**
     * Returns the policy component type this builder builds an artefact for.
     */
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }
    
    public IProductCmptType getProductCmptType() throws CoreException {
        return getPcType().findProductCmptType(getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @param element An ips element needed to access the ipsproject where the neccessary configuration
     * information is stored.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     */
    public String getAbbreviationForGenerationConcept(IIpsElement element) {
        return getChangesInTimeNamingConvention(element).
            getGenerationConceptNameAbbreviation(getLanguageUsedInGeneratedSourceCode(element));
    }

    /**
     * Returns the name (singular form) for the generation (changes over time) concept.
     * 
     * @param element An ips element needed to access the ipsproject where the neccessary configuration
     * information is stored.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     */
    public String getNameForGenerationConcept(IIpsElement element) {
        return getChangesInTimeNamingConvention(element).
            getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode(element));
    }

    /**
     * Generates the sourcecode of the generated Java class or interface.
     */
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(getClassModifier());
        mainSection.setSuperClass(getSuperclass());
        mainSection.setExtendedInterfaces(getExtendedInterfaces());
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.setClass(!generatesInterface());
        generateCodeForProductCmptTypeAttributes(mainSection);
        generateCodeForAttributes(mainSection.getConstantBuilder(), 
                mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
        generateCodeForAssociations(mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
        generateOther(mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
        generateCodeForMethodsDefinedInModel(mainSection.getMethodBuilder());
        generateConstructors(mainSection.getConstructorBuilder());
        generateTypeJavadoc(mainSection.getJavaDocForTypeBuilder());
        generateConstants(mainSection.getConstantBuilder());
        generateInnerClasses();
    }

    /**
     * Subclasses of this builder can generate inner classes within this method. Inner classes are created by
     * calling the <code>createInnerClassSection()</code> method.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions and 
     *  propagated by this method
     */
    protected void generateInnerClasses() throws CoreException{
        
    }

    /**
     * Constants are supposed to be generated within this method. Especially the generated
     * code should be passed to the provided builder.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions and 
     *  propagated by this method
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException{
    }
    
    /**
     * Generates the Javadoc for the Java class or interface.
     * 
     * @param builder The builder to use to generate the Javadoc via it's javadoc method.
     */
    protected abstract void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * A hook to generate code that is not based on attributes, associations, rules and
     * methods.
     */
    protected abstract void generateOther(
    		JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

    /**
     * Returns the qualified name of the superclass or <code>null</code> if the class being
     * generated is not derived from a class or is an interface.
     */
    protected abstract String getSuperclass() throws CoreException;

    /**
     * Returns the class modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    protected int getClassModifier() throws CoreException {
        return getPcType().isAbstract() ? java.lang.reflect.Modifier.PUBLIC
                | java.lang.reflect.Modifier.ABSTRACT : java.lang.reflect.Modifier.PUBLIC;
    }

    /**
     * Returns the qualified name of the interfaces the generated class or interface extends.
     * Returns an empty array if no interfaces are extended
     */
    protected abstract String[] getExtendedInterfaces() throws CoreException;

    /**
     * Constructors are supposed to be generated within this method. Especially the generated
     * code should be passed to the provided builder.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions and 
     *  propagated by this method
     */
    protected abstract void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException;

    /*
     * Generates the code for all attributes.
     */
    private void generateCodeForAttributes(JavaCodeFragmentBuilder constantBuilder,
                                           JavaCodeFragmentBuilder memberVarsBuilder,
                                           JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.validate(getIpsProject()).containsErrorMsg()) {
                try {
                    Datatype datatype = a.findDatatype(getIpsProject());
                    DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                    if (helper == null) {
                        throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));             //$NON-NLS-1$
                    }
                    generateCodeForAttribute(a, helper, constantBuilder, memberVarsBuilder, methodsBuilder);
                } catch (Exception e) {

                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attributes[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                    + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }
    }

    /**
     * This method is called from the build attributes method if the attribute is valid and
     * therefore code can be generated.
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param constantBuilder The code fragment builder for the static section
     * @param memberVarsBuilder The code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /*
     * Generates the code for all product component type attributes.
     */
    private void generateCodeForProductCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        
        IProductCmptType productCmptType = getProductCmptType();
        if (productCmptType==null) {
            return;
        }
        IProductCmptTypeAttribute[] attributes = productCmptType.getProductCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IProductCmptTypeAttribute a = attributes[i];
            if (!a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype(getIpsProject());
                DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));             //$NON-NLS-1$
                }
                generateCodeForProductCmptTypeAttribute(a, helper, typeSection.getConstantBuilder(), typeSection.getMemberVarBuilder(), typeSection.getMethodBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building attribute " + attributes[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    /**
     * This method is called from the build product component type attributes method if the attribute is valid and
     * therefore code can be generated.
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param constantBuilder The code fragment builder to build the java source file's static section.
     * @param memberVarsBuilder The code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForProductCmptTypeAttribute(
            IProductCmptTypeAttribute attribute, 
            DatatypeHelper helper, 
            JavaCodeFragmentBuilder constantBuilder, 
            JavaCodeFragmentBuilder memberVarBuilder, 
            JavaCodeFragmentBuilder methodBuilder) throws CoreException;
    
    /*
     * Loops over the associations and generates code for a association if it is valid.
     * Takes care of proper exception handling.
     */
    private void generateCodeForAssociations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        HashMap containerAssociations = new HashMap();
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            try {
                if (!associations[i].isValid()) {
                    continue;
                }
                generateCodeForAssociation(associations[i], fieldsBuilder, methodsBuilder);                
                if (associations[i].isSubsetOfADerivedUnion()) {
                    IAssociation derivedUnion = associations[i].findSubsettedDerivedUnion(getIpsProject());
                    List implementationAssociations = (List)containerAssociations.get(derivedUnion);
                    if (implementationAssociations==null) {
                        implementationAssociations = new ArrayList();
                        containerAssociations.put(derivedUnion, implementationAssociations);
                    }
                    implementationAssociations.add(associations[i]);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building association " //$NON-NLS-1$
                        + associations[i].getName() + " of " //$NON-NLS-1$
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        CodeGeneratorForContainerAssociationImplementation generator = new CodeGeneratorForContainerAssociationImplementation(containerAssociations, fieldsBuilder, methodsBuilder);
        generator.start(getPcType());
    }
    
    /**
     * Generates the code for a association. The method is called for every 
     * valid association defined in the policy component type we currently build sourcecode for.
     * 
     * @param association the association source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder the code fragment builder to build the method section.
     * @throws Exception Any exception thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() method of the super
     *             class.
     * @see JavaSourceFileBuilder#addToBuildStatus(CoreException)
     * @see JavaSourceFileBuilder#addToBuildStatus(IStatus)
     */
    protected abstract void generateCodeForAssociation(
    		IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for the implementation of an abstract container association.
     * 
     * @param containerAssociation the container association that is common for the associations in the group
     * @param subAssociations a group of association instances that have the same container association
     * @param memberVarsBuilder the code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder the code fragment builder to build the method section.
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() methods of the super
     *             class.
     */
    protected abstract void generateCodeForContainerAssociationImplementation(
    		IPolicyCmptTypeAssociation containerAssociation,
            List subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the sourcecode for all methods defined in the policy component type.
     * 
     * @throws CoreException
     */
    protected final void generateCodeForMethodsDefinedInModel(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IMethod[] methods = getPcType().getMethods();
        IIpsProject project = getPcType().getIpsProject();
        for (int i = 0; i < methods.length; i++) {
            IMethod method = methods[i];
            if (!method.validate(getIpsProject()).containsErrorMsg()) {
                try {
                    Datatype returnType = project.findDatatype(method.getDatatype());
                    IParameter[] params = method.getParameters();
                    Datatype[] paramDatatypes = new Datatype[params.length];
                    for (int j = 0; j < paramDatatypes.length; j++) {
                    	paramDatatypes[j] = project.findDatatype(params[j].getDatatype());
					}
                    generateCodeForMethodDefinedInModel(method, returnType, paramDatatypes, methodsBuilder);
                    
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building method " + methods[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                    + getQualifiedClassName(method.getIpsObject()), e));
                }
            }
        }
    }

	/**
	 * Generates the sourcecode for the indicated method.
	 */
	protected abstract void generateCodeForMethodDefinedInModel(
			IMethod method,
			Datatype returnType,
			Datatype[] paramTypes,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    
    class CodeGeneratorForContainerAssociationImplementation extends PolicyCmptTypeHierarchyCodeGenerator {

        private HashMap containerImplMap;
        
        public CodeGeneratorForContainerAssociationImplementation(HashMap containerImplMap, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) {
            super(fieldsBuilder, methodsBuilder);
            this.containerImplMap = containerImplMap;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPolicyCmptTypeAssociation[] associations = currentType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion() && associations[i].isValid()) {
                    try {
                        List implAssociations = (List)containerImplMap.get(associations[i]);
                        if (implAssociations!=null) {
                            generateCodeForContainerAssociationImplementation(associations[i], implAssociations, fieldsBuilder, methodsBuilder);
                        }
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus("Error building container association implementation. " //$NON-NLS-1$
                            + "ContainerAssociation: " + associations[i] //$NON-NLS-1$
                            + "Implementing Type: " + getPcType(), e)); //$NON-NLS-1$
                    }
                }
            }
            return true;
        }
        
    }
}
