/*
 * AstroCalendar.java
 *
 * Created on 2006/05/22, 6:58
 */

package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import static java.lang.System.*;
import java.net.URL;
import java.text.DateFormatSymbols;
import static java.util.Calendar.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import to.tetramorph.starbase.util.AstroFont;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Home;
import to.tetramorph.util.TimeZoneDialog;

/**
 * �{�C�h�^�C����V�̉^�s��\������V�̃J�����_�[�B
 * @author ���V�`��
 */
public final class AstroCalendar extends javax.swing.JFrame {
    private Font astrofont;
    private Font smallAstroFont;
    private Font monthFont;
    private Font smallFont = new Font("SansSerif",Font.PLAIN,10);
    private Font dayNumberFont = new Font("SansSerif",Font.PLAIN,20);
    private Font weekHeadderFont = null;
    private String [] weekName;
    private static final String [] STATE = { "","","D","R" };
    private final static String [] defSignColors = {
        "0xff8080","0xffff80","0x80ff80","0x8080ff","0xff8080","0xffff80",
        "0x80ff80","0x8080ff","0xff8080","0xffff80","0x80ff80","0x8080ff"};
    private final static String [] weekKeys = { 
        "sunday","monday","tuesday","wednesday","thursday","friday","saturday" };
    private final static String [] defWeekColors = {
        "0xff0000","0","0","0","0","0","0x0000ff" };
    private String [] monthNames;
    private Color [] weekColors = new Color[7];
    private Color [] weekHeadderColors = new Color[7];
    private int width,height;
    private Calendar cal;
    private Calendar todayCal;
    private CalendarPanel calPanel;
    private Color [] testcolor = new Color[12];
    private Color [] signSymColors = new Color[12];
    private Color cellColor;             //�ʏ�̓��t�Z���̔w�i�F
    private Color todayCellColor;        //�����̓��t�Z���̔w�i�F
    private Color lineColor;             //�Z����`�����̐F
    private Color bgColor;               //�J�����_�[�̂���ɊO���̔w�i�F
    private Color weekCellColor;         //�j���w�b�_�Z���̔w�i�F
    private Color monthNumberColor;      //���̐��̕����F
    private Color yearNumberColor;       //�N�̐��̕����F
    private Color voidTimeStringColor;   //�{�C�h���Ԃ�\�����镶���F
    private Color nullCellColor;         //�������݂��Ȃ��Z���̔w�i�F
    private Color voidBarColor;          //�{�C�h�o�[�̐F
    private Color moonFaceColor;         //���[���t�F�C�X�̋L���ƕ����F
    private int headderHeight = 25;
    private boolean outOfRange = false;
    private int dayOfMonth = 0;
    private int dayOfWeek = 0;
    private List<VoidTime> voidList = null;
    private List<Integer>dayList = null;
    private int zodiacBarHeight = 18;
    private Image testImage;
    private Properties prop; //�F�ݒ�̃v���p�e�B
    private Properties holidayProp = new Properties(); //�x���p
    private Properties masterProp; //�V�X�e�����p
    private List<JRadioButtonMenuItem> themeList = 
                                        new ArrayList<JRadioButtonMenuItem>();
    private ButtonGroup themeButtonGroup = new ButtonGroup();
    private File masterPropFile = 
                   new File(Home.properties,"AstroCalendarConfig.properties");
    private int [] moonCourseBitmap = null;
    private Map<Integer,MoonFace> moonFaceMap;
    private Map<Integer,java.util.List<PlanetEvent>> planetEventMap;
    private javax.swing.Timer clockTimer;
    private Map<String,String> voidStampMap = new HashMap<String,String>();
    
