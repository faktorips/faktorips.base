/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;

/**
 * PMO for {@link ForeignKeyEditDialog}
 */
public class ForeignKeyPMO extends IpsObjectPartPmo {

    public static final String UNIQUE_KEY = "uniqueKey"; //$NON-NLS-1$
    public static final String REFERENCE_TABLE = "referenceTable"; //$NON-NLS-1$
    private static final IIndex[] EMPTY_ARRAY = new IIndex[0];

    public ForeignKeyPMO(IForeignKey foreignKey) {
        super(foreignKey);
    }

    @Override
    public IForeignKey getIpsObjectPartContainer() {
        return (IForeignKey)super.getIpsObjectPartContainer();
    }

    public String getReferenceTable() {
        return getIpsObjectPartContainer().getReferencedTableStructure();
    }

    public void setReferenceTable(String referenceTable) {
        getIpsObjectPartContainer().setReferencedTableStructure(referenceTable);
    }

    public String getUniqueKey() {
        return getIpsObjectPartContainer().getReferencedUniqueKey();
    }

    public void setUniqueKey(String uniqueKey) {
        getIpsObjectPartContainer().setReferencedUniqueKey(uniqueKey);
    }

    public IIndex[] getAvailableUniqueKeys() {
        IIpsObject ipsObject;
        try {
            ipsObject = getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, getReferenceTable());
            if (ipsObject != null && ipsObject instanceof TableStructure) {
                TableStructure tableStructure = (TableStructure)ipsObject;
                return tableStructure.getUniqueKeys();
            }
            return EMPTY_ARRAY;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
