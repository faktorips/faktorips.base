package org.faktorips.devtools.htmlexport.documentor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.IDocumentorScript;

public class DocumentorConfiguration {
    private Set<IpsObjectType> linkedTypes = new HashSet<IpsObjectType>();
    private String path;
    private List<IDocumentorScript> scripts = new ArrayList<IDocumentorScript>();
    private IIpsProject ipsProject;
    
    public void setLinkPolicyClasses(boolean linkPolicyClasses) {
        if (linkPolicyClasses) {
            linkedTypes.add(IpsObjectType.POLICY_CMPT_TYPE);
            return;
        }
        linkedTypes.remove(IpsObjectType.POLICY_CMPT_TYPE);
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public void setLinkProductClasses(boolean linkProductClasses) {
        if (linkProductClasses) {
            linkedTypes.add(IpsObjectType.PRODUCT_CMPT_TYPE);
            return;
        }
        linkedTypes.remove(IpsObjectType.PRODUCT_CMPT_TYPE);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IpsObjectType[] getLinkedTypes() {
        IpsObjectType[] array = linkedTypes.toArray(new IpsObjectType[linkedTypes.size()]);
        return array;
    }

    public List<IIpsObject> getLinkedObjects() {
        List<IIpsObject> objects = new ArrayList<IIpsObject>();

        List<IIpsSrcFile> srcFiles = getLinkedSources();
        for (IIpsSrcFile ipsSrcFile : srcFiles) {
            try {
                objects.add(ipsSrcFile.getIpsObject());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    public List<IIpsSrcFile> getLinkedSources() {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(result, getLinkedTypes());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addDocumentorScript(IDocumentorScript script) {
        scripts.add(script);
    }

    public List<IDocumentorScript> getScripts() {
        return scripts;
    }
}
