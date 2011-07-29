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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchQuery;

/**
 * ModelSearchQuery contains the logic of the Faktor-IPS Model Search. A
 * ModelSearchPresentationModel contains the conditions for the search and the result is stored in a
 * IpsSearchResult
 * 
 * @author dicker
 */
public class ModelSearchQuery extends AbstractIpsSearchQuery<ModelSearchPresentationModel> {

    protected ModelSearchQuery(ModelSearchPresentationModel model) {
        this(model, IpsPlugin.getDefault().getIpsModel());
    }

    /**
     * Just for testing with a mocked {@link IIpsModel} as param
     */
    protected ModelSearchQuery(ModelSearchPresentationModel model, IIpsModel ipsModel) {
        super(model, ipsModel);
    }

    @Override
    protected void searchDetails() throws CoreException {
        Set<IType> searchedTypes = getTypes(getMatchingSrcFiles());

        addMatches(searchedTypes);
    }

    private void addMatches(Set<IType> searchedTypes) throws CoreException {
        WildcardMatcher stringMatcher = new WildcardMatcher(searchModel.getSearchTerm());
        ClassMatcher classMatcher = new ClassMatcher(searchModel.getSearchedClazzes());
        ExtensionPropertyMatcher extensionPropertyMatcher = new ExtensionPropertyMatcher(stringMatcher, ipsModel);

        for (IType type : searchedTypes) {
            IIpsElement[] children = type.getChildren();
            for (IIpsElement childElement : children) {
                if (isMatchingElement(childElement, classMatcher, stringMatcher, extensionPropertyMatcher)) {
                    searchResult.addMatch(new Match(childElement, 0, 0));
                }
            }
        }
    }

    private boolean isMatchingElement(IIpsElement element,
            ClassMatcher classMatcher,
            WildcardMatcher stringMatcher,
            ExtensionPropertyMatcher extensionPropertyMatcher) {
        if (!classMatcher.isMatchingClass(element)) {
            return false;
        }
        if (stringMatcher.isMatching(element.getName())) {
            return true;
        }

        if (isMatchingLabel(element, stringMatcher)) {
            return true;
        }

        return extensionPropertyMatcher.isMatchingElement(element);
    }

    private boolean isMatchingLabel(IIpsElement element, WildcardMatcher stringMatcher) {
        if (!(element instanceof ILabeledElement)) {
            return false;
        }

        ILabeledElement labeledElement = (ILabeledElement)element;

        ILabel label = labeledElement.getLabel(searchModel.getSearchLocale());

        if (label == null) {
            return false;
        }

        return stringMatcher.isMatching(label.getValue()) || stringMatcher.isMatching(label.getPluralValue());
    }

    private Set<IType> getTypes(Set<IIpsSrcFile> searchedSrcFiles) throws CoreException {
        Set<IType> types = new HashSet<IType>(searchedSrcFiles.size());

        for (IIpsSrcFile srcFile : searchedSrcFiles) {
            if (srcFile.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE
                    || srcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {

                types.add((IType)srcFile.getIpsObject());
            }
        }

        return types;
    }

    @Override
    protected List<IpsObjectType> getIpsObjectTypeFilter() {
        List<IpsObjectType> objectTypes = new ArrayList<IpsObjectType>();
        objectTypes.add(IpsObjectType.POLICY_CMPT_TYPE);
        objectTypes.add(IpsObjectType.PRODUCT_CMPT_TYPE);
        return objectTypes;
    }

    @Override
    protected boolean isJustTypeNameSearch() {
        return StringUtils.isEmpty(searchModel.getSearchTerm());
    }

    /**
     * returns a specific label for the result
     */
    @Override
    public String getResultLabel(int matchingCount) {
        List<Object> args = new ArrayList<Object>();

        String message;
        if (isJustTypeNameSearch()) {
            args.add(searchModel.getSrcFilePattern());

            if (matchingCount == 1) {
                message = Messages.ModelSearchQuery_labelHitTypeName;
            } else {
                args.add(matchingCount);
                message = Messages.ModelSearchQuery_labelHitsTypeName;
            }
        } else {
            args.add(searchModel.getSearchTerm());

            if (StringUtils.isEmpty(searchModel.getSrcFilePattern())) {
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTerm;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTerm;
                }

            } else {
                args.add(searchModel.getSrcFilePattern());
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTermAndTypeName;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTermAndTypeName;
                }
            }
        }

        args.add(searchModel.getSearchScope().getScopeDescription());

        String resultLabel = Messages.bind(message, args.toArray());

        return resultLabel;
    }

    @Override
    public String getLabel() {
        return Messages.ModelSearchQuery_faktorIpsModelSearchLabel;
    }
}
