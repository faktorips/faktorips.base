/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.MultiMap;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ElementSelectionPage extends WizardPage {
    private Set<IFixDifferencesToModelSupport> ipsElementsToFix;
    private CheckboxTreeViewer treeViewer;
    private ContentProvider contentProvider;

    public ElementSelectionPage(Set<IFixDifferencesToModelSupport> ipsElementsToFix) {
        super(Messages.ElementSelectionPage_SelectElementsMessage);
        this.ipsElementsToFix = ipsElementsToFix;
        setTitle(Messages.FixDifferencesToModelWizard_Title);
        if (!(ipsElementsToFix.size() > 0)) {
            this.setMessage(Messages.ElementSelectionPage_ElementSelectionPage_NoElementsFoundMessage);
        } else {
            this.setMessage(Messages.ElementSelectionPage_SelectElementsMessage);
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer = new CheckboxTreeViewer(root);
        contentProvider = new ContentProvider(ipsElementsToFix);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$

        treeViewer.setExpandedElements(contentProvider.getElements(null));
        treeViewer.setCheckedElements(contentProvider.getAllElements());

        treeViewer.addCheckStateListener(event -> {
            Object element = event.getElement();
            if (element instanceof IIpsPackageFragment) {
                treeViewer.setSubtreeChecked(element, treeViewer.getChecked(element));
                treeViewer.setGrayed(element, false);
            } else {
                Object parent1 = contentProvider.getParent(element);
                boolean checked = treeViewer.getChecked(element);
                Object[] children = contentProvider.getChildren(parent1);
                treeViewer.setGrayed(parent1, false);
                for (Object element2 : children) {
                    boolean checked2 = treeViewer.getChecked(element2);
                    if (checked2 != checked) {
                        treeViewer.setGrayed(parent1, true);
                    }
                    checked = checked || checked2;
                }
                treeViewer.setChecked(parent1, checked);
            }
            setPageComplete(getElementsToFix().size() > 0);
        });
        setPageComplete(getElementsToFix().size() > 0);
        super.setControl(root);
    }

    /**
     * @return all IIpsElements to be fixed.
     */
    protected Set<IFixDifferencesToModelSupport> getElementsToFix() {
        Object[] checked = treeViewer.getCheckedElements();
        Set<IFixDifferencesToModelSupport> elements = new HashSet<>();
        for (Object element : checked) {
            if (element instanceof IFixDifferencesToModelSupport) {
                elements.add((IFixDifferencesToModelSupport)element);
            }
        }
        return elements;
    }

    private static class TreeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return ((IIpsElement)element).getName();
        }

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)element);
        }
    }

    private static class ContentProvider implements ITreeContentProvider {

        private MultiMap<IIpsPackageFragment, IFixDifferencesToModelSupport> packages;

        ContentProvider(Set<IFixDifferencesToModelSupport> ipsElementsToFix) {
            packages = MultiMap.createWithLinkedSetAsValues();
            for (IFixDifferencesToModelSupport fixDifferenceObject : ipsElementsToFix) {
                IIpsPackageFragment pack = fixDifferenceObject.getIpsSrcFile().getIpsPackageFragment();
                packages.put(pack, fixDifferenceObject);
            }
        }

        public Object[] getAllElements() {
            ArrayList<Object> result = new ArrayList<>(packages.keySet());
            result.addAll(packages.values());
            return result.toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IIpsPackageFragment) {
                return (packages.get(parentElement)).toArray();
            }
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getParent(Object element) {
            if (element instanceof IpsObject) {
                return ((IpsObject)element).getIpsPackageFragment();
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof IIpsPackageFragment) {
                return packages.get(element) != null;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object inputElement) {
            return packages.keySet().toArray();
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
