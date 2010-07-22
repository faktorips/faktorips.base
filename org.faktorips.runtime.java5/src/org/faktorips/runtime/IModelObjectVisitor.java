/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

/**
 * Visitor for hierarchical model object structures.
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectVisitor {

    /**
     * Visits the given model object.
     * 
     * @return <code>true</code> if the visitor should continue visiting the object's children,
     *         otherwise <code>false</code>.
     */
    public boolean visit(IModelObject modelObject);

}
