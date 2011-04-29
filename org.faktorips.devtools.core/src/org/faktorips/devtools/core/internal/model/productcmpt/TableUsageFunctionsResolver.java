/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

public class TableUsageFunctionsResolver implements FunctionResolver {

    private IIpsProject ipsProject;
    private ITableContentUsage[] tableContentUsages;

    private FlFunction[] flFunctionsCache;

    public TableUsageFunctionsResolver(IIpsProject ipsProject, ITableContentUsage[] tableContentUsages) {
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(tableContentUsages);
        this.ipsProject = ipsProject;
        this.tableContentUsages = tableContentUsages;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public FlFunction[] getFunctions() {
        List<FlFunction> functions = new ArrayList<FlFunction>();
        try {
            // return the functions of all table structures which are based by the used table
            // contents
            for (ITableContentUsage tableContentUsage : tableContentUsages) {
                ITableContents tableContents = tableContentUsage.findTableContents(ipsProject);
                if (tableContents == null) {
                    // ignore if the table content wasn't found (validation error)
                    continue;
                }
                ITableStructure table = tableContents.findTableStructure(ipsProject);
                if (table != null) {
                    // only add the access-function if the content has a structure...
                    addTableAccessFunction(functions, table, tableContents, tableContentUsage.getStructureUsage());
                }
            }
        } catch (CoreException e) {
            // if an error occurs while search for the function, the functions are not
            // provided and an error is logged.
            IpsPlugin.log(e);
        }
        flFunctionsCache = functions.toArray(new FlFunction[functions.size()]);
        return flFunctionsCache;
    }

    private void addTableAccessFunction(List<FlFunction> functions,
            ITableStructure table,
            ITableContents tableContents,
            String contentUsage) throws CoreException {

        ITableAccessFunction[] fcts = table.getAccessFunctions(IpsPlugin.getMultiLanguageSupport()
                .getLocalizationLocale());
        for (int j = 0; j < fcts.length; j++) {
            if (!fcts[j].validate(table.getIpsProject()).containsErrorMsg()) {
                functions.add(createFlFunctionAdapter(tableContents, fcts[j], contentUsage));
            }
        }
    }

    /**
     * Returns a new table function adapter.
     */
    protected FlFunction createFlFunctionAdapter(ITableContents tableContents,
            ITableAccessFunction function,
            String roleName) {

        return new TableUsageAccessFunctionFlFunctionAdapter(tableContents, function, roleName);
    }

}
