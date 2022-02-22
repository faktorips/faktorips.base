/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * 
 * @author Thorsten Guenther
 */
public class ProjectSelectionPage extends WizardPage {
    private CheckboxTreeViewer treeViewer;
    private List<IIpsProject> preSelected;

    protected ProjectSelectionPage(List<IIpsProject> preSelected) {
        super(Messages.ProjectSelectionPage_titleSelectProjects);
        this.preSelected = preSelected;
    }

    @Override
    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer = new CheckboxTreeViewer(root);
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$

        treeViewer.setCheckedElements(preSelected.toArray());

        treeViewer.addSelectionChangedListener($ -> setPageComplete(getProjects().length > 0));
        setPageComplete(false);
        super.setControl(root);
    }

    /**
     * @return All IIpsProjects selected.
     */
    protected IIpsProject[] getProjects() {
        Object[] checked = treeViewer.getCheckedElements();
        IIpsProject[] projects = new IIpsProject[checked.length];
        for (int i = 0; i < projects.length; i++) {
            projects[i] = (IIpsProject)checked[i];
        }
        return projects;
    }

    private class TreeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return ((IIpsProject)element).getName();
        }

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsProject)element);
        }
    }

    private class ContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return false;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            List<IIpsProject> result = new ArrayList<>();
            IIpsProject[] projects;
            projects = IIpsModel.get().getIpsProjects();
            for (IIpsProject project : projects) {
                try {
                    if (!IpsPlugin.getDefault().getMigrationOperation(project).isEmpty()) {
                        result.add(project);
                    }
                } catch (IpsException e) {
                    IpsPlugin.log(e);
                }
            }
            if (result.size() == 0) {
                setMessage(Messages.ProjectSelectionPage_msgNoProjects, IMessageProvider.INFORMATION);
            } else {
                setMessage(Messages.ProjectSelectionPage_msgSelectProjects);
            }
            return result.toArray(new IIpsProject[result.size()]);
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }

    }
}
