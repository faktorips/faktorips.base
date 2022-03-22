/*******************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.com
 *
 * All Rights Reserved - Alle Rechte vorbehalten.
 ******************************************************/
package org.faktorips.devtools.filetypes;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class ProductCmptTypeFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new ProductCmptTypeFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/filetypes/ProductCmptType.svg", ProductCmptTypeFileType.class);
    private static final String FILE_EXTENSION = "ipsproductcmpttype";

    public ProductCmptTypeFileType() {
        super(XMLLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "ProductComponentType";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Product Component Type";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public @Nullable Icon getIcon() {
        return ICON;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return getName();
    }
}
