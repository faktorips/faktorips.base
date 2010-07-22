/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

/**
 * Marks an model object as accepting visitors.
 * 
 * @author Jan Ortmann
 */
public interface IVisitorSupport {

    /**
     * Accepts the given visitor. This results in a call of the visitor's visit method for this
     * object and all its children.
     * 
     * @param visitor The visitor to accept.
     * 
     * @return The result of the visitor's visit method.
     * 
     * @see IModelObjectVisitor#visit(IModelObject)
     */
    public boolean accept(IModelObjectVisitor visitor);

}
