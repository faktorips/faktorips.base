/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    public TableStructureReferenceFunctionFlFunctionAdapter(String tableContentsQName, ITableAccessFunction fct,
            String referencedName, IIpsProject ipsProject) {
        super(tableContentsQName, fct, referencedName, ipsProject);
    }

    @Override
    public String getName() {
        return (getReferencedName()) + "." + getTableAccessFunction().getAccessedColumn(); //$NON-NLS-1$
    }

}
