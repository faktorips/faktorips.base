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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;

/**
 * A reference to a <code>ITableContentUsage</code> which is used in the
 * <code>IProductCmptStructure</code>
 * 
 * @author Joerg Ortmann
 */
public interface IProductCmptStructureTblUsageReference extends IProductCmptStructureReference {

    /**
     * @return The <code>IProductCmptTypeRelation</code> this reference refers to.
     */
    public ITableContentUsage getTableContentUsage();

}
