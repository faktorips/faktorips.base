/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.IpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Empty Migration 
 * 
 * @author Peter Erzberger
 */
public class Migration_0_9_41 extends AbstractMigrationOperation {

    public Migration_0_9_41(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Some bugs fixed."; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "1.0.0.rc1"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException 
     * @throws SAXException 
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        migrateIpsProjectProperties(monitor);
        return new MessageList();
    }
    
    private void migrateIpsProjectProperties(IProgressMonitor monitor) throws CoreException{
        IFile ipsProperties = getIpsProject().getIpsProjectPropertiesFile();
        if(ipsProperties.exists()){
            try {
                DocumentBuilder docBuilder = XmlUtil.getDefaultDocumentBuilder();
                Document doc = docBuilder.parse(ipsProperties.getContents());
                changeIpsObjectPaths(doc);
                changeIpsSrcFolderEntries(doc);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                XmlUtil.writeXMLtoStream(bos, doc, null, 4, ipsProperties.getCharset());
                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                ipsProperties.setContents(bis, IFile.FORCE, monitor);
                
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(e));
            }
        }
    }
    
    private void changeIpsSrcFolderEntries(Document doc) throws CoreException{
        NodeList ipsSrcFolderEntryElements = doc.getElementsByTagName(IpsSrcFolderEntry.XML_ELEMENT);
        for (int i = 0; i < ipsSrcFolderEntryElements.getLength(); i++) {
            Element el = (Element)ipsSrcFolderEntryElements.item(i);
            changeIpsSrcFolderEntry(el, doc);
        }
    }
    
    private void changeIpsSrcFolderEntry(Element el, Document doc) throws CoreException{
        String outputFolderMergable = el.getAttribute("outputFolderGenerated"); //outputFolderMergable
        String basePackageMergable = el.getAttribute("basePackageGenerated"); //basePackageMergable
        //outputFolderExtension should not have a value
        //basePackageExtension should not have a value
        
        el.removeAttribute("outputFolderGenerated");
        el.removeAttribute("basePackageGenerated");
        el.removeAttribute("outputFolderExtension");
        el.removeAttribute("basePackageExtension");
        el.setAttribute("outputFolderMergable", outputFolderMergable);
        el.setAttribute("basePackageMergable", basePackageMergable);
        if(!StringUtils.isEmpty(outputFolderMergable)){
            String folderPath = outputFolderMergable + "derived";
            el.setAttribute("outputFolderDerived", folderPath);
            createDerivedSrcFolder(folderPath);
        }
        else{
            el.setAttribute("outputFolderDerived","");
        }
        el.setAttribute("basePackageDerived", !StringUtils.isEmpty(basePackageMergable) ? basePackageMergable : "");
    }
    
    private void createDerivedSrcFolder(String path) throws CoreException{
        IFolder derivedsrcFolder = getIpsProject().getProject().getFolder(new Path(path));
        if(!derivedsrcFolder.exists()){
            derivedsrcFolder.create(true, true, new NullProgressMonitor());
        }
        derivedsrcFolder.setDerived(true);
        IClasspathEntry derivedsrc = JavaCore.newSourceEntry(derivedsrcFolder.getFullPath());
        IClasspathEntry[] rawClassPath = getIpsProject().getJavaProject().getRawClasspath();
        IClasspathEntry[] newClassPath = new IClasspathEntry[rawClassPath.length + 1];
        System.arraycopy(rawClassPath, 0, newClassPath, 0, rawClassPath.length);
        newClassPath[newClassPath.length - 1] = derivedsrc;
        getIpsProject().getJavaProject().setRawClasspath(newClassPath, new NullProgressMonitor());
    }
    
    private void changeIpsObjectPaths(Document doc) throws CoreException{
        NodeList ipsObjectPathElements = doc.getElementsByTagName(IpsObjectPath.XML_TAG_NAME);
        for (int i = 0; i < ipsObjectPathElements.getLength(); i++) {
            Element el = (Element)ipsObjectPathElements.item(i);
            changeIpsObjectPathAttributes(el, doc);
        }
    }
    
    private void changeIpsObjectPathAttributes(Element el, Document doc) throws CoreException{
        String outputFolderMergableSources = el.getAttribute("outputFolderGenerated"); //outputFolderMergableSources
        String basePackageMergable = el.getAttribute("basePackageGenerated"); //basePackageMergable
        //outputFolderExtension should not have a value
        //basePackageExtension should not have a value
        
        el.removeAttribute("outputFolderGenerated");
        el.removeAttribute("basePackageGenerated");
        el.removeAttribute("outputFolderExtension");
        el.removeAttribute("basePackageExtension");
        el.setAttribute("outputFolderMergableSources", outputFolderMergableSources);
        el.setAttribute("basePackageMergable", basePackageMergable);
        if(!StringUtils.isEmpty(outputFolderMergableSources)){
            String folderPath = "derived";
            el.setAttribute("outputFolderDerivedSources", folderPath);
            createDerivedSrcFolder(folderPath);
        }
        else{
            el.setAttribute("outputFolderDerivedSources", "");
        }
        el.setAttribute("basePackageDerived", !StringUtils.isEmpty(basePackageMergable) ? basePackageMergable : "");
    }
}
