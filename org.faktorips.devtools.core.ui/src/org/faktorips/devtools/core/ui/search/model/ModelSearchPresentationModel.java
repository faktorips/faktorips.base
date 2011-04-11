/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchScope;

public class ModelSearchPresentationModel extends PresentationModelObject {

    public static final String CLASS_NAME = "className"; //$NON-NLS-1$
    public static final String SEARCH_STRING = "searchString"; //$NON-NLS-1$

    private ModelSearchScope searchScope;

    private String searchTerm;
    private String typeName;

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
        notifyListeners(new PropertyChangeEvent(this, CLASS_NAME, oldValue, newValue));
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String newValue) {
        String oldValue = searchTerm;
        this.searchTerm = newValue;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_STRING, oldValue, newValue));
    }

    protected boolean isSearchAttributes() {
        return searchAttributes;
    }

    // TODO notify bei allen settern einbauen!!!
    protected void setSearchAttributes(boolean searchAttributes) {
        this.searchAttributes = searchAttributes;
    }

    protected boolean isSearchMethods() {
        return searchMethods;
    }

    protected void setSearchMethods(boolean searchMethods) {
        this.searchMethods = searchMethods;
    }

    protected boolean isSearchAssociations() {
        return searchAssociations;
    }

    protected void setSearchAssociations(boolean searchAssociation) {
        this.searchAssociations = searchAssociation;
    }

    protected boolean isSearchTableStructureUsages() {
        return searchTableStructureUsages;
    }

    protected void setSearchTableStructureUsages(boolean searchTableStructureUsages) {
        this.searchTableStructureUsages = searchTableStructureUsages;
    }

    protected boolean isSearchValidationRules() {
        return searchValidationRules;
    }

    protected void setSearchValidationRules(boolean searchValidationRules) {
        this.searchValidationRules = searchValidationRules;
    }

    public void setSearchScope(ModelSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public ModelSearchScope getSearchScope() {
        return searchScope;
    }
}
