package org.faktorips.devtools.htmlexport.io.html;

import java.util.List;

import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public class OverviewHtmlDocumentorWriter extends AbstractHtmlDocumentorWriter {

    @Override
    protected String createFileContent(DocumentorConfiguration config, IIpsElement element) {
        StringBuilder content = new StringBuilder();
        content.append(getHead());
        
        if (element instanceof IIpsProject) {
            IIpsProject project = (IIpsProject) element;
            List<IIpsSrcFile> srcFiles = config.getLinkedSources();
            if (srcFiles.size() > 0) {
                content.append("<h2>SourceFiles</h2>");
                content.append("<ul>");
                for (IIpsSrcFile ipsSrcFile : srcFiles) {
                    content.append("<li>");
                    content.append(ipsSrcFile.getName());
                    content.append("</li>");
                }
                content.append("</ul>");
            }
        }
        
        if (element instanceof IpsModel) {
            IpsModel model = (IpsModel) element;
            
        }
        
        content.append(getFoot());
        return content.toString();
    }

    private String getFoot() {
        return "</body></html>";
    }

    private String getHead() {
        return "<html><head><title>Overview</title></head><body>";
    }

    @Override
    protected String createFilePath(DocumentorConfiguration config, IIpsElement element) {
        return "index.html";
    }
}
