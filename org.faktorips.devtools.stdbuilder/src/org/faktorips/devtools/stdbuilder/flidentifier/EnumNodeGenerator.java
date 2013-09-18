/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

public class EnumNodeGenerator extends AbstractIdentifierGenerator<JavaCodeFragment> {

    public EnumNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory, StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResult(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        EnumClassNode classNode = (EnumClassNode)identifierNode;
        Datatype classDatatype = classNode.getDatatype();
        EnumValueNode valueNode = classNode.getSuccessor();

        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.getImportDeclaration().add(classDatatype.getJavaClassName());
        // if (classDatatype instanceof EnumTypeDatatypeAdapter) {
        //
        // } else {
        DatatypeHelper helper = getBuilderSet().getIpsProject().getDatatypeHelper(classDatatype);
        codeFragment.append(helper.newInstance(valueNode.getEnumValueName()));
        // }

        return new CompilationResultImpl(codeFragment, classDatatype);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getErrorCompilationResult(InvalidIdentifierNode invalidIdentifierNode) {
        return new CompilationResultImpl(invalidIdentifierNode.getMessage());
    }
}
