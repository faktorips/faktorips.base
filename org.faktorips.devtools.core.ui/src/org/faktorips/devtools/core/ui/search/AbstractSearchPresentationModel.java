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

import java.beans.PropertyChangeEvent;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.model.scope.IModelSearchScope;

public abstract class AbstractSearchPresentationModel extends PresentationModelObject implements
        ISearchPresentationModel {

    private IModelSearchScope searchScope;
    private String typeName = ""; //$NON-NLS-1$

    @Override
    public void setSearchScope(IModelSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    @Override
    public IModelSearchScope getSearchScope() {
        return searchScope;
    }

    @Override
    public Locale getSearchLocale() {
        return new MultiLanguageSupport().getLocalizationLocale();
    }

    @Override
    public abstract void store(IDialogSettings settings);

    @Override
    public abstract void read(IDialogSettings settings);

    @Override
    public String getSrcFilePattern() {
        return typeName;
    }

    @Override
    public void setSrcFilePattern(String newValue) {
        String oldValue = typeName;
        typeName = newValue;
        notifyListeners(new PropertyChangeEvent(this, SRC_FILE_PATTERN, oldValue, newValue));
    }
}