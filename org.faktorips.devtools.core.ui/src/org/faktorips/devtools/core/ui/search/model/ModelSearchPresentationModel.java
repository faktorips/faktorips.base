/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.model;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;

/**
 * Presentation Model for the Faktor-IPS Model Search
 * 
 * @author dicker
 */
public class ModelSearchPresentationModel extends AbstractSearchPresentationModel {

    public static final String SEARCH_TERM = "searchTerm"; //$NON-NLS-1$
    public static final String SEARCH_ATTRIBUTES = "searchAttributes"; //$NON-NLS-1$
    public static final String SEARCH_METHODS = "searchMethods"; //$NON-NLS-1$
    public static final String SEARCH_ASSOCIATIONS = "searchAssociations"; //$NON-NLS-1$
    public static final String SEARCH_TABLE_STRUCTURE_USAGES = "searchTableStructureUsages"; //$NON-NLS-1$
    public static final String SEARCH_VALIDATION_RULES = "searchValidationRules"; //$NON-NLS-1$

    private String searchTerm = StringUtils.EMPTY;
    private final Set<Class<? extends IIpsObjectPart>> searchedClazzes = new HashSet<>();

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

    /**
     * stores the actual values into the dialog settings
     */
    @Override
    public void store(IDialogSettings settings) {
        settings.put(SEARCH_TERM, getSearchTerm());
        settings.put(SRC_FILE_PATTERN, getSrcFilePattern());

        settings.put(SEARCH_ATTRIBUTES, isSearchAttributes());
        settings.put(SEARCH_METHODS, isSearchMethods());
        settings.put(SEARCH_ASSOCIATIONS, isSearchAssociations());
        settings.put(SEARCH_TABLE_STRUCTURE_USAGES, isSearchTableStructureUsages());
        settings.put(SEARCH_VALIDATION_RULES, isSearchValidationRules());
    }

    /**
     * reads the dialog setting and uses the values for the actual search
     */
    @Override
    public void read(IDialogSettings settings) {
        setSearchTerm(settings.get(SEARCH_TERM));
        setSrcFilePattern(settings.get(SRC_FILE_PATTERN));

        setSearchAttributes(settings.getBoolean(SEARCH_ATTRIBUTES));
        setSearchMethods(settings.getBoolean(SEARCH_METHODS));
        setSearchAssociations(settings.getBoolean(SEARCH_ASSOCIATIONS));
        setSearchTableStructureUsages(settings.getBoolean(SEARCH_TABLE_STRUCTURE_USAGES));
        setSearchValidationRules(settings.getBoolean(SEARCH_VALIDATION_RULES));
    }

    public Set<Class<? extends IIpsObjectPart>> getSearchedClazzes() {
        return searchedClazzes;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
