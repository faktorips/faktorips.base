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
import java.util.Iterator;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();

    private boolean generateChangeListenerSupport;
    
    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, 
                                     String kindId, 
                                     LocalizedStringsSet stringsSet, 
                                     boolean generateChangeListenerSupport) throws CoreException {
        super(builderSet, kindId, stringsSet);
        this.generateChangeListenerSupport = generateChangeListenerSupport;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        initPartGenerators();
    }

    private void initPartGenerators() throws CoreException {
        genAttributes.clear();
        generatorsByPart.clear();
        LocalizedStringsSet attrStringsSet = new LocalizedStringsSet(GenAttribute.class);
        IPolicyCmptType type = getPcType();
        IPolicyCmptTypeAttribute[] attrs = type.getPolicyCmptTypeAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isValid()) {
                GenAttribute generator = createGenerator(attrs[i], attrStringsSet);
                if (generator!=null) {
                    genAttributes.add(generator);
                    generatorsByPart.put(attrs[i], generator);
                }
            }
        }
    }
    
    protected abstract GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet) throws CoreException;
    
    protected Iterator getGenAttributes() {
        return genAttributes.iterator();
    }
    
    protected DefaultJavaGeneratorForIpsPart getGenerator(IIpsObjectPartContainer part) {
        return (DefaultJavaGeneratorForIpsPart)generatorsByPart.get(part);
    }
    
    protected GenAttribute getGenerator(IPolicyCmptTypeAttribute a) {
        return (GenAttribute)generatorsByPart.get(a);
    }
    

    public boolean isGenerateChangeListenerSupport() {
        return generateChangeListenerSupport;
    }
    
    /**
     * This validation is necessary because otherwise a java class file is created with a wrong java class name
     * this causes jmerge to throw an exception
     */
    protected boolean hasValidProductCmptTypeName() throws CoreException{
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
        GenAttribute generator = (GenAttribute)getGenerator(attribute);
        if (generator!=null) {
            generator.generate();
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
     * Generations the code for a association unspecific to the cardinality of the association. 
     * The method is called for every valid association defined in the policy component type
     * we currently build sourcecode for.
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
     * Generates the code for a 1-to-many association. The method is called for every valid association
     * defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1ToManyAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many association. The method is called for every valid association
     * defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1To1Association(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Methods to create a new child object should be generated if the association is a composite and
     * the target is not abstract. If the target is configurable by product a second method with the
     * product component type as argument should also be generated.
     */
    protected void generateNewChildMethodsIfApplicable(IPolicyCmptTypeAssociation association,
            IPolicyCmptType target,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (!association.getAssociationType().isCompositionMasterToDetail()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(association, target, false, methodsBuilder);
        if (target.isConfigurableByProductCmptType() && target.findProductCmptType(getIpsProject())!=null) {
            generateMethodNewChild(association, target, true, methodsBuilder);
        }
    }

    /**
     * Generates a method to create a new child object for a parent obejct and attach it to the
     * parent.
     * 
     * @param association The parent to child association
     * @param target The child type.
     * @param inclProductCmptArg <code>true</code> if the product component type should be
     *            included as arg.
     * @param builder The builder sourcecode can be appended to.
     * 
     * @throws CoreException
     */
    public abstract void generateMethodNewChild(IPolicyCmptTypeAssociation association,
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException;

    boolean isFirstDependantTypeInHierarchy(IPolicyCmptType type) throws CoreException {
        if (!type.isDependantType()) {
            return false;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype==null) {
            return true;
        }
        return !supertype.isDependantType();
    }
}
