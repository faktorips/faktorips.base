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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * The ips source file's content.  
 */
class IpsSourceFileContents {
    
    // handle to the file this is the content of.
    private IIpsSrcFile ipsSrcFile;
    
    // the source text (the "real" content")
    private String sourceText = null;
    
    // the IpsObject the content represents
    private IIpsObject ipsObject = null;
    
    // TRUE if the source text has been successfully parsed.
    // FALSE if the source text couldn't be parsed. 
    // null, if the source text hasn't been parsed yet. 
    private Boolean contentParsable = null;
    
    // true if the content has been modified.
    private boolean modified = false;
    
    // the text's encoding
    private String encoding;

    IpsSourceFileContents(IIpsSrcFile file, String sourceText, String encoding) {
        this.ipsSrcFile = file;
        this.sourceText = sourceText;
        this.encoding = encoding;
    }
    
    String getSourceText() {
        return sourceText;
    }
    
    void setSourceText(String newSourceText, boolean forceReparse) {
        this.sourceText = newSourceText;
        if (forceReparse) {
            contentParsable = null;
        }
        markAsModified();
    }
    
    void updateStateFromMemento(IIpsSrcFileMemento memento) throws CoreException {
        if (!memento.getIpsSrcFile().equals(this.ipsSrcFile)) {
            throw new CoreException(new IpsStatus(this + ": Memento " + memento + " is from different object.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sourceText = memento.getContents();
        contentParsable = null;
        modified = memento.isDirty();
        notifyChangeListeners();
    }
    
    /**
     * Returns true if the content has been modified since object creation or
     * the last call to <code>markAsUnmodified</code>.
     */
    boolean modified() {
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
            notifyChangeListeners();
        }
    }
    
    void markAsModified() {
        modified = true;
        notifyChangeListeners();
    }
    
    void notifyChangeListeners() {
        ContentChangeEvent event = new ContentChangeEvent(ipsSrcFile);
        ((IpsModel)ipsSrcFile.getIpsModel()).notifyChangeListeners(event);
    }
    
    boolean contentIsParsable() {
        if (contentParsable==null) {
            try {
                parse();    
            } catch (CoreException e) {
                return false;
            }
        }
        return contentParsable.booleanValue();
    }
    
    /**
     * Returns the IpsObject defined by the content.
     * 
     * @throws CoreException if the content can't be parsed.
     */
    IIpsObject getIpsObject() throws CoreException {
        if (contentParsable==null) {
            parse();
        }
        if (contentParsable.booleanValue()) {
            return ipsObject;    
        }
        throw new CoreException(new IpsStatus(this.toString() + " can't be parsed!")); //$NON-NLS-1$
    }
    
    /*
     * Parses the contents.
     */
    private void parse() throws CoreException {
        contentParsable = Boolean.FALSE;
        ipsObject = null;
        IpsObjectType type = IpsObjectType.getTypeForExtension(ipsSrcFile.getCorrespondingResource().getFileExtension());
        if (type==null) {
            return;
        }
        ipsObject = getIpsObject(type);
        contentParsable = Boolean.TRUE;
    }
    
    private IIpsObject getIpsObject(IpsObjectType type) throws CoreException {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(sourceText.getBytes(encoding)); 
            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
            Document doc = builder.parse(is);
            is.close();
            IIpsObject pdObject = type.newObject(ipsSrcFile);
            pdObject.initFromXml((Element)doc.getFirstChild());
            return pdObject;
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    public String toString() {
        return "Content of file: " + ipsSrcFile + ", parse status: " + contentParsable; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
