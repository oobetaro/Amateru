package to.tetramorph.astrocalendar;
import java.io.DataInputStream;
import java.io.IOException;
import static java.util.Calendar.*;
import static java.lang.System.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
/**
 * tool�p�b�P�[�W���̓V����쐬�v���O�����ō�����V�����ǂݏo�����߂�static���\�b�h�Q�B
 * �����ł�������calendar�͓����ŃR�s�[���Ďg�p���邽�ߒl�͏����������Ă��܂����Ƃ͂Ȃ��B
 * Calendar�ň����n���̂́A�^�C���]�[����񂪂�������Ɉ����n����邩��B
 * �^�C���]�[������{�ȊO�̏ꏊ�ɃZ�b�g�����J�����_�[��n���΁A���̎����n�Ńf�[�^�͕Ԃ�B
 */
class Almanac {

    /**
     * <pre>
     * �w�肳�ꂽcalendar�̔N(����N)��(0�`11)�ɊY������{�C�h�^�C���̃��X�g��Ԃ��B
     * ���͓��t�͔N�ƌ��݂̂�F�������͕K��1���Ƃ݂Ȃ��B
     * MoonVoidAlmanac�ō쐬�����V����t�@�C�����Q�Ƃ���B
     * �V����͈̔͊O���w�肳�ꂽ�Ƃ���null��Ԃ��B
     * 
     * �V����t�@�C���ւ̃p�X�B<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/MoonVoidAlmanac.bin");
     * </pre>
     */
    public static List<VoidTime> getVoidOfCourseMoonList(Calendar calendar) {
        List<VoidTime> list = new ArrayList<VoidTime>();
        Calendar cal = (Calendar)calendar.clone();
        int year = cal.get(YEAR);
        int month = cal.get(MONTH);
        DataInputStream stream = null;
        try {
            java.net.URL fileURL = Almanac.class.getResource("/resources/MoonVoidAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean find = false;
            do {
                long beginTime = stream.readLong();
                long endTime = stream.readLong();
                int ingSign = stream.readByte();
                cal.setTimeInMillis(endTime);
                if( !find && cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH)) {
                    rec = 13; find = true;
                }
                if( find ) {
                    Calendar begin = (Calendar)calendar.clone();
                    Calendar end = (Calendar)calendar.clone();
                    begin.setTimeInMillis(beginTime);
                    end.setTimeInMillis(endTime);
                    list.add(new VoidTime(begin,end,ingSign));
                    rec--;
                }
            } while(rec>=0);
        } catch(IOException e) {
            list = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return list;
    }
    

    /**
     * <pre>
     * calendar�Ŏw�肳�ꂽ�N(����N)��(0�`11)�ɊY������
     * ����}�b�v��Ԃ��B
     * ���͓��t�͔N�ƌ��݂̂�F�������͕K��1���Ƃ݂Ȃ��B
     * MoonFaceAlmanac�ō쐬�����V����t�@�C�����Q�Ƃ���B
     * �V����͈̔͊O���w�肳�ꂽ�Ƃ���null��Ԃ��B
     * 
     * ���̖߂�l��Map�ɑ΂��āAMap#get(��)�Ƃ���ƁA�V���A�㌷�A�����A�����̓��Ȃ�A
     * ����MoonFace�I�u�W�F�N�g���Ԃ�B�Ⴄ�Ƃ���null���Ԃ�B
     * 
     * MoonVoidAlmanac�ō쐬�������[���t�F�C�X�V����t�@�C�����Q�Ƃ��āA�w��N���̃��X�g��Ԃ��B
     * �V����t�@�C���ւ̃p�X�B<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/MoonFaceAlmanac.bin");
     * </pre>
     */
    public static Map<Integer,MoonFace> getMoonFaceMap(Calendar calendar) {
        Map<Integer,MoonFace> moonFaceMap = new HashMap<Integer,MoonFace>();
        DataInputStream stream = null;
        Calendar cal = (Calendar)calendar.clone();
        try {
            //Calendar cal = Calendar.getInstance();
            java.net.URL fileURL = Almanac.class.getResource("/resources/MoonFaceAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            for(;;) {
                long time = stream.readLong();
                int face = stream.readByte();
                cal.setTimeInMillis(time);
                if( cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH) ) {
                    found = true;
                    int day = cal.get(DAY_OF_MONTH);
                    moonFaceMap.put(day,new MoonFace((Calendar)cal.clone(),face));
                }else if(found) break;
            }
        } catch(IOException e) {
            moonFaceMap = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return moonFaceMap;
    }

    /**
     * �w�肳�ꂽcalendar�̔N(����N)��(0�`11)�ɊY������
     * �V�̃C�x���g(�C���O���X�A�t�s�A���s)�̃n�b�V����Ԃ��B
     * ���͓��t�͔N�ƌ��݂̂�F�������͕K��1���Ƃ݂Ȃ��B
     * PlanetAlmanac�ō쐬�����V����t�@�C��/resources/PlanetAlmanac.bin���Q�Ƃ���B
     * �V����͈̔͊O���w�肳�ꂽ�Ƃ���null��Ԃ��B
     * 
     * �����ɕ����̃C�x���g���d�Ȃ鎖���z�肳��Ă�̂ŁA�n�b�V���̖߂�l��List�I�u�W�F�N�g�B
     * List�ɂ�PlanetEvent�I�u�W�F�N�g�������Ă���B
     * List#size()==0�Ƃ������Ƃ͂��肦�Ȃ��B�C�x���g����������null���Ԃ�B
     * 
     * �V����t�@�C���ւ̃p�X�B<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/PlanetAlmanac.bin");
     */
    public static Map<Integer,List<PlanetEvent>> getPlanetEventMap(Calendar calendar) {
        Map<Integer,List<PlanetEvent>> map = new HashMap<Integer,List<PlanetEvent>>();
        DataInputStream stream = null;
        try {
            Calendar cal = (Calendar)calendar.clone();
            java.net.URL fileURL = Almanac.class.getResource("/resources/PlanetAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            for(;;) {
                long time = stream.readLong();
                int planet = stream.readByte();
                int state = stream.readByte();
                int sign = stream.readByte();
                cal.setTimeInMillis(time);
                if( cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH) ) {
                    found = true;
                    int day = cal.get(DAY_OF_MONTH);
                    PlanetEvent pe = new PlanetEvent((Calendar)cal.clone(),planet,state,sign);
                    if(map.containsKey(day)) {
                        map.get(day).add(pe);
                    }else {
                        List<PlanetEvent> list = new ArrayList<PlanetEvent>();
                        list.add(pe);
                        map.put(day,list);
                    }
                }else if(found) break;
            }
        } catch(IOException e) {
            map = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return map;
    }
    /**
     * �w�肳�ꂽcalendar�̓�������ߋ��Ɍ������Č�������ԋ߂��V���̓������߁A
     * �������玟�̐V���̓��܂ł̃G�j�A�O�����̃��[���t�F�C�X�����X�g�Ԃ��B
     * ���̐V���܂łȂ̂Ń��X�g�̗v�f��10�B
     * EnneaMoonAlmanac�ō쐬�����V����t�@�C��/resource/EnneaMoonAlmanac.bin���Q�Ƃ��Ă���B
     * �V����͈̔͊O���w�肳�ꂽ�Ƃ���null��Ԃ��B
     */
    public static List<MoonFace> getEnneaMoonFace(Calendar calendar) {
        List<MoonFace> faceList = new ArrayList<MoonFace>();
        List<MoonFace> bufList = new ArrayList<MoonFace>();
        DataInputStream stream = null;
        //�V�����bufList�Ɉ�C�ɓǂ݂���
        try {
            URL fileURL = Almanac.class.getResource("/resources/EnneaMoonAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            while(stream.available() > 0 ) {
                long time = stream.readLong();
                int face = stream.readByte();
                Calendar cal = (Calendar)calendar.clone();
                cal.setTimeInMillis(time);
                bufList.add(new MoonFace(cal,face));
            }
        }catch(IOException e) {
            out.println(e);
        }finally {
            try {stream.close(); } catch(Exception e) { }
        }
        //����calendar�Ŏw�肳�ꂽ�������z����n�_�܂�count��i�߂�
        int count = 0;
        for(;;) {
            MoonFace face = bufList.get(count);
            if(face.date.getTimeInMillis() > calendar.getTimeInMillis()) break;
            if(count++ >= bufList.size()) return null;
        }
        //������������k���ĐV���̓��܂�count��߂�
        try {
            do {
                count--;
            } while(bufList.get(count).face != 0);
        }catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }               
        //�V�����玟�̐V���܂ł̌��̗��߂�l�p�̃��X�g�Ɋi�[
        for(int i=0; i<10; i++) faceList.add(bufList.get(count+i));;
        return faceList;
    }
    /**
     * �G�j�A�O�����J�����_�[�p�̓V����S�Ă����X�g�ɓ���ĕԂ��B
     */
    public static List<MoonFace> getEnneaAlmanac(TimeZone zone) {
      List<MoonFace> bufList = new ArrayList<MoonFace>();
      DataInputStream stream = null;
      try {
        URL fileURL = Almanac.class.getResource("/resources/EnneaMoonAlmanac.bin");
        stream = new DataInputStream(fileURL.openStream());
        while(stream.available() > 0 ) {
          long time = stream.readLong();
          int face = stream.readByte();
          Calendar cal = Calendar.getInstance(zone);
          cal.setTimeInMillis(time);
          bufList.add(new MoonFace(cal,face));
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try { stream.close(); } catch(Exception e) { }
      }
      return bufList;
    }
}

