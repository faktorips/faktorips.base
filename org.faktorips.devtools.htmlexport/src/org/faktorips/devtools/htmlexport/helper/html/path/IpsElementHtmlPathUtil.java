package org.faktorips.devtools.htmlexport.helper.html.path;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Util-Klasse für die Erstellung von Links und Ermittlung relativer Pfade auf Basis eines IIpsElement
 * @author dicker
 *
 */
public interface IpsElementHtmlPathUtil {

    /**
     * gibt den relativen Link vom <code>IIpsElement</code> ins Root-Verzeichnis
     * 
     * @return relativer Link ins Root-Verzeichnis
     */
    public String getPathToRoot();

    /**
     * gibt den relativen Link vom Root-Verzeichnis zum <code>IIpsElement</code>
     * @param linkedFileType Typ des Links
     * 
     * @return relativer Link vom Root-Verzeichnis
     */
    public String getPathFromRoot(LinkedFileTypes linkedFileType);

    /**
     * Name des IIPSElement im Link
     * 
     * @param withImage
     *            true: Darstellung mit Thumb
     * @return Html-Link-Text
     */
    public String getLinkText(boolean withImage);
    
    /**
     * 
     * @return IipsElement des PathUtils 
     */
    public IIpsElement getIpsElement();

}