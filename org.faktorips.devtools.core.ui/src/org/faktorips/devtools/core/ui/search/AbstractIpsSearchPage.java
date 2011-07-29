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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchProjectsScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchSelectionScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkingSetScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkspaceScope;

public abstract class AbstractIpsSearchPage<T extends IIpsSearchPresentationModel> extends DialogPage implements
        ISearchPage {

    private final BindingContext bindingContext = new BindingContext();
    private final T model = createPresentationModel();

    private ISearchPageContainer container;
    private List<IDialogSettings> previousSearchData;
    private IDialogSettings dialogSettings;

    public AbstractIpsSearchPage() {
        super();
    }

    public AbstractIpsSearchPage(String title) {
        super(title);
    }

    public AbstractIpsSearchPage(String title, ImageDescriptor image) {
        super(title, image);
    }

    protected IIpsSearchScope createSearchScope() {
        int selectedScope = getContainer().getSelectedScope();

        switch (selectedScope) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                return new IpsSearchWorkspaceScope();

            case ISearchPageContainer.SELECTION_SCOPE:
                return new IpsSearchSelectionScope(getContainer().getSelection());

            case ISearchPageContainer.WORKING_SET_SCOPE:
                return new IpsSearchWorkingSetScope(getContainer().getSelectedWorkingSets());

            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                return new IpsSearchProjectsScope(getContainer().getSelection());

            default:
                break;
        }
        return null;
    }

    protected void readConfiguration() {
        IDialogSettings settings = getDialogSettings();

        IDialogSettings[] sections = settings.getSections();

        setPreviousSearchData(new ArrayList<IDialogSettings>());
        for (IDialogSettings dialogSettings : sections) {
            if (dialogSettings.getName().startsWith(getDialogSettingPrefix())) {
                getPreviousSearchData().add(dialogSettings);
            }
        }

        Comparator<IDialogSettings> comparator = new Comparator<IDialogSettings>() {

            @Override
            public int compare(IDialogSettings arg0, IDialogSettings arg1) {
                return arg1.getName().compareTo(arg0.getName());
            }

        };
        Collections.sort(getPreviousSearchData(), comparator);
    }

    protected abstract String getDialogSettingPrefix();

    protected abstract String getSearchPageName();

    protected void writeConfiguration() {
        IDialogSettings settings = getDialogSettings();

        // TODO evtl. sortierkriterium optimieren
        IDialogSettings newSection = settings.addNewSection(getDialogSettingPrefix() + System.currentTimeMillis());

        getModel().store(newSection);

    }

    private IDialogSettings getDialogSettings() {
        if (dialogSettings == null) {
            IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings();
            dialogSettings = settings.getSection(getSearchPageName());
            if (dialogSettings == null) {
                dialogSettings = settings.addNewSection(getSearchPageName());
            }
        }
        return dialogSettings;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getBindingContext() != null) {
            getBindingContext().dispose();
        }
    }

    @Override
    public boolean performAction() {
        // it is impossible to link the search scope to the model with the context binding, because
        // a changed selection of the scope doesn't throw an event.
        getModel().setSearchScope(createSearchScope());

        ISearchQuery query = getModel().createSearchQuery();

        writeConfiguration();

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    @Override
    public void setContainer(ISearchPageContainer container) {
        this.container = container;
    }

    protected abstract T createPresentationModel();

    protected ISearchPageContainer getContainer() {
        return container;
    }

    protected void setPreviousSearchData(List<IDialogSettings> previousSearchData) {
        this.previousSearchData = previousSearchData;
    }

    protected List<IDialogSettings> getPreviousSearchData() {
        return previousSearchData;
    }

    protected BindingContext getBindingContext() {
        return bindingContext;
    }

    protected T getModel() {
        return model;
    }
}