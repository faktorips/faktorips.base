/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Able to load {@link Document}s and {@link InputStream}s from XML resources using a
 * {@link ClassLoader}.
 * 
 * @author Alexander Weickmann
 */
public class ClassLoaderDataSource {

    private final ClassLoader classLoader;

    public ClassLoaderDataSource(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Document loadDocument(String resourcePath, DocumentBuilder documentBuilder) {
        URL url = getResourceUrl(resourcePath);
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            return documentBuilder.parse(inputStream);
        } catch (IOException | SAXException e) {
            throw new RuntimeException("Cannot parse xml resource '" + resourcePath + "'", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public InputStream getResourceAsStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
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
        URL url = getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("Cannot find resource '" + resourcePath + "'");
        }
        return url;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
