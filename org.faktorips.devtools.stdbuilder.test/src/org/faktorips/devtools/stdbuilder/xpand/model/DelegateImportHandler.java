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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;

/**
 * Delegates to a {@link JavaCodeFragment} to ensure the same behavior as the standard
 * {@link ImportHandler} via test cases.
 * 
 * @author widmaier
 */
public class DelegateImportHandler extends ImportHandler {

    private JavaCodeFragment fragment;

    public DelegateImportHandler() {
        super("");
        fragment = new JavaCodeFragment();
    }

    @Override
    public ImportStatement add(String importStatement) {
        ImportStatement statement = new ImportStatement(importStatement);
        fragment.appendClassName(importStatement);
        return statement;
    }

    @Override
    public Set<ImportStatement> getImports() {
        Set<ImportStatement> result = new LinkedHashSet<ImportStatement>();
        Set<String> impDecl = fragment.getImportDeclaration().getImports();
        for (String importString : impDecl) {
            result.add(new ImportStatement(importString));
        }
        return result;
    }
}
