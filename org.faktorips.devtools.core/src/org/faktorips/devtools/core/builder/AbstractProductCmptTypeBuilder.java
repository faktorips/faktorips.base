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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractProductCmptTypeBuilder extends AbstractTypeBuilder {

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

    protected IPolicyCmptType getPcType() throws CoreException {
        return getProductCmptType().findPolicyCmptType(getIpsProject());
    }

    
    /*
     * Generates the sourcecode of the generated Java class or interface.
     */
    protected final void generateCodeForJavatype(TypeSection mainSection) throws CoreException {
        generateCodeForTableUsages(mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
    }

    /*
     * Loops over the attributes and generates code for an attribute if it is valid.
     * Takes care of proper exception handling.
     */
    protected final void generateCodeForPolicyCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        
        IPolicyCmptType policyCmptType = getPcType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0] : policyCmptType.getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.isProductRelevant() || !a.isChangeable() || !a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype(getIpsProject());
                DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));            
                }
                generateCodeForPolicyCmptTypeAttribute(a, helper, typeSection.getMemberVarBuilder(), typeSection.getMethodBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building attribute " + attributes[i].getName() + " of "
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
            if (tsus[i].isValid()) {
                generateCodeForTableUsage(tsus[i], fieldCodeBuilder, methodCodeBuilder);
            }
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

    protected abstract void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    /*
     * Loops over the associations and generates code for a association if it is valid.
     * Takes care of proper exception handling.
     */
    protected final void generateCodeForAssociations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        HashMap containerAssociations = new HashMap();
        IAssociation[] associations = getProductCmptType().getAssociations();
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)associations[i];
            try {
                if (associations[i].validate(getIpsProject()).containsErrorMsg()) {
                    continue;
                }
                if (associations[i].isDerivedUnion()) {
                    generateCodeForDerivedUnionAssociationDefinition(association, fieldsBuilder, methodsBuilder);
                } else {
                    generateCodeForNoneDerivedUnionAssociation(association, fieldsBuilder, methodsBuilder);                
                }
                if (associations[i].isSubsetOfADerivedUnion()) {
                    IAssociation containerRel = associations[i].findSubsettedDerivedUnion(getIpsSrcFile().getIpsProject());
                    List implementationAssociations = (List)containerAssociations.get(containerRel);
                    if (implementationAssociations==null) {
                        implementationAssociations = new ArrayList();
                        containerAssociations.put(containerRel, implementationAssociations);
                    }
                    implementationAssociations.add(associations[i]);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building association "
                        + associations[i].getName() + " of "
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        CodeGeneratorForContainerAssosImpl generator = new CodeGeneratorForContainerAssosImpl(
                getIpsProject(), containerAssociations, fieldsBuilder, methodsBuilder);
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
    protected abstract void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
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
    protected abstract void generateCodeForDerivedUnionAssociationDefinition(
            IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates code for a container association implementation. 
     * The method is called for every valid container association in the product 
     * component type we currently build sourcecode for and for each valid container association
     * in one of it's supertypes.
     * 
     * @param containerAssociation the container association source code should be generated for.
     * @param implementationAssociations the association implementing the container association.
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param methodsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForDerivedUnionAssociationImplementation(
            IProductCmptTypeAssociation containerAssociation,
            List implementationAssociations, 
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;


    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     * @param element An isp element that gives access to the ips project.
     */
    public static String getVarNameEffectiveDate(IIpsElement element) {
        IChangesOverTimeNamingConvention convention = element.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
        String conceptName = convention.getEffectiveDateConceptName(element.getIpsProject().getGeneratedJavaSourcecodeDocumentationLanguage());
        return StringUtils.uncapitalize(conceptName);
    }
    
    class CodeGeneratorForContainerAssosImpl extends ProductCmptTypeHierarchyCodeGenerator {

        private HashMap containerImplMap; 

        public CodeGeneratorForContainerAssosImpl(
                IIpsProject ipsProject,
                HashMap containerImplMap,
                JavaCodeFragmentBuilder fieldsBuilder, 
                JavaCodeFragmentBuilder methodsBuilder) {
            super(ipsProject, fieldsBuilder, methodsBuilder);
            this.containerImplMap = containerImplMap;
        }

        /**
         * {@inheritDoc}
         * @throws CoreException 
         */
        protected boolean visit(IProductCmptType type) throws CoreException {
            IAssociation[] associations = type.getAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion() && associations[i].isValid()) {
                    try {
                        List implAssociations = (List)containerImplMap.get(associations[i]);
                        if (implAssociations!=null) {
                            generateCodeForDerivedUnionAssociationImplementation((IProductCmptTypeAssociation)associations[i], implAssociations, fieldsBuilder, methodsBuilder);
                        }
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus("Error building container association implementation. "
                            + "DerivedUnionAssociation: " + associations[i]
                            + "Implementing Type: " + getProductCmptType(), e));
                    }
                }
            }
            return true;
        }
        
    }
}
