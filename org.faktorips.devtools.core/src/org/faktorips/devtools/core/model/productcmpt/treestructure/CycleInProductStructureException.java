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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Thrown when a cycle is detected in the product structure and so a structure tree can't be
 * constructed.
 * 
 * @author Thorsten Guenther
 */
public class CycleInProductStructureException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3945323856832361062L;

    private IIpsElement[] cyclePath;

    /**
     * Creates a new exception with the given path.
     */
    public CycleInProductStructureException(IIpsElement[] cyclePath) {
        this.cyclePath = cyclePath;
    }

    /**
     * Returns the path for this cycle. The content of the path depends on the creator of this
     * exception.
     */
    public IIpsElement[] getCyclePath() {
        return cyclePath;
    }

}
