package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

public class TableFunctionsResolver implements FunctionResolver {

    private IIpsProject project;
    
    
    public TableFunctionsResolver(IIpsProject project) {
        ArgumentCheck.notNull(project);
        this.project = project;
    }

    public FlFunction[] getFunctions() {
        List functions = new ArrayList();
        try {
            IIpsObject[] tables = project.findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
            for (int i = 0; i < tables.length; i++) {
                ITableStructure table = (ITableStructure)tables[i];
                ITableAccessFunction[] fcts = table.getAccessFunctions();
                for (int j = 0; j < fcts.length; j++) {
                    if (!fcts[j].validate().containsErrorMsg()) {
                        functions.add(new TableAccessFunctionFlFunctionAdapter(fcts[j]));
                    }
                }
            }
        } catch (CoreException e) {
            // if an error occurs while search for the function, the functions are not
            // provided and an error is logged.
            IpsPlugin.log(e);
        }
        return (FlFunction[])functions.toArray(new FlFunction[functions.size()]);
    }

}
