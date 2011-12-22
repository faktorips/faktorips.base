/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;

/**
 * Is the interface for the base presentasion models for model and product search
 * <p>
 * IIpsSearchPresentationModel contains functionality for
 * <ul>
 * <li>using the {@link IIpsSearchScope}</li>
 * <li>searching the name of {@link IIpsSrcFile IIpsSrcFiles}</li>
 * </ul>
 * 
 * 
 * @author dicker
 */
public interface IIpsSearchPresentationModel extends IIpsSearchPartPresentationModel {

    public static final String SRC_FILE_PATTERN = "srcFilePattern"; //$NON-NLS-1$

    /**
     * @see #getSearchScope()
     */
    public void setSearchScope(IIpsSearchScope searchScope);

    /**
     * Returns the {@link IIpsSearchScope} of the search
     */
    public IIpsSearchScope getSearchScope();

    /**
     * @see #getSrcFilePattern()
     */
    public void setSrcFilePattern(String newValue);

    /**
     * Returns the pattern for matching {@link IIpsSrcFile IIpsSrcFiles}
     */
    public String getSrcFilePattern();
}