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
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;

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
        this.searchModel = model;
        this.searchResult = new IpsSearchResult(this);
        this.ipsModel = ipsModel;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        getSearchResult().removeAll();

        monitor.beginTask(this.getLabel(), 2);

        try {
            if (isOnlyTypeNameSearch()) {
                for (IIpsSrcFile srcFile : getMatchingSrcFiles()) {
                    getSearchResult().addMatch(new Match(srcFile.getIpsObject(), 0, 0));
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

    /**
     * Searches by using all conditions. The method is called by {@code run()}, if
     * {@link #isOnlyTypeNameSearch()} returns {@code false}
     * <p>
     * The logic must be specified in Subclasses.
     * <p>
     * The method should not be called by clients.
     */
    protected abstract void searchDetails() throws CoreException;

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
    protected Set<IIpsSrcFile> getMatchingSrcFiles() throws CoreException {
        Set<IIpsSrcFile> searchedSrcFiles = getSelectedSrcFiles();

        if (StringUtils.isNotBlank(getSearchModel().getSrcFilePattern())) {
            WildcardMatcher typeNameMatcher = new WildcardMatcher(getSearchModel().getSrcFilePattern());

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

    /**
     * Returns a Set with the selected {@link IIpsSrcFile IIpsSrcFiles}
     * 
     */
    protected Set<IIpsSrcFile> getSelectedSrcFiles() throws CoreException {
        Set<IIpsSrcFile> ipsSrcFilesInScope = getSearchModel().getSearchScope().getSelectedIpsSrcFiles();

        Set<IIpsSrcFile> selectedSrcFiles = new HashSet<IIpsSrcFile>();

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