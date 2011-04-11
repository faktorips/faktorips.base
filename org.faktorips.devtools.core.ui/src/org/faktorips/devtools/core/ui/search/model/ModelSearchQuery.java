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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.search.model.finder.AssociationFinder;
import org.faktorips.devtools.core.ui.search.model.finder.AttributeFinder;
import org.faktorips.devtools.core.ui.search.model.finder.IpsObjectPartFinder;
import org.faktorips.devtools.core.ui.search.model.finder.MethodFinder;
import org.faktorips.devtools.core.ui.search.model.finder.StringMatcher;
import org.faktorips.devtools.core.ui.search.model.finder.TableStructureUsageFinder;
import org.faktorips.devtools.core.ui.search.model.finder.ValidationRuleFinder;

public class ModelSearchQuery implements ISearchQuery {

    private final ModelSearchPresentationModel model;
    private final ModelSearchResult searchResult;
    private final StringMatcher stringMatcher = new StringMatcher();

    protected ModelSearchQuery(ModelSearchPresentationModel model) {
        this.model = model;
        this.searchResult = new ModelSearchResult(this);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        searchResult.removeAll();

        monitor.beginTask(this.getLabel(), 2);

        try {
            Set<IIpsSrcFile> searchedSrcFiles = getMatchingSrcFiles();

            if (isJustTypeNameSearch()) {
                for (IIpsSrcFile srcFile : searchedSrcFiles) {
                    searchResult.addMatch(new Match(srcFile, 0, 0));
                }
            } else {
                Set<IType> searchedTypes = getTypes(searchedSrcFiles);

                List<IpsObjectPartFinder> finders = findFinders();

                for (IpsObjectPartFinder finder : finders) {
                    findAttributes(searchedTypes, finder);
                }

            }
        } catch (CoreException e) {
            return new IpsStatus(e);
        }

        monitor.done();
        return new IpsStatus(IStatus.OK, 0,
                org.faktorips.devtools.core.ui.search.model.Messages.ModelSearchQuery_okStatus, null);
    }

    private List<IpsObjectPartFinder> findFinders() {
        List<IpsObjectPartFinder> finders = new ArrayList<IpsObjectPartFinder>();

        if (model.isSearchAttributes()) {
            finders.add(new AttributeFinder());
        }

        if (model.isSearchMethods()) {
            finders.add(new MethodFinder());
        }

        if (model.isSearchAssociations()) {
            finders.add(new AssociationFinder());
        }

        if (model.isSearchTableStructureUsages()) {
            finders.add(new TableStructureUsageFinder());
        }

        if (model.isSearchValidationRules()) {
            finders.add(new ValidationRuleFinder());
        }
        return finders;
    }

    protected void findAttributes(Set<IType> searchedTypes, IpsObjectPartFinder finder) {
        List<Match> matches = finder.findMatchingIpsObjectParts(searchedTypes, model.getSearchTerm());
        if (matches.isEmpty()) {
            return;
        }

        for (Match match : matches) {
            searchResult.addMatch(match);
        }
    }

    protected Set<IType> getTypes(Set<IIpsSrcFile> searchedSrcFiles) throws CoreException {
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

        if (StringUtils.isNotBlank(model.getTypeName())) {
            Set<IIpsSrcFile> hits = new HashSet<IIpsSrcFile>();
            for (IIpsSrcFile srcFile : searchedTypes) {
                if (isMatching(model.getTypeName(), srcFile.getName())) {
                    hits.add(srcFile);
                }
            }
            searchedTypes = hits;
        }
        return searchedTypes;
    }

    protected boolean isMatching(String searchTerm, String text) {
        return stringMatcher.isMatching(searchTerm, text);
    }

    private Set<IIpsSrcFile> getSelectedTypes() throws CoreException {
        Set<IIpsSrcFile> ipsSrcFilesInScope = model.getSearchScope().getSelectedIpsSrcFiles();

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
        return StringUtils.isEmpty(model.getSearchTerm());
    }

    @Override
    public String getLabel() {
        return Messages.ModelSearchQuery_faktorIpsModelSearchLabel;
    }

    protected String getResultLabel(int matchingCount) {
        List<Object> args = new ArrayList<Object>();

        String message;
        if (isJustTypeNameSearch()) {
            args.add(model.getTypeName());

            if (matchingCount == 1) {
                message = Messages.ModelSearchQuery_labelHitTypeName;
            } else {
                args.add(matchingCount);
                message = Messages.ModelSearchQuery_labelHitsTypeName;
            }
        } else {
            args.add(model.getSearchTerm());

            if (StringUtils.isEmpty(model.getTypeName())) {
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTerm;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTerm;
                }

            } else {
                args.add(model.getTypeName());
                if (matchingCount == 1) {
                    message = Messages.ModelSearchQuery_labelHitSearchTermAndTypeName;
                } else {
                    args.add(matchingCount);
                    message = Messages.ModelSearchQuery_labelHitsSearchTermAndTypeName;
                }
            }
        }

        args.add(model.getSearchScope().getScopeDescription());

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
