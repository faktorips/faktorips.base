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

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IDependantObject;

/**
 * Internal interface that defines methods that must be implemented for dependant objects.
 * 
 * @author Jan Ortmann
 */
public interface DependantObject extends IDependantObject {

    /**
     * Sets the new parent this part belongs to.
     */
    public void setParentModelObjectInternal(AbstractModelObject newParent);

}
