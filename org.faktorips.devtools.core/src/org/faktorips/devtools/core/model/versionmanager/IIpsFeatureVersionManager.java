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

package org.faktorips.devtools.core.model.versionmanager;

/**
 * A manager for version-related topics in FaktorIps. 
 * 
 * @author Thorsten Guenther
 */
public interface IIpsFeatureVersionManager {

    /**
     * Set the id of the feature this is a version manager for.
     * 
     * @param featureId The feature id.
     */
    public void setFeatureId(String featureId);
    
    /**
     * @return The id of the feature this is the version manager for.
     */
    public String getFeatureId();
    
    /**
     * @return The current version of the feature this is a manager for.
     */
    public String getCurrentVersion();
    
    /**
     * @param otherVersion The version-string to check for compatibility.
     * 
     * @return <code>true</code> if the current version is compatible to the given one. If otherVersion 
     * is greater than the current version, <code>false</code> is returned, because only backward-compatibility 
     * is provided by these method.
     */
    public boolean isCurrentVersionCompatibleWith(String otherVersion);
    
    /**
     * @param otherVersion The version-string to compare the current version to.
     * 
     * @return A value less 0 if otherVersion is less then currentVersion, 0 if they are
     * equal and a value greater 0 if otherVersion is greater then currentVersion. 
     */
    public int compareToCurrentVersion(String otherVersion);
    
}
