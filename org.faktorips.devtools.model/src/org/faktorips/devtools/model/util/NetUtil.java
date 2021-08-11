/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public class NetUtil {

    private static final int TIMEOUT = 500;

    private NetUtil() {
        // utility class
    }

    /**
     * Checks if a URL is reachable with a timeout of 500 ms.
     * <p>
     * Throws an {@link RuntimeException} if the URL String is not valid.
     * 
     * @param url An String URL
     * @return true if a connection could be established
     */
    public static boolean isUrlReachable(String url) {
        return isUrlReachable(parseUrl(url));

    }

    /**
     * Checks if a URL is reachable with a timeout of 500 ms.
     * 
     * @param url An URL
     * @return true if a connection could be established
     */
    public static boolean isUrlReachable(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.connect();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a XSD schema file is reachable with a timeout of 500 ms. The URL is derived from
     * the {@link IpsObjectType}.
     * 
     * @param ipsObjectType An ips-object-type
     * @return true if a connection could be established
     */
    public static boolean isSchemaReachable(IpsObjectType ipsObjectType) {
        return isUrlReachable(XmlUtil.getSchemaLocation(ipsObjectType));
    }

    /**
     * Gets the content of an URL with a timeout of 500 ms.
     * <p>
     * Throws an {@link RuntimeException} if the URL String is not valid or an {@link IOException}
     * occurs while reading the content.
     * 
     * @param url An String URL
     * @return The content as line separated list
     */
    public static List<String> getContentFromUrl(String url) {
        return getContentFromUrl(parseUrl(url));
    }

    /**
     * Gets the content of an URL with a timeout of 500 ms.
     * <p>
     * Throws an {@link RuntimeException} if an {@link IOException} occurs while reading the
     * content.
     * 
     * @param url An URL
     * @return The content as line separated list
     */
    public static List<String> getContentFromUrl(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            return readFromConnection(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readFromConnection(URLConnection connection) {
        List<String> content = new ArrayList<>();
        try (InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                content.add(inputLine);
            }
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
