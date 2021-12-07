/*
 * ColorPropertiesTool.java
 *
 * Created on 2007/02/19, 17:13
 *
 */

package to.tetramorph.util;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import to.tetramorph.util.*;

/**
 * セキュリティマネージャの導入でこのクラスはプラグイン側から使えなくなった。
 * 廃止予定。
 * オブジェクトのフィールド値をPreferenceに書き出し/読込を行う。
 * 色の設定情報などを個別にプロパティに書き出すのは面倒なので、Refrectionを使って
 * オブジェクトのフィールドを検出し、その変数名をキーとしてプロパティに値をセットする。
 * 使える型は今のところColor,Color [],booleanのみ。
 * @author 大澤義鷹
 */
class ColorPropertiesTool {
  
  /** オブジェクトを作成は禁止 */
  private ColorPropertiesTool() { }
  /**
   * Preferenceの値をoの該当するフィールドに書きこむ。未対応の型がフィールドに
   * 含まれている場合は例外を出す。
   * @exception IllegalAccessException 未対応の型
   */
  public static void setPreference(Preference p,Object o)
    throws IllegalAccessException {
    //Class c = Class.forName("to.tetramorph.starbase.ColorData");
    Class c = o.getClass();
    Field[] fields = c.getFields();
    for (int i=0;i<fields.length;i++) {
      Field fld = fields[i];
      if(! Modifier.toString(fld.getModifiers()).equals("public")) continue;
      String fieldType = fld.getType().getName();
      if(fieldType.equals("[Ljava.awt.Color;")) {
        fields[i].set(o,p.getColors(fld.getName())); //変数名でプロパティを取得
      } else if(fieldType.equals("java.awt.Color")) {
        fields[i].set(o,p.getColor(fld.getName()));
      } else if(fieldType.equals("boolean")) {
        fields[i].setBoolean(o,p.getBoolean(fld.getName()));
      } else throw new IllegalAccessException("未対応の型");
    }
  }
  /**
   * オブジェクトoのフィールド値をPreferenceに書きこむ。未対応の型がフィールドに
   * 含まれている場合は例外を出す。
   * @exception IllegalAccessException 未対応の型
   */
  public static Preference getPreference(Object o,Preference p) 
  throws IllegalAccessException {
    //Class c = Class.forName("to.tetramorph.starbase.ColorData");
    Class c = o.getClass();
    Field[] fields = c.getFields();
    for (int i=0;i<fields.length;i++) {
      Field fld = fields[i];
      if(! Modifier.toString(fld.getModifiers()).equals("public")) continue;
      String fieldType = fld.getType().getName();
      if(fieldType.equals("[Ljava.awt.Color;")) {
        Color [] col = (Color[])fields[i].get(o);
        if(col != null) p.setColors(fld.getName(),col);
      } else if(fieldType.equals("java.awt.Color")) {
        Color col = (Color)fields[i].get(o);
        if(col != null) p.setColor(fld.getName(),col);
      } else if(fieldType.equals("boolean")) {
        boolean b = (boolean)fields[i].getBoolean(o);
        p.setBoolean(fld.getName(),b);
      } else throw new IllegalAccessException("未対応の型");
    }
    return p;
  }
  
//  public static void main(String [] args) {
//    Preference pref = new Preference();
//    NewClass colorConf = new NewClass();
//    try {
//      getPreference(colorConf,pref);
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    for(Enumeration enu = pref.keys(); enu.hasMoreElements(); ) {
//      String key = (String)enu.nextElement();
//      System.out.println(key + " = " + pref.getProperty(key));
//    }
//    //別のNewClassオブジェクトを作り、値を書き換えたprefを書き戻してみる。
//    pref.setColor("signsBorder",Color.BLUE);
//    NewClass payo = new NewClass();
//    try {
//      setPreference(pref,payo);
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
//    System.out.println("#signBorder = " + payo.signsBorder);
//  }
}
