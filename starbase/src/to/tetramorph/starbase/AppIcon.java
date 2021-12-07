/*
 * AppIcon.java
 * Created on 2011/08/08, 15:51:25.
 */
package to.tetramorph.starbase;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.util.IconLoader;

/**
 * アマテルのデスクトップアイコンを表す定数。
 * アイコンを変更するときは、このソースを変更すること。
 * @author ohsawa
 */
public class AppIcon {
    public static final Image DESKTOP_ICON;
    public static final Image TITLE_BAR_ICON;
    public static final List<Image> DESKTOP_ICONS;
    static {
        DESKTOP_ICON = IconLoader.getImage("/resources/niwatori32.png");
        TITLE_BAR_ICON = IconLoader.getImage("/resources/niwatori16.png");
        DESKTOP_ICONS = new ArrayList<Image>();
        int [] size = { 16,32,48,96,64 };
        for ( int sz : size ) {
            DESKTOP_ICONS.add(
                    IconLoader.getImage("/resources/niwatori"+ sz + ".png"));
        }
    }
}
