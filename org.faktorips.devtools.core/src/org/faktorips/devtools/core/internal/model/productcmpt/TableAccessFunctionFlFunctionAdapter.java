/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An adapter that adapts a table access function to the FlFunction interfaces.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public class TableAccessFunctionFlFunctionAdapter extends AbstractFlFunctionAdapter<JavaCodeFragment> {

    private ITableAccessFunction fct;
    private String tableContentsQualifiedName;
    private String referencedName;

    /**
     * @param tableContentsQName cannot be null
     * @param fct the table access function
     */
    public TableAccessFunctionFlFunctionAdapter(String tableContentsQName, ITableAccessFunction fct,
            String referencedName, IIpsProject ipsProject) {

        super(ipsProject);
        ArgumentCheck.notNull(fct);
        ArgumentCheck.notNull(tableContentsQName);
        ArgumentCheck.notNull(referencedName);
        this.fct = fct;
        this.tableContentsQualifiedName = tableContentsQName;
        this.referencedName = referencedName;
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        try {
            IIpsArtefactBuilderSet builderSet = getIpsProject().getIpsArtefactBuilderSet();
            if (!builderSet.isSupportTableAccess()) {
                CompilationResultImpl result = new CompilationResultImpl(Message.newError(
                        "", Messages.TableAccessFunctionFlFunctionAdapter_msgNoTableAccess)); //$NON-NLS-1$
                return result;
            }
            return builderSet.getTableAccessCode(tableContentsQualifiedName, fct, argResults);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new CompilationResultImpl(Message.newError(
                    "", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    @Override
    public String getDescription() {
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(fct);
        return localizedDescription;
    }

    @Override
    public Datatype getType() {
        try {
            return getIpsProject().findValueDatatype(fct.getType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return StringUtils.capitalize(referencedName) + "." + fct.getAccessedColumn(); //$NON-NLS-1$
    }

    @Override
    public Datatype[] getArgTypes() {
        IIpsProject project = getIpsProject();
        String[] argTypes = fct.getArgTypes();
        Datatype[] types = new Datatype[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            try {
                types[i] = project.findValueDatatype(argTypes[i]);
            } catch (CoreException e) {
                throw new RuntimeException("Error searching for datatype " + argTypes[i], e); //$NON-NLS-1$
            }
        }
        return types;
    }

    protected ITableAccessFunction getTableAccessFunction() {
        return fct;
    }

    protected String getTableContentsQualifiedName() {
        return tableContentsQualifiedName;
    }

    protected String getReferencedName() {
        return referencedName;
    }
}
