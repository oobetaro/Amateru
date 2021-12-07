package to.tetramorph.astrocalendar;
import java.util.Calendar;
/**
 * ���t�ƃ��[���t�F�C�X(�V���A�㌷�A�����A����)��ۑ�����N���X�B
 * �G�j�A�O�����̋㑊��ۑ������肷��̂ɂ��g���B
 */
class MoonFace {
    
    private static final String [] faceNames = { "�V��","�㌷","����","����" };
    public int face;
    public Calendar date;
    /**
     * @param date face�Ŏw�肵�������ɂȂ���B
     * @param face 0�`3�Ō��̐V���A�㌷�A�����A������\��
     */
    public MoonFace(Calendar date,int face) {
        this.date = date;
        this.face = face;
    }
    /**
     * ���̃I�u�W�F�N�g�̓��t�ƌ����𕶎���ŕԂ��B
     */
    public String toString() {
        return String.format("%tY/ %tm/%td %tT",date,date,date,date) ;
    }
    
    /** 24���Ԑ��Ŏ����������Ԃ��B"0:00"�`"23:59" */
    
    public String getTime() {
        return String.format("%Tk:%TM",date,date,date).replaceAll("��","");
    }
    
    public String getName() {
        return faceNames[face];
    }
}
