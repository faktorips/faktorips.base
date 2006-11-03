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
    
    private long modificationStamp;
    
    private boolean parsable = true;

    /**
     * 
     */
    public IpsSrcFileContent(IpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);
        this.ipsObject = ipsObject;
    }
    
    /**
     * @return Returns the ipsObject.
     */
    public IpsObject getIpsObject() {
        return ipsObject;
    }
    
    public void updateState(String xml, boolean modified) {
        try {
            String encoding = ipsObject.getIpsProject().getXmlFileCharset();
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding)); 
            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
            Document doc = builder.parse(is);
            is.close();
            ipsObject.initFromXml(doc.getDocumentElement());
            this.modified = modified;
            parsable = true;
        } catch (Exception e) {
            parsable = false;
        } finally {
            contentHasChanged();
        }
    }
    
    public void updateState(Element el, boolean modified) {
        try {
            ipsObject.initFromXml(el);
            this.modified = modified;
            parsable = true;
        } catch (Exception e) {
            parsable = false;
        } finally {
            contentHasChanged();
        }
    }
            
    public IpsSrcFile getIpsSrcFile() {
        return (IpsSrcFile)ipsObject.getIpsSrcFile();
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
            // change from modified to unmodified is also a change
            contentHasChanged();
        }
    }
    
    void markAsModified() {
        modified = true;
        contentHasChanged();
    }
    
    void contentHasChanged() {
        ((IpsModel)ipsObject.getIpsModel()).ipsSrcFileHasChanged(getIpsSrcFile());
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
        IpsSrcFile file = getIpsSrcFile();
        try {
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile(): About to read content from disk, file=" +  file
                        + ", Thead: " + Thread.currentThread().getName());
            }
            String fileContent = file.getContentFromEnclosingResource();
            String encoding = file.getIpsProject().getXmlFileCharset();
            ByteArrayInputStream is = new ByteArrayInputStream(fileContent.getBytes(encoding)); 
            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
            Document doc = builder.parse(is);
            is.close();
            modificationStamp = file.getCorrespondingFile().getModificationStamp();
            getIpsObject().initFromXml(doc.getDocumentElement());
            modified = false;
            parsable = true;
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile: Content read from disk, file=" +  file
                        + ", Thead: " + Thread.currentThread().getName());
            }
        } catch (Exception e) {
            parsable = false;
            IpsPlugin.log(new IpsStatus("Error reading xml doc from file " + file, e));
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile: Error reading content from disk, file=" +  file
                        + ", Thead: " + Thread.currentThread().getName());
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
                        System.out.println("IpsSrcFileContent.save() begin: " + IpsSrcFileContent.this);
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
                        System.out.println("IpsSrcFileContent.save() finished. ModStamp=" + modificationStamp + ", " + IpsSrcFileContent.this);
                    }
                } catch (Exception e) {
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() failed: " + IpsSrcFileContent.this);
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
        return "IpsSrcFileContent " + getIpsSrcFile();
    }
    
    
}
