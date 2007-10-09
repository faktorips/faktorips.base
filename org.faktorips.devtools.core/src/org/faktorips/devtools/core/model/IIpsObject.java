/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.Dependency;


/**
 *
 */
public interface IIpsObject extends IIpsObjectPartContainer {
    
    /**
     * Returns the object's type.
     */
    public IpsObjectType getIpsObjectType();
    
    /**
     * Returns the IPS source file this object is stored in.
     */
    public IIpsSrcFile getIpsSrcFile();
    
    /**
     * Returns <code>true</code> if the object was created from a parsable file content,
     * <code>false</code> otherwise.
     */
    public boolean isFromParsableFile();
    
    /**
     * Returns the object's qualified name. The qualified name is the name of the ips package fragment's
     * name followed by a dot followed by the object's unqualified name. So this is basically the
     * same concept as the qualified name of Java classes. 
     * <p>
     * <b>Example:</b><br>
     * The qualified name of an ips object called 'MotorCoverage' in the package fragment 
     * 'mycompany.motor' has the qualified name 'mycompany.motor.MotorCoverage'.
     */
    public String getQualifiedName();
    
    /**
     * Returns the qualified name type for this ips object which is the combination of this ips objects qualified name and
     * its ips object type. I
     */
    public QualifiedNameType getQualifiedNameType();
    
    /**
     * Returns the IPS package fragment the object is contained in.
     */
    public IIpsPackageFragment getIpsPackageFragment();
    
    /**
     * Returns the Dependencys of this IpsObject. A Dependency contains the QualifiedNameType of the
     * IpsObject this IpsObject depends on and an additional information if the dependency is a deep
     * one or not. A deep dependency means that not only the direct dependency will be considered
     * but also the dependencies of the IpsObject this one depends on will be considered when the dependency
     * graph is evaluated. 
     * <p>
     * We use Dependency instances instead of ips object references because an object can depend
     * on another object that does not exist, e.g. because the other object has been deleted.
     * However, if the deleted object is created again, we must rebuild this dependant object so
     * that the problem marker will be removed.
     * 
     * @throws CoreException
     */
    public Dependency[] dependsOn() throws CoreException;
    
}
