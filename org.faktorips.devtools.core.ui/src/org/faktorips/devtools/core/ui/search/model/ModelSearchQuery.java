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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.type.IType;

/**
 * ModelSearchQuery contains the logic of the Faktor-IPS Model Search. A
 * ModelSearchPresentationModel contains the conditions for the search and the result is stored in a
 * ModelSearchResult
 * 
 * @author dicker
 */
public class ModelSearchQuery implements ISearchQuery {

    private final ModelSearchPresentationModel searchModel;
    private final ModelSearchResult searchResult;
    private final IIpsModel ipsModel;

    protected ModelSearchQuery(ModelSearchPresentationModel model) {
        this.searchModel = model;
        this.searchResult = new ModelSearchResult(this);
        ipsModel = IpsPlugin.getDefault().getIpsModel();
    }

    /**
     * Just for testing with a mocked {@link IIpsModel} as param
     */
    protected ModelSearchQuery(ModelSearchPresentationModel model, IIpsModel ipsModel) {
        this.searchModel = model;
        this.searchResult = new ModelSearchResult(this);
        this.ipsModel = ipsModel;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        searchResult.removeAll();

        monitor.beginTask(this.getLabel(), 2);

        try {
            if (isJustTypeNameSearch()) {
                for (IIpsSrcFile srcFile : getMatchingSrcFiles()) {
                    searchResult.addMatch(new Match(srcFile.getIpsObject(), 0, 0));
                }
            } else {
                Set<IType> searchedTypes = getTypes(getMatchingSrcFiles());

                addMatches(searchedTypes);
            }
        } catch (CoreException e) {
            return new IpsStatus(e);
        }

        monitor.done();
        return new IpsStatus(IStatus.OK, 0,
                org.faktorips.devtools.core.ui.search.model.Messages.ModelSearchQuery_okStatus, null);
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

    private Set<IIpsSrcFile> getMatchingSrcFiles() throws CoreException {
        Set<IIpsSrcFile> searchedTypes = getSelectedTypes();

        if (StringUtils.isNotBlank(searchModel.getTypeName())) {
            WildcardMatcher typeNameMatcher = new WildcardMatcher(searchModel.getTypeName());

            Set<IIpsSrcFile> hits = new HashSet<IIpsSrcFile>();
            for (IIpsSrcFile srcFile : searchedTypes) {

                if (typeNameMatcher.isMatching(srcFile.getIpsObjectName())) {
                    hits.add(srcFile);
                }
            }
            searchedTypes = hits;
        }
        return searchedTypes;
    }

    private Set<IIpsSrcFile> getSelectedTypes() throws CoreException {
        Set<IIpsSrcFile> ipsSrcFilesInScope = searchModel.getSearchScope().getSelectedIpsSrcFiles();

        Set<IIpsSrcFile> selectedSrcFiles = new HashSet<IIpsSrcFile>();

        List<IpsObjectType> objectTypes = getIpsObjectTypeFilter();
        for (IIpsSrcFile srcFile : ipsSrcFilesInScope) {
            if (objectTypes.contains(srcFile.getIpsObjectType())) {
                selectedSrcFiles.add(srcFile);
            }
        }

        return selectedSrcFiles;

    }

    private List<IpsObjectType> getIpsObjectTypeFilter() {
        List<IpsObjectType> objectTypes = new ArrayList<IpsObjectType>();
        objectTypes.add(IpsObjectType.POLICY_CMPT_TYPE);
        objectTypes.add(IpsObjectType.PRODUCT_CMPT_TYPE);
        return objectTypes;
    }

    private boolean isJustTypeNameSearch() {
        return StringUtils.isEmpty(searchModel.getSearchTerm());
    }

    @Override
    public String getLabel() {
        return Messages.ModelSearchQuery_faktorIpsModelSearchLabel;
    }

    /**
     * returns a specific label for the result
     */
    protected String getResultLabel(int matchingCount) {
        List<Object> args = new ArrayList<Object>();

        String message;
        if (isJustTypeNameSearch()) {
            args.add(searchModel.getTypeName());

            if (matchingCount == 1) {
                message = Messages.ModelSearchQuery_labelHitTypeName;
            } else {
                args.add(matchingCount);
                message = Messages.ModelSearchQuery_labelHitsTypeName;
            }
        } else {
            args.add(searchModel.getSearchTerm());

            if (StringUtils.isEmpty(searchModel.getTypeName())) {
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTerm;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTerm;
                }

            } else {
                args.add(searchModel.getTypeName());
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
    public boolean canRerun() {
        return true;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public ISearchResult getSearchResult() {
        return searchResult;
    }

}
