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

package org.faktorips.devtools.formulalibrary;

import java.util.Locale;

import org.faktorips.devtools.core.AbstractProjectRelatedFunctionResolverFactory;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

/**
 * The {@link FormulaLibraryResolverFactory} create a new {@link FormulaLibraryResolver}. The class
 * is defined as {@link ExtensionPoints}.
 * 
 * @author frank
 */
public class FormulaLibraryResolverFactory extends AbstractProjectRelatedFunctionResolverFactory {

    @Override
    public FunctionResolver newFunctionResolver(IIpsProject ipsProject, Locale locale) {
        return new FormulaLibraryResolver(ipsProject);
    }
}
