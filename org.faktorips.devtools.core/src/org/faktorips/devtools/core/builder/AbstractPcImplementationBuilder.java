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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractPcImplementationBuilder extends
		AbstractPcTypeBuilder {

	protected static final String[] ANNOTATION_GENERATED = new String[] { "generated" }; //$NON-NLS-1$
	protected static final String[] ANNOTATION_MODIFIABLE = new String[] { "modifiable" }; //$NON-NLS-1$
	protected AbstractPcInterfaceBuilder policyInterfaceBuilder;

	public AbstractPcImplementationBuilder(
			IIpsArtefactBuilderSet builderSet, String kindId,
			LocalizedStringsSet stringsSet,
			AbstractPcInterfaceBuilder policyInterfaceBuilder) {
		super(builderSet, kindId, stringsSet);
		this.policyInterfaceBuilder = policyInterfaceBuilder;
	}

	protected abstract void generateGetterBodyForNonDerivedAttribute(IPolicyCmptTypeAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder);
	

	abstract protected void generateGetterBodyForDerivedAttribute(IPolicyCmptTypeAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder);


	protected void generateCodeForRelation(IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
	
		IPolicyCmptType targetType = relation.findTarget();
		if (targetType == null) {
			throw new CoreException(new IpsStatus("Target not found: " + relation.getTarget())); //$NON-NLS-1$
		}
	
		if (relation.getMaxCardinality() == IPolicyCmptTypeAssociation.CARDINALITY_ONE) {
			/* get */
	
			policyInterfaceBuilder.generateCodeForRelationGetOneSignature(relation,memberVarsBuilder,methodsBuilder);
			generateRelationGetToOneBody(targetType, relation, methodsBuilder);
			/* set */
			policyInterfaceBuilder.generateCodeForRelationSetSignature(relation,memberVarsBuilder,methodsBuilder);
			generateRelationSetBody(targetType,relation, methodsBuilder);
	
		} else {
			/* get */
			policyInterfaceBuilder.generateCodeForRelationGetManySignature(relation,memberVarsBuilder,methodsBuilder);
			generateRelationGetToManyBody(targetType, relation, methodsBuilder);
			/* add */
			policyInterfaceBuilder.generateCodeForRelationAddSignature(relation,memberVarsBuilder,methodsBuilder);
			generateRelationAddBody(targetType, relation, methodsBuilder);
			/* remove */
			policyInterfaceBuilder.generateCodeForRelationRemoveSignature(relation,memberVarsBuilder,methodsBuilder);
			generateRelationRemoveBody(targetType, relation, methodsBuilder);
	
		}
	}	
	abstract protected void generateRelationGetToOneBody(IPolicyCmptType targetType, IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;	
	abstract protected void generateRelationGetToManyBody(IPolicyCmptType targetType, IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationSetBody(IPolicyCmptType targetType, IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationAddBody(IPolicyCmptType targetType, IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationRemoveBody(IPolicyCmptType targetType, IPolicyCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
		
	

	protected void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute, DatatypeHelper datatypeHelper, 
            JavaCodeFragmentBuilder constantBuilder, JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
		generateCodeForMemberVariable(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
		generateGetterForAttribute(attribute, datatypeHelper, methodsBuilder);
		if (attribute.isChangeable()) {
			generateSetterForAttribute(attribute, datatypeHelper,
					methodsBuilder);
		}
	}
	
	
	abstract protected void generateCodeForMemberVariable(IPolicyCmptTypeAttribute attribute,
			DatatypeHelper datatypeHelper,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	
	abstract protected void generateGetterForAttribute(IPolicyCmptTypeAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract protected void generateSetterForAttribute(IPolicyCmptTypeAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

}
