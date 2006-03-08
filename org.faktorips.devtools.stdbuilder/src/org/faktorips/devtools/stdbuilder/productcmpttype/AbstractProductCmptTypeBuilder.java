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
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * 
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractProductCmptTypeBuilder extends JavaSourceFileBuilder {

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
     * Overridden.
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IPolicyCmptType type = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        if (type.isConfigurableByProductCmptType()) {
            // this condition can't be handled in isBuilderFor() as the isBuilderFor() method
            // is also called in the case the file has been deleted. In this case, the 
            // file's ips object can't be accessed. 
            super.build(ipsSrcFile);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        // TODO delete generate files for product cmpt type
        // the problem here is that the name of the product component type can't
        // be derived from the file name. It is stored in the ips srcfile that contains
        // the policy component type. But this has been deleted, so we don't now the name
        // of the product component type. perhaps we can use the toc.
    }

    /**
     * Returns the product component type this builder builds an artefact for.
     */
    public IProductCmptType getProductCmptType() {
        try {
            return ((IPolicyCmptType)getIpsObject()).findProductCmptType();
        } catch (CoreException e) {
            throw new RuntimeException(e); // this can never happen
        }
    }

    /**
     * Returns the product component type this builder builds an artefact for.
     */
    public IProductCmptType getProductCmptType(IIpsSrcFile ipsSrcFile) throws CoreException {
        IPolicyCmptType type = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        if (!type.isConfigurableByProductCmptType()) {
            return null;
        }
        return type.findProductCmptType();
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
    
    /**
     * Overridden.
     * 
     * Calls the generateInternal() method and addes the package and import declarations to the
     * content.
     */
    public String generate() throws CoreException {
        StringBuffer content = new StringBuffer();
        content.append("package ");
        content.append(getPackage(getIpsSrcFile()));
        content.append(';');
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        JavaCodeFragment code = generateCodeForJavatype();
        content.append(code.getImportDeclaration().toString());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(code.getSourcecode());
        return content.toString();
    }

    /*
     * Generates the sourcecode of the generated Java class or interface.
     */
    private JavaCodeFragment generateCodeForJavatype() throws CoreException {
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        generateTypeJavadoc(codeBuilder);
        if (generatesInterface()) {
            codeBuilder.interfaceBegin(getUnqualifiedClassName(), getExtendedInterfaces());
        } else {
            codeBuilder.classBegin(getClassModifier(), getUnqualifiedClassName(), getSuperclass(),
                getExtendedInterfaces());
        }
        JavaCodeFragmentBuilder fieldCodeBuilder = new JavaCodeFragmentBuilder();
        JavaCodeFragmentBuilder methodCodeBuilder = new JavaCodeFragmentBuilder();

        generateCodeForAttributes(fieldCodeBuilder, methodCodeBuilder);
        generateCodeForRelations(fieldCodeBuilder, methodCodeBuilder);
        generateOtherCode(fieldCodeBuilder, methodCodeBuilder);

        codeBuilder.append(fieldCodeBuilder.getFragment());
        generateConstructors(codeBuilder);
        codeBuilder.append(methodCodeBuilder.getFragment());

        codeBuilder.classEnd();
        return codeBuilder.getFragment();
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
    private void generateCodeForAttributes(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IAttribute[] attributes = getProductCmptType().findPolicyCmptyType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.isProductRelevant()) {
                continue;
            }
            if (!a.validate().containsErrorMsg()) {
                try {
                    Datatype datatype = a.getIpsProject().findDatatype(a.getDatatype());
                    DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                    if (helper == null) {
                        throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));            
                    }
                    generateCodeForAttribute(a, helper, fieldsBuilder, methodsBuilder);
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attributes[i].getName() + " of "
                                    + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }
    }

    /**
     * This method is called from the build attributes method if the attribute is valid and
     * therefore code can be generated.
     * <p>
     * The default implementation delegates to special methods depending on the atttribute type.
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder The code fragment builder to build the method section.
     */
    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (attribute.isChangeable()) {
            generateCodeForChangeableAttribute(attribute, datatypeHelper, fieldsBuilder, methodsBuilder);
        } else if (attribute.getAttributeType()==AttributeType.CONSTANT) {
            generateCodeForConstantAttribute(attribute, datatypeHelper, fieldsBuilder, methodsBuilder);
        } else if (attribute.isDerivedOrComputed()) {
            generateCodeForComputedAndDerivedAttribute(attribute, datatypeHelper, fieldsBuilder, methodsBuilder);
        } else {
            throw new RuntimeException("Attribute " + attribute +" has an unknown type " + attribute.getAttributeType());
        }
    }
    
    protected abstract void generateCodeForChangeableAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForConstantAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    protected abstract void generateCodeForComputedAndDerivedAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper, 
            JavaCodeFragmentBuilder fieldsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /*
     * Loops over the relations and generates code for a relation if it is valid.
     * Takes care of proper exception handling.
     */
    private void generateCodeForRelations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        HashMap containerRelations = new HashMap();
        IProductCmptTypeRelation[] relations = getProductCmptType().getRelations();
        for (int i = 0; i < relations.length; i++) {
            try {
                if (relations[i].validate().containsErrorMsg()) {
                    continue;
                }
                if (relations[i].isAbstractContainer()) {
                    generateCodeForContainerRelationDefinition(relations[i], fieldsBuilder, methodsBuilder);
                } else {
                    generateCodeForNoneContainerRelation(relations[i], fieldsBuilder, methodsBuilder);                
                }
                if (relations[i].implementsContainerRelation()) {
                    IProductCmptTypeRelation containerRel = relations[i].findContainerRelation();
                    List implementationRelations = (List)containerRelations.get(containerRel);
                    if (implementationRelations==null) {
                        implementationRelations = new ArrayList();
                        containerRelations.put(containerRel, implementationRelations);
                    }
                    implementationRelations.add(relations[i]);
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building relation "
                        + relations[i].getName() + " of "
                        + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
        generateCodeForContainerRelationImplementation(getProductCmptType(), containerRelations, fieldsBuilder, methodsBuilder);
    }
    
    /**
     * Generates code for container relation implementation for all container relations defined
     * in the indicated type and it's supertypes.
     */
    private void generateCodeForContainerRelationImplementation(
            IProductCmptType type,
            HashMap containerImplMap, 
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IProductCmptTypeRelation[] relations = type.getRelations();
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].isAbstractContainer()) {
                try {
                    List implRelations = (List)containerImplMap.get(relations[i]);
                    if (implRelations!=null) {
                        generateCodeForContainerRelationImplementation(relations[i], implRelations, fieldsBuilder, methodsBuilder);
                    }
                } catch (Exception e) {
                    addToBuildStatus(new IpsStatus("Error building container relation implementation. "
                        + "ContainerRelation: " + relations[i]
                        + "Implementing Type: " + getProductCmptType()));
                }
            }
        }
        IProductCmptType supertype = type.findSupertype();
        if (supertype!=null) {
            generateCodeForContainerRelationImplementation(supertype, containerImplMap, fieldsBuilder, methodsBuilder);
        }
    }
    
    /**
     * Generates the code for a none-container relation definition. The method is called for every 
     * valid none-container relation defined in the product component type we currently build sourcecode for.
     * 
     * @param relation the relation source code should be generated for
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder the code fragment builder to build the method section.
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() method of the super
     *             class.
     * @see JavaSourceFileBuilder#addToBuildStatus(CoreException)
     * @see JavaSourceFileBuilder#addToBuildStatus(IStatus)
     */
    protected abstract void generateCodeForNoneContainerRelation(IProductCmptTypeRelation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;


    /**
     * Generates the code for a container relation definition. The method is called for every 
     * valid container relation defined in the product component type we currently build sourcecode for.
     * 
     * @param containerRelation the container relation source code should be generated for.
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param fieldsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForContainerRelationDefinition(
            IProductCmptTypeRelation containerRelation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates code for a container relation implementation. 
     * The method is called for every valid container relation in the product 
     * component type we currently build sourcecode for and for each valid container relation
     * in one of it's supertypes.
     * 
     * @param containerRelation the container relation source code should be generated for.
     * @param implementationRelations the relation implementing the container relation.
     * @param fieldsBuilder the code fragment builder to build the memeber variabales section.
     * @param methodsBuilder the code fragment builder to build the method section.
     */
    protected abstract void generateCodeForContainerRelationImplementation(
            IProductCmptTypeRelation containerRelation,
            List implementationRelations, 
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
}
