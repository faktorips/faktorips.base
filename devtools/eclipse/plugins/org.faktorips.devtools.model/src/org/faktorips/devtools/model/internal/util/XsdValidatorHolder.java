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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.NetUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This thread local class holds an instance of an XSD {@link Validator}. The used
 * {@link SchemaFactory} can either load the XSD directly from the name space URL, from a
 * {@link File} from a {@link Source} or from a given {@link URL}.
 * <p>
 * Per default an logging {@link ErrorHandler} is added, that will only throw a
 * {@link RuntimeException} if a fatal error occurs.
 */
public class XsdValidatorHolder extends ThreadLocal<Validator> {

    private final IpsObjectType ipsObjectType;

    /**
     * Create the {@link ThreadLocal} with {@link Validator} for a specific {@link IpsObjectType}.
     * 
     * @param ipsObjectType the IPS object type
     */
    public XsdValidatorHolder(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    protected Validator initialValue() {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema;
        try {
            if (NetUtil.isSchemaReachable(ipsObjectType)
                    && NetUtil.isUrlReachable("https://www.w3.org/2009/01/xml.xsd")) {
                schema = factory.newSchema();
            } else {
                // offline developers need to copy the content of
                // faktorips-schemas/src/main/resources to the folder
                // org.faktorips.devtools.model/xsd-schema
                factory.setResourceResolver(new IpsXsdResourceResolver());
                schema = factory.newSchema(loadIpsSchemaFileFromClasspath());
            }
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new LoggingErrorHandler());
            return validator;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private StreamSource loadIpsSchemaFileFromClasspath() {
        String resource = "xsd-schema/" + ipsObjectType.getXmlElementName() + ".xsd"; //$NON-NLS-1$//$NON-NLS-2$
        InputStream xsdSchemaStream = ipsObjectType.getClass().getClassLoader().getResourceAsStream(resource);
        return new StreamSource(new InputStreamReader(xsdSchemaStream));
    }

    private InputStreamReader loadDependendSchemaFilesFromClasspath(String systemId) {
        String path;
        if (systemId.endsWith("xml.xsd")) { //$NON-NLS-1$
            path = "xsd-schema/xml.xsd"; //$NON-NLS-1$
            // $NON-NLS-1$
        } else if (systemId.endsWith("ips-global.xsd")) { //$NON-NLS-1$
            path = "xsd-schema/ips-global.xsd"; //$NON-NLS-1$
            // $NON-NLS-1$
        } else if (systemId.startsWith(XmlUtil.FAKTOR_IPS_SCHEMA_URL)) {
            path = "xsd-schema/" + systemId.substring(systemId.lastIndexOf('/') + 1);
        } else {
            throw new IllegalArgumentException("Unknown dependent schema file: " + systemId); //$NON-NLS-1$
        }
        InputStream xsdSchemaStream = getClass().getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(xsdSchemaStream);
    }

    /**
     * We either use {@code systemId} as full path or valid URI e.g. http://example.org/schema.xsd,
     * file:/some/path/schema.xsd, jar:file:/my-jar.jar!/schema.xsd or /some/path/schema.xsd
     * <p>
     * or
     * <p>
     * leave the {@code systemId} as it is and load the resource as {@link Reader} from a known
     * location e.g. as resource form the classloader or from the file system.
     */
    class IpsXsdResourceResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type,
                String namespaceURI,
                String publicId,
                String systemId,
                String baseURI) {

            return new IpsXsdLSInput(systemId, loadDependendSchemaFilesFromClasspath(systemId));
        }
    }

    class IpsXsdLSInput implements LSInput {

        private String systemId;
        private Reader characterStream;

        public IpsXsdLSInput(String systemId, InputStreamReader characterStream) {
            this.systemId = systemId;
            this.characterStream = characterStream;
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        @Override
        public Reader getCharacterStream() {
            return characterStream;
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
            this.characterStream = characterStream;
        }

        @Override
        public InputStream getByteStream() {
            return null;
        }

        @Override
        public void setByteStream(InputStream byteStream) {
            // ignore
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
            // ignore
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public void setPublicId(String publicId) {
            // ignore
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) {
            // ignore
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) {
            // ignore
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
            // ignore
        }
    }

    /**
     * Default error handler. Will log all XSD validation errors and warnings to the {@link IpsLog
     * IPS logger}.
     */
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
