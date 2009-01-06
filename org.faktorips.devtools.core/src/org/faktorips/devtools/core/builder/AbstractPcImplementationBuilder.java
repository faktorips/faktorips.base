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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
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


	protected void generateCodeForRelation(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
	
		IPolicyCmptType targetType = association.findTargetPolicyCmptType(getIpsProject());
		if (targetType == null) {
			throw new CoreException(new IpsStatus("Target not found: " + association.getTarget())); //$NON-NLS-1$
		}
	
		if (association.getMaxCardinality() == IPolicyCmptTypeAssociation.CARDINALITY_ONE) {
			/* get */
			policyInterfaceBuilder.generateCodeForRelationGetOneSignature(association,memberVarsBuilder,methodsBuilder);
			generateRelationGetToOneBody(targetType, association, methodsBuilder);
			/* set */
			policyInterfaceBuilder.generateCodeForRelationSetSignature(association,memberVarsBuilder,methodsBuilder);
			generateRelationSetBody(targetType,association, methodsBuilder);
	
		} else {
			/* get */
			policyInterfaceBuilder.generateCodeForRelationGetManySignature(association,memberVarsBuilder,methodsBuilder);
			generateRelationGetToManyBody(targetType, association, methodsBuilder);
			/* add */
			policyInterfaceBuilder.generateCodeForRelationAddSignature(association,memberVarsBuilder,methodsBuilder);
			generateRelationAddBody(targetType, association, methodsBuilder);
			/* remove */
			policyInterfaceBuilder.generateCodeForRelationRemoveSignature(association,memberVarsBuilder,methodsBuilder);
			generateRelationRemoveBody(targetType, association, methodsBuilder);
	
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
