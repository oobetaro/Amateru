package to.tetramorph.astrocalendar;
import java.util.Calendar;
import java.util.Locale;
/**
 * �{�C�h�^�C����\������N���X�ŁA�{�C�h�J�n�ƏI�������A
 * �I�������Ƃ��̌��̃T�C��(�C���O���X�T�C��)����Ȃ�B
 */
class VoidTime {
  int ingSign;
  Calendar begin;
  Calendar end;
  /**
   * �{�C�h�^�C����\������I�u�W�F�N�g���쐬����B
   * @param begin �{�C�h�J�n����
   * @param end �{�C�h�I������
   * @param ingSign �C���O���X�T�C���ԍ�(0-11)
   */
  public VoidTime(Calendar begin,Calendar end,int ingSign) {
    this.begin = begin;
    this.end = end;
    this.ingSign = ingSign;
  }
  /** �{�C�h�̊J�n�����ƏI�������A�C���O���X�T�C���ԍ��������Ԃ��B*/
  public String toString() {
    return String.format("%tY/%tm/%td %tT",begin,begin,begin,begin) + "  "
      + String.format("%tY/%tm/%td %tT",end,end,end,end)
      + "  " + ingSign;
  }
  /**
   * 24���Ԑ��Ń{�C�h�J�n�����̕������Ԃ��B"0:00"�`"23:59"�Ƃ������\��
   */
  public String getBeginTime() {
    return String.format("%Tk:%TM",begin,begin,begin);
  }
  
  /**
   * 24���Ԑ��Ń{�C�h�I�������̕������Ԃ��B"0:00"�`"23:59"�Ƃ������\��
   */
  public String getEndTime() {
    return String.format("%Tk:%TM",end,end,end);
  }
  /**
   * �C���O���X�T�C���̑O�̃T�C���ԍ�(0-11)��Ԃ��B
   */
  public int getPrevSign() {
    int temp = ingSign -1;
    if(temp < 0) temp = 11;
    return temp;
  }
}