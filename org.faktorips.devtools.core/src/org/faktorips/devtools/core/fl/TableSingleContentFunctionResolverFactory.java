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

package org.faktorips.devtools.core.fl;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.fl.TableSingleContentFunctionsResolver;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

public class TableSingleContentFunctionResolverFactory extends AbstractProjectRelatedFunctionResolverFactory {

    @Override
    public FunctionResolver<JavaCodeFragment> newFunctionResolver(IIpsProject ipsProject, Locale locale) {
        return new TableSingleContentFunctionsResolver(ipsProject);
    }

}
