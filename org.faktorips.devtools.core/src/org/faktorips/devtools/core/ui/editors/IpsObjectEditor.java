/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;


/**
 *
 */
public abstract class IpsObjectEditor extends FormEditor 
	implements ContentsChangeListener, IPartListener {

    // the file that's being edited (if any)
    private IIpsSrcFile ipsSrcFile;
    
    // dirty flag
    private boolean dirty = false;
    
    /**
     * 
     */
    public IpsObjectEditor() {
        super();
    }
    
    protected IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }
    
    public IIpsObject getIpsObject() {
        try {
            return getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the title that is shown on every page.
     */
    protected abstract String getUniformPageTitle();

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            IIpsModel model = IpsPlugin.getDefault().getIpsModel();
            ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
            model.addChangeListener(this);
            setPartName(ipsSrcFile.getName());
            site.getPage().addPartListener(this);
        }
    }
    
    /** 
     * Overridden.
     */
    protected void setActivePage(int pageIndex) {
        super.setActivePage(pageIndex);
        refresh();
    }
    
    /** 
     * Overridden.
     */
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        refresh();
    }
    
    /**
     * Refresh the controls on the active page with the data from the model.
     */
    protected void refresh() {
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
        	IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
        	page.refresh();
        }
    }
    
    /**
     *  
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ContentsChangeListener#contentsChanged(org.faktorips.devtools.core.model.ContentChangeEvent)
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (!ipsSrcFile.equals(event.getIpsSrcFile())) {
            return;
        }
        setDirty(ipsSrcFile.isDirty());
        refresh();
    }
    
    protected void setDirty(boolean newValue) {
        if (dirty==newValue) {
            return;
        }
        dirty = newValue;
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /** 
     * Overridden.
     */
    public void doSave(IProgressMonitor monitor) {
        try {
            ipsSrcFile.save(true, monitor);    
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        refresh();
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        // nothing to do
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        if (part==this) {
            ipsSrcFile.discardChanges();
            part.getSite().getPage().removePartListener(this);
        }
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
        // nothing to do
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
        // nothing to do
    }
}
