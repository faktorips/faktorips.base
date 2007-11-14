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

package org.faktorips.devtools.core.model;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An enumeration type that describes the type of dependency.
 * 
 * @author Peter Erzberger
 */
public class DependencyType implements Serializable{

    private static final long serialVersionUID = 615796376725042939L;

    /**
     *  
     */
    public final static DependencyType INSTANCEOF = new DependencyType("instance of dependency"); //$NON-NLS-1$

    /**
     * 
     */
    public final static DependencyType SUBTYPE = new DependencyType("subtype dependency"); //$NON-NLS-1$

    /**
     * 
     */
    public final static DependencyType REFERENCE_COMPOSITION_MASTER_DETAIL = new DependencyType("master to detail composition dependency"); //$NON-NLS-1$

    /**
     * 
     */
    public final static DependencyType REFERENCE = new DependencyType("reference dependency"); //$NON-NLS-1$

    /**
     * 
     */
    public final static DependencyType DATATYPE = new DependencyType("datatype dependency"); //$NON-NLS-1$

    private String name;
    
    /**
     * @param id
     * @param name
     */
    private DependencyType(String name) {
        super();
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return name;
    }
    
    private DependencyType getDependencyType(String name){
        if(INSTANCEOF.name.equals(name)){
            return INSTANCEOF;
        }
        if(SUBTYPE.name.equals(name)){
            return SUBTYPE;
        }
        if(REFERENCE.name.equals(name)){
            return REFERENCE;
        }
        if(REFERENCE_COMPOSITION_MASTER_DETAIL.name.equals(name)){
            return REFERENCE_COMPOSITION_MASTER_DETAIL;
        }
        if(DATATYPE.name.equals(name)){
            return DATATYPE;
        }
        throw new IllegalArgumentException("No type specified for the provided name.");
    }
    
    /*
     * This is necessary to ensure that when deserialized there exisits only one instance of a dependency type within the vm.  
     */
    private Object readResolve() throws ObjectStreamException {
        return getDependencyType(name);
    }

}
