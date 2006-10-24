/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * Resolver to resolve functions for the table formula test fl function adapter.
 */
public class TableFunctionsFormulaTestResolver implements FunctionResolver {

    private IIpsProject project;
    
    private IFormulaTestCase formulaTestCase;
    
    public TableFunctionsFormulaTestResolver(IIpsProject project) {
        ArgumentCheck.notNull(project);
        this.project = project;
    }

    public TableFunctionsFormulaTestResolver(IIpsProject project, IFormulaTestCase formulaTestCase) {
        ArgumentCheck.notNull(project);
        ArgumentCheck.notNull(formulaTestCase);
        this.project = project;
        this.formulaTestCase = formulaTestCase;
    }

    public FlFunction[] getFunctions() {
        List functions = new ArrayList();
        try {
        	IIpsObject[] tableContentses = project.findIpsObjects(IpsObjectType.TABLE_CONTENTS);
            for(int t = 0; t < tableContentses.length; t++){
            	
            	ITableContents tableContents = (ITableContents)tableContentses[t];
	            ITableStructure table = tableContents.findTableStructure();
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
        return (FlFunction[])functions.toArray(new FlFunction[functions.size()]);
    }

    private void addTableAccessFunction(List functions, ITableStructure table, ITableContents tableContents) throws CoreException{
        ITableAccessFunction[] fcts = table.getAccessFunctions();
        for (int j = 0; j < fcts.length; j++) {
            if (!fcts[j].validate().containsErrorMsg()) {
                functions.add(new TableFunctionFormulaTestFlFunctionAdapter(tableContents, fcts[j], formulaTestCase));
            }
        }
    }
}
