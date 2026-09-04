/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.faktorips.abstracttest.matcher.Matchers.allOf;
import static org.faktorips.abstracttest.matcher.Matchers.hasProperty;
import static org.faktorips.abstracttest.matcher.Matchers.hasSame;
import static org.faktorips.abstracttest.matcher.Matchers.hasSameByteArray;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.faktorips.abstracttest.matcher.PropertyMatcher;
import org.faktorips.abstracttest.matcher.SamePropertyMatcher;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class ImageDescriptorMatchers {

    /**
     * Maximum allowed per-byte difference (0-255) between two independently rasterized SVG pixel
     * values in {@link #descriptorOfSimilar(ImageData, Function, String)}.
     */
    private static final int PIXEL_TOLERANCE = 16;

    private ImageDescriptorMatchers() {
        // util class
    }

    public static Matcher<ImageDescriptor> descriptorOf(String imageFileName) {
        return descriptorOf(
                getSharedImageDescriptor(imageFileName).getImageData(100),
                i -> i.getImageData(100),
                "an ImageDescriptor for " + imageFileName);
    }

    private static ImageData getSharedImageData(String imageFileName) {
        return IIpsDecorators.getImageHandling().getSharedImage(imageFileName, true).getImageData();
    }

    private static ImageDescriptor getSharedImageDescriptor(String imageFileName) {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(imageFileName, true);
    }

    public static Matcher<ImageDescriptor> hasBaseImage(String imageFileName) {
        return descriptorOfSimilar(
                getSharedImageData(imageFileName),
                ImageDescriptorMatchers::getImageData,
                "an ImageDescriptor based on " + imageFileName);
    }

    public static Matcher<ImageDescriptor> hasOverlay(String imageFileName, int position) {
        return descriptorOf(
                getSharedImageDescriptor(imageFileName).getImageData(100),
                i -> getOverlayImageData(i, position),
                "an ImageDescriptor with overlay " + imageFileName + " in position " + position);
    }

    public static Matcher<ImageDescriptor> hasNoOverlay() {
        return new NoOverlayMatcher();
    }

    private static ImageDescriptor[] getOverlays(DecorationOverlayIcon decorationOverlayIcon) {
        try {
            Field overlaysField = DecorationOverlayIcon.class.getDeclaredField("overlays");
            overlaysField.setAccessible(true);
            return (ImageDescriptor[])overlaysField.get(decorationOverlayIcon);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError("Could not access overlays:\n" + e.getMessage());
        }
    }

    private static PropertyMatcher<ImageDescriptor, ImageData> descriptorOf(ImageData referenceImageData,
            Function<ImageDescriptor, ImageData> imageDataGetter,
            String description) {
        return new PropertyMatcher<>(imageDataGetter,
                description,
                allOf(
                        hasSame("alpha", i -> i.alpha, referenceImageData),
                        hasSameByteArray("alphaData", i -> i.alphaData, referenceImageData),
                        hasSame("bytesPerLine", i -> i.bytesPerLine, referenceImageData),
                        hasSameByteArray("data", i -> i.data, referenceImageData),
                        hasSame("delayTime", i -> i.delayTime, referenceImageData),
                        hasSame("depth", i -> i.depth, referenceImageData),
                        hasSame("disposalMethod", i -> i.disposalMethod, referenceImageData),
                        hasSame("height", i -> i.height, referenceImageData),
                        hasSameByteArray("maskData", i -> i.maskData, referenceImageData),
                        hasSame("maskPad", i -> i.maskPad, referenceImageData),
                        hasSamePalette(referenceImageData),
                        hasSame("scanlinePad", i -> i.scanlinePad, referenceImageData),
                        hasSame("transparentPixel", i -> i.transparentPixel, referenceImageData),
                        hasSame("type", i -> i.type, referenceImageData),
                        hasSame("width", i -> i.width, referenceImageData),
                        hasSame("x", i -> i.x, referenceImageData),
                        hasSame("y", i -> i.y, referenceImageData)));
    }

    /**
     * Like {@link #descriptorOf(ImageData, Function, String)}, but compares the raw pixel data
     * ({@code alphaData}, {@code data}) with a small per-byte tolerance instead of requiring exact
     * equality. SVG icons are rasterized on the fly by SWT; loading the same SVG through different
     * code paths (e.g. from a bundle URL vs. from a project file) does not guarantee bit-identical
     * anti-aliasing, even though the resulting image is visually identical. All other properties
     * (dimensions, depth, palette, transparency, ...) are still compared exactly.
     */
    private static PropertyMatcher<ImageDescriptor, ImageData> descriptorOfSimilar(ImageData referenceImageData,
            Function<ImageDescriptor, ImageData> imageDataGetter,
            String description) {
        return new PropertyMatcher<>(imageDataGetter,
                description,
                allOf(
                        hasSame("alpha", i -> i.alpha, referenceImageData),
                        hasSimilarByteArray("alphaData", i -> i.alphaData, referenceImageData, PIXEL_TOLERANCE),
                        hasSame("bytesPerLine", i -> i.bytesPerLine, referenceImageData),
                        hasSimilarByteArray("data", i -> i.data, referenceImageData, PIXEL_TOLERANCE),
                        hasSame("delayTime", i -> i.delayTime, referenceImageData),
                        hasSame("depth", i -> i.depth, referenceImageData),
                        hasSame("disposalMethod", i -> i.disposalMethod, referenceImageData),
                        hasSame("height", i -> i.height, referenceImageData),
                        hasSameByteArray("maskData", i -> i.maskData, referenceImageData),
                        hasSame("maskPad", i -> i.maskPad, referenceImageData),
                        hasSamePalette(referenceImageData),
                        hasSame("scanlinePad", i -> i.scanlinePad, referenceImageData),
                        hasSame("transparentPixel", i -> i.transparentPixel, referenceImageData),
                        hasSame("type", i -> i.type, referenceImageData),
                        hasSame("width", i -> i.width, referenceImageData),
                        hasSame("x", i -> i.x, referenceImageData),
                        hasSame("y", i -> i.y, referenceImageData)));
    }

    private static Matcher<ImageData> hasSimilarByteArray(String propertyDescription,
            Function<ImageData, byte[]> propertyGetter,
            ImageData referenceImageData,
            int tolerance) {
        byte[] reference = propertyGetter.apply(referenceImageData);
        return new TypeSafeMatcher<>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has similar " + propertyDescription + " (tolerance " + tolerance + ") "
                        + Arrays.toString(reference));
            }

            @Override
            protected boolean matchesSafely(ImageData item) {
                return isSimilar(reference, propertyGetter.apply(item), tolerance);
            }

            @Override
            protected void describeMismatchSafely(ImageData item, Description mismatchDescription) {
                mismatchDescription.appendText("was " + Arrays.toString(propertyGetter.apply(item)));
            }
        };
    }

    private static boolean isSimilar(byte[] reference, byte[] actual, int tolerance) {
        if (reference == null || actual == null) {
            return reference == actual;
        }
        if (reference.length != actual.length) {
            return false;
        }
        for (int i = 0; i < reference.length; i++) {
            if (Math.abs(Byte.toUnsignedInt(reference[i]) - Byte.toUnsignedInt(actual[i])) > tolerance) {
                return false;
            }
        }
        return true;
    }

    private static Matcher<ImageData> hasSamePalette(ImageData referenceImageData) {
        PaletteData palette = referenceImageData.palette;
        return hasProperty(i -> i.palette, "palette",
                allOf(
                        hasSame("isDirect", p -> p.isDirect, palette),
                        new SamePropertyMatcher<PaletteData, RGB[]>(p -> p.colors, "colors", palette) {
                            @Override
                            public boolean matches(Object item) {
                                return Arrays.equals(getNullSafe(item), getNullSafe(getObjectToMatch()));
                            }
                        },
                        hasSame("redMask", p -> p.redMask, palette),
                        hasSame("greenMask", p -> p.greenMask, palette),
                        hasSame("blueMask", p -> p.blueMask, palette),
                        hasSame("redShift", p -> p.redShift, palette),
                        hasSame("greenShift", p -> p.greenShift, palette),
                        hasSame("blueShift", p -> p.blueShift, palette)));
    }

    private static ImageData getImageData(ImageDescriptor imageDescriptor) {
        if (imageDescriptor instanceof DecorationOverlayIcon) {
            return getBaseImageDataProvider((DecorationOverlayIcon)imageDescriptor).getImageData(100);
        }
        return imageDescriptor.getImageData(100);
    }

    private static ImageDataProvider getBaseImageDataProvider(DecorationOverlayIcon decorationOverlayIcon) {
        try {
            Field baseImageDataProviderField = DecorationOverlayIcon.class.getDeclaredField("baseImageDataProvider");
            baseImageDataProviderField.setAccessible(true);
            return (ImageDataProvider)baseImageDataProviderField.get(decorationOverlayIcon);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError("Could not access baseImageDataProvider:\n" + e.getMessage());
        }
    }

    private static ImageData getOverlayImageData(ImageDescriptor imageDescriptor, int position) {
        if (imageDescriptor instanceof DecorationOverlayIcon) {
            return getOverlays((DecorationOverlayIcon)imageDescriptor)[position].getImageData(100);
        }
        return null;
    }

    private static final class NoOverlayMatcher extends TypeSafeMatcher<ImageDescriptor> {
        @Override
        public void describeTo(Description description) {
            description.appendText("an ImageDescriptor without overlays");
        }

        @Override
        protected boolean matchesSafely(ImageDescriptor imageDescriptor) {
            return !(imageDescriptor instanceof DecorationOverlayIcon)
                    || isEmpty(getOverlays((DecorationOverlayIcon)imageDescriptor));
        }

        private boolean isEmpty(ImageDescriptor[] overlays) {
            for (ImageDescriptor overlay : overlays) {
                if (overlay != null) {
                    return false;
                }
            }
            return true;
        }
    }

}
