/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.htmlexport.helper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * 
 * Utility-Class for the documentation
 * 
 * @author dicker
 * 
 */
public class DocumentorUtil {

    /**
     * returns the {@link IIpsObject} of the given {@link IIpsSrcFile}
     * 
     * @param <T>
     * @param srcFile
     * @return
     */
    public static IIpsObject getIpsObject(IIpsSrcFile srcFile) {
        try {
            return srcFile.getIpsObject();
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
            @SuppressWarnings("unchecked")
            T ipsObject = (T)getIpsObject(srcFile);
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
