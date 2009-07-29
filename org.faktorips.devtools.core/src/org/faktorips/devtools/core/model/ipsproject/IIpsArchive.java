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

package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * An ips archive is an archive for ips objects. It is physically stored in a file. The file's format is jar.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchive {

    /**
     * Constant for the top-level folder in the archive file that contains the entries for the ips 
     * objects. 
     */
    public final static String IPSOBJECTS_FOLDER = "ipsobjects"; //$NON-NLS-1$
    
    /**
     * Constant for the jar entry name" that contains additional ipsobjects properties like the mapping to Java base packages.
     */
    public final static String JAVA_MAPPING_ENTRY_NAME = IPSOBJECTS_FOLDER + IPath.SEPARATOR + "ipsobjects.properties"; //$NON-NLS-1$

    public final static String QNT_PROPERTY_POSTFIX_SEPARATOR = "#"; //$NON-NLS-1$
    
    public final static String PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE = "basePackageMergable"; //$NON-NLS-1$

    public final static String PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED = "basePackageDerived"; //$NON-NLS-1$

    /**
     * Returns the path to the underlying file. Note that the file might exists outside the workspace or might not 
     * exists at all.
     */
    public IPath getArchivePath();

    /**
     * Returns the absolute path in the local file system to this resource, 
     * or <code>null</code> if no path can't be determined.
     */
    public IPath getLocation();

    /**
     * Returns the package fragment root that is represented by this archive.
     */
    public IIpsPackageFragmentRoot getRoot();
    
    /**
     * Returns <code>true</code> if the archive exists, otherwise <code>false</code>.
     */
    public boolean exists();
    
    /**
     * Returns the names (in ascending order) of the non-empty packages contained in the archive.
     * 
     * <p>
     * A package is not empty, if it contains at least one ips object.
     */
    public String[] getNonEmptyPackages() throws CoreException;

    /**
     * Returns <code>true</code> if the archive contains the package (empty or not),
     * otherwise <code>false</code>.
     */
    public boolean containsPackage(String name) throws CoreException;

    /**
     * Returns the names (in ascending order) of the non-empty subpackages for the given parent
     * package as list.
     */
    public String [] getNonEmptySubpackages(String pack) throws CoreException;

    /**
     * Returns the set of qualified name types for the ips objects stored in the archive
     */
    public Set<QualifiedNameType> getQNameTypes() throws CoreException;
    
    /**
     * Returns the set of qualified name types for the ips objects stored in the given package.
     * Returns an empty set if the archive does not contain an object for the given package or
     * packName is <code>null</code>.
     */
    public Set<QualifiedNameType> getQNameTypes(String packName) throws CoreException;
    
    /**
     * Returns <code>true</code> if the archive contains the ips object indentified by the given
     * qualified name type, otherwise <code>false</code>.
     */
    public boolean contains(QualifiedNameType qnt) throws CoreException;
    
    /**
     * Returns the content for the qualified name type or <code>null</code> if the archive
     * does not contain the given qualified name type. Returns <code>null</code> if qnt is <code>null</code>. 
     */
    public InputStream getContent(QualifiedNameType qnt) throws CoreException;    

    public InputStream getSortDefinitionContent(String packName) throws CoreException;
    
    /**
     * Returns the name of the base package for the mergable artefacts (XML-Files, Java source files).
     * All mergable artefacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForMergableArtefacts(QualifiedNameType qnt) throws CoreException;

    /**
     * Returns the name of the base package for the derived artefacts (XML-Files, Java source files).
     * All derived artefacts are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForDerivedArtefacts(QualifiedNameType qnt) throws CoreException;
    
}
