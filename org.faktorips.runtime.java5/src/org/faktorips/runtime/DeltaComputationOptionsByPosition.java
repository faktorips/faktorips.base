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
 * Delta computation options that create child deltas per position and don't ignore any property.
 * 
 * TODO the following reference does not exist
 * 
 * @see IDeltaComputationOptions#createChildDeltasByPosition(String)
 * 
 * @author Jan Ortmann
 */
public class DeltaComputationOptionsByPosition implements IDeltaComputationOptions {

    public ComputationMethod getMethod(String association) {
        return ComputationMethod.BY_POSITION;
    }

    /**
     * Returns <code>true</code> if the specified object references are identical.
     */
    public boolean isSame(IModelObject object1, IModelObject object2) {
        return object1 == object2;
    }

    /**
     * Returns <code>false</code>.
     */
    public boolean ignore(Class<?> clazz, String property) {
        return false;
    }

}
