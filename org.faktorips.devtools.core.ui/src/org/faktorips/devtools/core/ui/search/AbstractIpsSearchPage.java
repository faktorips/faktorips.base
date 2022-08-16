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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchProjectsScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchSelectionScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkingSetScope;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkspaceScope;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * An abstract implementation of the {@link ISearchPage} for the model and product search, which
 * contains basic and common functionality.
 * 
 * @param <T> the type of the {@link IIpsSearchPresentationModel} for the search
 * 
 * @author dicker
 */
public abstract class AbstractIpsSearchPage<T extends IIpsSearchPresentationModel> extends DialogPage implements
        ISearchPage {

    private final BindingContext bindingContext = new BindingContext();
    private final T presentationModel = createPresentationModel();

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

    /**
     * Creates a {@link IIpsSearchScope} from the scope selected in the {@link ISearchPageContainer}
     * 
     * @return the {@link IIpsSearchScope} matching the selected scope
     */
    private IIpsSearchScope createSearchScope() {
        int selectedScope = getSearchPageContainer().getSelectedScope();

        switch (selectedScope) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                return new IpsSearchWorkspaceScope();

            case ISearchPageContainer.SELECTION_SCOPE:
                return new IpsSearchSelectionScope(getSearchPageContainer().getSelection());

            case ISearchPageContainer.WORKING_SET_SCOPE:
                return new IpsSearchWorkingSetScope(getSearchPageContainer().getSelectedWorkingSets());

            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                return new IpsSearchProjectsScope(getSearchPageContainer().getSelection());

            default:
                break;
        }
        return null;
    }

    /**
     * reads the {@link IDialogSettings} of the search
     */
    protected void readDialogSettings() {
        IDialogSettings settings = getDialogSettings();

        IDialogSettings[] sections = settings.getSections();

        previousSearchData = new ArrayList<>();
        for (IDialogSettings childSection : sections) {
            if (childSection.getName().startsWith(getDialogSettingPrefix())) {
                previousSearchData.add(childSection);
            }
        }

        Collections.sort(getPreviousSearchData(), Comparator.comparing(IDialogSettings::getName));
    }

    /**
     * returns a prefix to identify the {@link IDialogSettings} for the search
     */
    protected abstract String getDialogSettingPrefix();

    /**
     * returns the name of the search page
     */
    protected abstract String getSearchPageName();

    /**
     * reads the {@link IDialogSettings} of the search
     */
    protected void storeDialogSettings() {
        IDialogSettings settings = getDialogSettings();

        IDialogSettings newSection = settings.addNewSection(getDialogSettingPrefix() + System.currentTimeMillis());

        getPresentationModel().store(newSection);

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
        if (!getPresentationModel().isValid()) {
            return false;
        }

        getPresentationModel().setSearchScope(createSearchScope());

        ISearchQuery query = createSearchQuery();

        storeDialogSettings();

        NewSearchUI.runQueryInBackground(query);

        return true;
    }

    /**
     * creates the {@link ISearchQuery}
     */
    protected abstract ISearchQuery createSearchQuery();

    @Override
    public void setContainer(ISearchPageContainer container) {
        this.container = container;
        container.setPerformActionEnabled(getPresentationModel().isValid());
    }

    /**
     * creates and returns an {@link IIpsSearchPresentationModel}
     */
    protected abstract T createPresentationModel();

    /**
     * returns the {@link ISearchPageContainer}
     */
    protected ISearchPageContainer getSearchPageContainer() {
        return container;
    }

    protected List<IDialogSettings> getPreviousSearchData() {
        return previousSearchData;
    }

    /**
     * returns the {@link BindingContext} between search page and presentation model
     */
    protected BindingContext getBindingContext() {
        return bindingContext;
    }

    /**
     * returns the {@link IIpsSearchPresentationModel}
     */
    protected T getPresentationModel() {
        return presentationModel;
    }

    /**
     * 
     * returns a {@link Text text control} to restrict the name of an {@link IIpsSrcFile} by a
     * wildcard pattern for the search. The control is already connected to the
     * {@link IIpsSearchPresentationModel} by the {@link BindingContext}.
     * 
     * @param toolkit the UIToolkit
     * @param composite the parent composite
     * @param srcFilePatternTextLabel the label for the control
     */
    protected Text createSrcFilePatternText(UIToolkit toolkit, Composite composite, String srcFilePatternTextLabel) {
        String patternLabel = Messages.AbstractIpsSearchPage_patternLabel;
        toolkit.createLabel(composite,
                NLS.bind(Messages.AbstractIpsSearchPage_labelSrcFilePattern, srcFilePatternTextLabel, patternLabel));
        Text txtTypeName = toolkit.createText(composite);

        getBindingContext().bindContent(txtTypeName, getPresentationModel(),
                IIpsSearchPresentationModel.SRC_FILE_PATTERN);
        return txtTypeName;
    }

}
