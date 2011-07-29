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

import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.search.model.scope.IModelSearchScope;

public interface ISearchPresentationModel {

    public static final String SRC_FILE_PATTERN = "srcFilePattern"; //$NON-NLS-1$

    public void setSearchScope(IModelSearchScope searchScope);

    public IModelSearchScope getSearchScope();

    public Locale getSearchLocale();

    /**
     * stores the actual values into the dialog settings
     */
    public void store(IDialogSettings settings);

    /**
     * reads the dialog setting and uses the values for the actual search
     */
    public void read(IDialogSettings settings);

    public void setSrcFilePattern(String newValue);

    public String getSrcFilePattern();
}