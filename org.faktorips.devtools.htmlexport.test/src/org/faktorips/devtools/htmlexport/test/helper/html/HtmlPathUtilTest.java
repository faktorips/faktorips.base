package org.faktorips.devtools.htmlexport.test.helper.html;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractFipsDocTest;

public class HtmlPathUtilTest extends AbstractFipsDocTest {
   
    public void testUpPathProductCmp() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "base.sub.SubBVB");
        assertEquals("../../", HtmlUtil.getPathToRoot(cmpt));
    }

    public void testFromPathProductCmp() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "base.sub.SubBVB");
        LinkedFileType fileType = LinkedFileType.getLinkedFileTypeByIpsElement(cmpt);
        assertEquals("base/sub/" + fileType.getPrefix() + "PolicyCmptType_SubBVB" + fileType.getSuffix(), HtmlUtil.getPathFromRoot(cmpt, fileType));
    }

    public void testUpPathProject() throws Exception {
        String upPath = HtmlUtil.getPathToRoot(ipsProject);
        assertEquals("", upPath);
    }

    public void testFromPathProject() throws Exception {
        String upPath = HtmlUtil.getPathFromRoot(ipsProject, null);
        assertEquals("indes.html", upPath);
    }

    public void testUpPathPackage() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "base.sub.SubBVB");
        IIpsPackageFragment packageFragment = cmpt.getIpsPackageFragment();
        assertEquals("../../", HtmlUtil.getPathToRoot(packageFragment));
    }

    /*
    public void testFromPathPackage() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "base.sub.SubBVB");
        IIpsPackageFragment packageFragment = cmpt.getIpsPackageFragment();
        assertEquals("base/sub/package_index.html", HtmlUtil.getPathFromRoot(packageFragment, LinkedFileTypes.PACKAGE_CLASSES_OVERVIEW));
    }
    */
    
    public void testUpPathRootPackage() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "BVB");
        IIpsPackageFragment packageFragment = cmpt.getIpsPackageFragment();
        assertEquals("", HtmlUtil.getPathToRoot(packageFragment));
    }

    public void testFromPathRootPackage() throws Exception {
        PolicyCmptType cmpt = newPolicyCmptType(ipsProject, "BVB");
        IIpsPackageFragment packageFragment = cmpt.getIpsPackageFragment();
        assertEquals("package_index.html", HtmlUtil.getPathFromRoot(packageFragment, LinkedFileType.getLinkedFileTypeByIpsElement(packageFragment)));
    }

    public void testNotImplementedUtilClass() throws Exception {
        ConfigElement confElem = new ConfigElement(null,0);
        try {
            HtmlUtil.getPathToRoot(confElem);
            fail();
        } catch (NotImplementedException e) {
        }
    }

}