    public AstroCalendar() {
        this(true);
    }
    /**
     * �V�̃J�����_�[�I�u�W�F�N�g�����
     */
    public AstroCalendar(boolean singleExec) {
        // �萯�p�t�H���g�̓Ǎ�
        astrofont = AstroFont.getFont(14f); //FileTools.getAstroFont();
        //�}�X�^�[�̐ݒ��񃍁[�h(���̈ʒu�Ƃ��^�C���]�[���ۑ��p)
        masterProp = new Properties();
        if(! FileTools.loadProperties(masterProp,masterPropFile)) {
            masterProp.setProperty("ColorConfigNumber","0");
        }
        //�V���b�g�_�E���t�b�N��o�^
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Rectangle rect = getBounds(null);
                masterProp.setProperty("frame.x",""+rect.x);
                masterProp.setProperty("frame.y",""+rect.y);
                masterProp.setProperty("frame.width",""+rect.width);
                masterProp.setProperty("frame.height",""+rect.height);
                masterProp.setProperty("cell.width",""+width);
                masterProp.setProperty("cell.height",""+height);
                clockTimer.stop();
                FileTools.saveProperties(masterProp,masterPropFile,
                    "AstroCalendar master configuration");
            }
        });
        setNow();
        setVoidMap();
        smallAstroFont = AstroFont.getFont(14f); //astrofont.deriveFont(14F);
        astrofont = AstroFont.getFont(14f); //astrofont.deriveFont(14F);
        monthFont = new Font("SansSerif",Font.PLAIN,30);
        
        initComponents();
        if(! singleExec )
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        calPanel = new CalendarPanel(
            Integer.parseInt(masterProp.getProperty("cell.width","80")),
            Integer.parseInt(masterProp.getProperty("cell.height","80")));
        mainPanel.add(calPanel,BorderLayout.CENTER);
        setBounds(
            Integer.parseInt(masterProp.getProperty("frame.x","0")),
            Integer.parseInt(masterProp.getProperty("frame.y","0")),
            Integer.parseInt(masterProp.getProperty("frame.width","600")),
            Integer.parseInt(masterProp.getProperty("frame.height","600"))
            );
        pack();
        DateChangeHandler dateChanger = new DateChangeHandler();
        yearIncButton.addActionListener(dateChanger);
        yearDecButton.addActionListener(dateChanger);
        monthIncButton.addActionListener(dateChanger);
        monthDecButton.addActionListener(dateChanger);
        nowButton.addActionListener(dateChanger);
        configButton.addActionListener(dateChanger);
        //�|�b�v�A�b�v���j���[��g�ݗ��Ă�
        JMenuItem confMenu1 = new JMenuItem("�j���f�[�^���_�E�����[�h");
        JMenuItem confMenu2 = new JMenuItem("�z�F�ݒ�������[�h");
        configPopup.add(confMenu1);
        configPopup.add(confMenu2);
        configPopup.add(new JSeparator());
        //�J���[�f�U�C���̃e�[�}�v���p�e�B�����[�h���ă��j���[�ɂ���B
        JMenu themeMenu = new JMenu("�J���[�e�[�}");
        for(int i=0; i<10; i++) {
            loadConfig(""+i);
            String title = prop.getProperty("title");
            if(title == null) break;
            themeList.add(new JRadioButtonMenuItem(title));
            themeMenu.add(themeList.get(i));
            themeButtonGroup.add(themeList.get(i));
        }
        configPopup.add(themeMenu);
        int themeNum = Integer.parseInt(masterProp.getProperty("ColorConfigNumber"));
        themeButtonGroup.setSelected(themeList.get(themeNum).getModel(),true);
        //����Z���N�g�̃��j���[
        ButtonGroup langButtonGroup = new ButtonGroup();
        JMenu langMenu = new JMenu("����̑I��");
        Locale [] localeList = Locale.getAvailableLocales();
        //localList�̏d�����Ă��錾�꺰�ނ��ư��ɂ��Ď��o��
        HashSet<String> hash = new HashSet<String>();
        for(Locale s:localeList) hash.add(s.getLanguage());
        Iterator ite = hash.iterator();
        while(ite.hasNext()) {
            String language = (String)ite.next();
            Locale locale = new Locale(language);
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(locale.getDisplayLanguage());
            langButtonGroup.add(item);
            item.setActionCommand(language);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    masterProp.setProperty("language",event.getActionCommand());
                    setMonthNameByLocale();
                    repaint();
                }
            });
            langMenu.add(item);
        }
        configPopup.add(new JSeparator());
        configPopup.add(langMenu);
        JMenuItem confMenu3 = new JMenuItem("�^�C���]�[����ύX");
        configPopup.add(confMenu3);
        confMenu2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadConfig(masterProp.getProperty("ColorConfigNumber"));
            }
        });
        //�����ύX�A�N�V����
        confMenu3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TimeZone zone = TimeZoneDialog.showDialog(AstroCalendar.this);
                //TimeZone zone = TimeZoneDialog.showDialog((Component)event.getSource());
                if(zone != null) {
                    masterProp.setProperty("TimeZoneID",zone.getID());
                    cal.setTimeZone(zone);
                    todayCal.setTimeZone(zone);
                    setNow();
                    setVoidMap(); //������ύX����΃{�C�h���͍�蒼��
                    setMonthNameByLocale();
                    repaint();
                }
            }
        });
        confMenu1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                downloadHoliday();
            }
        });
        for(int i=0; i<themeList.size(); i++) {
            themeList.get(i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    for(int j=0; j<themeList.size(); j++) {
                        if(event.getSource() == themeList.get(j)) {
                            loadConfig(""+j);
                            masterProp.setProperty("ColorConfigNumber",""+j);
                        }
                    }
                }
            });
        }
        loadConfig(masterProp.getProperty("ColorConfigNumber"));
        loadHoliday();
        setMonthNameByLocale();
        Border zoneBorder = BorderFactory.createEmptyBorder(0,10,0,0);
        //zoneLabel.setForeground(monthNumberColor);
        zoneLabel.setBorder(zoneBorder);
        //clockLabel.setForeground(monthNumberColor);
        clockLabel.setBorder(zoneBorder);
        //���v
        clockTimer = new javax.swing.Timer(1000,new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TimeZone zone = TimeZone.getTimeZone(masterProp.getProperty("TimeZoneID",TimeZone.getDefault().getID()));
                Calendar cal = Calendar.getInstance(zone);
                zoneLabel.setForeground(monthNumberColor);
                clockLabel.setForeground(monthNumberColor);
                clockLabel.setText(String.format(getLocaleProperty(),"%tB %td %ta, %tp %tl:%tM:%tS",cal,cal,cal,cal,cal,cal,cal));
                clockLabel.revalidate();
            }
        });
        clockTimer.start(); //stop()�̓V���b�g�_�E���t�b�N�̒��ōs���B
        
    }
    
