package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.ArgumentCheck;

public class QualifiedNameType {

    private String qualifiedName;
    private IpsObjectType type;
    private int hashCode;
    
    /**
     * @param name
     * @param type
     */
    public QualifiedNameType(String name, IpsObjectType type) {
        super();
        ArgumentCheck.notNull(name, this);
        ArgumentCheck.notNull(type, this);
        qualifiedName = name;
        this.type = type;
        calculateHashCode();
    }

    public String getName() {
    	return qualifiedName;
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof QualifiedNameType){
            QualifiedNameType other = (QualifiedNameType)obj;
            return type.equals(other.type) && qualifiedName.equals(other.qualifiedName);
        }
        return false;
    }
    
    public IpsObjectType getIpsObjectType(){
        return type;
    }
    
    public IIpsObject getIpsObject(IIpsPackageFragmentRoot root) throws CoreException{
        if(root == null || !root.exists()){
            return null;
        }

        String folderName = "";
        String unqualifiedName = qualifiedName;
        int index = qualifiedName.lastIndexOf(IIpsPackageFragment.SEPARATOR);
        if (index>0) {
            folderName = qualifiedName.substring(0, index);
            unqualifiedName = qualifiedName.substring(index+1);
        }
        IIpsPackageFragment pack = root.getIpsPackageFragment(folderName);
        IIpsSrcFile file = pack.getIpsSrcFile(type.getFileName(unqualifiedName));
        if (!file.exists()) {
            return null;
        }
        return file.getIpsObject();
    }
    
    
    private void calculateHashCode(){
        int result = 17;
        result = result*37 + qualifiedName.hashCode();
        result = result*37 + type.hashCode();
        hashCode = result;
    }
    
    public int hashCode() {
        return hashCode;
    }
    
    public String toString() {
        return type + ": " + qualifiedName;
    }
    

}
