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
    public final static DependencyType INSTANCEOF = new DependencyType("instance of dependency");

    /**
     * 
     */
    public final static DependencyType SUBTYPE = new DependencyType("subtype dependency");

    /**
     * 
     */
    public final static DependencyType REFERENCE_COMPOSITION_MASTER_DETAIL = new DependencyType("master to detail composition dependency");

    /**
     * 
     */
    public final static DependencyType REFERENCE = new DependencyType("reference dependency");

    /**
     * 
     */
    public final static DependencyType USES = new DependencyType("uses dependency");

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
}
