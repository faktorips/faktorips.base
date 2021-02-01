/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

public class IpsObjectCompletionProcessor extends AbstractCompletionProcessor {

    private IpsObjectRefControl control;
    private IpsObjectType ipsObjectType;

    public IpsObjectCompletionProcessor(IpsObjectRefControl control) {
        ArgumentCheck.notNull(control);
        this.control = control;
        ipsProject = control.getIpsProject();
        ipsObjectType = null;
    }

    public IpsObjectCompletionProcessor(IIpsProject ipsProject, IpsObjectType type) {
        super(ipsProject);
        ArgumentCheck.notNull(type);
        ipsObjectType = type;
        control = null;
    }

    /**
     * Returns whether the given qualified name will match the given package and name prefixes.
     * 
     * @param pack The package the given qualified name must match. Can be empty, but must not be
     *            <code>null</code>.
     * @param prefix The prefix the unqualifed name must match. Can be emtpy, but must not be
     *            <code>null</code>.
     * @param qualifiedName The qulified name to match against the given package and name prefix.
     * 
     * @return <code>true</code> if the given qualified name matches, <code>false</code> otherwise.
     */
    private boolean match(String pack, String prefix, String qualifiedName) {
        String toMatchPack = StringUtil.getPackageName(qualifiedName).toLowerCase();
        String toMatchName = StringUtil.unqualifiedName(qualifiedName).toLowerCase();
        return (StringUtils.isEmpty(pack) || toMatchPack.startsWith(pack.toLowerCase()))
                && (StringUtils.isEmpty(prefix) || toMatchName.startsWith(prefix.toLowerCase()));
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl,
            int documentOffset) {

        if (ipsProject == null && control != null) {
            ipsProject = control.getIpsProject();
        }
        return super.computeCompletionProposals(contentAssistSubjectControl, documentOffset);
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        if (control == null && ipsProject == null) {
            setErrorMessage(Messages.IpsObjectCompletionProcessor_msgNoProject);
            return;
        }
        String match = prefix.toLowerCase();

        String matchPack = StringUtil.getPackageName(match);
        String matchName = StringUtil.unqualifiedName(match);

        try {
            IIpsSrcFile[] ipsSrcFiles;
            if (control != null) {
                ipsSrcFiles = control.getIpsSrcFiles();
            } else {
                ipsSrcFiles = ipsProject.findIpsSrcFiles(ipsObjectType);
            }
            for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                QualifiedNameType qnt = ipsSrcFile.getQualifiedNameType();
                if (match(matchPack, matchName, qnt.getName())) {
                    String qName = qnt.getName();
                    String displayText = qnt.getUnqualifiedName()
                            + " - " + mapDefaultPackageName(ipsSrcFile.getIpsPackageFragment().getName()); //$NON-NLS-1$
                    String localizedDescription = null;
                    if (IpsObjectType.TABLE_CONTENTS != ipsSrcFile.getIpsObjectType()) {
                        // table contents doesn't support description, thus doen't call getIpsObject
                        // due to performance reason
                        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
                        localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(ipsObject);
                    }

                    CompletionProposal proposal = new CompletionProposal(qName, 0, documentOffset, qName.length(),
                            IpsUIPlugin.getImageHandling().getImage(ipsSrcFile), displayText, null,
                            localizedDescription);
                    result.add(proposal);
                }
            }

            IIpsProject prj = ipsProject;
            if (prj == null && control != null) {
                prj = control.getIpsProject();
            }
            if (prj == null) {
                return;
            }

            // find packages of the project this completion processor was created in
            IIpsPackageFragmentRoot[] roots = prj.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot root : roots) {
                matchPackages(root.getIpsPackageFragments(), prefix, documentOffset, result);
            }

            // find packages of projects, the project of this compeltion processor refers to...
            List<IIpsProject> projects = prj.getDirectlyReferencedIpsProjects();
            for (IIpsProject project : projects) {
                roots = project.getIpsPackageFragmentRoots();
                for (IIpsPackageFragmentRoot root : roots) {
                    matchPackages(root.getIpsPackageFragments(), prefix, documentOffset, result);
                }
            }
        } catch (Exception e) {
            // TODO catches all exceptions while throwing Exception?
            setErrorMessage(Messages.IpsObjectCompletionProcessor_msgInternalError);
            IpsPlugin.log(e);
            return;
        }
    }
}
