package org.faktorips.devtools.htmlexport;

import java.util.List;

import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public class Documentor {
    private DocumentorConfiguration config;

    public Documentor(DocumentorConfiguration config) {
        setDocumentorConfiguration(config);
    }

    private void setDocumentorConfiguration(DocumentorConfiguration config) {
        if (config == null)
            throw new IllegalArgumentException("config darf nicht null sein");
        this.config = config;
    }

    public void run() {
        List<IDocumentorScript> scripts = config.getScripts();
        for (IDocumentorScript documentorScript : scripts) {
            documentorScript.execute(config);
        }
    }

    public DocumentorConfiguration getConfig() {
        return config;
    }

    public void setConfig(DocumentorConfiguration config) {
        this.config = config;
    }
}
