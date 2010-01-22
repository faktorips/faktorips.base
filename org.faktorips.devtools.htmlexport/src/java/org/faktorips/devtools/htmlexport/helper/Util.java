package org.faktorips.devtools.htmlexport.helper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

public class Util {
    public static String getIpsPackageName(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage())
            return "(default package)";
        return ipsPackageFragment.getName();
    }
    
    @SuppressWarnings("unchecked")
	public static <T extends IIpsObject> T getIpsObject(IIpsSrcFile srcFile) {
    	try {
			return (T) srcFile.getIpsObject();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
    }
    
	public static <T extends IIpsObject> List<T> getIpsObjects(IIpsSrcFile... srcFiles) {
    	List<T> objects = new ArrayList<T>();

    	for (IIpsSrcFile srcFile : srcFiles) {
			T ipsObject = getIpsObject(srcFile);
			objects.add(ipsObject);
		}
    	
    	return objects;
    }
	
	public static byte[] convertImageDataToByteArray(ImageData imageData, int format) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {imageData};
		imageLoader.save(out, format);
		
		return out.toByteArray();
	}
}
