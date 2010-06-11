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

/**
 * 
 * Utility-Class for the documentation
 * 
 * @author dicker
 * 
 */
public class DocumentorUtil {

    private static final String DEFAULT_PACKAGE_NAME = Messages.DocumentorUtil_defaultPackageName;

    /**
     * returns the name of the given ipsPackageFragment. If the ipsPackageFragment is the default
     * package then the DEFAULT_PACKAGE_NAME is returned
     * 
     * @param ipsPackageFragment
     * @return name of the ipsPckageFragment
     */
    public static String getIpsPackageName(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage()) {
            return DEFAULT_PACKAGE_NAME;
        }
        return ipsPackageFragment.getName();
    }

    /**
     * returns the {@link IIpsObject} of the given {@link IIpsSrcFile}
     * 
     * @param <T>
     * @param srcFile
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends IIpsObject> T getIpsObject(IIpsSrcFile srcFile) {
        try {
            return (T)srcFile.getIpsObject();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * returns a list of {@link IIpsObject}s of the given {@link IIpsSrcFile}s
     * 
     * @param <T>
     * @param srcFiles
     * @return
     */
    public static <T extends IIpsObject> List<T> getIpsObjects(IIpsSrcFile... srcFiles) {
        List<T> objects = new ArrayList<T>();

        for (IIpsSrcFile srcFile : srcFiles) {
            T ipsObject = getIpsObject(srcFile);
            objects.add(ipsObject);
        }

        return objects;
    }

    /**
     * converts {@link ImageData} to a byte[] using the given format
     * 
     * @param imageData
     * @param format
     * @return
     */
    public static byte[] convertImageDataToByteArray(ImageData imageData, int format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { imageData };
        imageLoader.save(out, format);

        return out.toByteArray();
    }
}