//����N��ݒ�{�^���̃C�x���g���X�i
    class DateChangeHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            Object button = event.getSource();
            if(button == yearIncButton)
                cal.add(YEAR,1);
            else if(button == yearDecButton)
                cal.add(YEAR,-1);
            else if(button == monthIncButton)
                cal.add(MONTH,1);
            else if(button == monthDecButton)
                cal.add(MONTH,-1);
            else if(button == nowButton) {
                setNow();
            }
            setVoidMap();
            setMonthNameByLocale();
            calPanel.repaint();
        }
    }
//�l�b�g����j���v���p�e�B�t�@�C�������[�h����
    void downloadHoliday() {
        InputStream inputStream = null;
        File holidayFile = new File(Home.properties,"Holiday.properties");
        holidayProp = new Properties();
        try {
            URL url = new URL("http://tetramorph.to/Holiday.properties");
            inputStream = url.openStream();
            holidayProp.loadFromXML(inputStream);
            out.println("�_�E�����[�h����");
        } catch(IOException e) {
            out.println(e);
        } finally { try { inputStream.close(); }catch(Exception e) { } }
        
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(holidayFile);
            holidayProp.storeToXML(outputStream,"�j���f�[�^");
        }catch(IOException e) {
            out.println(e);
        }finally { try { outputStream.close(); } catch(Exception e) { } }
        repaint();
    }
//�j���v���p�e�B�t�@�C�������[�h����
    void loadHoliday() {
        InputStream inputStream = null;
        File holidayFile = new File(Home.properties,"Holiday.properties");
        if(holidayFile.exists()) {
            holidayProp = new Properties();
            try {
                inputStream = new FileInputStream(holidayFile);
                holidayProp.loadFromXML(inputStream);
            } catch(IOException e) {
                out.println(e);
            } finally { try { inputStream.close(); }catch(Exception e) { } }
        } else {
            // �j���f�[�^������܂���B�l�b�g���[�N����_�E�����[�h���܂����H(yes/no)
            // downloadHoliday();
        }
    }
//�J�����gCalendar��������1���ɃZ�b�g����Calendar�����ݎ����ɃZ�b�g
    private void setNow() {
        TimeZone zone = TimeZone.getTimeZone(masterProp.getProperty("TimeZoneID",TimeZone.getDefault().getID()));
        cal = Calendar.getInstance(zone);
        todayCal = (Calendar)cal.clone();
        cal.set(DAY_OF_MONTH,1);
    }
//�{�C�h�����i�[�������X�g��}�b�v���쐬�B�O���[�o���ϐ���cal���Q�Ƃ��Ă���B
    private void setVoidMap() {
        voidList = Almanac.getVoidOfCourseMoonList(cal);
        moonFaceMap = Almanac.getMoonFaceMap(cal);
        planetEventMap = Almanac.getPlanetEventMap(cal);
        if(voidList == null||moonFaceMap == null||planetEventMap == null) outOfRange = true;
        else outOfRange = false;
        dayOfMonth = cal.getActualMaximum(DAY_OF_MONTH);
        dayOfWeek = cal.get(DAY_OF_WEEK);
        dayList = new ArrayList<Integer>();
        for(int i=1; i<= dayOfMonth; i++) dayList.add(i);
        for(int i=1; i<  dayOfWeek; i++) dayList.add(0,null);
    }
//���P�[���ɏ]�����u���A�T�A�^�C���]�[���v�̖��O��z��ɃZ�b�g����B
    void setMonthNameByLocale() {
        //12�����̌��̖��O�����P�[���ɏ]���Ď擾�B�f�t�H���g�͓��{��B
        //�I�v�V�����ɂ���ė��́E�t���l�[����ʂ肠��B
        Locale locale = getLocaleProperty();
        DateFormatSymbols dateSym = new DateFormatSymbols(locale);
        monthNames = (prop.getProperty("monthShortName","no").equals("yes")) ?
            dateSym.getShortMonths() : dateSym.getMonths();
        weekName = (prop.getProperty("weekShortName","no").equals("yes")) ?
            dateSym.getShortWeekdays() : dateSym.getWeekdays();
        TimeZone zone = TimeZone.getTimeZone(masterProp.getProperty("TimeZoneID",TimeZone.getDefault().getID()));
        zoneLabel.setText(zone.getDisplayName(true,TimeZone.SHORT,locale));
        //�{�C�h���ԕ������AM,PM,�ߑO,�ߌ�Ȃǂ��ύX���Ȃ���Ȃ��
        if(!outOfRange) {
            voidStampMap = new HashMap<String,String>();
            for(int i=0; i<voidList.size(); i++) {
                VoidTime vt = voidList.get(i);
                if(equalCalendar(vt.begin,vt.end)) {
                    String time = vt.getBeginTime() + "#" + vt.getEndTime();
                    voidStampMap.put(""+vt.begin.get(MONTH) + "."+vt.begin.get(DAY_OF_MONTH),time);
                }else {
                    voidStampMap.put(""+vt.begin.get(MONTH) + "."+vt.begin.get(DAY_OF_MONTH),vt.getBeginTime()+"�`");
                    voidStampMap.put(""+vt.end.get(MONTH)+"."+vt.end.get(DAY_OF_MONTH),"�`"+vt.getEndTime());
                };
            }
        }
    }
