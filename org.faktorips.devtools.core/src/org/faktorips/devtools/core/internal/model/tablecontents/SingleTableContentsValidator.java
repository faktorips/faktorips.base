/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * This class is used to validate that a table structure with table-type "single content" has only
 * one existing table content. If there are more than one table contents an error message will be
 * created.
 * <p>
 * Concept discussion: https://wiki.faktorzehn.de/display/FaktorIPSdevelWiki/Validierung
 * 
 * @author Andreas Koenig
 * 
 */
public class SingleTableContentsValidator {

    private ITableStructure tableStructure;

    /**
     * Creates a new <code>SingleTableContentsValidator</code>.
     * 
     * @param tableStructure The <code>ITableStructure</code> which is used to validate.
     * 
     */
    public SingleTableContentsValidator(ITableStructure tableStructure) {
        this.tableStructure = tableStructure;
    }

    /**
     * Validates if {@link #canValidate()} returns <code>true</code>. Does nothing otherwise.
     * 
     * @return the message list containing the validation messages. Contains no messages if no
     *         problems were detected.
     */
    public MessageList validateIfPossible() {
        MessageList messageList = new MessageList();
        if (canValidate()) {
            validateAndAppendMessages(messageList);
        }
        return messageList;
    }

    /**
     * @return <code>true</code> if this validator has been given a <code>ITableStructure</code>
     *         which is not <code>null</code>. Returns <code>false</code> if the
     *         <code>ITableStructure</code> is <code>null</code>.
     */
    public boolean canValidate() {
        return tableStructure != null;
    }

    protected void validateAndAppendMessages(MessageList messageList) {
        if (isNumberOfContentsIllegal()) {
            List<String> contentSrcFileNames = getSrcFileNames();
            String text = NLS.bind(Messages.TableContents_msgTooManyContentsForSingleTableStructure,
                    tableStructure.getName(), contentSrcFileNames);
            messageList.add(new Message(ITableContents.MSGCODE_TOO_MANY_CONTENTS_FOR_SINGLETABLESTRUCTURE, text,
                    Message.ERROR, tableStructure.getName(), ITableContents.PROPERTY_TABLESTRUCTURE));
        }
    }

    private List<String> getSrcFileNames() {
        List<IIpsSrcFile> contentSrcFiles = findContentSrcFiles();
        List<String> contentSrcFileNames = new ArrayList<String>();
        for (IIpsSrcFile srcFile : contentSrcFiles) {
            contentSrcFileNames.add(srcFile.getIpsObjectName());
        }
        return contentSrcFileNames;
    }

    private List<IIpsSrcFile> findContentSrcFiles() {
        return tableStructure.getIpsProject().findAllTableContentsSrcFiles(tableStructure);
    }

    private boolean isNumberOfContentsIllegal() {
        return !tableStructure.isMultipleContentsAllowed() && getNumberofContents() > 1;
    }

    private int getNumberofContents() {
        List<IIpsSrcFile> contentSrcFiles = findContentSrcFiles();
        return contentSrcFiles.size();
    }

    /**
     * Returns <code>false</code> if the table structure allows an additional table content,
     * <code>true</code> otherwise. This method always returns <code>false</code> for table
     * structures, that support multiple contents. For single-content structures <code>false</code>
     * is returned only if it has no table contents, <code>true</code> otherwise.
     * <p>
     * Returns <code>true</code> if the table structure is <code>null</code>.
     */
    public boolean forbidsAdditionalContents() {
        if (tableStructure == null) {
            return true;
        }
        return !tableStructure.isMultipleContentsAllowed() && getNumberofContents() >= 1;
    }
}
