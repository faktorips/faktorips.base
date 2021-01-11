/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.fl;

import org.faktorips.devtools.core.internal.model.productcmpt.TableAccessFunctionFlFunctionAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;

/**
 * Implementation of FlFunction for a table structure
 * 
 * @author dicker
 */
public class TableStructureReferenceFunctionFlFunctionAdapter extends TableAccessFunctionFlFunctionAdapter {

    private final String name;

    public TableStructureReferenceFunctionFlFunctionAdapter(String tableContentsQName, ITableAccessFunction fct,
            String referencedName, IIpsProject ipsProject) {
        super(tableContentsQName, fct, referencedName, ipsProject);
        name = getReferencedName() + "." + getTableAccessFunction().getAccessedColumnName(); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return name;
    }

}
