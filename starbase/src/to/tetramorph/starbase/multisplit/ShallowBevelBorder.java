/*
 * ShallowBevelBorder.java
 *
 * Created on 2007/11/08, 12:41
 *
 */

package to.tetramorph.starbase.multisplit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.border.BevelBorder;

/**
 * へこんだベベルボーダー(親は彫り込みが2pixcelだが1pixcelにしたもの。)
 * @author 大澤義鷹
 */
public class ShallowBevelBorder extends BevelBorder {
  
  /**  ShallowBevelBorder オブジェクトを作成する */
  public ShallowBevelBorder() {
    super( LOWERED );
  }
  
  protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
    int width, int height)  {
    Color oldColor = g.getColor();
    int h = height;
    int w = width;
    
    g.translate(x, y);
    
    g.setColor(getShadowInnerColor(c));
    g.drawLine(0, 0, 0, h-1); //左
    g.drawLine(1, 0, w-1, 0); //上
    
    g.setColor(getHighlightOuterColor(c));
    g.drawLine(1, h-1, w-1, h-1); //下
    g.drawLine(w-1, 1, w-1, h-2); //右
    
    g.translate(-x, -y);
    g.setColor(oldColor);
    
  }
}
