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

package org.faktorips.devtools.core.ui.search.model;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchScope;

public class ModelSearchPresentationModel extends PresentationModelObject {

    public static final String TYPE_NAME = "typeName"; //$NON-NLS-1$
    public static final String SEARCH_TERM = "searchTerm"; //$NON-NLS-1$
    public static final String SEARCH_ATTRIBUTES = "searchAttributes"; //$NON-NLS-1$
    public static final String SEARCH_METHODS = "searchMethods"; //$NON-NLS-1$
    public static final String SEARCH_ASSOCIATIONS = "searchAssociations"; //$NON-NLS-1$
    public static final String SEARCH_TABLE_STRUCTURE_USAGES = "searchTableStructureUsages"; //$NON-NLS-1$
    public static final String SEARCH_VALIDATION_RULES = "searchValidationRules"; //$NON-NLS-1$

    private ModelSearchScope searchScope;

    private String searchTerm = ""; //$NON-NLS-1$
    private String typeName = ""; //$NON-NLS-1$

    private boolean searchAttributes = true;
    private boolean searchMethods = true;
    private boolean searchAssociations = true;
    private boolean searchTableStructureUsages = true;
    private boolean searchValidationRules = true;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String newValue) {
        String oldValue = typeName;
        typeName = newValue;
        notifyListeners(new PropertyChangeEvent(this, TYPE_NAME, oldValue, newValue));
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String newValue) {
        String oldValue = searchTerm;
        this.searchTerm = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_TERM, oldValue, newValue));
    }

    public boolean isSearchAttributes() {
        return searchAttributes;
    }

    public void setSearchAttributes(boolean newValue) {
        boolean oldValue = searchAttributes;
        this.searchAttributes = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_ATTRIBUTES, oldValue, newValue));
    }

    public boolean isSearchMethods() {
        return searchMethods;
    }

    public void setSearchMethods(boolean newValue) {
        boolean oldValue = searchMethods;
        this.searchMethods = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_METHODS, oldValue, newValue));
    }

    public boolean isSearchAssociations() {
        return searchAssociations;
    }

    public void setSearchAssociations(boolean newValue) {
        boolean oldValue = searchAssociations;
        this.searchAssociations = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_ASSOCIATIONS, oldValue, newValue));
    }

    public boolean isSearchTableStructureUsages() {
        return searchTableStructureUsages;
    }

    public void setSearchTableStructureUsages(boolean newValue) {
        boolean oldValue = searchTableStructureUsages;
        this.searchTableStructureUsages = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_TABLE_STRUCTURE_USAGES, oldValue, newValue));
    }

    public boolean isSearchValidationRules() {
        return searchValidationRules;
    }

    public void setSearchValidationRules(boolean newValue) {
        boolean oldValue = searchValidationRules;
        this.searchValidationRules = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_VALIDATION_RULES, oldValue, newValue));
    }

    public void setSearchScope(ModelSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public ModelSearchScope getSearchScope() {
        return searchScope;
    }

    public void store(IDialogSettings settings) {
        settings.put(SEARCH_TERM, getSearchTerm());
        settings.put(TYPE_NAME, getTypeName());

        settings.put(SEARCH_ATTRIBUTES, isSearchAttributes());
        settings.put(SEARCH_METHODS, isSearchMethods());
        settings.put(SEARCH_ASSOCIATIONS, isSearchAssociations());
        settings.put(SEARCH_TABLE_STRUCTURE_USAGES, isSearchTableStructureUsages());
        settings.put(SEARCH_VALIDATION_RULES, isSearchValidationRules());
    }

    public void read(IDialogSettings settings) {
        setSearchTerm(settings.get(SEARCH_TERM));
        setTypeName(settings.get(TYPE_NAME));

        setSearchAttributes(settings.getBoolean(SEARCH_ATTRIBUTES));
        setSearchMethods(settings.getBoolean(SEARCH_METHODS));
        setSearchAssociations(settings.getBoolean(SEARCH_ASSOCIATIONS));
        setSearchTableStructureUsages(settings.getBoolean(SEARCH_TABLE_STRUCTURE_USAGES));
        setSearchValidationRules(settings.getBoolean(SEARCH_VALIDATION_RULES));
    }
}
