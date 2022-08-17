/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.internal.ipsobject.Messages;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Handles all errors and warnings that occur during a xsd schema validation. A message is created
 * and saved for each error and warning.
 */
public class XsdValidationHandler implements ErrorHandler {

    private final Set<String> xsdValidationErrors = new LinkedHashSet<>();
    private final Set<String> xsdValidationWarnings = new LinkedHashSet<>();

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        String localizedMessage = exception.getLocalizedMessage();
        IpsLog.log(new IpsStatus(IStatus.WARNING, createLogMessage(localizedMessage)));

        if (isXsdSchemaMissingError(localizedMessage)) {
            xsdValidationWarnings.add(Messages.IpsSrcFileContent_msgXsdValidationReferenzIsMissing);
        } else {
            xsdValidationWarnings.add(localizedMessage);
        }
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        String localizedMessage = exception.getLocalizedMessage();
        if (isXsdSchemaMissingError(localizedMessage)) {
            warning(exception);
        } else {
            IpsLog.log(new IpsStatus(IStatus.ERROR, createLogMessage(localizedMessage)));
            xsdValidationErrors.add(localizedMessage);
        }
    }

    public Set<String> getXsdValidationErrors() {
        return Set.copyOf(xsdValidationErrors);
    }

    public Set<String> getXsdValidationWarnings() {
        return Set.copyOf(xsdValidationWarnings);
    }

    private boolean isXsdSchemaMissingError(String localizedMessage) {
        return StringUtils.startsWith(localizedMessage, "cvc-elt.1.a"); //$NON-NLS-1$
    }

    private String createLogMessage(String localizedMessage) {
        return new StringBuilder().append("XSD validation: ").append(localizedMessage).toString(); //$NON-NLS-1$
    }
}
