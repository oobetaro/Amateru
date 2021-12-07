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
 * �Z�L�����e�B�}�l�[�W���̓����ł��̃N���X�̓v���O�C��������g���Ȃ��Ȃ����B
 * �p�~�\��B
 * �I�u�W�F�N�g�̃t�B�[���h�l��Preference�ɏ����o��/�Ǎ����s���B
 * �F�̐ݒ���Ȃǂ��ʂɃv���p�e�B�ɏ����o���͖̂ʓ|�Ȃ̂ŁARefrection���g����
 * �I�u�W�F�N�g�̃t�B�[���h�����o���A���̕ϐ������L�[�Ƃ��ăv���p�e�B�ɒl���Z�b�g����B
 * �g����^�͍��̂Ƃ���Color,Color [],boolean�̂݁B
 * @author ���V�`��
 */
class ColorPropertiesTool {
  
  /** �I�u�W�F�N�g���쐬�͋֎~ */
  private ColorPropertiesTool() { }
  /**
   * Preference�̒l��o�̊Y������t�B�[���h�ɏ������ށB���Ή��̌^���t�B�[���h��
   * �܂܂�Ă���ꍇ�͗�O���o���B
   * @exception IllegalAccessException ���Ή��̌^
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
        fields[i].set(o,p.getColors(fld.getName())); //�ϐ����Ńv���p�e�B���擾
      } else if(fieldType.equals("java.awt.Color")) {
        fields[i].set(o,p.getColor(fld.getName()));
      } else if(fieldType.equals("boolean")) {
        fields[i].setBoolean(o,p.getBoolean(fld.getName()));
      } else throw new IllegalAccessException("���Ή��̌^");
    }
  }
  /**
   * �I�u�W�F�N�go�̃t�B�[���h�l��Preference�ɏ������ށB���Ή��̌^���t�B�[���h��
   * �܂܂�Ă���ꍇ�͗�O���o���B
   * @exception IllegalAccessException ���Ή��̌^
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
      } else throw new IllegalAccessException("���Ή��̌^");
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
//    //�ʂ�NewClass�I�u�W�F�N�g�����A�l������������pref�������߂��Ă݂�B
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
