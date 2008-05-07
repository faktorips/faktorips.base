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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationTo1;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationToMany;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

public class GenProductCmptType extends GenType {

    private Map generatorsByPart = new HashMap();
    private List genProdAttributes = new ArrayList();
    private List genProdAssociations = new ArrayList();

    public GenProductCmptType(IProductCmptType productCmptType, StandardBuilderSet builderSet,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(productCmptType, builderSet, stringsSet);
        ArgumentCheck.notNull(productCmptType, this);
        ArgumentCheck.notNull(builderSet, this);

        createGeneratorsForProdAttributes();
        createGeneratorsForProdAssociations();
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getType();
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        if (forInterface) {
            String name = getType().getName() + getAbbreviationForGenerationConcept();
            return getJavaNamingConvention().getPublishedInterfaceName(name);
        }
        String generationAbb = getAbbreviationForGenerationConcept();
        return getJavaNamingConvention().getImplementationClassName(getType().getName() + generationAbb);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IIpsProject ipsProject, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        IPolicyCmptType pcType = getProductCmptType().findPolicyCmptType(ipsProject);
        String[] replacements = new String[] { getNameForGenerationConcept(), getType().getName(),
                pcType != null ? pcType.getName() : "missing" };
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, methodsBuilder);
        generateSignatureGetProductCmptGeneration(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Returns the name of the method to access the product component generation, e.g.
     * getMotorProductGen
     */
    // TODO duplicate method in PolicyCmptInterfaceBuilder
    public String getMethodNameGetProductCmptGeneration() throws CoreException {
        String[] replacements = new String[] { getType().getName(), getAbbreviationForGenerationConcept(),
                getNameForGenerationConcept() };
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetProductCmptGeneration();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedClassNameForProductCmptTypeGen(true),
                methodName, new String[0], new String[0]);
    }

    /**
     * Returns the name of the method to access the product component, e.g. getMotorProduct
     */
    public String getMethodNameGetProductCmpt() throws CoreException {
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_NAME", getProductCmptType().getName());
    }

    private void createGeneratorsForProdAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdAttribute.class);
        IProductCmptType type = getProductCmptType();
        if (type != null) {
            IProductCmptTypeAttribute[] attrs = type.getProductCmptTypeAttributes();
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i].isValid()) {
                    GenProdAttribute generator = createGenerator(attrs[i], stringsSet);
                    if (generator != null) {
                        genProdAttributes.add(generator);
                        generatorsByPart.put(attrs[i], generator);
                    }
                }
            }
        }
    }

    private void createGeneratorsForProdAssociations() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdAssociation.class);
        IProductCmptType type = getProductCmptType();
        if (type != null) {
            IAssociation[] ass = type.getAssociations();
            for (int i = 0; i < ass.length; i++) {
                if (ass[i].isValid() && ass[i] instanceof IProductCmptTypeAssociation) {
                    GenProdAssociation generator = createGenerator((IProductCmptTypeAssociation)ass[i], stringsSet);
                    if (generator != null) {
                        genProdAssociations.add(generator);
                        generatorsByPart.put(ass[i], generator);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAttribute createGenerator(IProductCmptTypeAttribute a, LocalizedStringsSet stringsSet)
            throws CoreException {
        return new GenProdAttribute(this, a, stringsSet);
    }

    protected GenProdAssociation createGenerator(IProductCmptTypeAssociation association, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (association.is1ToMany()) {
            return new GenProdAssociationToMany(this, association, stringsSet);
        }
        return new GenProdAssociationTo1(this, association, stringsSet);
    }

    public GenProdAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        GenProdAttribute generator = (GenProdAttribute)generatorsByPart.get(a);
        if (null == generator) {
            generator = createGenerator(a, new LocalizedStringsSet(GenProdAttribute.class));
            if (generator != null) {
                genProdAttributes.add(generator);
                generatorsByPart.put(a, generator);
            }
        }
        return generator;
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        if (generatorsByPart.containsKey(a)) {
            return (GenProdAssociation)generatorsByPart.get(a);
        } else if (a.isValid()) {
            GenProdAssociation generator = createGenerator(a, new LocalizedStringsSet(GenProdAssociation.class));
            if (generator != null) {
                genProdAssociations.add(generator);
                generatorsByPart.put(a, generator);
            }
            return generator;
        }
        return null;
    }

    public Iterator getGenProdAttributes() {
        return genProdAttributes.iterator();
    }

    public JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()."
                + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }

    public String getMethodNameGetGeneration() throws CoreException {
        IChangesOverTimeNamingConvention convention = getProductCmptType().getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        String generationConceptName = convention.getGenerationConceptNameSingular(getProductCmptType().getIpsProject()
                .getGeneratedJavaSourcecodeDocumentationLanguage());
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(getProductCmptType()
                .getIpsProject().getGeneratedJavaSourcecodeDocumentationLanguage());
        return getLocalizedText("METHOD_GET_GENERATION_NAME", new String[] { getProductCmptType().getName(),
                generationConceptAbbreviation, generationConceptName });
    }

    public String getQualifiedClassNameForProductCmptTypeGen(boolean forInterface) throws CoreException {
        return getQualifiedName(forInterface)
                + getChangesInTimeNamingConvention().getGenerationConceptNameAbbreviation(
                        getLanguageUsedInGeneratedSourceCode());
    }

}
