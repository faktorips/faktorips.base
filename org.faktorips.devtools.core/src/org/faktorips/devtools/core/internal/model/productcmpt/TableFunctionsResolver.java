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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * @deprecated
 * @see org.faktorips.devtools.core.internal.model.productcmpt.TableUsageFunctionsResolver
 */
@Deprecated
public class TableFunctionsResolver implements FunctionResolver {

    private IIpsProject project;

    public TableFunctionsResolver(IIpsProject project) {
        ArgumentCheck.notNull(project);
        this.project = project;
    }

    public FlFunction[] getFunctions() {
        List<FlFunction> functions = new ArrayList<FlFunction>();
        try {
            IIpsSrcFile[] tableContentFiles = project.findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
            for (IIpsSrcFile tableContentFile : tableContentFiles) {

                ITableContents tableContents = (ITableContents)tableContentFile.getIpsObject();
                ITableStructure table = tableContents.findTableStructure(project);
                if (table != null) {
                    // only add the access-function if the content has a structure...
                    addTableAccessFunction(functions, table, tableContents);
                }
            }
        } catch (CoreException e) {
            // if an error occurs while search for the function, the functions are not
            // provided and an error is logged.
            IpsPlugin.log(e);
        }
        return functions.toArray(new FlFunction[functions.size()]);
    }

    private void addTableAccessFunction(List<FlFunction> functions, ITableStructure table, ITableContents tableContents)
            throws CoreException {
        ITableAccessFunction[] fcts = table.getAccessFunctions();
        for (int j = 0; j < fcts.length; j++) {
            if (!fcts[j].validate(table.getIpsProject()).containsErrorMsg()) {
                functions.add(new TableAccessFunctionFlFunctionAdapter(tableContents, fcts[j]));
            }
        }
    }
}
