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

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * This interface describes that a source depends on a target. The dependency type descibes
 * the kind of the dependency. Dependency instances are created by the dependsOn() methods of
 * {@link IIpsObject}s to indicate the dependency to other IpsObjects. The DependencyGraph which is
 * used by the IpsBuilder to determine the dependent IpsObjects during an incremental build cycle
 * utilizes the dependsOn() method to determine its state. It is up to the IpsBuilder how to
 * interpret this type.
 * <p>
 * Known implementations of this interface are {@link IpsObjectDependency} and
 * {@link DatatypeDependency}.
 * 
 * @author Peter Erzberger
 */
public interface IDependency {
    
    /**
     * The source which depends on the target. Sources are discribed by their qualified name types
     * since a source must always be an IpsObject.
     */
    public QualifiedNameType getSource();

    /**
     * The target from which the source depends on.
     */
    public Object getTarget();
    
    /**
     * Returns the type of this dependency.
     */
    public DependencyType getType();

}
