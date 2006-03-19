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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet stringsSet) {
        super(builderSet, kindId, stringsSet);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        AttributeType type = attribute.getAttributeType();
        if (type == AttributeType.CHANGEABLE) {
            generateCodeForChangeableAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.CONSTANT) {
            generateCodeForConstantAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.DERIVED) {
            generateCodeForDerivedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.COMPUTED) {
            generateCodeForComputedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else {
            throw new RuntimeException("Unkown attribute type " + type);
        }
    }
    
    protected abstract void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
     
    protected abstract void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
        
    
    protected abstract void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    protected abstract void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (relation.is1ToMany()) {
            generateCodeFor1ToManyRelation(relation, fieldsBuilder, methodsBuilder);
        } else {
            generateCodeFor1To1Relation(relation, fieldsBuilder, methodsBuilder);
        }
    }

    /**
     * Generates the code for a 1-to-many relation. The method is called for every 
     * valid relation defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1ToManyRelation(
            IRelation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Generates the code for a 1-to-many relation. The method is called for every 
     * valid relation defined in the policy component type we currently build sourcecode for.
     */
    protected abstract void generateCodeFor1To1Relation(
            IRelation relation,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;
    
    /**
     * Methods to create a new child object should be generated if the relation
     * is a composite and the target is not abstract. If the target is configurable
     * by product a second method with the product component type as argument should
     * also be generated.
     */
    protected void generateNewChildMethodsIfApplicable(
            IRelation relation,
            IPolicyCmptType target, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (!relation.getRelationType().isComposition()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(relation, target, false, methodsBuilder);
        if (target.isConfigurableByProductCmptType()) {
            generateMethodNewChild(relation, target, true, methodsBuilder);
        }
    }
    
    /**
     * Generates a method to create a new child object for a parent obejct and attach
     * it to the parent.
     * 
     * @param relation The parent to child relation
     * @param target   The child type.
     * @param inclProductCmptArg <code>true</code> if the product component type should be included as arg.
     * @param builder  The builder sourcecode can be appended to.
     * 
     * @throws CoreException
     */
    public abstract void generateMethodNewChild(
            IRelation relation, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException;
    
    
}
