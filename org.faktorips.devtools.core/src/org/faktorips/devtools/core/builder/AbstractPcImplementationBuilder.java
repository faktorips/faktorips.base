package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
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
		// TODO Auto-generated constructor stub
	}

	protected abstract void generateGetterBodyForNonDerivedAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder);
	

	abstract protected void generateGetterBodyForDerivedAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder);


	protected void generateCodeForRelation(IRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
	
		IPolicyCmptType targetType = relation.findTarget();
		if (targetType == null) {
			// !TODO richtige Exception werfen
			throw new CoreException(new IpsStatus("FEEEEEEHHHLER")); //$NON-NLS-1$
		}
	
		if (relation.getMaxCardinality() == IRelation.CARDINALITY_ONE) {
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
	abstract protected void generateRelationGetToOneBody(IPolicyCmptType targetType, IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;	
	abstract protected void generateRelationGetToManyBody(IPolicyCmptType targetType, IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationSetBody(IPolicyCmptType targetType, IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationAddBody(IPolicyCmptType targetType, IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
	abstract protected void generateRelationRemoveBody(IPolicyCmptType targetType, IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException ;
		
	

	protected void generateCodeForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
		generateCodeForMemberVariable(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
		generateGetterForAttribute(attribute, datatypeHelper, methodsBuilder);
		if (attribute.isChangeable()) {
			generateSetterForAttribute(attribute, datatypeHelper,
					methodsBuilder);
		}
	}
	
	
	abstract protected void generateCodeForMemberVariable(IAttribute attribute,
			DatatypeHelper datatypeHelper,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder);
	
	abstract protected void generateGetterForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract protected void generateSetterForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

}
