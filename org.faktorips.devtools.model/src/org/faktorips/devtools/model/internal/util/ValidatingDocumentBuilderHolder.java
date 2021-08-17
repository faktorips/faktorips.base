/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.util;

import java.io.File;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.NetUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This thread local class holds an instance of an document builder with XSD validation
 * capabilities. The used {@link SchemaFactory} can either load the XSD directly from the name space
 * url, from a {@link File} from a {@link Source} or from a given {@link URL}.
 * <p>
 * Per default an logging {@link ErrorHandler} is added, that will only throw an
 * {@link RuntimeException} if an fatal error occurs.
 * 
 */
public class ValidatingDocumentBuilderHolder extends ThreadLocal<DocumentBuilder> {

    private final IpsObjectType ipsObjectType;

    public ValidatingDocumentBuilderHolder(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    protected DocumentBuilder initialValue() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);

        SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        DocumentBuilder builder;
        try {
            if (NetUtil.isSchemaReachable(ipsObjectType)) {
                factory.setSchema(schemafactory.newSchema());
            }
            // else load from jar file
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
        // set a default logging error handler
        builder.setErrorHandler(new LoggingErrorHandler());
        return builder;
    }

    private static final class LoggingErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException e) throws SAXException {
            IpsLog.log(new IpsStatus(IStatus.WARNING, e.getLocalizedMessage()));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw new RuntimeException(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            IpsLog.log(new IpsStatus(IStatus.ERROR, e.getLocalizedMessage()));
        }
    }

}
