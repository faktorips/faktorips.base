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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFileContent {

    private IpsObject ipsObject;

    // true if the content has been modified.
    private boolean modified = false;
    
    // the corresponding file's modification stamp at the time of reading
    // the content from it.
    private long modificationStamp;
    
    private boolean parsable = true;

    public IpsSrcFileContent(IpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);
        this.ipsObject = ipsObject;
    }
    
    public IpsObject getIpsObject() {
        return ipsObject;
    }
    
    public void updateState(String xml, boolean newModified) {
        boolean wasModified = newModified;
        try {
            String encoding = ipsObject.getIpsProject().getXmlFileCharset();
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding)); 
            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
            Document doc = builder.parse(is);
            is.close();
            ipsObject.initFromXml(doc.getDocumentElement());
            this.modified = newModified;
            parsable = true;
        } catch (Exception e) {
            parsable = false;
        } finally {
            this.modified = wasModified; // set back modified flag
            setModified(newModified); // and use setter to trigger notification
            wholeContentChanged();
        }
    }
    
    public void updateState(Element el, boolean modified) {
        try {
            ipsObject.initFromXml(el);
            setModified(modified);
            parsable = true;
        } catch (Exception e) {
            parsable = false;
        } finally {
            wholeContentChanged();
        }
    }
            
    public IIpsSrcFile getIpsSrcFile() {
        return ipsObject.getIpsSrcFile();
    }

    /**
     * @return Returns the modified.
     */
    public boolean isModified() {
        return modified;
    }

    void setModified(boolean flag) {
        if (flag) {
            markAsModified();
        } else {
            markAsUnmodified();
        }
    }
    
    void markAsUnmodified() {
        if (modified==true) { 
            modified = false;
            modificationStatusHasChanged();
        }
    }
    
    void markAsModified() {
        if (!modified) {
            modified = true;
            modificationStatusHasChanged();
        }
    }
    
    private void modificationStatusHasChanged() {
        ModificationStatusChangedEvent event = new ModificationStatusChangedEvent(getIpsSrcFile());
        ((IpsModel)ipsObject.getIpsModel()).notifyModificationStatusChangeListener(event);
    }
    
    private void wholeContentChanged() {
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        ((IpsModel)ipsObject.getIpsModel()).ipsSrcFileContentHasChanged(event);
    }

    public void ipsObjectChanged(ContentChangeEvent event) {
        markAsModified();
        ((IpsModel)ipsObject.getIpsModel()).ipsSrcFileContentHasChanged(event);
    }

    /**
     * @return Returns the modStamp.
     */
    public long getModificationStamp() {
        return modificationStamp;
    }
    
    /**
     * @param modStamp The modStamp to set.
     */
    public void setModificationStamp(long modStamp) {
        this.modificationStamp = modStamp;
    }

    /**
     * @return Returns the parsable.
     */
    public boolean isParsable() {
        return parsable;
    }

    /**
     * @param parsable The parsable to set.
     */
    public void setParsable(boolean parsable) {
        this.parsable = parsable;
    }
    
    public void discardChanges() {
        if (!modified) {
            return;
        }
        initContentFromFile();
    }
    
    public void initContentFromFile() {
        IIpsSrcFile file = getIpsSrcFile();
        try {
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile(): About to read content from disk, file=" +  file //$NON-NLS-1$
                        + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
            }
            String fileContent = file.getContentFromEnclosingResource();
            String encoding = file.getIpsProject().getXmlFileCharset();
            ByteArrayInputStream is = new ByteArrayInputStream(fileContent.getBytes(encoding)); 
            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
            Document doc = builder.parse(is);
            is.close();
            modificationStamp = file.getEnclosingResource().getModificationStamp();
            getIpsObject().initFromXml(doc.getDocumentElement());
            modified = false;
            parsable = true;
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile: Content read from disk, file=" +  file //$NON-NLS-1$
                        + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
            }
        } catch (Exception e) {
            parsable = false;
            IpsPlugin.log(new IpsStatus("Error reading xml doc from file " + file, e)); //$NON-NLS-1$
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile: Error reading content from disk, file=" +  file //$NON-NLS-1$
                        + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
            }
        }
    }
    
    public void save(final boolean force, final IProgressMonitor monitor) throws CoreException {
        if (!modified) {
            return;
        }
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() begin: " + IpsSrcFileContent.this); //$NON-NLS-1$
                    }
                    Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
                    String encoding = ipsObject.getIpsProject().getXmlFileCharset();
                    String newXml = XmlUtil.nodeToString(getIpsObject().toXml(doc), encoding);
                    ByteArrayInputStream is = new ByteArrayInputStream(newXml.getBytes(encoding));
                    IFile file = ipsObject.getIpsSrcFile().getCorrespondingFile();
                    file.setContents(is, force, true, monitor);
                    modificationStamp = file.getModificationStamp();
                    markAsUnmodified();
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() finished. ModStamp=" + modificationStamp + ", " + IpsSrcFileContent.this); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (Exception e) {
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() failed: " + IpsSrcFileContent.this); //$NON-NLS-1$
                    }
                    throw new CoreException(new IpsStatus(e));
                }
            }
            
        };
        ResourcesPlugin.getWorkspace().run(runnable, monitor);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "IpsSrcFileContent " + getIpsSrcFile(); //$NON-NLS-1$
    }
    
    
}
