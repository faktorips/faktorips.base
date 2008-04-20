/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genProdAttributes = new ArrayList();
    private List genAssociations = new ArrayList();

    private boolean generateChangeListenerSupport;

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet,
            boolean generateChangeListenerSupport) throws CoreException {
        super(builderSet, kindId, stringsSet);
        this.generateChangeListenerSupport = generateChangeListenerSupport;
    }

    public abstract PolicyCmptInterfaceBuilder getInterfaceBuilder();

    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        initPartGenerators();
    }

    private void initPartGenerators() throws CoreException {
        genAttributes.clear();
        genProdAttributes.clear();
        genAssociations.clear();
        generatorsByPart.clear();

        createGeneratorsForProdAttributes();
        createGeneratorsForAssociations();
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

    protected GenProdAttribute getGenerator(IProductCmptTypeAttribute a) {
        return (GenProdAttribute)generatorsByPart.get(a);
    }

    protected abstract GenProdAttribute createGenerator(IProductCmptTypeAttribute a,
            LocalizedStringsSet localizedStringsSet) throws CoreException;

    private void createGeneratorsForAssociations() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAssociation.class);
        IPolicyCmptType type = getPcType();
        IPolicyCmptTypeAssociation[] ass = type.getPolicyCmptTypeAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (ass[i].isValid()) {
                GenAssociation generator = createGenerator(ass[i], stringsSet);
                if (generator != null) {
                    genAssociations.add(generator);
                    generatorsByPart.put(ass[i], generator);
                }
            }
        }
    }

    protected abstract GenAssociation createGenerator(IPolicyCmptTypeAssociation association,
            LocalizedStringsSet attrStringsSet) throws CoreException;

    protected DefaultJavaGeneratorForIpsPart getGenerator(IIpsObjectPartContainer part) {
        return (DefaultJavaGeneratorForIpsPart)generatorsByPart.get(part);
    }

    protected GenAssociation getGenerator(IPolicyCmptTypeAssociation a) {
        return (GenAssociation)generatorsByPart.get(a);
    }

    public boolean isGenerateChangeListenerSupport() {
        return generateChangeListenerSupport;
    }

    /**
     * This validation is necessary because otherwise a java class file is created with a wrong java
     * class name this causes jmerge to throw an exception
     */
    protected boolean hasValidProductCmptTypeName() throws CoreException {
        IProductCmptType type = getProductCmptType();
        MessageList msgList = type.validate(getIpsProject());
        return !msgList.getMessagesFor(type, IProductCmptType.PROPERTY_NAME).containsErrorMsg();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (attribute.isProductRelevant() && getProductCmptType() == null) {
            return;
        }
        GenAttribute generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        GenMethod generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    protected void generateCodeForValidationRule(IValidationRule validationRule) throws CoreException {
        GenValidationRule generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(validationRule);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        generateCodeForAssociationInCommon(association, fieldsBuilder, methodsBuilder);
        if (association.is1ToMany()) {
            generateCodeFor1ToManyAssociation(association, fieldsBuilder, methodsBuilder);
        } else {
            generateCodeFor1To1Association(association, fieldsBuilder, methodsBuilder);
        }
    }

    /**
     * Generations the code for a association unspecific to the cardinality of the association. The
     * method is called for every valid association defined in the policy component type we
     * currently build sourcecode for.
     * 
     * @param association
     * @param fieldsBuilder
     * @param methodsBuilder
     * @throws Exception
     */
    protected abstract void generateCodeForAssociationInCommon(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many association. The method is called for every valid
     * association defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1ToManyAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many association. The method is called for every valid
     * association defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1To1Association(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

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
}
