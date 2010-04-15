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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class that can be used for builders generating Java source code for a policy
 * component type.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPcTypeBuilder extends AbstractTypeBuilder {

    public AbstractPcTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet) {
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

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    /**
     * Generates the source code of the generated Java class or interface.
     */
    @Override
    protected void generateCodeForJavatype(TypeSection mainSection) throws CoreException {
        generateCodeForValidationRules(mainSection.getConstantBuilder(), mainSection.getMemberVarBuilder(), mainSection
                .getMethodBuilder());
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
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.validate(getIpsProject()).containsErrorMsg()) {
                try {
                    Datatype datatype = a.findDatatype(getIpsProject());
                    DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                    if (helper == null) {
                        throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype)); //$NON-NLS-1$
                    }
                    generateCodeForAttribute(a, helper, mainSection.getConstantBuilder(), mainSection
                            .getMemberVarBuilder(), mainSection.getMethodBuilder());
                } catch (Exception e) {

                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attributes[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
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
     * @param datatypeHelper The data type code generation helper for the attribute's data type.
     * @param constantBuilder The code fragment builder for the static section
     * @param memberVarsBuilder The code fragment builder to build the member variables section.
     * @param memberVarsBuilder The code fragment builder to build the method section.
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
     * @throws CoreException if an exception occurs while generating code
     */
    protected void generateCodeForValidationRules(JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            try {
                if (!rules[i].validate(getIpsProject()).containsErrorMsg()) {
                    generateCodeForValidationRule(rules[i]);
                }
            } catch (CoreException e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building validation rule " + rules[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    /**
     * Generates the code for the provided validation rule.
     * 
     * @param validationRule the validation rule for which this method can generate code
     * @throws CoreException
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
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            try {
                if (!associations[i].isValid()) {
                    continue;
                }
                generateCodeForAssociation(associations[i], fieldsBuilder, methodsBuilder);
                if (associations[i].isSubsetOfADerivedUnion()) {
                    IAssociation derivedUnion = associations[i].findSubsettedDerivedUnion(getIpsProject());
                    List<IAssociation> implementationAssociations = derivedUnions.get(derivedUnion);
                    if (implementationAssociations == null) {
                        implementationAssociations = new ArrayList<IAssociation>();
                        derivedUnions.put(derivedUnion, implementationAssociations);
                    }
                    implementationAssociations.add(associations[i]);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building association " //$NON-NLS-1$
                        + associations[i].getName() + " of " //$NON-NLS-1$
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        CodeGeneratorForContainerAssociationImplementation generator = new CodeGeneratorForContainerAssociationImplementation(
                derivedUnions, fieldsBuilder, methodsBuilder);
        generator.start(getPcType());
    }

    /**
     * Generates the code for a association. The method is called for every valid association
     * defined in the policy component type we currently build source code for.
     * 
     * @param association the association source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the member variables section.
     * @param fieldsBuilder the code fragment builder to build the method section.
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
     * @param memberVarsBuilder the code fragment builder to build the method section.
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

        public CodeGeneratorForContainerAssociationImplementation(
                HashMap<IAssociation, List<IAssociation>> derivedUnionMap, JavaCodeFragmentBuilder fieldsBuilder,
                JavaCodeFragmentBuilder methodsBuilder) {
            super(fieldsBuilder, methodsBuilder);
            this.derivedUnionMap = derivedUnionMap;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPolicyCmptTypeAssociation[] associations = currentType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion() && associations[i].isValid()) {
                    try {
                        List<IAssociation> implAssociations = derivedUnionMap.get(associations[i]);
                        if (implAssociations != null) {
                            generateCodeForContainerAssociationImplementation(associations[i], implAssociations,
                                    fieldsBuilder, methodsBuilder);
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
