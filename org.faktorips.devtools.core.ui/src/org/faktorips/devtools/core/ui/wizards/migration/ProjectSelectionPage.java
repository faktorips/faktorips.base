/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * 
 * @author Thorsten Guenther
 */
public class ProjectSelectionPage extends WizardPage {
    private CheckboxTreeViewer treeViewer;
    private List<IIpsProject> preSelected;

    /**
     * @param pageName
     */
    protected ProjectSelectionPage(List<IIpsProject> preSelected) {
        super(Messages.ProjectSelectionPage_titleSelectProjects);
        this.preSelected = preSelected;
    }

    /**
     * {@inheritDoc}
     */
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

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(getProjects().length > 0);
            }
        });
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

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getParent(Object element) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasChildren(Object element) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object inputElement) {
            List<IIpsProject> result = new ArrayList<IIpsProject>();
            IIpsProject[] projects;
            try {
                projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                setMessage("An internal error occurred while reading the projects", IMessageProvider.ERROR);
                return new Object[0];
            }
            for (int i = 0; i < projects.length; i++) {
                try {
                    if (!IpsPlugin.getDefault().getMigrationOperation(projects[i]).isEmpty()) {
                        result.add(projects[i]);
                    }
                } catch (CoreException e) {
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

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            // nothing to do
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }
    }
}