//�J�����_�[���m��N�����܂ł���v���Ă��r�������Ȃ�^��Ԃ��B
    boolean equalCalendar(Calendar cal1,Calendar cal2) {
        return cal1.get(DAY_OF_MONTH) == cal2.get(DAY_OF_MONTH) &&
            cal1.get(MONTH) == cal2.get(MONTH) &&
            cal1.get(YEAR) == cal2.get(YEAR);
    }
//�}�X�^�[�v���p�e�B����Locale�����擾���ĕԂ��B
    Locale getLocaleProperty() {
        return new Locale(masterProp.getProperty("language","ja"),
            masterProp.getProperty("country","JP"));
    }
//�f�U�C�������t�@�C������ǂ݂��ށB�t�@�C���͕�����Ŕԍ��Ŏw�肷��B"0"�Ƃ�"1"�Ƃ��B
    void loadConfig(String num) {
        prop = new Properties();
        InputStream inputStream = getClass()
        .getResourceAsStream("/resources/AstroCalendar"+num+".properties");
        if(! FileTools.loadProperties(prop,inputStream)) return;
        String fontName = prop.getProperty("weekHeadderFontName","SansSerif");
        int fontSize = integer("weekHeadderFontSize","20");
        weekHeadderFont = new Font(fontName,Font.PLAIN,fontSize);
        fontName = prop.getProperty("dayNumberFontName","SansSerif");
        fontSize = integer("dayNumberFontSize","20");
        dayNumberFont = new Font(fontName,Font.PLAIN,fontSize);
        
        for(int i=0; i<ZODIAC_NAMES.length; i++) {
            testcolor[i] = color(ZODIAC_NAMES[i]+"Color",defSignColors[i]);
            signSymColors[i] = color(ZODIAC_NAMES[i]+"SignColor","0x000000");
        }
        if(prop.getProperty("mixColorMode","no").equals("yes")) mixColor();
        cellColor = color("cellColor","0xffffff");
        todayCellColor = color("todayCellColor","0xffffc0");
        lineColor = color("lineColor","0x000000");
        bgColor =  color("bgColor","0xffffff");
        setBackground(bgColor);
        
        for(int i=0; i<weekKeys.length; i++) {
            weekColors[i] = color(weekKeys[i]+"Color",defWeekColors[i]);
            weekHeadderColors[i] = color(weekKeys[i]+"HeadderColor",defWeekColors[i]);
        }
        weekCellColor = color("weekCellColor","0xffffff");
        monthNumberColor = color("monthNumberColor","0");
        voidTimeStringColor = color("voidTimeStringColor","0");
        yearNumberColor = color("yearNumberColor","0");
        nullCellColor = color("nullCellColor","0x2e6b4c");
        voidBarColor = color("voidBarColor","0xFF0000");
        moonFaceColor = color("moonFaceColor","0x0");
        out.println("�F�ʐݒ�����[�h���܂���");
        setMonthNameByLocale();
        repaint(); //�J�����_�[�̍ĕ`���v��
    }
//�f�U�C���v���p�e�B����l���Ƃ肾���AColor�I�u�W�F�N�g�ŕԂ��B
//defValue�ɂ̓v���p�e�B�ɓo�^���Ȃ������Ƃ��ɍ̗p����l���w�肷��B
    Color color(String propKey,String defValue) {
        int v = intValue(prop.getProperty(propKey,defValue));
        if( (v & 0xff000000) != 0) return new Color(v,true);
        return new Color(v);
    }
//�f�U�C���v���p�e�B����l�����o���AInteger�I�u�W�F�N�g�ŕԂ��B
    Integer integer(String key,String value) {
        return intValue(prop.getProperty(key,value));
    }
//������̐�����Integer�ɂ��ĕԂ����A�ُ�ȕ�����Ȃ�0��Ԃ��B
//�܂�Exception���o���Ȃ����߂̂��́B
    Integer intValue(String value) {
        int i = 0;
        try { i = Integer.decode(value); } catch(Exception e) { }
        return i;
    }
//12�F�Őԉ��ΐƕω�����O���f�[�V�����J���[�����
//4�F���w�肷��ƁA�����̐F�����������12�F�̐F�̉~�����B
    void mixColor() {
        //testcolor[0] = RED;
        testcolor[1] = addColor(addColor(testcolor[0],testcolor[3]),testcolor[0]);
        testcolor[2] = addColor(addColor(testcolor[0],testcolor[3]),testcolor[3]);
        //testcolor[3] = YELLOW;
        testcolor[4] = addColor(addColor(testcolor[3],testcolor[6]),testcolor[3]);
        testcolor[5] = addColor(addColor(testcolor[3],testcolor[6]),testcolor[6]);
        //testcolor[6] = GREEN;
        testcolor[7] = addColor(addColor(testcolor[6],testcolor[9]),testcolor[6]);
        testcolor[8] = addColor(addColor(testcolor[6],testcolor[9]),testcolor[9]);
        //testcolor[9] = BLUE;
        testcolor[10] = addColor(addColor(testcolor[9],testcolor[0]),testcolor[9]);
        testcolor[11] = addColor(addColor(testcolor[9],testcolor[0]),testcolor[0]);
    }
