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
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ElementSelectionPage extends WizardPage {
    private Set ipsElementsToFix;
    private CheckboxTreeViewer treeViewer;
    private ContentProvider contentProvider;

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
        contentProvider = new ContentProvider();
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$

        treeViewer.setExpandedElements(contentProvider.getElements(null));
        treeViewer.setCheckedElements(contentProvider.getAllElements());
        
        treeViewer.addCheckStateListener(new ICheckStateListener(){

            public void checkStateChanged(CheckStateChangedEvent event) {
                Object element = event.getElement();
                if(element instanceof IIpsPackageFragment){
                    treeViewer.setSubtreeChecked(element, treeViewer.getChecked(element));
                    treeViewer.setGrayed(element, false);
                }else{
                    Object parent = contentProvider.getParent(element);
                    boolean checked = treeViewer.getChecked(element);
                    Object[] children = contentProvider.getChildren(parent);
                    treeViewer.setGrayed(parent, false);
                    for (int i = 0; i < children.length; i++) {
                        boolean checked2 = treeViewer.getChecked(children[i]);
                        if(checked2!=checked){
                            treeViewer.setGrayed(parent, true);
                        }
                        checked = checked | checked2;
                    }
                    treeViewer.setChecked(parent, checked);
                }
                setPageComplete(getElementsToFix().length > 0);
            }
            
        });
        setPageComplete(false);
        super.setControl(root);
    }



    /**
     * @return all IIpsElements to be fixed.
     */
    protected IFixDifferencesToModelSupport[] getElementsToFix() {
        Object[] checked = treeViewer.getCheckedElements();
        Set elements = new HashSet();
        for (int i = 0; i < checked.length; i++) {
            if(checked[i] instanceof IFixDifferencesToModelSupport){
                elements.add((IFixDifferencesToModelSupport)checked[i]);
            }
        }
        return (IFixDifferencesToModelSupport[])elements.toArray(new IFixDifferencesToModelSupport[elements.size()]);
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

        private Map packages;
        private Object[] allElements;
        
        ContentProvider(){
            Object[] elements = ipsElementsToFix.toArray();
            packages = new HashMap();
            for (int i = 0; i < elements.length; i++) {
                Object object = elements[i];
                if(object instanceof IpsObject){
                    IIpsPackageFragment pack = ((IpsObject) object).getIpsPackageFragment();
                    Set children = (Set)packages.get(pack);
                    if(children==null){
                        children = new HashSet();
                    }
                    children.add(object);
                    packages.put(pack, children);
                }
            }
            allElements = new Object[elements.length+packages.size()];
            System.arraycopy(elements, 0, allElements, 0, elements.length);
            int i = elements.length;
            for (Iterator it = packages.keySet().iterator(); it.hasNext();) {
                Object pack = (Object)it.next();
                allElements[i++] = pack;
            }    
        }

        /**
         * {@inheritDoc}
         */
        public Object[] getChildren(Object parentElement) {
            if(parentElement instanceof IIpsPackageFragment){
                return ((Set)packages.get(parentElement)).toArray();
            }
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        public Object getParent(Object element) {
            if(element instanceof IpsObject){
                return ((IpsObject) element).getIpsPackageFragment();
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasChildren(Object element) {
            if(element instanceof IIpsPackageFragment){
                return packages.get(element)!=null;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            return packages.keySet().toArray();
        }
        
        private Object[] getAllElements(){
            return allElements;
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
