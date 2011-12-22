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
import org.faktorips.devtools.core.ui.search.IpsSearchResult;
import org.faktorips.devtools.core.ui.search.matcher.ClassMatcher;
import org.faktorips.devtools.core.ui.search.matcher.ExtensionPropertyMatcher;
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;

/**
 * Contains the logic of the Faktor-IPS Model Search. It contains a
 * {@link ModelSearchPresentationModel} with the conditions for the search and the result is stored
 * in the {@link IpsSearchResult}
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
        WildcardMatcher stringMatcher = new WildcardMatcher(getSearchModel().getSearchTerm());
        ClassMatcher classMatcher = new ClassMatcher(getSearchModel().getSearchedClazzes());
        ExtensionPropertyMatcher extensionPropertyMatcher = new ExtensionPropertyMatcher(stringMatcher, getIpsModel());

        for (IType type : searchedTypes) {
            IIpsElement[] children = type.getChildren();
            for (IIpsElement childElement : children) {
                if (isMatchingElement(childElement, classMatcher, stringMatcher, extensionPropertyMatcher)) {
                    getSearchResult().addMatch(new Match(childElement, 0, 0));
                }
            }
        }
    }

    private boolean isMatchingElement(IIpsElement element,
            ClassMatcher classMatcher,
            WildcardMatcher stringMatcher,
            ExtensionPropertyMatcher extensionPropertyMatcher) {
        if (!classMatcher.isMatching(element)) {
            return false;
        }
        if (stringMatcher.isMatching(element.getName())) {
            return true;
        }

        if (isMatchingLabel(element, stringMatcher)) {
            return true;
        }

        return extensionPropertyMatcher.isMatching(element);
    }

    private boolean isMatchingLabel(IIpsElement element, WildcardMatcher stringMatcher) {
        if (!(element instanceof ILabeledElement)) {
            return false;
        }

        ILabeledElement labeledElement = (ILabeledElement)element;

        List<ILabel> labels = labeledElement.getLabels();

        for (ILabel label : labels) {

            if (label == null) {
                continue;
            }

            if (stringMatcher.isMatching(label.getValue())) {
                return true;
            }
            if (stringMatcher.isMatching(label.getPluralValue())) {
                return true;
            }
        }

        return false;
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
    protected List<IpsObjectType> getAllowedIpsObjectTypes() {
        List<IpsObjectType> objectTypes = new ArrayList<IpsObjectType>();
        objectTypes.add(IpsObjectType.POLICY_CMPT_TYPE);
        objectTypes.add(IpsObjectType.PRODUCT_CMPT_TYPE);
        return objectTypes;
    }

    @Override
    protected boolean isOnlyTypeNameSearch() {
        return StringUtils.isEmpty(getSearchModel().getSearchTerm());
    }

    /**
     * returns a specific label for the result
     */
    @Override
    public String getResultLabel(int matchingCount) {
        List<Object> args = new ArrayList<Object>();

        String message;
        if (isOnlyTypeNameSearch()) {
            args.add(getSearchModel().getSrcFilePattern());

            if (matchingCount == 1) {
                message = Messages.ModelSearchQuery_labelHitTypeName;
            } else {
                args.add(matchingCount);
                message = Messages.ModelSearchQuery_labelHitsTypeName;
            }
        } else {
            args.add(getSearchModel().getSearchTerm());

            if (StringUtils.isEmpty(getSearchModel().getSrcFilePattern())) {
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTerm;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTerm;
                }

            } else {
                args.add(getSearchModel().getSrcFilePattern());
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTermAndTypeName;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTermAndTypeName;
                }
            }
        }

        args.add(getSearchModel().getSearchScope().getScopeDescription());

        String resultLabel = Messages.bind(message, args.toArray());

        return resultLabel;
    }

    @Override
    public String getLabel() {
        return Messages.ModelSearchQuery_faktorIpsModelSearchLabel;
    }
}
