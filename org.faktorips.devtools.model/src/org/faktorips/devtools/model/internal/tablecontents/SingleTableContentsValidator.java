/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * This class is used to validate that a project (and its referenced projects) contain at most one
 * table content instance, if the respective table structure is of type "single content". If there
 * are more than one table contents for such a structure, an error message is created.
 * <p>
 * Concept discussion: https://wiki.faktorzehn.de/display/FaktorIPSdevelWiki/Validierung
 * 
 * @author Andreas Koenig
 * 
 */
public class SingleTableContentsValidator {

    private ITableStructure tableStructure;
    private IIpsProject ipsProject;

    /**
     * Creates a new <code>SingleTableContentsValidator</code>.
     * 
     * @param ipsProject the IPS project a new table content is to be created in. Note though, that
     *            in most cases, this is <em>not</em> the project that contains the table structure.
     *            Must not be <code>null</code>.
     * @param tableStructure The <code>ITableStructure</code> of the new table content.
     * 
     */
    public SingleTableContentsValidator(IIpsProject ipsProject, ITableStructure tableStructure) {
        Assert.isNotNull(ipsProject);
        this.ipsProject = ipsProject;
        this.tableStructure = tableStructure;
    }

    /**
     * Uses the table contents' project and table structure to create a
     * {@link SingleTableContentsValidator} instance.
     * 
     * @param tableContents the table content to be validated.
     * @return a newly created {@link SingleTableContentsValidator}
     */
    public static SingleTableContentsValidator createFor(ITableContents tableContents) {
        IIpsProject project = tableContents.getIpsProject();
        return new SingleTableContentsValidator(project, getStructureFor(tableContents, project));
    }

    private static ITableStructure getStructureFor(ITableContents tableContents, IIpsProject project) {
        return tableContents.findTableStructure(project);
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
            String text = MessageFormat.format(Messages.TableContents_msgTooManyContentsForSingleTableStructure,
                    tableStructure.getName());
            messageList.add(new Message(ITableContents.MSGCODE_TOO_MANY_CONTENTS_FOR_SINGLETABLESTRUCTURE, text,
                    Message.ERROR, tableStructure.getName(), ITableContents.PROPERTY_TABLESTRUCTURE));
        }
    }

    private List<IIpsSrcFile> findContentSrcFiles() {
        return ipsProject.findAllTableContentsSrcFiles(tableStructure);
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
