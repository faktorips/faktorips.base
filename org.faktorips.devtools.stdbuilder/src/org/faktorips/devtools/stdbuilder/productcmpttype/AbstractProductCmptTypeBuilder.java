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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.ProductCmptTypeHierarchyCodeGenerator;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractProductCmptTypeBuilder extends DefaultJavaSourceFileBuilder {

    /**
     * @param packageStructure
     * @param kindId
     * @param localizedStringsSet
     */
    public AbstractProductCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT_TYPE_V2.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * Returns the product component type this builder builds an artefact for.
     * 
     * @throws CoreException 
     */
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();    }

    /**
     * Returns the product component type stored in given ips src file.
     */
    public IProductCmptType getProductCmptType(IIpsSrcFile ipsSrcFile) throws CoreException {
        return (IProductCmptType)ipsSrcFile.getIpsObject();
    }

    protected IPolicyCmptType getPolicyCmptType() throws CoreException {
        return getProductCmptType().findPolicyCmptType(getIpsProject());
    }

    /**
     * Returns the class modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    protected int getClassModifier() throws CoreException {
        return getProductCmptType().isAbstract() ? java.lang.reflect.Modifier.PUBLIC
                | java.lang.reflect.Modifier.ABSTRACT : java.lang.reflect.Modifier.PUBLIC;
    }

    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @param element An ips element needed to access the ipsproject where the neccessary configuration
     * information is stored.
     * 
     * @see org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention
     */
    public String getAbbreviationForGenerationConcept(IIpsElement element) {
        return getChangesInTimeNamingConvention(element).
            getGenerationConceptNameAbbreviation(getLanguageUsedInGeneratedSourceCode(element));
    }
    
    /*
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
        generateCodeForPolicyCmptTypeAttributes(mainSection);
        generateCodeForRelations(mainSection.getMemberVarSectionBuilder(), mainSection.getMethodSectionBuilder());
        generateCodeForMethods(mainSection.getConstantSectionBuilder(), mainSection.getMemberVarSectionBuilder(), mainSection.getMethodSectionBuilder());
        generateConstructors(mainSection.getConstructorSectionBuilder());
        generateTypeJavadoc(mainSection.getJavaDocForTypeSectionBuilder());
        generateCodeForTableUsages(mainSection.getMemberVarSectionBuilder(), mainSection.getMethodSectionBuilder());
        generateOtherCode(mainSection.getMemberVarSectionBuilder(), mainSection.getMethodSectionBuilder());
    }

    /**
     * Generates the Javadoc for the Java class or interface.
     * 
     * @param builder The builder to use to generate the Javadoc via it's javadoc method.
     */
    protected abstract void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * A hook to generate code that is not based on attributes, relations, rules and
     * methods.
     */
    protected abstract void generateOtherCode(
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

    protected abstract void generateConstructors(JavaCodeFragmentBuilder builder)
            throws CoreException;

    /**
     * Returns the qualified name of the superclass or <code>null</code> if the class being
     * generated is not derived from a class or is an interface.
     */
    protected abstract String getSuperclass() throws CoreException;

    /**
     * Returns the qualified name of the interfaces the generated class or interface extends.
     * Returns an empty array if no interfaces are extended
     */
    protected abstract String[] getExtendedInterfaces() throws CoreException;

    /*
     * Loops over the attributes and generates code for an attribute if it is valid.
     * Takes care of proper exception handling.
     */
    private void generateCodeForPolicyCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        
        IPolicyCmptType policyCmptType = getPolicyCmptType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0] : policyCmptType.getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.isProductRelevant() || !a.isChangeable() || !a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype();
                DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));            
                }
                generateCodeForPolicyCmptTypeAttribute(a, helper, typeSection.getMemberVarSectionBuilder(), typeSection.getMethodSectionBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building attribute " + attributes[i].getName() + " of "
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    /*
     * Loops over the attributes and generates code for an attribute if it is valid.
     * Takes care of proper exception handling.
     */
    private void generateCodeForProductCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        
        IProductCmptType productCmptType = getProductCmptType();
        IProductCmptTypeAttribute[] attributes = 
            productCmptType == null ? new IProductCmptTypeAttribute[0] : productCmptType.getProductCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IProductCmptTypeAttribute a = attributes[i];
            if (!a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype(getIpsProject());
                DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));            
                }
                generateCodeForProductCmptTypeAttribute(a, helper, typeSection.getConstantSectionBuilder(), typeSection.getMemberVarSectionBuilder(), typeSection.getMethodSectionBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building attribute " + attributes[i].getName() + " of "
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    private void generateCodeForMethods(JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IProductCmptTypeMethod[] methods = getProductCmptType().getProductCmptTypeMethods();
        for (int i = 0; i < methods.length; i++) {
            try {
                if (!methods[i].isValid()) {
                    continue;
                }
                generateCodeForModelMethod(methods[i], fieldsBuilder, methodsBuilder);
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building method " + methods[i].getName() + " of "
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }    
        
    /*
     * Loops over all table structure usages and generates code for the table content access method.
     */
    private void generateCodeForTableUsages(JavaCodeFragmentBuilder fieldCodeBuilder,
            JavaCodeFragmentBuilder methodCodeBuilder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type==null) {
            return;
        }
        ITableStructureUsage[] tsus = type.getTableStructureUsages();
        for (int i = 0; i < tsus.length; i++) {
            generateCodeForTableUsage(tsus[i], fieldCodeBuilder, methodCodeBuilder);
        }
    }
    
    /**
     * This method is called from the abstract builder if the policy component attribute is valid and
     * therefore code can be generated.
     * <p>
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the member variabales section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForPolicyCmptTypeAttribute(
            IPolicyCmptTypeAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * This method is called from the abstract builder if the product component attribute is valid and
     * therefore code can be generated.
     * <p>
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the member variabales section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForProductCmptTypeAttribute(
            org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder, 
            JavaCodeFragmentBuilder constantBuilder) throws CoreException;
    
    /**
     * Generates the code for a method defined in the model. This includes formula signature definitions.
     */
    protected abstract void generateCodeForModelMethod (
            IProductCmptTypeMethod method,  
            JavaCodeFragmentBuilder fieldsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    /*
     * Loops over the relations and generates code for a relation if it is valid.
     * Takes care of proper exception handling.
     */
    private void generateCodeForRelations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        HashMap containerRelations = new HashMap();
        IAssociation[] associations = getProductCmptType().getAssociations();
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)associations[i];
            try {
                if (associations[i].validate().containsErrorMsg()) {
                    continue;
                }
                if (associations[i].isDerivedUnion()) {
                    generateCodeForContainerRelationDefinition(association, fieldsBuilder, methodsBuilder);
                } else {
                    generateCodeForNoneContainerRelation(association, fieldsBuilder, methodsBuilder);                
                }
                if (associations[i].isSubsetOfADerivedUnion()) {
                    IAssociation containerRel = associations[i].findSubsettedDerivedUnion(getIpsSrcFile().getIpsProject());
                    List implementationRelations = (List)containerRelations.get(containerRel);
                    if (implementationRelations==null) {
                        implementationRelations = new ArrayList();
                        containerRelations.put(containerRel, implementationRelations);
                    }
                    implementationRelations.add(associations[i]);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building relation "
                        + associations[i].getName() + " of "
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        CodeGeneratorForContainerAssociationImplementation generator = new CodeGeneratorForContainerAssociationImplementation(
                getIpsProject(), containerRelations, fieldsBuilder, methodsBuilder);
        generator.start(getProductCmptType());
    }
    
    /**
     * Generates the code for a none-container association definition. The method is called for every 
     * valid none-container association defined in the product component type we currently build sourcecode for.
     * 
     * @param association the association source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder the code fragment builder to build the method section.
     * 
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() method of the super
     *             class.
     *             
     * @see JavaSourceFileBuilder#addToBuildStatus(CoreException)
     * @see JavaSourceFileBuilder#addToBuildStatus(IStatus)
     */
    protected abstract void generateCodeForNoneContainerRelation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;


    /**
     * Generates the code for a container association definition. The method is called for every 
     * valid container association defined in the product component type we currently build sourcecode for.
     * 
     * @param containerAssociation the container association source code should be generated for.
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForContainerRelationDefinition(
            IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates code for a container association implementation. 
     * The method is called for every valid container association in the product 
     * component type we currently build sourcecode for and for each valid container relation
     * in one of it's supertypes.
     * 
     * @param containerAssociation the container association source code should be generated for.
     * @param implementationAssociations the relation implementing the container relation.
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param methodsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForContainerRelationImplementation(
            IProductCmptTypeAssociation containerAssociation,
            List implementationAssociations, 
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;


    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     * @param element An isp element that gives access to the ips project.
     */
    public String getVarNameEffectiveDate(IIpsElement element) {
        IChangesOverTimeNamingConvention convention = element.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
        String conceptName = convention.getEffectiveDateConceptName(element.getIpsProject().getGeneratedJavaSourcecodeDocumentationLanguage());
        return StringUtils.uncapitalise(conceptName);
    }
    
    class CodeGeneratorForContainerAssociationImplementation extends ProductCmptTypeHierarchyCodeGenerator {

        private HashMap containerImplMap; 

        public CodeGeneratorForContainerAssociationImplementation(
                IIpsProject ipsProject,
                HashMap containerImplMap,
                JavaCodeFragmentBuilder fieldsBuilder, 
                JavaCodeFragmentBuilder methodsBuilder) {
            super(ipsProject, fieldsBuilder, methodsBuilder);
            this.containerImplMap = containerImplMap;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType type) {
            IAssociation[] associations = type.getAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion()) {
                    try {
                        List implRelations = (List)containerImplMap.get(associations[i]);
                        if (implRelations!=null) {
                            generateCodeForContainerRelationImplementation((IProductCmptTypeAssociation)associations[i], implRelations, fieldsBuilder, methodsBuilder);
                        }
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus("Error building container relation implementation. "
                            + "ContainerRelation: " + associations[i]
                            + "Implementing Type: " + getProductCmptType()));
                    }
                }
            }
            return true;
        }
        
    }
}
