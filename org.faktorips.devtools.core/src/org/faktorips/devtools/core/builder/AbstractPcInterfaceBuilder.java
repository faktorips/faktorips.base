package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractPcInterfaceBuilder extends AbstractPcTypeBuilder {

	public AbstractPcInterfaceBuilder(IJavaPackageStructure packageStructure, String kindId, LocalizedStringsSet stringsSet) {
		super(packageStructure, kindId, stringsSet);
	}
	
	abstract public void generateCodeForRelationGetOneSignature(IRelation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract public void generateCodeForRelationGetManySignature(IRelation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract public void generateCodeForRelationSetSignature(IRelation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract public void generateCodeForRelationAddSignature(IRelation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	abstract public void generateCodeForRelationRemoveSignature(IRelation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

}
