/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class IpsValidationMessageMapper {

    /* private */ static final String MOJO_NAME = "[Faktor-IPS-Validation]";
    /* private */ static final String DATATYPE_WARNING = "The class %s is in this project. Therefore the project must be built before the datatype can be validated. To avoid this, the class should be moved to another project.";

    private IpsValidationMessageMapper() {
        // Utility class not to be instantiated.
    }

    /* public */ static void logMessages(MessageList messageList,
            Log log,
            MavenProject project) {

        for (Message message : messageList) {
            checkAndLogDatatypeError(message, log, project);

            StringBuilder logMessage = new StringBuilder()
                    .append(MOJO_NAME)
                    .append(" ").append(message.getText()).append(" ")
                    .append("(").append(message.getCode()).append(")");

            message.appendInvalidObjectProperties(logMessage);

            switch (message.getSeverity()) {
                case ERROR:
                    log.error(logMessage.toString());
                    break;
                case WARNING:
                    log.warn(logMessage.toString());
                    break;
                default:
                    log.info(logMessage.toString());
            }
        }
    }

    private static void checkAndLogDatatypeError(Message message,
            Log log,
            MavenProject project) {
        if (message.getCode().equals(GenericValueDatatype.MSGCODE_JAVACLASS_NOT_FOUND)) {
            Object object = message.getInvalidObjectProperties().get(0).getObject();
            if (object instanceof String) {
                // otherwise this is the wrapped message from e.g.
                // ValidationUtils#checkValueDatatypeReference
                String datatypeClassName = (String)object;

                for (String compileSourceRoot : project.getCompileSourceRoots()) {
                    StringBuilder pathToDatatypeClass = new StringBuilder()
                            .append(compileSourceRoot)
                            .append(File.separatorChar)
                            .append(StringUtils.replace(datatypeClassName, ".", String.valueOf(File.separatorChar)))
                            .append(".java");

                    if (FileUtils.fileExists(pathToDatatypeClass.toString())) {
                        StringBuilder logMessage = new StringBuilder()
                                .append(MOJO_NAME)
                                .append(" ")
                                .append(String.format(DATATYPE_WARNING, datatypeClassName));
                        log.warn(logMessage.toString());
                        break;
                    }
                }
            }
        }

    }
}