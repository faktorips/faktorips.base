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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
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
        EnumDatatype datatype = classNode.getDatatype().getEnumDatatype();
        EnumValueNode valueNode = classNode.getSuccessor();

        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.getImportDeclaration().add(datatype.getJavaClassName());
        // if (classDatatype instanceof EnumTypeDatatypeAdapter) {
        // getE
        // }
        // else {
        // StandardBuilderSet builderSet = getBuilderSet();
        // IpsProject ipsProject = (IpsProject)builderSet.getIpsProject();
        // DatatypeHelper helper = ipsProject.getDatatypeHelper(datatype);

        JavaCodeFragment codeFragment2 = new JavaCodeFragment(valueNode.getEnumValueName());
        codeFragment.append(codeFragment2);

        // codeFragment.append(helper.newInstance(valueNode.getEnumValueName()));
        // }

        return new CompilationResultImpl(codeFragment, datatype);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getErrorCompilationResult(InvalidIdentifierNode invalidIdentifierNode) {
        return new CompilationResultImpl(invalidIdentifierNode.getMessage());
    }
}
