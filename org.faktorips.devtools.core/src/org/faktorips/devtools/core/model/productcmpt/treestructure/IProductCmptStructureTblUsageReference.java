/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
