/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import java.io.File;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * Base implementation of the {@link IHtmlPath}
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsElementHtmlPath<T extends IIpsElement> implements IHtmlPath {
    protected static final String UP_PATH = "../"; //$NON-NLS-1$

    protected T ipsElement;

    protected AbstractIpsElementHtmlPath(T ipsElement) {
        this.ipsElement = ipsElement;
    }

    /**
     * returns the relative path from the {@link IIpsPackageFragment} to the root
     * 
     */
    protected String getPackageFragmentPathToRoot(IIpsPackageFragment packageFragment) {
        if (packageFragment.isDefaultPackage()) {
            return EMPTY_PATH;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(UP_PATH);
        packageFragment = packageFragment.getParentIpsPackageFragment();
        while (packageFragment.getParentIpsPackageFragment() != null) {
            builder.append(UP_PATH);
            packageFragment = packageFragment.getParentIpsPackageFragment();
        }
        return builder.toString();
    }

    /**
     * returns the relative path from the root to the {@link IIpsPackageFragment}
     * 
     */
    protected String getPackageFragmentPathFromRoot(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage()) {
            return EMPTY_PATH;
        }
        return ipsPackageFragment.getRelativePath().toOSString() + File.separator;
    }

    public T getIpsElement() {
        return ipsElement;
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPackageFragmentPathFromRoot(getIpsPackageFragment()));
        builder.append(getFileName());
        builder.append(linkedFileType.getSuffix());

        return builder.toString();
    }

    /**
     * @return fileName of the {@link IIpsElement}
     */
    protected String getFileName() {
        return getIpsElement().getName();
    }

    /**
     * @return {@link IIpsPackageFragment} of the {@link IIpsElement}
     */
    protected abstract IIpsPackageFragment getIpsPackageFragment();
}
