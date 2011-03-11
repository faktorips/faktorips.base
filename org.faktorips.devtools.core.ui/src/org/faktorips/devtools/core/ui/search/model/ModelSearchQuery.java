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
import java.util.List;

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
    private final ModelSearchFaktoripsResources resources;
    private final StringMatcher stringMatcher = new StringMatcher();

    protected ModelSearchQuery(ModelSearchPresentationModel model, ModelSearchFaktoripsResources resources) {
        this.model = model;
        this.searchResult = new ModelSearchResult(this);
        this.resources = resources;
    }

    public ModelSearchQuery(ModelSearchPresentationModel model) {
        this(model, new ModelSearchFaktoripsResources());
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        searchResult.removeAll();

        monitor.beginTask(this.getLabel(), 2);

        try {
            List<IIpsSrcFile> searchedSrcFiles = getMatchingSrcFiles();

            model.getSelectedProjects();

            if (justClassNameSearched()) {
                for (IIpsSrcFile srcFile : searchedSrcFiles) {
                    searchResult.addMatch(new Match(srcFile, 0, 0));
                }
            } else {
                List<IType> searchedTypes = getTypes(searchedSrcFiles);

                List<IpsObjectPartFinder> finders = findFinders();

                for (IpsObjectPartFinder finder : finders) {
                    findAttributes(searchedTypes, finder);
                }

            }
        } catch (CoreException e) {
            return new IpsStatus(e);
        }

        monitor.done();
        return new IpsStatus(IStatus.OK, 0, "NOCHMAL GUT GEGANGEN", null);
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

    protected void findAttributes(List<IType> searchedTypes, IpsObjectPartFinder finder) {
        List<Match> matches = finder.findMatchingIpsObjectParts(searchedTypes, model.getSearchTerm());
        if (matches.isEmpty()) {
            return;
        }

        for (Match match : matches) {
            searchResult.addMatch(match);
        }
    }

    protected List<IType> getTypes(List<IIpsSrcFile> searchedSrcFiles) throws CoreException {
        List<IType> types = new ArrayList<IType>(searchedSrcFiles.size());

        for (IIpsSrcFile srcFile : searchedSrcFiles) {
            IType type = resources.getType(srcFile);
            if (type != null) {
                types.add(type);
            }
        }

        return types;
    }

    private List<IIpsSrcFile> getMatchingSrcFiles() throws CoreException {
        List<IIpsSrcFile> searchedTypes = getSearchedTypes();

        if (StringUtils.isNotBlank(model.getTypeName())) {
            List<IIpsSrcFile> hits = new ArrayList<IIpsSrcFile>();
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

    private List<IIpsSrcFile> getSearchedTypes() throws CoreException {
        return resources.getIpsSourceFiles(model.getSelectedProjects(), getIpsObjectTypeFilter());
    }

    private IpsObjectType[] getIpsObjectTypeFilter() {
        return new IpsObjectType[] { IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE };
    }

    private boolean justClassNameSearched() {
        return StringUtils.isEmpty(model.getSearchTerm());
    }

    @Override
    public String getLabel() {
        return model.toString();
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
