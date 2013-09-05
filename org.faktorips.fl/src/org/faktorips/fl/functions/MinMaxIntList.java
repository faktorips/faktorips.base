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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilationResult;

public class MinMaxIntList extends AbstractMinMaxList {

    public MinMaxIntList(String name, String description, boolean isMax) {
        super(name, description, isMax);
    }

    @Override
    protected JavaCodeFragment createCodeFragment(AbstractCompilationResult<JavaCodeFragment> listArgument) {
        JavaCodeFragment codeFragment = super.createCodeFragment(listArgument);
        codeFragment.append(".intValue()");
        return codeFragment;
    }

    @Override
    protected String getDatatypeClassName() {
        return Datatype.INTEGER.getJavaClassName();
    }

}
