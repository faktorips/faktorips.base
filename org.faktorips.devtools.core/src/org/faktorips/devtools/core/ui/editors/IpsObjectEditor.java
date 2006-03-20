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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;


/**
 *
 */
public abstract class IpsObjectEditor extends FormEditor 
	implements ContentsChangeListener, IPartListener, IResourceChangeListener {

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
        IIpsModel model = IpsPlugin.getDefault().getIpsModel();
        if (input instanceof IFileEditorInput) {
        	IFile file = ((IFileEditorInput)input).getFile();
            ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
            setPartName(ipsSrcFile.getName());
        } else if (input instanceof IStorageEditorInput) {
        	initFromStorageEditorInput((IStorageEditorInput)input);
        	setPartName(((IStorageEditorInput)input).getName());
    	}
        
        if (ipsSrcFile == null) {
        	throw new PartInitException("Unsupported editor input type " + input.getClass().getName());
        }
        
        model.addChangeListener(this);    	
        site.getPage().addPartListener(this);            
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }
    
    private void initFromStorageEditorInput(IStorageEditorInput input) throws PartInitException{
    	try {
    		IStorage storage = input.getStorage();
    		IPath path = storage.getFullPath();
    		if (path == null) {
    			return;
    		}
    		
    		int nameIndex = path.lastSegment().indexOf(IpsObjectType.PRODUCT_CMPT.getFileExtension());
    		
    		if (nameIndex == -1) {
    			return;
    		}
    		String name = path.lastSegment().substring(0, nameIndex) + IpsObjectType.PRODUCT_CMPT.getFileExtension();
    		path = path.removeLastSegments(1).append(name);
    		
    		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    		while (!file.exists() && path.segmentCount() > 0) {
    			path = path.removeFirstSegments(1);
        		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    		}

    		if (!file.exists()) {
    			return;
    		}

    		path = path.removeLastSegments(1);
    		IIpsElement el = IpsPlugin.getDefault().getIpsModel().getIpsElement(file.getParent());
    		
    		if (el instanceof IIpsPackageFragment) {
    			InputStream in = storage.getContents();
    			ipsSrcFile = ((IIpsPackageFragment)el).createIpsFile(input.getName(), storage.getContents(), true, null);
    			ResourceAttributes attrs = new ResourceAttributes();
    			attrs.setReadOnly(true);
    			ipsSrcFile.getCorrespondingFile().setResourceAttributes(attrs);
    			in.close();
    		}
    		
    	} catch (CoreException e) {
    		throw new PartInitException(e.getStatus());
    	} catch (Exception e) {
    		IpsPlugin.log(e);
    		throw new PartInitException(e.getMessage());
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
    	if (part != this) {
    		return;
    	}

    	refresh();
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
    	if (part != this) {
    		return;
    	}

    	if (!ipsSrcFile.getCorrespondingFile().isSynchronized(IResource.DEPTH_ONE)) {
    		String msg = NLS.bind(Messages.IpsObjectEditor_msgResourceOutOfSync, ipsSrcFile.getName());
    		if (isDirty()) {
    			msg += Messages.IpsObjectEditor_msgOutOfSyncOptions;	
        		boolean ok = MessageDialog.openQuestion(super.getSite().getShell(), Messages.IpsObjectEditor_msgOutOfSyncTitle, msg);
        		if (ok) {
            		this.close(false);
        		} else {
        			doSave(null);
        		}
    		}
    		else {
    			msg += Messages.IpsObjectEditor_msgEditorWillBeClosed;
    			MessageDialog.openError(super.getSite().getShell(), Messages.IpsObjectEditor_msgOutOfSyncTitle, msg);
    			this.close(false);
    		}
    		
    	}
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

    /**
     * We have to close the editor if the underlying resource is removed.
     * {@inheritDoc}
     */
	public void resourceChanged(IResourceChangeEvent event) {
		if (!ipsSrcFile.exists()) {
			this.close(false);
		}
	}

	/**
	 * Returns <code>true</code> if the <code>IIpsSrcFile</code> this editor is based on exists
	 * and is in sync.
	 */
	protected boolean isSrcFileUsable() {
		return ipsSrcFile != null && ipsSrcFile.exists() && ipsSrcFile.getCorrespondingFile().isSynchronized(IResource.DEPTH_ONE);
	}

}
