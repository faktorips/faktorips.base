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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibraryIpsObjectType;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * The {@link FormulaLibraryResolver} search for all {@link IIpsSrcFile} in the project with the
 * {@link FormulaLibraryIpsObjectType}.
 * 
 * @author frank
 */
public class FormulaLibraryResolver implements FunctionResolver {

    private IIpsProject ipsProject;

    public FormulaLibraryResolver(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
    }

    @Override
    public FlFunction[] getFunctions() {
        List<FlFunction> functions = new ArrayList<FlFunction>();

        try {
            IIpsSrcFile[] ipsSrcFiles = ipsProject.findIpsSrcFiles(FormulaLibraryIpsObjectType.getInstance());
            for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                IFormulaLibrary formulaLibrary = (IFormulaLibrary)ipsSrcFile.getIpsObject();
                functions.addAll(formulaLibrary.getFlFunctions());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        return functions.toArray(new FlFunction[functions.size()]);
    }
}
