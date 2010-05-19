/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ElementSelectionPage extends WizardPage {
    private Set<? extends Object> ipsElementsToFix;
    private CheckboxTreeViewer treeViewer;
    private ContentProvider contentProvider;

    public ElementSelectionPage(Set<? extends Object> ipsElementsToFix) {
        super(Messages.ElementSelectionPage_SelectElementsMessage);
        this.ipsElementsToFix = ipsElementsToFix;
        setTitle(Messages.FixDifferencesToModelWizard_Title);
        if (!(ipsElementsToFix.size() > 0)) {
            this.setMessage(Messages.ElementSelectionPage_ElementSelectionPage_NoElementsFoundMessage);
        } else {
            this.setMessage(Messages.ElementSelectionPage_SelectElementsMessage);
        }
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
        contentProvider = new ContentProvider();
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$

        treeViewer.setExpandedElements(contentProvider.getElements(null));
        treeViewer.setCheckedElements(contentProvider.getAllElements());

        treeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object element = event.getElement();
                if (element instanceof IIpsPackageFragment) {
                    treeViewer.setSubtreeChecked(element, treeViewer.getChecked(element));
                    treeViewer.setGrayed(element, false);
                } else {
                    Object parent = contentProvider.getParent(element);
                    boolean checked = treeViewer.getChecked(element);
                    Object[] children = contentProvider.getChildren(parent);
                    treeViewer.setGrayed(parent, false);
                    for (Object element2 : children) {
                        boolean checked2 = treeViewer.getChecked(element2);
                        if (checked2 != checked) {
                            treeViewer.setGrayed(parent, true);
                        }
                        checked = checked | checked2;
                    }
                    treeViewer.setChecked(parent, checked);
                }
                setPageComplete(getElementsToFix().length > 0);
            }

        });
        setPageComplete(getElementsToFix().length > 0);
        super.setControl(root);
    }

    /**
     * @return all IIpsElements to be fixed.
     */
    protected IFixDifferencesToModelSupport[] getElementsToFix() {
        Object[] checked = treeViewer.getCheckedElements();
        Set<Object> elements = new HashSet<Object>();
        for (Object element : checked) {
            if (element instanceof IFixDifferencesToModelSupport) {
                elements.add(element);
            }
        }
        return elements.toArray(new IFixDifferencesToModelSupport[elements.size()]);
    }

    private class TreeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return ((IIpsElement)element).getName();
        }

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsElement)element);
        }
    }

    private class ContentProvider implements ITreeContentProvider {

        private Map<IIpsPackageFragment, Set<Object>> packages;
        private Object[] allElements;

        ContentProvider() {
            Object[] elements = ipsElementsToFix.toArray();
            packages = new HashMap<IIpsPackageFragment, Set<Object>>();
            for (Object object : elements) {
                if (object instanceof IpsObject) {
                    IIpsPackageFragment pack = ((IpsObject)object).getIpsPackageFragment();
                    Set<Object> children = packages.get(pack);
                    if (children == null) {
                        children = new HashSet<Object>();
                    }
                    children.add(object);
                    packages.put(pack, children);
                }
            }
            allElements = new Object[elements.length + packages.size()];
            System.arraycopy(elements, 0, allElements, 0, elements.length);
            int i = elements.length;
            for (Iterator<IIpsPackageFragment> it = packages.keySet().iterator(); it.hasNext();) {
                Object pack = it.next();
                allElements[i++] = pack;
            }
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

        private Object[] getAllElements() {
            return allElements;
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
