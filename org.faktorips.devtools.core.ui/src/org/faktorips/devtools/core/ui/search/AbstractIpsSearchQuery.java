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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;

public abstract class AbstractIpsSearchQuery<T extends IIpsSearchPresentationModel> implements IIpsSearchQuery {

    protected final T searchModel;
    protected final IpsSearchResult searchResult;
    protected final IIpsModel ipsModel;

    public AbstractIpsSearchQuery(T model, IIpsModel ipsModel) {
        this.searchModel = model;
        this.searchResult = new IpsSearchResult(this);
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
                searchDetails();
            }
        } catch (CoreException e) {
            return new IpsStatus(e);
        }

        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.AbstractSearchQuery_searchStatusOk, null);
    }

    protected abstract void searchDetails() throws CoreException;

    protected abstract boolean isJustTypeNameSearch();

    protected Set<IIpsSrcFile> getMatchingSrcFiles() throws CoreException {
        Set<IIpsSrcFile> searchedSrcFiles = getSelectedSrcFiles();

        if (StringUtils.isNotBlank(searchModel.getSrcFilePattern())) {
            WildcardMatcher typeNameMatcher = new WildcardMatcher(searchModel.getSrcFilePattern());

            Set<IIpsSrcFile> hits = new HashSet<IIpsSrcFile>();
            for (IIpsSrcFile srcFile : searchedSrcFiles) {

                if (typeNameMatcher.isMatching(srcFile.getIpsObjectName())) {
                    hits.add(srcFile);
                }
            }
            searchedSrcFiles = hits;
        }
        return searchedSrcFiles;
    }

    protected Set<IIpsSrcFile> getSelectedSrcFiles() throws CoreException {
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

    protected abstract List<IpsObjectType> getIpsObjectTypeFilter();

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

    @Override
    public abstract String getResultLabel(int matchCount);
}