/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Action for expanding and collapsing all folders in a given treeViewer. When run, this action will 
 * expand (or collapse respectively) the given treeviewer and toggles its state. The image an the 
 * tooltiptext are changed in this process too. After instanciation this action will first expand,
 * after running once it will collapse the tree, and so on.
 * 
 * @author Stefan Widmaier
 */
public class ExpandCollapseAllAction extends Action {
    
    /**
     * The TreeViewer to be expanded or collapsed.
     */
    private AbstractTreeViewer treeViewer;
    /**
     * Status flag that defines the next action to be performed (expand or collapse) as well as
     * the image and the tooltiptext this action possesses at the moment.
     * <p>
     * Default value is <code>true</code> denoting that the next call of run() will expand.
     */
    private boolean expand= true;
    
    public ExpandCollapseAllAction(AbstractTreeViewer treeViewer){
        this.treeViewer= treeViewer;
    }
    
    /**
     * Expands or collapses the given Treeviewer, toggles this action's state and changes its image
     * and its tooltiptext accordingly.
     * {@inheritDoc}
     */
    public void run() {
        if(expand){
            treeViewer.expandAll();
        }else{
            treeViewer.collapseAll();
        }
        expand= !expand;
        setImageDescriptor(getImageDescriptor());
        setToolTipText(getToolTipText());
    }

    /**
     * If this action is in expansion-state the imagedescriptor for the expandAll icon,
     * if it is in collapse-state the imagedescriptor for the collapseAll icon will be returned.
     */
    public ImageDescriptor getImageDescriptor() {
        if(expand){
            return IpsPlugin.getDefault().getImageDescriptor("ExpandAll.gif"); //$NON-NLS-1$
        }else{
            return IpsPlugin.getDefault().getImageDescriptor("CollapseAll.gif"); //$NON-NLS-1$
        }
    }
    /**
     * If this action is in expansion-state "Expand All", if it is in collapse-state 
     * "Collapse All" will be returned.
     */
    public String getToolTipText() {
        if(expand){
            return Messages.ExpandCollapseAllAction_Expand_Description;
        }else{
            return Messages.ExpandCollapseAllAction_Collapse_Description;
        }
    }
}
