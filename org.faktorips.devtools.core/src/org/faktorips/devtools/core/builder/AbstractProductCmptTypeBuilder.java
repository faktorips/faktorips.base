/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
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

    public AbstractProductCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {

        super(builderSet, kindId, localizedStringsSet);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * Returns the product component type this builder builds an artifact for.
     */
    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected IPolicyCmptType getPcType() throws CoreException {
        return getProductCmptType().findPolicyCmptType(getIpsProject());
    }

    /**
     * Generates the source code of the generated Java class or interface.
     */
    @Override
    protected final void generateCodeForJavatype(TypeSection mainSection) throws CoreException {
        generateCodeForTableUsages(mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
    }

    /**
     * Loops over the attributes and generates code for an attribute if it is valid. Takes care of
     * proper exception handling.
     */
    @Override
    protected final void generateCodeForPolicyCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        IPolicyCmptType policyCmptType = getPcType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0]
                : policyCmptType.getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute a = attribute;
            if (!a.isProductRelevant() || !a.isChangeable() || !a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype(getIpsProject());
                DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype)); //$NON-NLS-1$
                }
                generateCodeForPolicyCmptTypeAttribute(a, helper, typeSection.getMemberVarBuilder(), typeSection
                        .getMethodBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building attribute " + attribute.getName() //$NON-NLS-1$
                        + " of " + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e)); //$NON-NLS-1$
            }
        }
    }

    /**
     * Loops over all table structure usages and generates code for the table content access method.
     */
    private void generateCodeForTableUsages(JavaCodeFragmentBuilder fieldCodeBuilder,
            JavaCodeFragmentBuilder methodCodeBuilder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type == null) {
            return;
        }
        ITableStructureUsage[] tsus = type.getTableStructureUsages();
        for (ITableStructureUsage tsu : tsus) {
            if (tsu.isValid()) {
                generateCodeForTableUsage(tsu, fieldCodeBuilder, methodCodeBuilder);
            }
        }
    }

    /**
     * This method is called from the abstract builder if the policy component attribute is valid
     * and therefore code can be generated.
     * 
     * @param attribute The attribute source code should be generated for.
     * @param datatypeHelper The data type code generation helper for the attribute's data type.
     * @param fieldsBuilder The code fragment builder to build the member variables section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * Loops over the associations and generates code for a association if it is valid. Takes care
     * of proper exception handling.
     */
    @Override
    protected final void generateCodeForAssociations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        HashMap<IAssociation, List<IAssociation>> derivedUnionAssociations = new HashMap<IAssociation, List<IAssociation>>();
        IAssociation[] associations = getProductCmptType().getAssociations();
        for (IAssociation association2 : associations) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)association2;
            try {
                if (association.validate(getIpsProject()).containsErrorMsg()) {
                    continue;
                }
                if (association.isDerivedUnion()) {
                    generateCodeForDerivedUnionAssociationDefinition(association, fieldsBuilder, methodsBuilder);
                } else {
                    generateCodeForNoneDerivedUnionAssociation(association, fieldsBuilder, methodsBuilder);
                }
                if (association.isSubsetOfADerivedUnion()) {
                    IAssociation derivedUnion = association2.findSubsettedDerivedUnion(getIpsSrcFile().getIpsProject());
                    List<IAssociation> implementationAssociations = derivedUnionAssociations.get(derivedUnion);
                    if (implementationAssociations == null) {
                        implementationAssociations = new ArrayList<IAssociation>();
                        derivedUnionAssociations.put(derivedUnion, implementationAssociations);
                    }
                    implementationAssociations.add(association2);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building association " //$NON-NLS-1$
                        + association2.getName() + " of " + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e)); //$NON-NLS-1$
            }
        }
        CodeGeneratorForDerivedUnionSubsets generator = new CodeGeneratorForDerivedUnionSubsets(getIpsProject(),
                derivedUnionAssociations, fieldsBuilder, methodsBuilder);
        generator.start(getProductCmptType());
    }

    /**
     * Generates the code for a none-container association definition. The method is called for
     * every valid none-container association defined in the product component type we currently
     * build source code for.
     * 
     * @param association the association source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the member variables section.
     * @param methodsBuilder the code fragment builder to build the method section.
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
     * valid container association defined in the product component type we currently build source
     * code for.
     * 
     * @param containerAssociation the container association source code should be generated for.
     * @param fieldsBuilder the code fragment builder to build the member variables section.
     * @param methodsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates code for a container association implementation. The method is called for every
     * valid container association in the product component type we currently build source code for
     * and for each valid container association in one of it's super types.
     * 
     * @param derivedUnionAssociation the container association source code should be generated for.
     * @param implementationAssociations the association implementing the container association.
     * @param fieldsBuilder the code fragment builder to build the member variables section.
     * @param methodsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> implementationAssociations,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    class CodeGeneratorForDerivedUnionSubsets extends ProductCmptTypeHierarchyCodeGenerator {

        private HashMap<IAssociation, List<IAssociation>> derivedUnionMap;

        public CodeGeneratorForDerivedUnionSubsets(IIpsProject ipsProject,
                HashMap<IAssociation, List<IAssociation>> derivedUnionMap, JavaCodeFragmentBuilder fieldsBuilder,
                JavaCodeFragmentBuilder methodsBuilder) {
            super(ipsProject, fieldsBuilder, methodsBuilder);
            this.derivedUnionMap = derivedUnionMap;
        }

        @Override
        protected boolean visit(IProductCmptType type) throws CoreException {
            IAssociation[] associations = type.getAssociations();
            for (IAssociation association : associations) {
                if (association.isDerivedUnion() && association.isValid()) {
                    try {
                        List<IAssociation> implAssociations = derivedUnionMap.get(association);
                        if (implAssociations != null) {
                            generateCodeForDerivedUnionAssociationImplementation(
                                    (IProductCmptTypeAssociation)association, implAssociations, fieldsBuilder,
                                    methodsBuilder);
                        }
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus("Error building container association implementation. " //$NON-NLS-1$
                                + "DerivedUnionAssociation: " + association + "Implementing Type: " //$NON-NLS-1$ //$NON-NLS-2$
                                + getProductCmptType(), e));
                    }
                }
            }
            return true;
        }

    }

}
