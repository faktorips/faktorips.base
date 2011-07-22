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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.model.scope.IModelSearchScope;

/**
 * Presentation Model for the Faktor-IPS Model Search
 * 
 * @author dicker
 */
public class ModelSearchPresentationModel extends PresentationModelObject {

    public static final String TYPE_NAME = "typeName"; //$NON-NLS-1$
    public static final String SEARCH_TERM = "searchTerm"; //$NON-NLS-1$
    public static final String SEARCH_ATTRIBUTES = "searchAttributes"; //$NON-NLS-1$
    public static final String SEARCH_METHODS = "searchMethods"; //$NON-NLS-1$
    public static final String SEARCH_ASSOCIATIONS = "searchAssociations"; //$NON-NLS-1$
    public static final String SEARCH_TABLE_STRUCTURE_USAGES = "searchTableStructureUsages"; //$NON-NLS-1$
    public static final String SEARCH_VALIDATION_RULES = "searchValidationRules"; //$NON-NLS-1$

    private IModelSearchScope searchScope;

    private String searchTerm = ""; //$NON-NLS-1$
    private String typeName = ""; //$NON-NLS-1$

    private final Set<Class<? extends IIpsObjectPart>> searchedClazzes = new HashSet<Class<? extends IIpsObjectPart>>();

    public ModelSearchPresentationModel() {
        initDefaultSearchValues();
    }

    private void initDefaultSearchValues() {
        setSearchAttributes(true);
        setSearchMethods(true);
        setSearchAssociations(true);
        setSearchTableStructureUsages(true);
        setSearchValidationRules(true);
    }

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
        return searchedClazzes.contains(IAttribute.class);
    }

    public void setSearchAttributes(boolean newValue) {
        boolean oldValue = isSearchAttributes();
        handleNewClassValue(IAttribute.class, newValue);

        notifyListeners(new PropertyChangeEvent(this, SEARCH_ATTRIBUTES, oldValue, newValue));
    }

    private void handleNewClassValue(Class<? extends IIpsObjectPart> clazz, boolean newValue) {
        if (newValue) {
            searchedClazzes.add(clazz);
        } else {
            searchedClazzes.remove(clazz);
        }
    }

    public boolean isSearchMethods() {
        return searchedClazzes.contains(IMethod.class);
    }

    public void setSearchMethods(boolean newValue) {
        boolean oldValue = isSearchMethods();
        handleNewClassValue(IMethod.class, newValue);

        notifyListeners(new PropertyChangeEvent(this, SEARCH_METHODS, oldValue, newValue));
    }

    public boolean isSearchAssociations() {
        return searchedClazzes.contains(IAssociation.class);
    }

    public void setSearchAssociations(boolean newValue) {
        boolean oldValue = isSearchAssociations();
        handleNewClassValue(IAssociation.class, newValue);

        notifyListeners(new PropertyChangeEvent(this, SEARCH_ASSOCIATIONS, oldValue, newValue));
    }

    public boolean isSearchTableStructureUsages() {
        return searchedClazzes.contains(ITableStructureUsage.class);
    }

    public void setSearchTableStructureUsages(boolean newValue) {
        boolean oldValue = isSearchTableStructureUsages();
        handleNewClassValue(ITableStructureUsage.class, newValue);

        notifyListeners(new PropertyChangeEvent(this, SEARCH_TABLE_STRUCTURE_USAGES, oldValue, newValue));
    }

    public boolean isSearchValidationRules() {
        return searchedClazzes.contains(IValidationRule.class);
    }

    public void setSearchValidationRules(boolean newValue) {
        boolean oldValue = isSearchValidationRules();
        handleNewClassValue(IValidationRule.class, newValue);

        notifyListeners(new PropertyChangeEvent(this, SEARCH_VALIDATION_RULES, oldValue, newValue));
    }

    public void setSearchScope(IModelSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public IModelSearchScope getSearchScope() {
        return searchScope;
    }

    /**
     * stores the actual values into the dialog settings
     */
    public void store(IDialogSettings settings) {
        settings.put(SEARCH_TERM, getSearchTerm());
        settings.put(TYPE_NAME, getTypeName());

        settings.put(SEARCH_ATTRIBUTES, isSearchAttributes());
        settings.put(SEARCH_METHODS, isSearchMethods());
        settings.put(SEARCH_ASSOCIATIONS, isSearchAssociations());
        settings.put(SEARCH_TABLE_STRUCTURE_USAGES, isSearchTableStructureUsages());
        settings.put(SEARCH_VALIDATION_RULES, isSearchValidationRules());
    }

    /**
     * reads the dialog setting and uses the values for the actual search
     */
    public void read(IDialogSettings settings) {
        setSearchTerm(settings.get(SEARCH_TERM));
        setTypeName(settings.get(TYPE_NAME));

        setSearchAttributes(settings.getBoolean(SEARCH_ATTRIBUTES));
        setSearchMethods(settings.getBoolean(SEARCH_METHODS));
        setSearchAssociations(settings.getBoolean(SEARCH_ASSOCIATIONS));
        setSearchTableStructureUsages(settings.getBoolean(SEARCH_TABLE_STRUCTURE_USAGES));
        setSearchValidationRules(settings.getBoolean(SEARCH_VALIDATION_RULES));
    }

    public Set<Class<? extends IIpsObjectPart>> getSearchedClazzes() {
        return searchedClazzes;
    }

    // TODO right place?
    public Locale getSearchLocale() {
        return new MultiLanguageSupport().getLocalizationLocale();
    }
}