//��̐F�����������J���[��Ԃ��B(RGB��ʁX�ɑ������킹�ĕ��ς����)�B
    Color addColor(Color color1, Color color2) {
        int r = (color1.getRed() + color2.getRed()) / 2;
        int g = (color1.getGreen() + color2.getGreen()) / 2;
        int b = (color1.getBlue() + color2.getBlue()) /2;
        int a = (color1.getAlpha() + color2.getAlpha())/2;
        return new Color(r,g,b,a);
    }
//�J�����_�[���O���t�B�b�N���쐬����p�l��
    class CalendarPanel extends JPanel {
        Image moonCourseImage = null;
        CalendarPanel(int cellWidth,int cellHeight) {
            setPreferredSize(new Dimension(cellWidth*7,cellHeight*6));
            setOpaque(false);
        }
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D)graphics;
            //�A���`�G�C���A�V���O�ŃX���[�X�Ȑ���`�������Ƃ���set����
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            //�[��������Ζ������ɐ؂�グ
            int rowSize = (((float)dayList.size() % 7) == 0 ) ?
                dayList.size()/7 : dayList.size()/7+1;
            //���̈�̕���7�Ŋ���1�Z���̕������߂�B
            //����O��-1���Ă���̂́A�O�g�����̈�O�ɂ݂͂����ĕ`�悳��Ȃ��������邽��
            width = (getVisibleRect().width-1) / 7;
            height = (getVisibleRect().height - headderHeight - 40 - 1) / rowSize;
            g.translate(getVisibleRect().x,getVisibleRect().y); //���̈�̍���������_�Ƃ���
            int zodiacBarWidth = width * dayOfMonth;
            moonCourseBitmap = new int[zodiacBarWidth];
            moonCourseImage = createImage(zodiacBarWidth,zodiacBarHeight);
            Graphics2D mg = (Graphics2D)moonCourseImage.getGraphics();
            int sign = -1;
            if(! outOfRange) {
                //���̏��߂̐[��0�������_�Ƃ���
                Calendar zeroCal = (Calendar)cal.clone();
                zeroCal.set(DAY_OF_MONTH,1);
                zeroCal.set(HOUR_OF_DAY,0);zeroCal.set(MINUTE,0);zeroCal.set(SECOND,0);
                long zero = zeroCal.getTimeInMillis();
                
                java.util.List<Calendar> signList = new ArrayList<Calendar>();
                signList.add(zeroCal);
                for(int i=0; i<voidList.size(); i++) signList.add(voidList.get(i).end);
                //���̏I���͗����̏��߂Ƃ���
                Calendar botomCal = (Calendar)zeroCal.clone();
                botomCal.add(MONTH,1);
                signList.add(botomCal);
                sign = voidList.get(0).getPrevSign();
                //�b�уo�[��`��
                for(int i=0; i<signList.size()-1; i++) {
                    long bt = (signList.get(i).getTimeInMillis() - zero)/1000L;
                    long et = (signList.get(i+1).getTimeInMillis() - zero)/1000L;
                    int s = Math.round(bt/3600f/24f * width);
                    int w = Math.abs(Math.round(et/3600f/24f * width - bt/3600f/24f * width));
                    mg.setColor(testcolor[sign]);
                    mg.fillRect(s,0,w,zodiacBarHeight);
                    //�������蕗�Ƀo�[�̏㉺�ɖ��邢���ƈʐ���`��
                    mg.setColor(Color.BLACK);
                    mg.drawLine(s,0,s+w,0);
                    mg.setColor(testcolor[sign].brighter());
                    mg.drawLine(s,1,s+w,1);
                    mg.setColor(testcolor[sign].darker());
                    mg.drawLine(s,zodiacBarHeight-1,s+w,zodiacBarHeight-1);
                    for(int j=0; j<w; j++) {
                        if((j+s)<moonCourseBitmap.length) moonCourseBitmap[j+s] = sign;
                    }
                    sign++; if(sign >= 12 ) sign=0;
                }
                // �{�C�h�o�[��`��
                for(int i=0; i<voidList.size(); i++) {
                    VoidTime vt = voidList.get(i);
                    //�{�C�h�J�n�E�I���������猴�_�̎����������A24���Ԃ�1.0�Ƃ����l�ɕϊ��B�Z���̕���������B
                    long bt = (vt.begin.getTimeInMillis() - zero)/1000L;
                    long et = (vt.end.getTimeInMillis()   - zero)/1000L;
                    int p1 = Math.round(bt/3600f/24f * width);
                    int p2 = Math.abs(Math.round(et/3600f/24f * width - bt/3600f/24f * width));
                    mg.setColor(addColor(voidBarColor,testcolor[vt.getPrevSign()]));
                    //Rectangle voidRect = new Rectangle(p1,0,p2,zodiacBarHeight/2);
                    //mg.fill(voidRect);
                    mg.fillRect(p1,2,p2,zodiacBarHeight/2-2);
                }
            }
            //�N����`��
            FontRenderContext render = g.getFontRenderContext();
            TextLayout tl = new TextLayout(""+(cal.get(MONTH)+1),monthFont,render);
            g.setFont(monthFont);
            g.setColor(monthNumberColor);
            //���̖��O(May,June��)�͍���
            g.drawString(monthNames[cal.get(MONTH)],0,30);
            //���̐����͒���
            g.drawString(""+(cal.get(MONTH)+1),(getVisibleRect().width - tl.getAdvance())/2,30);
            //�N���͉E��
            tl = new TextLayout(""+(cal.get(YEAR)),monthFont,render);
            g.setColor(yearNumberColor);
            g.drawString(""+cal.get(YEAR),getVisibleRect().width - tl.getAdvance(),30);
            // �j���w�b�_��`��
            g.translate(getVisibleRect().x,getVisibleRect().y+40);
            StringBuffer sb = new StringBuffer();
            float w = 0f;
            out.println("weekCellPaint = " + prop.getProperty("weekCellPaint"));
            for(int col = 0; col < 7 && prop.getProperty("weekCellPaint","yes").equals("yes"); col++) {
                g.setColor(weekCellColor);
                g.fillRect(col*width,0,width,headderHeight);
                g.setColor(lineColor);
                g.drawRect(col*width,0,width,headderHeight);
                g.setColor(weekCellColor);
                g.draw3DRect(col*width+1,1,width-2,headderHeight-2,true);
            }
            g.setFont(weekHeadderFont);
            for(int col = 0; col < 7; col++) {
                g.setColor(weekHeadderColors[col]);
                TextLayout textLayout = new TextLayout(weekName[col+1],weekHeadderFont,render);
                //sb.append(weekName[col+1]);
                float h = (textLayout.getAscent() + textLayout.getDescent()) / 2f;
                float ad =  textLayout.getAdvance();
                if(ad>w) w = ad;
                int tx = Math.round(col*width+width/2-ad/2);
                int ty = Math.round(headderHeight/2+h/2+1);
                g.setColor(Color.DARK_GRAY);
                g.drawString(weekName[col+1],tx+1,ty+1);
                g.setColor(weekHeadderColors[col]);
                g.drawString(weekName[col+1],tx,ty);
            }
            //�J�����_�[�̈��h��Ԃ��A�O�g����`��
            g.setColor(nullCellColor);
            g.fillRect(0,headderHeight,width*7,rowSize*height);
            g.setColor(lineColor);
            g.drawRect(0,headderHeight,width*7,rowSize*height);
            //1�Z�����J�����_�[��`��
            for(int row = 0; row < rowSize; row++) {
                for(int col = 0; col < 7; col++) {
                    paintCell(col,row,dayList,g);
                }
            }
        }
        void paintCell(int col,int row,java.util.List<Integer> dayList,Graphics2D g) {
            int x = col * width;
            int y = row * height + headderHeight;
            int n = row * 7 + col;
            String ym = cal.get(YEAR) + "/" + (cal.get(MONTH)+1) + "/";
            if(n<dayList.size()) {
                if(dayList.get(n) != null) {
                    int day = dayList.get(n);
                    int x2 = (day - 1) * width;
                    //1�`31�̓�����\��(���̓��t�Ȃ�Z���F��ω�������)
                    if(todayCal.get(DAY_OF_MONTH) == day &&
                        todayCal.get(MONTH) == cal.get(MONTH) &&
                        todayCal.get(YEAR) == cal.get(YEAR))
                        g.setColor(todayCellColor);
                    else g.setColor(cellColor);
                    g.fillRect(x,y,width,height-16);
                    
                    g.setFont(dayNumberFont);
                    g.setColor(new Color(0xc0c0c0));
                    g.drawString(""+day,x+4+1,y+15+1); // ���t�̐����̉e
                    //�y���j���̐����̐F���Z�b�g
                    String holiday = holidayProp.getProperty(ym+day);
                    if(holiday == null) g.setColor(weekColors[col]);
                    else g.setColor(weekColors[0]);
                    g.drawString(""+day,x+4,y+15); //���t�̐���
                    //�j�����̕\��
                    if(holiday != null && height > 72) {
                        g.setFont(smallFont);
                        g.drawString(holiday,x+5,y+26);
                    }
                    //����\��
                    if(!outOfRange) {
                        MoonFace moonface = moonFaceMap.get(day);
                        if(moonface != null) {
                            g.setColor(moonFaceColor);
                            g.setFont(astrofont);
                            g.drawString(""+MOON_CHARS[moonface.face],x+width-15,y+16);
                            String time = moonface.getTime();
                            int offset = (time.length() == 4) ? 38:44;
                            g.setFont(smallFont);
                            g.drawString(moonface.getTime(),x+width-offset,y+14);
                        }
                        //�b�т̑т�`��(��ɗp�ӂ����щ摜����R�s�[���Ă���)
                        g.drawImage(moonCourseImage, x, y + height-zodiacBarHeight,
                            x + width, y + height,
                            x2,0,x2+width,zodiacBarHeight,this);
                        //�����V���{����`��
                        int center = (day-1)*width + width/2;
                        int sign = moonCourseBitmap[center];
                        //�������獶�E10px�̈ʒu�𒲂ׂĐ����ԍ����قȂ�΁A���傤�ǐ����ؑ֓_�������ɂ���A
                        //�����ɐ����L���������Ƌ��ڂɕ`���Ă��܂����Ƃ��Ӗ����Ă���̂ŁB���ڂɂ͕`���Ȃ��B
                        if(moonCourseBitmap[center-10] == moonCourseBitmap[center+10]) {
                            FontRenderContext render = g.getFontRenderContext();
                            TextLayout textLayout = new TextLayout(""+ZODIAC_CHARS[sign],astrofont,render);
                            TextLayout textLayoutShadow = new TextLayout(""+ZODIAC_CHARS[sign],astrofont,render);
                            float h = textLayout.getAscent() + textLayout.getDescent();
                            float w = width/2f - textLayout.getAdvance()/2f;
                            
                            AffineTransform at = new AffineTransform();
                            at.translate((float)x+w+1, y+height-zodiacBarHeight/2+h/2+1-1);
                            g.setColor(Color.DARK_GRAY);
                            g.fill(textLayoutShadow.getOutline(at));
                            
                            at = new AffineTransform();
                            at.translate((float)x+w, y+height-zodiacBarHeight/2+h/2-1);
                            g.setColor(signSymColors[sign]);
                            g.fill(textLayout.getOutline(at));
                        }
                    }
                    //�����̓��t�̃Z���Ȃ玞���j��`��
                    if(todayCal.get(DAY_OF_MONTH) == day &&
                        todayCal.get(MONTH) == cal.get(MONTH) &&
                        todayCal.get(YEAR) == cal.get(YEAR)) {
                        
                        int w = Math.round((todayCal.get(HOUR_OF_DAY)/24f
                            + todayCal.get(MINUTE)/24f/60f) * width);
                        g.setColor(Color.RED);
                        g.drawLine(x+w,y + height-zodiacBarHeight+2,x+w,y + height-2);
                    }
                    //�{�C�h�J�n�`�I�����Ԃ�`��
                    if(!outOfRange) {
                        String time = voidStampMap.get(""+cal.get(MONTH)+"."+day);
                        if(time != null) {
                            g.setFont(smallFont);
                            g.setColor(voidTimeStringColor);
                            FontRenderContext render = g.getFontRenderContext();
                            String vstr = time.replaceAll("#","�`");
                            TextLayout textLayout = new TextLayout(vstr,smallFont,render);
                            int w = (int)((width - textLayout.getAdvance())/2);
                            g.drawString(vstr,x+w,y+height-zodiacBarHeight-1);
                        }
                    }
                    //�V�̃C�x���g(�C���O���X/�t�s/���s)��`��
                    if(!outOfRange) {
                        java.util.List<PlanetEvent> evList = planetEventMap.get(day);
                        if(evList != null) {
                            //g.setFont(smallFont);
                            g.setColor(new Color(0x990040));
                            int c = (width - 30)/2+5;
                            int lh = 14;  //line height
                            int yofs = (height-lh)/2;
                            if(moonFaceMap.get(day)== null && evList.size()>1) yofs -= 10;
                            else yofs += 10;
                            for(int i=0; i<evList.size(); i++) {
                                PlanetEvent ev = evList.get(i);
                                String s = "";
                                if(ev.state < 2) {
                                    g.setFont(smallAstroFont);
                                    g.setColor(Color.BLACK);
                                    g.drawString(""+BODY_CHARS[ev.planet]+ZODIAC_CHARS[ev.sign],
                                        c+x+5, y+yofs+(i*lh));
                                    g.setFont(smallFont);
                                } else {
                                    g.setFont(smallAstroFont);
                                    g.drawString(""+ZODIAC_CHARS[ev.sign]+BODY_CHARS[ev.planet],
                                        c+x+5,y+yofs+(i*lh));
                                    g.setFont(smallFont);
                                    g.drawString(STATE[ev.state],c+x+30,y+yofs+(i*lh)-1);
                                }
                            }
                        }
                    }
                    //�Z���̘g����`��
                    g.setColor(lineColor);
                    g.drawRect(x,y,width,height);
                }
            }
        }
    }
    /**
     * �V�̃J�����_�[�����s�B����͂��̃N���X�P�̂œ��삳����Ƃ��̃G���g���[�B
     * exec(true)�����s���邾���B
     */
    public static void main(String args[]) {
        exec(true);
    }
    /**
     * �V�̃J�����_�[�����s�B������true�ɂ��ăR�[������ƁA�t���[���~�{�^����
     * SystemExit����Bfalse��n���ƁADISPOSE���邾����SystemExit�͂��Ȃ��B
     * ���C���̃A�v���P�[�V��������Ăяo���Ƃ���false��^���ČĂяo���΂悢�B
     */
    public static void exec(final boolean singleExec ) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if(UIManager.getLookAndFeel().getName().equals("Metal")) {
                    UIManager.put("swing.boldMetal", Boolean.FALSE);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    Toolkit.getDefaultToolkit().setDynamicLayout(true);
                }
                AstroCalendar ac = new AstroCalendar();
                if(! singleExec )
                    ac.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                ac.setVisible(true);
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel buttonPanel1;
    javax.swing.JPanel controlPanel1;

    configPopup = new javax.swing.JPopupMenu();
    mainPanel = new javax.swing.JPanel();
    mainPanel = new JPanel() {
      public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D)graphics;
        int h = getSize().height;
        int w = getSize().width;
        g.setColor(bgColor);
        g.fillRect(0,0,w,h);
      }
    };
    controlPanel1 = new javax.swing.JPanel();
    buttonPanel1 = new javax.swing.JPanel();
    yearDecButton = new javax.swing.JRadioButton();
    yearIncButton = new javax.swing.JRadioButton();
    nowButton = new javax.swing.JRadioButton();
    monthDecButton = new javax.swing.JRadioButton();
    monthIncButton = new javax.swing.JRadioButton();
    confPanel = new javax.swing.JPanel();
    configButton = new javax.swing.JRadioButton();
    clockLabel = new javax.swing.JLabel();
    zoneLabel = new javax.swing.JLabel();

    getContentPane().setLayout(new java.awt.GridLayout(1, 0));

    setTitle("AstroCalendar");
    setBackground(new java.awt.Color(255, 0, 102));
    mainPanel.setLayout(new java.awt.BorderLayout());

    mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 10));
    controlPanel1.setLayout(new java.awt.BorderLayout());

    controlPanel1.setOpaque(false);
    buttonPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 5));

    buttonPanel1.setOpaque(false);
    yearDecButton.setMnemonic('H');
    yearDecButton.setToolTipText("\u524d\u306e\u5e74");
    yearDecButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    yearDecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year20.gif")));
    yearDecButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    yearDecButton.setOpaque(false);
    yearDecButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year21.gif")));
    yearDecButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year22.gif")));
    buttonPanel1.add(yearDecButton);

    yearIncButton.setMnemonic('Y');
    yearIncButton.setToolTipText("\u6b21\u306e\u5e74");
    yearIncButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    yearIncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year10.gif")));
    yearIncButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    yearIncButton.setOpaque(false);
    yearIncButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year11.gif")));
    yearIncButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/year12.gif")));
    buttonPanel1.add(yearIncButton);

    nowButton.setMnemonic('N');
    nowButton.setToolTipText("\u4eca\u6708");
    nowButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    nowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/now10.gif")));
    nowButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    nowButton.setOpaque(false);
    nowButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/now11.gif")));
    nowButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/now12.gif")));
    buttonPanel1.add(nowButton);

    monthDecButton.setMnemonic('J');
    monthDecButton.setToolTipText("\u524d\u306e\u6708");
    monthDecButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    monthDecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month20.gif")));
    monthDecButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    monthDecButton.setOpaque(false);
    monthDecButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month21.gif")));
    monthDecButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month22.gif")));
    buttonPanel1.add(monthDecButton);

    monthIncButton.setMnemonic('U');
    monthIncButton.setToolTipText("\u6b21\u306e\u6708");
    monthIncButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    monthIncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month10.gif")));
    monthIncButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    monthIncButton.setOpaque(false);
    monthIncButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month11.gif")));
    monthIncButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/month12.gif")));
    buttonPanel1.add(monthIncButton);

    controlPanel1.add(buttonPanel1, java.awt.BorderLayout.EAST);

    confPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    confPanel.setOpaque(false);
    configButton.setMnemonic('P');
    configButton.setToolTipText("\u52d5\u4f5c\u8a2d\u5b9a");
    configButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    configButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/config10.gif")));
    configButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    configButton.setOpaque(false);
    configButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/config11.gif")));
    configButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/button/config12.gif")));
    configButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configButtonActionPerformed(evt);
      }
    });

    confPanel.add(configButton);

    clockLabel.setText("jLabel3");
    confPanel.add(clockLabel);

    zoneLabel.setText("jLabel6");
    confPanel.add(zoneLabel);

    controlPanel1.add(confPanel, java.awt.BorderLayout.CENTER);

    mainPanel.add(controlPanel1, java.awt.BorderLayout.SOUTH);

    getContentPane().add(mainPanel);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  
    private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
        configPopup.show(confPanel,configButton.getX()+configButton.getWidth(),configButton.getY());
    }//GEN-LAST:event_configButtonActionPerformed
    
    
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel clockLabel;
  private javax.swing.JPanel confPanel;
  private javax.swing.JRadioButton configButton;
  private javax.swing.JPopupMenu configPopup;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JRadioButton monthDecButton;
  private javax.swing.JRadioButton monthIncButton;
  private javax.swing.JRadioButton nowButton;
  private javax.swing.JRadioButton yearDecButton;
  private javax.swing.JRadioButton yearIncButton;
  private javax.swing.JLabel zoneLabel;
  // End of variables declaration//GEN-END:variables
  
}
