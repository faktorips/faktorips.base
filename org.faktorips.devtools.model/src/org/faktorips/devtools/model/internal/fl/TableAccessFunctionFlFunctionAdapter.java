/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

/**
 * An adapter that adapts a table access function to the FlFunction interfaces.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public class TableAccessFunctionFlFunctionAdapter extends AbstractFlFunctionAdapter<JavaCodeFragment> {

    private final ITableAccessFunction fct;

    private final String tableContentsQualifiedName;

    private final String referencedName;

    private final String name;

    private List<Datatype> cachedArgTypes;

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
        this.name = StringUtils.capitalize(referencedName) + "." + fct.getAccessedColumnName(); //$NON-NLS-1$
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
        } catch (IpsException e) {
            IpsLog.log(e);
            return new CompilationResultImpl(Message.newError(
                    "", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    @Override
    public String getDescription() {
        return fct.getDescription();
    }

    @Override
    public Datatype getType() {
        return getIpsProject().findValueDatatype(fct.getType());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Datatype[] getArgTypes() {
        List<Datatype> argTypes = findArgTypes();
        return argTypes.toArray(new Datatype[argTypes.size()]);
    }

    private List<Datatype> findArgTypes() {
        if (cachedArgTypes == null) {
            ArrayList<Datatype> newCachedArgTypes = new ArrayList<>();
            IIpsProject project = getIpsProject();
            for (String argType : fct.getArgTypes()) {
                newCachedArgTypes.add(project.findValueDatatype(argType));
            }
            cachedArgTypes = Collections.unmodifiableList(newCachedArgTypes);
        }
        return cachedArgTypes;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fct == null) ? 0 : fct.hashCode());
        result = prime * result + ((referencedName == null) ? 0 : referencedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TableAccessFunctionFlFunctionAdapter other = (TableAccessFunctionFlFunctionAdapter)obj;
        if (fct == null) {
            if (other.fct != null) {
                return false;
            }
        } else if (!fct.equals(other.fct)) {
            return false;
        }
        if (referencedName == null) {
            if (other.referencedName != null) {
                return false;
            }
        } else if (!referencedName.equals(other.referencedName)) {
            return false;
        }
        return true;
    }
}
