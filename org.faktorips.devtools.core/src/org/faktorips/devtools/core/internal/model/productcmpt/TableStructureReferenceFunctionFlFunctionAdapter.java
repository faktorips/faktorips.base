/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;

/**
 * Implementation of FlFunction for a table structure
 * 
 * @author dicker
 */
public class TableStructureReferenceFunctionFlFunctionAdapter extends TableAccessFunctionFlFunctionAdapter {

    public TableStructureReferenceFunctionFlFunctionAdapter(ITableContents tableContents, ITableAccessFunction fct,
            String referencedName, IIpsProject ipsProject) {
        super(tableContents, fct, referencedName, ipsProject);
    }

    @Override
    public String getName() {
        return (getReferencedName()) + "." + getTableAccessFunction().getAccessedColumn(); //$NON-NLS-1$
    }

}
