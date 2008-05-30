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

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectChangedEvent;

public class ModelObjectChangedEvent implements IModelObjectChangedEvent {

    private IModelObject modelObject;
    private Type type;
    private String propertyName;
    private IModelObject childCmpt = null;

    public ModelObjectChangedEvent(IModelObject cmpt, Type type, String propName) {
        if (cmpt==null) {
            throw new NullPointerException();
        }
        this.modelObject = cmpt;
        this.type = type;
        this.propertyName = propName;
    }

    public ModelObjectChangedEvent(IModelObject cmpt, Type type, String propName, IModelObject childCmpt) {
        if (cmpt==null) {
            throw new NullPointerException();
        }
        this.modelObject = cmpt;
        this.type = type;
        this.propertyName = propName;
        this.childCmpt = childCmpt;
    }

    /**
     * {@inheritDoc}
     */
    public IModelObject getChangedObject() {
        return modelObject;
    }

    /**
     * {@inheritDoc}
     */
    public Type getType() {
        return type;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean propertyChanged() {
        return type==Type.MUTABLE_PROPERTY_CHANGED || type==Type.DERIVED_PROPERTY_CHANGED;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * {@inheritDoc}
     */
    public IModelObject getChildObject() {
        return childCmpt;
    }
}
