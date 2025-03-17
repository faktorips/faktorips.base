/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Contains the basic logic of the Faktor-IPS searches. It contains a specified
 * {@link IIpsSearchPresentationModel} with the conditions for the search and the result is stored
 * in the {@link IpsSearchResult}.
 * <p>
 * When the method {@link #run(IProgressMonitor)} is called, the AbstractIpsSearchQuery checks by
 * calling {@link #isOnlyTypeNameSearch()}, whether the search uses only the name of the selected
 * {@link IIpsSrcFile IIpsSrcFiles}. If not the method {@link #searchDetails()} is used for more
 * detailed conditions. Before calling searchDetails the IIpsSrcFiles are not opened.
 * 
 * @author dicker
 */
public abstract class AbstractIpsSearchQuery<T extends IIpsSearchPresentationModel> implements IIpsSearchQuery {

    private final T searchModel;
    private final IpsSearchResult searchResult;
    private final IIpsModel ipsModel;

    public AbstractIpsSearchQuery(T model, IIpsModel ipsModel) {
        searchModel = model;
        searchResult = new IpsSearchResult(this);
        this.ipsModel = ipsModel;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        getSearchResult().removeAll();

        monitor.beginTask(getLabel(), 2);

        try {
            if (isOnlyTypeNameSearch()) {
                for (IIpsSrcFile srcFile : getMatchingSrcFiles()) {
                    getSearchResult().addMatch(new Match(srcFile.getIpsObject(), 0, 0));
                }
            } else {
                searchDetails();
            }
        } catch (IpsException e) {
            return new IpsStatus(e);
        }

        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.AbstractSearchQuery_searchStatusOk, null);
    }

    /**
     * Searches by using all conditions. The method is called by {@code run()}, if
     * {@link #isOnlyTypeNameSearch()} returns {@code false}
     * <p>
     * The logic must be specified in Subclasses.
     * <p>
     * The method should not be called by clients.
     */
    protected abstract void searchDetails() throws IpsException;

    /**
     * Returns true, if only the name of an {@link IIpsSrcFile} is part of the search.
     * <p>
     * The logic must be specified in Subclasses.
     * <p>
     * The method should not be called by clients.
     */
    protected abstract boolean isOnlyTypeNameSearch();

    /**
     * Returns a Set of selected {@link IIpsSrcFile IIpsSrcFiles}, which match the pattern for the
     * {@link IpsSrcFile}.
     * 
     */
    protected Set<IIpsSrcFile> getMatchingSrcFiles() {
        Set<IIpsSrcFile> searchedSrcFiles = getSelectedSrcFiles();

        if (IpsStringUtils.isBlank(getSearchModel().getSrcFilePattern())) {
            return searchedSrcFiles;
        }

        WildcardMatcher typeNameMatcher = new WildcardMatcher(getSearchModel().getSrcFilePattern());

        Set<IIpsSrcFile> hits = new HashSet<>();
        for (IIpsSrcFile srcFile : searchedSrcFiles) {

            if (typeNameMatcher.isMatching(srcFile.getIpsObjectName())) {
                hits.add(srcFile);
            }
        }
        return hits;
    }

    /**
     * Returns a Set with the selected {@link IIpsSrcFile IIpsSrcFiles}
     * 
     */
    protected Set<IIpsSrcFile> getSelectedSrcFiles() {
        Set<IIpsSrcFile> ipsSrcFilesInScope = getSearchModel().getSearchScope().getSelectedIpsSrcFiles();

        Set<IIpsSrcFile> selectedSrcFiles = new HashSet<>();

        List<IpsObjectType> objectTypes = getAllowedIpsObjectTypes();
        for (IIpsSrcFile srcFile : ipsSrcFilesInScope) {
            if (objectTypes.contains(srcFile.getIpsObjectType())) {
                selectedSrcFiles.add(srcFile);
            }
        }

        return selectedSrcFiles;

    }

    /**
     * Returns a List of {@link IpsObjectType IpsObjectTypes}, which are allowed witin the specified
     * search
     */
    protected abstract List<IpsObjectType> getAllowedIpsObjectTypes();

    @Override
    public boolean canRerun() {
        return true;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public IpsSearchResult getSearchResult() {
        return searchResult;
    }

    @Override
    public abstract String getResultLabel(int matchCount);

    protected T getSearchModel() {
        return searchModel;
    }

    protected IIpsModel getIpsModel() {
        return ipsModel;
    }
}
