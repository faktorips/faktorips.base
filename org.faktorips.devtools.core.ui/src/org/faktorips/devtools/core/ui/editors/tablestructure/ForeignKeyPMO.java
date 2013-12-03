/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
    private IIndex[] EMPTY_ARRAY = new IIndex[0];

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
