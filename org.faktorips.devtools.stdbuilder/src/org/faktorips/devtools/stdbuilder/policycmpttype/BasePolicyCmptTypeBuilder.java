/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    /**
     * Configuration property that is supposed to be used to read a configuration value from
     * the IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener";
    
    private boolean changeListenerSupportActive;
    
    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, 
                                     String kindId, 
                                     LocalizedStringsSet stringsSet, 
                                     boolean isChangeListenerSupportActive) {
        super(builderSet, kindId, stringsSet);
        this.changeListenerSupportActive = isChangeListenerSupportActive;
    }

    public boolean isChangeListenerSupportActive() {
        return changeListenerSupportActive;
    }
    
    /**
     * This validation is necessary because otherwise a java class file is created with a wrong java class name
     * this causes jmerge to throw an exception
     */
    protected boolean hasValidProductCmptTypeName() throws CoreException{
        IProductCmptType type = getProductCmptType();
        MessageList msgList = type.validate();
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
        AttributeType type = attribute.getAttributeType();
        if (type == AttributeType.CHANGEABLE) {
            if (attribute.getOverwrites()) {
                return;
            }
            generateCodeForChangeableAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.CONSTANT) {
            generateCodeForConstantAttribute(attribute, datatypeHelper, constantBuilder, memberVarsBuilder,
                    methodsBuilder);
        } else if (type == AttributeType.DERIVED_ON_THE_FLY) {
            generateCodeForDerivedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
            if (attribute.getOverwrites()) {
                return;
            }
            generateCodeForComputedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else {
            throw new RuntimeException("Unkown attribute type " + type);
        }
    }

    protected abstract void generateCodeForConstantAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForChangeableAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForDerivedAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForComputedAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelation(IPolicyCmptTypeAssociation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        generateCodeForRelationInCommon(relation, fieldsBuilder, methodsBuilder);
        if (relation.is1ToMany()) {
            generateCodeFor1ToManyRelation(relation, fieldsBuilder, methodsBuilder);
        } else {
            generateCodeFor1To1Relation(relation, fieldsBuilder, methodsBuilder);
        }
       
    }

    /**
     * Generations the code for a relation unspecific to the cardinality of the relation. 
     * The method is called for every valid relation defined in the policy component type
     * we currently build sourcecode for.
     * 
     * @param relation
     * @param fieldsBuilder
     * @param methodsBuilder
     * @throws Exception
     */
    protected abstract void generateCodeForRelationInCommon(IPolicyCmptTypeAssociation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many relation. The method is called for every valid relation
     * defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1ToManyRelation(IPolicyCmptTypeAssociation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many relation. The method is called for every valid relation
     * defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1To1Relation(IPolicyCmptTypeAssociation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Methods to create a new child object should be generated if the relation is a composite and
     * the target is not abstract. If the target is configurable by product a second method with the
     * product component type as argument should also be generated.
     */
    protected void generateNewChildMethodsIfApplicable(IPolicyCmptTypeAssociation relation,
            IPolicyCmptType target,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (!relation.getAssociationType().isCompositionMasterToDetail()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(relation, target, false, methodsBuilder);
        if (target.isConfigurableByProductCmptType() && target.findProductCmptType(getIpsProject())!=null) {
            generateMethodNewChild(relation, target, true, methodsBuilder);
        }
    }

    /**
     * Generates a method to create a new child object for a parent obejct and attach it to the
     * parent.
     * 
     * @param relation The parent to child relation
     * @param target The child type.
     * @param inclProductCmptArg <code>true</code> if the product component type should be
     *            included as arg.
     * @param builder The builder sourcecode can be appended to.
     * 
     * @throws CoreException
     */
    public abstract void generateMethodNewChild(IPolicyCmptTypeAssociation relation,
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException;

    boolean isFirstDependantTypeInHierarchy(IPolicyCmptType type) throws CoreException {
        if (!type.isDependantType()) {
            return false;
        }
        IPolicyCmptType supertype = type.findSupertype();
        if (supertype==null) {
            return true;
        }
        return !supertype.isDependantType();
    }
}
