/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.model.XmlSaxSupport;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.XsdValidationHandler;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.IpsSrcFileSaxHelper;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.util.BeanUtil;
import org.faktorips.devtools.model.util.DefaultLineSeparator;
import org.faktorips.runtime.internal.XmlUtil;
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

    private XsdValidationHandler xsdValidationHandler;

    /** Map containing the root properties of the source file. */
    private volatile Map<String, String> rootProperties = null;

    /** This flag is <code>true</code> if the content has been modified. */
    private volatile boolean modified = false;

    /**
     * The corresponding file's modification stamp at the time of reading the content from it.
     */
    private long modificationStamp;

    private boolean parsable = true;

    /** See <code>wasModStampTriggeredBySave()</code> for details. */
    private List<Long> modStampsAfterSave = null;

    /** Indicates if the IPS object is completely initialized with the IPS source file content. */
    private volatile boolean initialized = false;

    public IpsSrcFileContent(IpsObject ipsObject) {
        ArgumentCheck.notNull(ipsObject);
        this.ipsObject = ipsObject;
    }

    public IpsObject getIpsObject() {
        return ipsObject;
    }

    public void updateState(String xml, boolean newModified) {
        boolean wasModified = modified;

        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(xml.getBytes(encoding));
            Document doc = XmlUtil.getDocumentBuilder().parse(is);
            ipsObject.initFromXml(doc.getDocumentElement());
            modified = newModified;
            parsable = true;
            initializedFinished();
        } catch (IOException | SAXException e) {
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
     * Returns <code>true</code> IPS object was initialized with the IPS source file content.
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

    private void validateXMLSchema(Document document) throws Exception {
        IpsObjectType type = getIpsSrcFile().getIpsObjectType();

        Validator validator = org.faktorips.devtools.model.util.XmlUtil.getXsdValidator(type,
                getXsdValidationHandler());
        validator.validate(new DOMSource(document));
    }

    public synchronized void initContentFromFile() {
        setXsdValidationHandler(new XsdValidationHandler());
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
                DocumentBuilder builder = XmlUtil.getDocumentBuilder();
                Document doc = builder.parse(is);

                if (getIpsObject().getIpsProject().getReadOnlyProperties().isValidateIpsSchema()) {
                    validateXMLSchema(doc);
                }

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
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            parsable = false;
            IpsLog.log(new IpsStatus("Error reading file " + file, e)); //$NON-NLS-1$
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

    private synchronized void clearRootPropertyCache() {
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
    public synchronized void initRootPropertiesFromFile() {
        IIpsSrcFile file = getIpsSrcFile();
        try {
            clearRootPropertyCache();
            modificationStamp = file.getEnclosingResource().getModificationStamp();
            rootProperties = IpsSrcFileSaxHelper.getHeaderAttributes(file);
        } catch (IpsException e) {
            IpsLog.logAndShowErrorDialog(e);
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
            synchronized (this) {
                if (rootProperties == null) {
                    // Lazy load root properties.
                    initRootPropertiesFromFile();
                }
            }
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
            Object result = readMethod.invoke(ipsObject);
            return result == null ? null : "" + result; //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // due to documentation this method should return null in case of property does not
            // exists.
            return null;
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            IpsLog.log(e);
            return null;
        }
    }

    public void save(final IProgressMonitor monitor) {
        if (!modified) {
            return;
        }
        ICoreRunnable runnable = monitor1 -> {
            try {
                synchronized (IpsSrcFileContent.this) {
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() begin: " + IpsSrcFileContent.this); //$NON-NLS-1$
                    }
                    Document doc = XmlUtil.getDocumentBuilder().newDocument();
                    IIpsProject ipsProject = ipsObject.getIpsProject();
                    String encoding = ipsProject.getXmlFileCharset();
                    Element xml = getIpsObject().toXml(doc);
                    org.faktorips.devtools.model.util.XmlUtil.removeIds(xml);
                    String newXml = XmlUtil.nodeToString(xml, encoding, DefaultLineSeparator.of(ipsProject),
                            ipsProject.getReadOnlyProperties().isEscapeNonStandardBlanks());
                    ByteArrayInputStream is = new ByteArrayInputStream(newXml.getBytes(encoding));
                    AFile file = ipsObject.getIpsSrcFile().getCorrespondingFile();
                    file.setContents(is, true, monitor1);
                    if (getIpsObject().getIpsProject().getReadOnlyProperties().isValidateIpsSchema()) {
                        getXsdValidationHandler().clear();
                    }
                    modificationStamp = file.getModificationStamp();
                    if (modStampsAfterSave == null) {
                        modStampsAfterSave = new ArrayList<>(1);
                    }
                    modStampsAfterSave.add(Long.valueOf(modificationStamp));
                    markAsUnmodified();
                    if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                        System.out.println("IpsSrcFileContent.save() finished. ModStamp=" + modificationStamp + ", " //$NON-NLS-1$ //$NON-NLS-2$
                                + IpsSrcFileContent.this);
                    }
                    clearRootPropertyCache();
                    if (ipsObject instanceof ProductCmpt productCmpt && productCmpt.isProductTemplate()) {
                        updateTemplatedProductCmpts(productCmpt);
                    }
                }
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("IpsSrcFileContent.save() failed: " + IpsSrcFileContent.this); //$NON-NLS-1$
                }
                throw new CoreException(new IpsStatus(e));
            }
        };
        Abstractions.getWorkspace().run(runnable, monitor);
    }

    private void updateTemplatedProductCmpts(ProductCmpt productCmpt) {
        productCmpt.getIpsProject()
                .findTemplateHierarchy(productCmpt)
                .getAllElements().stream()
                .forEach(srcFile -> {
                    boolean dirty = srcFile.isDirty();
                    ProductCmpt templatedProductCmpt = (ProductCmpt)srcFile.getIpsObject();
                    IPropertyValueContainerToTypeDelta delta = templatedProductCmpt
                            .computeDeltaToModel(srcFile.getIpsProject());
                    fixTemplateDifferences(delta);
                    if (!dirty) {
                        srcFile.save(null);
                    }
                });
    }

    private void fixTemplateDifferences(IFixDifferencesComposite delta) {
        if (delta instanceof IPropertyValueContainerToTypeDelta propertyValueContainerToTypeDelta) {
            IDeltaEntry[] entries = propertyValueContainerToTypeDelta.getEntries(DeltaType.INHERITED_TEMPLATE_MISMATCH);
            for (IDeltaEntry deltaEntry : entries) {
                deltaEntry.fix();
            }
        }
        for (IFixDifferencesComposite child : delta.getChildren()) {
            fixTemplateDifferences(child);
        }
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

    public XsdValidationHandler getXsdValidationHandler() {
        return xsdValidationHandler;
    }

    public void setXsdValidationHandler(XsdValidationHandler xsdValidationHandler) {
        this.xsdValidationHandler = xsdValidationHandler;
    }
}
