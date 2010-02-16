/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * This interface describes that a source depends on a target. The dependency type describes the
 * kind of the dependency. Dependency instances are created by the dependsOn() methods of
 * {@link IIpsObject}s to indicate the dependency to other IpsObjects. The DependencyGraph which is
 * used by the IpsBuilder to determine the dependent IpsObjects during an incremental build cycle
 * utilizes the dependsOn() method to determine its state. It is up to the IpsBuilder how to
 * interpret this type.
 * <p>
 * Known implementations of this interface are {@link IpsObjectDependency} and
 * {@link DatatypeDependency}.
 * 
 * @author Peter Erzberger
 * @author Thorsten Günther
 */
public interface IDependency {

    /**
     * Returns the type of this dependency.
     */
    public DependencyType getType();

    /**
     * The source which depends on the target. Sources are described by their qualified name types
     * since a source must always be an IpsObject.
     */
    public QualifiedNameType getSource();

    /**
     * For example if the source depends on the target because the target is the target of a
     * relation, the result of this method is an {@link IAssociation}.
     * <p>
     * <strong>Caution:</strong> This method will return null if this object was deserialized and
     * not gatherd from the {@link IIpsObject#dependsOn()} method.
     * 
     * @return The part of the source causing the dependency or <code>null</code>, if this
     *         dependency was deserialized.
     */
    public IIpsObjectPartContainer getPart();

    /**
     * The property name of the part causing this dependency.
     * <p>
     * <strong>Caution:</strong> This method will return null if this object was deserialized and
     * not gatherd from the {@link IIpsObject#dependsOn()} method.
     * 
     * @return The name of the property causing this dependency or <code>null</code>, if this
     *         dependency was deserialized.
     */
    public String getProperty();

    /**
     * The target from which the source depends on.
     */
    public Object getTarget();

}
