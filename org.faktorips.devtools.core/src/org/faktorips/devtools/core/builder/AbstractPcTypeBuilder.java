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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class that can be used for builders generating Java source code for a policy
 * component type.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPcTypeBuilder extends AbstractTypeBuilder {

    public AbstractPcTypeBuilder(DefaultBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet) {
        super(builderSet, kindId, stringsSet);
    }

    /**
     * Returns the policy component type this builder builds an artifact for.
     */
    @Override
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }

    @Override
    public IProductCmptType getProductCmptType() throws CoreException {
        return getPcType().findProductCmptType(getIpsProject());
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    /**
     * Generates the source code of the generated Java class or interface.
     */
    @Override
    protected void generateCodeForJavatype(TypeSection mainSection) throws CoreException {
        generateCodeForValidationRules(mainSection.getConstantBuilder(), mainSection.getMemberVarBuilder(),
                mainSection.getMethodBuilder());
        generateInnerClasses();
    }

    /**
     * Subclasses of this builder can generate inner classes within this method. Inner classes are
     * created by calling the <code>createInnerClassSection()</code> method.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions
     *             and propagated by this method
     */
    @Override
    protected void generateInnerClasses() throws CoreException {
        // could be implemented in subclass
    }

    @Override
    protected final void generateCodeForPolicyCmptTypeAttributes(TypeSection mainSection) throws CoreException {
        List<IPolicyCmptTypeAttribute> attributes = getPcType().getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute a = attribute;
            if (!a.validate(getIpsProject()).containsErrorMsg()) {
                try {
                    Datatype datatype = a.findDatatype(getIpsProject());
                    DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                    if (helper == null) {
                        throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype)); //$NON-NLS-1$
                    }
                    generateCodeForAttribute(a, helper, mainSection.getConstantBuilder(),
                            mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
                } catch (Exception e) {

                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attribute.getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                    + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }
    }

    protected abstract void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * This method is called from the build attributes method if the attribute is valid and
     * therefore code can be generated.
     * 
     * @param attribute The attribute source code should be generated for.
     * @param helper The data type code generation helper for the attribute's data type.
     * @param constantBuilder The code fragment builder for the static section
     * @param memberVarBuilder The code fragment builder to build the member variables section.
     * @param methodBuilder The code fragment builder to build the method section.
     */
    @Override
    protected abstract void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException;

    /**
     * Generates the code for the validation rules of the ProductCmptType which is assigned to this
     * builder.
     * 
     * @param constantBuilder The code fragment builder for the static section
     * @param memberVarBuilder The code fragment builder to build the member variables section.
     * @param methodBuilder The code fragment builder to build the method section.
     * 
     * @throws CoreException if an exception occurs while generating code
     */
    protected void generateCodeForValidationRules(JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        List<IValidationRule> rules = getPcType().getValidationRules();
        for (IValidationRule rule : rules) {
            try {
                if (!rule.validate(getIpsProject()).containsErrorMsg()) {
                    generateCodeForValidationRule(rule);
                }
            } catch (CoreException e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building validation rule " + rule.getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    /**
     * Generates the code for the provided validation rule.
     * 
     * @param validationRule the validation rule for which this method can generate code
     */
    protected abstract void generateCodeForValidationRule(IValidationRule validationRule) throws CoreException;

    /**
     * Loops over the associations and generates code for a association if it is valid. Takes care
     * of proper exception handling.
     */
    @Override
    protected final void generateCodeForAssociations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        HashMap<IAssociation, List<IAssociation>> derivedUnions = new HashMap<IAssociation, List<IAssociation>>();
        List<IPolicyCmptTypeAssociation> associations = getPcType().getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation association : associations) {
            try {
                if (!association.isValid(association.getIpsProject())) {
                    continue;
                }
                if (skipGenerateCodeForAssociation(association)) {
                    continue;
                }

                generateCodeForAssociation(association, fieldsBuilder, methodsBuilder);
                if (association.isSubsetOfADerivedUnion()) {
                    IAssociation derivedUnion = association.findSubsettedDerivedUnion(getIpsProject());
                    List<IAssociation> implementationAssociations = derivedUnions.get(derivedUnion);
                    if (implementationAssociations == null) {
                        implementationAssociations = new ArrayList<IAssociation>();
                        derivedUnions.put(derivedUnion, implementationAssociations);
                    }
                    implementationAssociations.add(association);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building association " //$NON-NLS-1$
                        + association.getName() + " of " //$NON-NLS-1$
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        CodeGeneratorForContainerAssociationImplementation generator = new CodeGeneratorForContainerAssociationImplementation(
                getIpsProject(), derivedUnions, fieldsBuilder, methodsBuilder);
        generator.start(getPcType());
    }

    /**
     * Could be overwritten in concrete implementation
     * 
     * @param association the association that may be skipped
     * @throws CoreException in case of an core exception
     */
    protected boolean skipGenerateCodeForAssociation(IPolicyCmptTypeAssociation association) throws CoreException {
        if (association.getAssociationType().isCompositionDetailToMaster()) {
            // Detail to Master associations could have the same name as a corresponding
            // association in the super type @see MTB#357 and FIPS-85
            // The Implementation is generateCodeForDependantObject(JavaCodeFragmentBuilder,
            // JavaCodeFragmentBuilder, List<IPolicyCmptTypeAssociation>)
            IType type = association.getType();
            IType supertype = type.findSupertype(type.getIpsProject());
            if (supertype != null
                    && supertype.findAssociation(association.getName(), supertype.getIpsProject()) != null) {
                // there is an association with the same name --> do not create the
                // getter
                return true;
            }
        }
        return false;
    }

    /**
     * Generates the code for a association. The method is called for every valid association
     * defined in the policy component type we currently build source code for.
     * 
     * @param association the association source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the member variables section.
     * @param methodsBuilder the code fragment builder to build the method section.
     * @throws Exception Any exception thrown leads to an interruption of the current build cycle of
     *             this builder. Alternatively it is possible to catch an exception and log it by
     *             means of the addToBuildStatus() method of the super class.
     * @see JavaSourceFileBuilder#addToBuildStatus(CoreException)
     * @see JavaSourceFileBuilder#addToBuildStatus(IStatus)
     */
    protected abstract void generateCodeForAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for the implementation of an abstract container association.
     * 
     * @param containerAssociation the container association that is common for the associations in
     *            the group
     * @param subAssociations a group of association instances that have the same container
     *            association
     * @param memberVarsBuilder the code fragment builder to build the member variables section.
     * @param methodsBuilder the code fragment builder to build the method section.
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() methods of the super
     *             class.
     */
    protected abstract void generateCodeForContainerAssociationImplementation(IPolicyCmptTypeAssociation containerAssociation,
            List<IAssociation> subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    class CodeGeneratorForContainerAssociationImplementation extends PolicyCmptTypeHierarchyCodeGenerator {

        private HashMap<IAssociation, List<IAssociation>> derivedUnionMap;

        public CodeGeneratorForContainerAssociationImplementation(IIpsProject ipsProject,
                HashMap<IAssociation, List<IAssociation>> derivedUnionMap, JavaCodeFragmentBuilder fieldsBuilder,
                JavaCodeFragmentBuilder methodsBuilder) {

            super(ipsProject, fieldsBuilder, methodsBuilder);
            this.derivedUnionMap = derivedUnionMap;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            List<IPolicyCmptTypeAssociation> associations = currentType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation association : associations) {
                if (association.isDerivedUnion() && association.isValid(association.getIpsProject())) {
                    try {
                        List<IAssociation> implAssociations = derivedUnionMap.get(association);
                        if (implAssociations != null) {
                            generateCodeForContainerAssociationImplementation(association, implAssociations,
                                    fieldsBuilder, methodsBuilder);
                        }
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus("Error building container association implementation. " //$NON-NLS-1$
                                + "ContainerAssociation: " + association + "Implementing Type: " + getPcType(), e)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            return true;
        }

    }

}
