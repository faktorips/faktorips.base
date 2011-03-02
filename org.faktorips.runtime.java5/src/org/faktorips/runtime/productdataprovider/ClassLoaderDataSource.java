/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.util.IoUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Able to load {@link Document}s and {@link InputStream}s from XML resources using a
 * {@link ClassLoader}.
 * 
 * @author Alexander Weickmann
 */
public class ClassLoaderDataSource {

    private ClassLoader classLoader;

    public ClassLoaderDataSource(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Document loadDocument(String resourcePath, DocumentBuilder documentBuilder) {
        URL url = getResourceUrl(resourcePath);
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            return documentBuilder.parse(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse xml resource '" + resourcePath + "'", e);
        } catch (SAXException e) {
            throw new RuntimeException("Cannot parse xml resource '" + resourcePath + "'", e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    public InputStream getResourceAsStream(String resourceName) {
        return classLoader.getResourceAsStream(resourceName);
    }

    public String getLastModificationStamp(String resourcePath) {
        URL url = getResourceUrl(resourcePath);
        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open a connection to resource '" + resourcePath + "'", e);
        }

        if (connection instanceof JarURLConnection) {
            JarURLConnection jarUrlConnection = (JarURLConnection)connection;
            URL jarUrl = jarUrlConnection.getJarFileURL();
            URI jarUri;
            try {
                jarUri = jarUrl.toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            File jarFile = new File(jarUri);
            return "" + jarFile.lastModified();
        } else {
            File tocFile = new File(url.getFile());
            return "" + tocFile.lastModified();
        }
    }

    private URL getResourceUrl(String resourcePath) {
        URL url = classLoader.getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("Cannot find resource '" + resourcePath + "'");
        }
        return url;
    }

}
