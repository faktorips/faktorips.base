/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.Set;

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
import org.faktorips.devtools.core.model.FixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ElementSelectionPage extends WizardPage {
    private Set ipsElementsToFix;
    private CheckboxTreeViewer treeViewer;

    public ElementSelectionPage(Set ipsElementsToFix) {
        super(Messages.ElementSelectionPage_SelectElementsMessage);
        this.ipsElementsToFix = ipsElementsToFix;
        this.setTitle(Messages.FixDifferencesToModelWizard_Title);
        this.setMessage(Messages.ElementSelectionPage_SelectElementsMessage);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer = new CheckboxTreeViewer(root);
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$
        
        treeViewer.setCheckedElements(ipsElementsToFix.toArray());
        
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(getElementsToFix().length > 0);
            }
        });
        setPageComplete(false);
        super.setControl(root);
    }



    /**
     * @return all IIpsElements to be fixed.
     */
    protected FixDifferencesToModelSupport[] getElementsToFix() {
        Object[] checked = treeViewer.getCheckedElements();
        FixDifferencesToModelSupport[] elements = new FixDifferencesToModelSupport[checked.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = (FixDifferencesToModelSupport)checked[i];
        }
        return elements;
    }
    
    private class TreeLabelProvider extends LabelProvider {

        public String getText(Object element) {
            return ((IIpsElement)element).getName();
        }

        public Image getImage(Object element) {
            return ((IIpsElement)element).getImage();
        }
    }

    private class ContentProvider implements ITreeContentProvider {

        /**
         * {@inheritDoc}
         */
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        public Object getParent(Object element) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasChildren(Object element) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            return ipsElementsToFix.toArray();
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
            // nothing to do
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }
    }
}
