/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.XmlSaxSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsSrcFileSaxHelper;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.devtools.core.util.EclipseIOUtil;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFileContent {

    private final IpsObject ipsObject;

    /** Map containing the root properties of the source file. */
    private Map<String, String> rootProperties = null;

    /** This flag is <tt>true</tt> if the content has been modified. */
    private boolean modified = false;

    /**
     * The corresponding file's modification stamp at the time of reading the content from it.
     */
    private long modificationStamp;

    private boolean parsable = true;

    /** See <tt>wasModStampTriggeredBySave()</tt> for details. */
    private List<Long> modStampsAfterSave = null;

    /** Indicates if the ips object is completely initialized with the ips source file content. */
    private boolean initialized = false;

    public IpsSrcFileContent(IpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);
        this.ipsObject = ipsObject;
    }

    public IpsObject getIpsObject() {
        return ipsObject;
    }

    public void updateState(String xml, boolean newModified) {
        boolean wasModified = newModified;

        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(xml.getBytes(encoding));
            Document doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
            ipsObject.initFromXml(doc.getDocumentElement());
            modified = newModified;
            parsable = true;
            initializedFinished();
        } catch (IOException e) {
            IoUtil.close(is);
            parsable = false;
            ipsObject.markAsFromUnparsableFile();
        } catch (SAXException e) {
            IoUtil.close(is);
            parsable = false;
            ipsObject.markAsFromUnparsableFile();
        } finally {
            // reset modified flag
            modified = wasModified;
            // and use setter to trigger notification
            setModified(newModified);
            wholeContentChanged();
        }
    }

    public void updateState(Element el, boolean modified) {
        ipsObject.initFromXml(el);
        setModified(modified);
        parsable = true;
        initializedFinished();
        wholeContentChanged();
    }

    public IIpsSrcFile getIpsSrcFile() {
        return ipsObject.getIpsSrcFile();
    }

    /**
     * Returns <code>true</code> ips object was initialized with the ips source file content.
     * Returns <code>false</code> if the content wasn't read from the source file.
     */
    public boolean isInitialized() {
        return initialized;
    }

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

    public void markAsUnmodified() {
        if (modified) {
            modified = false;
            modificationStatusHasChanged();
        }
    }

    public void markAsModified() {
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

    public long getModificationStamp() {
        return modificationStamp;
    }

    public void setModificationStamp(long modStamp) {
        modificationStamp = modStamp;
    }

    public boolean isParsable() {
        return parsable;
    }

    public void setParsable(boolean parsable) {
        this.parsable = parsable;
    }

    public void discardChanges() {
        if (!modified) {
            return;
        }
        markAsUnmodified();
        initContentFromFile();
        wholeContentChanged();
    }

    public void initContentFromFile() {
        IIpsSrcFile file = getIpsSrcFile();
        InputStream is = null;
        try {
            long startTime = 0;
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println(
                        "IpsSrcFileContent.initContentFromFile(): About to read content from disk, file=" + file //$NON-NLS-1$
                                + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
                startTime = System.currentTimeMillis();
            }
            if (ipsObject instanceof XmlSaxSupport) {
                is = file.getContentFromEnclosingResource();
                ((XmlSaxSupport)ipsObject).initFromInputStream(is);
            } else {
                is = file.getContentFromEnclosingResource();
                DocumentBuilder builder = IpsPlugin.getDefault().getDocumentBuilder();
                Document doc = builder.parse(is);
                ipsObject.initFromXml(doc.getDocumentElement());
            }
            modificationStamp = file.getEnclosingResource().getModificationStamp();
            modified = false;
            parsable = true;
            initializedFinished();
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("IpsSrcFileContent.initContentFromFile: Content read from disk, durration: " //$NON-NLS-1$
                        + (System.currentTimeMillis() - startTime) + ", file=" + file //$NON-NLS-1$
                        + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
            }
        } catch (Exception e) {
            parsable = false;
            IpsPlugin.log(new IpsStatus("Error reading file " + file, e)); //$NON-NLS-1$
            ipsObject.markAsFromUnparsableFile();
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out
                        .println("IpsSrcFileContent.initContentFromFile: Error reading content from disk, file=" + file //$NON-NLS-1$
                                + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
            }
        } finally {
            IoUtil.close(is);
        }
    }

    private void clearRootPropertyCache() {
        rootProperties = null;
    }

    /**
     * Indicates that the initialization was finished.
     */
    private void initializedFinished() {
        initialized = true;
        clearRootPropertyCache();
    }

    /**
     * Reads and stores all root properties of the corresponding source file.
     */
    public void initRootPropertiesFromFile() {
        IIpsSrcFile file = getIpsSrcFile();
        try {
            clearRootPropertyCache();
            modificationStamp = file.getEnclosingResource().getModificationStamp();
            rootProperties = IpsSrcFileSaxHelper.getHeaderAttributes(file);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns <code>true</code> if the root properties are read from the source file. Returns
     * <code>false</code> if the root properties are not read.
     */
    public boolean areRootPropertiesAvailable() {
        return rootProperties != null || initialized;
    }

    /**
     * Returns the given root property value. Returns <code>null</code> if the given root property
     * wasn't found.
     */
    public String getRootPropertyValue(String propertyName) {
        if (initialized) {
            return getPropertyFromIpsObject(propertyName);
        }
        if (rootProperties == null) {
            // Lazy load root properties.
            initRootPropertiesFromFile();
        }
        /*
         * rootProperties could be null if the workspace is out of sync and the file doesn't exist
         * anymore.
         */
        return (rootProperties != null ? rootProperties.get(propertyName) : null);
    }

    private String getPropertyFromIpsObject(String propertyName) {
        try {
            PropertyDescriptor propertyDescriptor = BeanUtil.getPropertyDescriptor(ipsObject.getClass(), propertyName);
            Method readMethod = propertyDescriptor.getReadMethod();
            Object result = readMethod.invoke(ipsObject, new Object[0]);
            return result == null ? null : "" + result; //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // due to documentation this method should return null in case of property does not
            // exists.
            return null;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    public void save(final boolean force, final IProgressMonitor monitor) throws CoreException {
        if (!modified) {
            return;
        }
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() begin: " + IpsSrcFileContent.this); //$NON-NLS-1$
                    }
                    Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
                    String encoding = ipsObject.getIpsProject().getXmlFileCharset();
                    String newXml = XmlUtil.nodeToString(getIpsObject().toXml(doc), encoding);
                    ByteArrayInputStream is = new ByteArrayInputStream(newXml.getBytes(encoding));
                    IFile file = ipsObject.getIpsSrcFile().getCorrespondingFile();
                    EclipseIOUtil.writeToFile(file, is, force, true, monitor);
                    modificationStamp = file.getModificationStamp();
                    if (modStampsAfterSave == null) {
                        modStampsAfterSave = new ArrayList<Long>(1);
                    }
                    modStampsAfterSave.add(Long.valueOf(modificationStamp));
                    markAsUnmodified();
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() finished. ModStamp=" + modificationStamp + ", " //$NON-NLS-1$ //$NON-NLS-2$
                                + IpsSrcFileContent.this);
                    }
                    clearRootPropertyCache();
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
     * Returns <code>true</code> if the given mod stamp is the result of saving the content to disk
     * via the save() method. The method should be called by the IpsModel's resource delta visitor
     * only!
     */
    public boolean wasModStampCreatedBySave(long modStamp) {
        if (modStampsAfterSave == null) {
            return false;
        }
        boolean rc = modStampsAfterSave.remove(Long.valueOf(modStamp));
        if (modStampsAfterSave.size() == 0) {
            modStampsAfterSave = null;
        }
        return rc;
    }

    @Override
    public String toString() {
        return "IpsSrcFileContent " + getIpsSrcFile(); //$NON-NLS-1$
    }

}
