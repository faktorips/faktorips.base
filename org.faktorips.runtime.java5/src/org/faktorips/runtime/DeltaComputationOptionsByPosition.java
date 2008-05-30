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

package org.faktorips.runtime;

/**
 * Delta computation options that create child deltas per position and don't ignore any property.
 * 
 * @see IDeltaComputationOptions#createChildDeltasByPosition(String)
 * 
 * @author Jan Ortmann
 */
public class DeltaComputationOptionsByPosition implements IDeltaComputationOptions {

    public DeltaComputationOptionsByPosition() {
    }

    /**
     * {@inheritDoc}
     */
    public ComputationMethod getMethod(String association) {
        return ComputationMethod.BY_POSITION;
    }

    /**
     * Returns <code>true</code>.
     */
    public boolean isSame(IModelObject object1, IModelObject object2) {
        return object1==object2;
    }

    /**
     * Returns <code>false</code>.
     */
    public boolean ignore(Class<?> clazz, String property) {
        return false;
    }



}
