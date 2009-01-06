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
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractPcInterfaceBuilder extends AbstractPcTypeBuilder {

	public AbstractPcInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet) {
		super(builderSet, kindId, stringsSet);
	}
	
	abstract public void generateCodeForRelationGetOneSignature(IPolicyCmptTypeAssociation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
	abstract public void generateCodeForRelationGetManySignature(IPolicyCmptTypeAssociation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	
    abstract public void generateCodeForRelationSetSignature(IPolicyCmptTypeAssociation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	
    abstract public void generateCodeForRelationAddSignature(IPolicyCmptTypeAssociation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
	
    abstract public void generateCodeForRelationRemoveSignature(IPolicyCmptTypeAssociation relation,
			JavaCodeFragmentBuilder memberVarsBuilder,
			JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

}
