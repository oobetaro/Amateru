/*
 * SabianDialog.java
 *
 * Created on 2007/01/19, 5:59
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.util.WindowMoveHandler;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import to.tetramorph.fontchooser.FontChooser;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.Sabian;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.util.ColorCalcurator;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.ParentWindow;

/**
 * サビアン辞書ダイアログ。サビアンの一覧表を表示して、setSelect()で指定された
 * 度数のサビアンに焦点を合わせる。setBodyList()で天体位置を与えておくと、
 * 天体が在泊しているサビアンに、惑星のシンボルが赤で表示される。
 *
 * ショートカットキーはメニューバーとそのアイテムに表記されているもの以外に、
 * 次のものがある。
 * <ul>
 *     <li>Alt+上下矢印キーで感受点ごとにジャンプできる。
 *     <li>ESCキーでダイアログを閉じる。
 * </ul>
 * 全サビアン表示の時、セルをダブクリすることで、天体順表示に切り替える事ができる。<br>
 * セルに天体が入っている場合、天体順表示の該当天体が選択されハイライト表示される。<br>
 * 天体順表示の時、セルをダブクリすることで、全サビアン表示に切り替える事ができる。<br>
 * セルの天体が全サビアン表示の際には選択されハイライト表示される。
 * 2011-07-28 レジストリの使用をやめた。
 * 2011-08-01 フォントほカスタマイズ可能にした。変更したフォントはConfig.usrに保管、
 * 起動のときは読み出しを行うが、save()は一切行わない。アマテル本体の保存処理に依存
 * している。
 * @author 大澤義孝
 */
final class SabianDialog extends JDialog implements SabianDialogHandler {
    // changePanel()でパネルを切り替えるときの定数。
    final static int SABIAN_PANEL = 0;
    final static int BODY_SABIAN_PANEL = 1;
    final static int EDIT_PANEL = 2;

    // リストのフォントサイズの変更はこの変数を変更すればよいのだが、
    // このクラスのsetFont()を使用すればもっと簡単。
    // レンダラがカスタマイズされているためJList#setFontするだけでは変更できない
    Font defaultFont;
    Font astroFont;                    // 占星術記号のフォント

    //float fontSize;                    // 記号や文字のフォントサイズ
    int ej = Sabian.JP;                // 日本語・英語モードの状態を表す
    static SabianDialog dialog;        //テスト用
    private List<Body> bodyList = new ArrayList<Body>();

    //　度数→天体ID[] に変換するマップ。
    // ある度数に在泊している惑星(複数)を記憶する。
    Map<Integer,List<Integer>> planetsMap =
                                       new HashMap<Integer,List<Integer>>();
    CardLayout cardLayout;

    int panelType;             //現在表示しているパネル(全表示/天体順表示/編集)
                               //SABIAN_PANEL,BODY_SABIAN_PANEL,EDIT_PANELが代入

    /**
     * 親となるFrameを指定してオブジェクトを作成する。その際、"SabianDialog.ej"と
     * いうプロパティをConfig.data.getProperty()で参照し、
     * その値によって英語/日本語の切替ボタンが選択される。
     * 取得できなかったときは日本語となる。
     * WindowMoveHandlerによってダイアログのサイズと位置は保存される。
     * @param parent 親となるフレーム
     */
    public SabianDialog( java.awt.Frame parent ) {
        super(parent, false);
        initComponents();
        this.cardLayout = (CardLayout)getContentPane().getLayout();

        if ( System.getProperty("java.version").startsWith("1.6") ) {
            Image img = IconLoader.getImage("/resources/sabianIcon.png");
            setIconImage(img);
        }
        // 推奨フォントを用意。メイリオがないときはこのダイアログのフォント。
        Font preferFont = FontChooser.isFamilyExists("メイリオ") ?
        new Font("メイリオ",Font.PLAIN,14)   :   getFont().deriveFont(14F);

        // フォント設定。
        // setListFontを呼びたいところだが、コンストラクトの時は事情が違う
        defaultFont = Config.usr.getFont( "SabianFont", preferFont );
        astroFont = AstroFont.getFont( Font.BOLD, defaultFont.getSize() );
        signAngleLabel.setFont( defaultFont );
        editTextArea.setFont( defaultFont );

        initJList();
        setEJ();
        initSignButtons();
        this.setPreferredSize( new Dimension(430,300) );
        this.setMinimumSize( null );
        WindowMoveHandler winmove =
            new WindowMoveHandler("SabianDialog.BOUNDS", this);
        addComponentListener(winmove);
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        initKey();
        pack();
        winmove.setBounds();
    }

    private static final String UP_KEY = "UP_KEY";
    private static final String DOWN_KEY = "DOWN_KEY";
    /**
     * 次の天体にジャンプするボタンにショートカットキーを設定する。
     * Alt+↑とAlt+↓
     */
    private void initKey() {
        int UP = KeyEvent.VK_UP;
        int DOWN = KeyEvent.VK_DOWN;
        int ALT = InputEvent.ALT_DOWN_MASK;

        ActionMap actionMap = getRootPane().getActionMap();
        InputMap imap =
            getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        imap.put( KeyStroke.getKeyStroke( UP, ALT ), UP_KEY);
        actionMap.put( UP_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prevButtonActionPerformed(null);
            }
        });
        imap.put( KeyStroke.getKeyStroke( DOWN, ALT ), DOWN_KEY);
        actionMap.put( DOWN_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                nextButtonActionPerformed(null);
            }
        });

    }

    /**
     * JListの選択モード、リストモデル、セルレンダラの登録と、
     * JList内でセルをダブクリした際に全サビアン表示と天体順リストを切り替える
     * アクションリスナをjList,jList2に登録する。
     */
    private void initJList() {
        //jList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        //jList2.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        jList.setModel(new SabianListModel());
        jList2.setModel(new SabianTableListModel());
        jList.setCellRenderer(new SabianCellRenderer());
        jList2.setCellRenderer(new SabianTableCellRenderer());
        jList2.addMouseListener( new MouseAdapter() {
           //天体順表示でダブクリのときは、全サビアン表示に切り替え
           //ダブクリされたときの天体を選択する。
            @Override
           public void mouseClicked( MouseEvent evt) {
               if ( evt.getClickCount() != 2 ) return;
               int index = jList2.getSelectedIndex();
               viewRadioButtonMenuItem1.doClick();
               setSelect( (int)bodyList.get(index).lon );
           }
        });
        jList.addMouseListener( new MouseAdapter() {
           //全サビアン表示でダブクリのときは、天体順表示に切り替え
           //ダブクリされた度数に入っている天体すべてを選択する。
            @Override
           public void mouseClicked( MouseEvent evt) {
               if ( evt.getClickCount() != 2 ) return;
               viewRadioButtonMenuItem2.doClick();
               int index = jList.getSelectedIndex();
               setSelectedBodyList(index);
           }
        });

    }
    /**
     * 天体順表示のリスト(jList2)で指定された度数をもっているセルを選択状態にする。
     * palnetMapにangleで指定された位置に在泊している天体のIDを求め、次にbodyList
     * 内からそのIDをもっているBody(複数)を検出し、JListの複数選択メソッドで選択。
     */
    private void setSelectedBodyList(int angle) {
        List<Integer> idList = planetsMap.get(angle);
        if ( idList == null ) return;
        List<Integer> selectedList = new ArrayList<Integer>();
        for ( Integer id : idList ) {
            for ( int j=0; j<bodyList.size(); j++ ) {
                if ( bodyList.get(j).id == id ) {
                    selectedList.add(j);
                }
            }
        }
        int [] cols = new int [ selectedList.size() ];
        for(int i=0; i<selectedList.size(); i++ )
            cols[i] = selectedList.get(i);
        //jList2.validate();
        jList2.repaint();
        jList2.setSelectedIndices( cols );
    }

    /**
     * 指定された度数のサビアンを選択してそれが表示される位置までスクロールする。
     * @param angle 0から359度まで。
     */
    @Override
    public void setSelect(int angle) {
        if ( getPanelType() == SABIAN_PANEL ) {
            jList.setSelectedIndex(angle);
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int max = bar.getMaximum();
            int h = max / 360;
            int height = scrollPane.getViewport().getExtentSize().height;
            int rows = height /h;
            bar.setValue(angle * h - (rows / 2 * h));
        } else if ( getPanelType() == BODY_SABIAN_PANEL ) {
            setSelectedBodyList(angle);
        }
    }

    /**
     * 日本語/英語の切替をプロパティに応じて変数ejに設定し、
     * メニューのラジオボタンにもその設定を反映させる。
     */
    private void setEJ() {
        ej = Config.usr.getInteger( "SabianDialog.ej", Sabian.JP );
        if ( ej == Sabian.EN ) {
            enRadioButtonMenuItem.setSelected( true );
        } else {
            jpRadioButtonMenuItem.setSelected( true );
        }
    }

    /**
     * 星座ボタンにリスナを登録。
     */
    private void initSignButtons() {
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int sel = jList.getSelectedIndex();
                if(sel < 0) sel = 0;
                int s = Integer.
                    parseInt(((JButton)evt.getSource()).getActionCommand()) - 1;
                setSelect(s * 30 + (sel % 30));
            }
        };
        JButton [] buttons = new JButton[] {
            jButton1,jButton2,jButton3,jButton4,jButton5,jButton6,
            jButton7,jButton8,jButton9,jButton10,jButton11,jButton12
        };
        for( JButton button : buttons ) button.addActionListener(l);
    }

    /**
     * サビアンリストのフォント(ファミリー、スタイル、サイズ)を変更する。
     * 変更されたフォントはConfigに書き込まれる。(Config.save()はしない)
     * @param font
     */
    public void setListFont( Font font ) {
        defaultFont = font;
        astroFont = AstroFont.getFont( Font.BOLD, font.getSize() );
        // レンダラはdefaultFontの値を参照しているので、レンダラを作りなおし再セット
        // することでフォンサイズが変更される。
        jList.setCellRenderer( new SabianCellRenderer() );
        jList.repaint();
        jList2.setCellRenderer( new SabianTableCellRenderer() );
        jList2.repaint();
        signAngleLabel.setFont( font );
        editTextArea.setFont( font );
        pack();
        Config.usr.setFont("SabianFont", font);
    }

    /**
     * 全サビアン表示用のリストモデル(jListにセット)
     * Sabianクラスをラップして、JListの要求に応じてサビアンを返す。
     */
    class SabianListModel extends AbstractListModel {
        @Override
        public int getSize() { return 360; }
        // indexにはJListが何番目の行のデータを必要としているかが入ってくる。
        @Override
        public Object getElementAt( int index ) {
            return Sabian.getText( index, ej );
        }
    }

    /**
     * 星座シンボルと度数と複数の天体をひとまとめにアイコンとして表示する。
     * 全サビアン表示モードの際に使用。
     */
    class SignIcon implements Icon {
        int angle, sign, index, xpos;
        TextLayout tl;
        float signWidth=0, signHeight=0, fontHeight=0, fontWidth = 0;

        // 0〜359度までの値を指定
        SignIcon(int index) {
            angle = index % 30;
            sign = index / 30;
            this.index = index;
        }

        //ｾﾙﾚﾝﾀﾞﾗのﾗﾍﾞﾙがcに入って呼ばれる
        @Override
        public void paintIcon(Component c, Graphics g1, int x, int y) {
            Graphics2D g = (Graphics2D)g1;
            g.setColor(Color.BLACK);
            RenderingHints hints = g.getRenderingHints(); //値を退避
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
            String signStr = "" + Const.ZODIAC_CHARS[sign];
            String angleStr = "" + angle;
            if ( tl == null ) {
                //リソース節約のためサイズ取得は1回限りとする。
                FontRenderContext render = g.getFontRenderContext();
                tl = new TextLayout( "0", defaultFont, render );
                TextLayout tl2 = new TextLayout( signStr, astroFont, render );
                signWidth  = tl2.getAdvance();
                signHeight = tl2.getAscent();
                fontWidth  = tl.getAdvance();
                fontHeight = tl.getAscent();
            }
            //度数を表示
            g.setColor( c.getForeground() );
            g.drawString( angleStr, signWidth, fontHeight ); //文字の左下が原点

            // サインを表示
            g.setFont( astroFont );
            g.drawString( signStr, 0, fontHeight );

            // もしあれば惑星を表示
            if ( planetsMap != null ) {
                List<Integer> planets = planetsMap.get( index );
                StringBuilder sb = new StringBuilder();
                if ( planets != null ) {
                    for ( int i: planets )
                        sb.append( Const.BODY_CHARS[i] );
                    g.setColor( Color.RED );
                    g.drawString( sb.toString(),
                                   astroFont.getSize() * 2
                                 + defaultFont.getSize(),
                                   fontHeight );
                    g.setColor( getForeground() );
                }
            }
            //以下、呼び出し前の設定に戻す
            g.setFont( defaultFont );
            g.setRenderingHints( hints );
        }

        /* getIconWith,getIconHeightは理想的にはTextLayoutのメソッドから取得した
           フォントサイズを返すべきだが、Graphicsオブジェクトがなければそれを取得で
           きない。このメソッドはpaintIconが呼ばれる前に呼び出されるため、
           paintIcon内でもとまった値を返す方法も使えない。だからFont#getSize()から
           推定した値を返す。*/

        @Override
        public int getIconWidth() {
            if ( planetsMap != null ) {
                List<Integer> planets = planetsMap.get(index);
                if ( planets != null )
                    return defaultFont.getSize() * 8;
            }
            return astroFont.getSize() + defaultFont.getSize();
        }

        @Override
        public int getIconHeight() {
            return astroFont.getSize(); //(int)signHeight; //14;
        }
    }

    /**
     * 星座アイコンつきのセルレンダラ。jListにセットする。
     */
    private class SabianCellRenderer extends JLabel implements ListCellRenderer {

        Icon [] icons = new Icon[360];

        SabianCellRenderer() {
            for( int i=0; i<icons.length; i++)
                icons[i] = new SignIcon(i);
            setFont( defaultFont );
        }

        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,              // value to display
            int index,                // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            setText( value.toString() );
            if ( isSelected ) {
                setBackground( list.getSelectionBackground() );
                setForeground( list.getSelectionForeground() );
            } else {
                Color bg = list.getBackground();
                if ( (index & 1) == 1 )
                    setBackground( ColorCalcurator.darker( bg, 0.9f ) );
                else setBackground( bg );
                setForeground( list.getForeground() );
            }
            setIcon( icons[index] );
            //アイコンをセットしないかぎり無効
            setIconTextGap( defaultFont.getSize() );
            setEnabled( list.isEnabled() );
            setOpaque( true );
            return this;
        }
    }

    /**
     * 天体順表示用のリストモデル(jList2にセット)
     * bodyList内の天体位置(0-359)をJListの要求に応じて返す。
     */
    class SabianTableListModel extends AbstractListModel {
        @Override
        public int getSize() {
            return bodyList.size();
        }
        // indexにはJListが何番目の行のデータを必要としているかが入ってくる。
        @Override
        public Object getElementAt(int index) {
            int i = (int)bodyList.get(index).lon;
            return Sabian.getText(i,ej);
        }
    }

    /**
     * 天体 星座 度数をひとまとめにしたアイコン。天体順のリスト表示用
     */
    class SignIcon2 implements Icon {
        int angle;
        int sign;
        int index;
        int xpos;
        int bodyID;

        // 0〜359度までの値と天体IDを指定。
        SignIcon2(int index,int bodyID ) {
            angle = index % 30;
            sign = index / 30;
            this.index = index;
            this.bodyID = bodyID;
        }

        //ｾﾙﾚﾝﾀﾞﾗのﾗﾍﾞﾙがcに入って呼ばれる
        @Override
        public void paintIcon(Component c, Graphics g1, int x, int y) {
            Graphics2D g = (Graphics2D)g1;
            RenderingHints hints = g.getRenderingHints();
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            String signStr  = "" + Const.ZODIAC_CHARS[ sign ];
            String bodyStr  = "" + Const.BODY_CHARS[ bodyID ];
            String angleStr = "" + angle;
            FontRenderContext render = g.getFontRenderContext();
            TextLayout tl  = new TextLayout( "0", defaultFont, render );
            TextLayout tl2 = new TextLayout( signStr, astroFont, render );
            float signWidth  = tl2.getAdvance();
            float signHeight = tl2.getAscent();
            float fontHeight =  tl.getAscent();

            // 度数を表示
            g.setColor( c.getForeground() );
            // 文字左下が原点
            g.drawString( angleStr, signWidth * 2, fontHeight);

            // サインを表示
            g.setFont(astroFont);
            g.drawString( signStr, signWidth, fontHeight);

            // 天体を表示
            g.setColor( Color.RED );
            g.drawString( bodyStr, 0, fontHeight);

            // 描画設定を元に戻す
            g.setFont( defaultFont );
            g.setRenderingHints( hints );
        }

        @Override
        public int getIconWidth() {
            return astroFont.getSize() * 2 + defaultFont.getSize();
        }

        @Override
        public int getIconHeight() {
            return astroFont.getSize();
        }
    }

    /**
     * 天体・サイン・度数・サビアンテキストの順で表示するセルレンダラ。
     * 天体順表示用のjList2で使用。
     */
    class SabianTableCellRenderer extends JLabel implements ListCellRenderer {

        SabianTableCellRenderer() {
            setFont( defaultFont );
        }

        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,              // value to display
            int index,                // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            setText( value.toString() );
            if ( isSelected ) {
                setBackground( list.getSelectionBackground() );
                setForeground( list.getSelectionForeground() );
            } else {
                Color bg = list.getBackground();
                if ( ( index & 1 ) == 1 )
                    setBackground( ColorCalcurator.darker( bg, 0.9f ) );
                else
                    setBackground( bg );
                setForeground( list.getForeground() );
            }
            int sa = (int)bodyList.get(index).lon;
            int bid  = bodyList.get(index).id;
            setIcon( new SignIcon2(sa,bid) );
            //アイコンをセットしないかぎり無効
            setIconTextGap( defaultFont.getSize() );
            setEnabled( list.isEnabled() );
            setOpaque( true );
            return this;
        }
    }


    /**
     * 天体位置が計算済みの天体リストを引き渡すと、
     * 該当する感受点の行にそのシンボルを赤で表示するようにする。
     * @exception IllegalArgumentException bodyListがnullのとき。
     */
    @Override
    public void setBodyList(List<Body> bodyList) {
        if ( bodyList == null )
            throw new IllegalArgumentException("nullは禁止");
        this.bodyList.clear();
        if ( bodyList.isEmpty() ) return;
        for ( int i=0; i < bodyList.size(); i++ )
            this.bodyList.add( bodyList.get(i) );
        this.planetsMap = new HashMap<Integer,List<Integer>>();
        for ( Body body : bodyList ) {
            if ( body != null ) {
                if ( planetsMap.get((int)body.lon) == null )
                    planetsMap.put((int)body.lon,new ArrayList<Integer>());
                planetsMap.get( (int)body.lon ).add( body.id );
            }
        }
        bodyMessageLabel.setText("");
        jList2.setCellRenderer( new SabianTableCellRenderer() );
        jList.repaint();
        jList2.repaint();
    }
    /**
     * メニューバーのメニュー(表示、編集、言語)のEnabledを一括設定
     * 主に編集中にそれらのメニューはDisenabledにセットされ編集終了とともに
     * Enabledにセットされる。
     */
    private void menuEnabled( boolean b ) {
        viewMenu.setEnabled(b);
        langMenu.setEnabled(b);
        editMenu.setEnabled(b);
    }

    /**
     * 全サビアン表示と天体別サビアン表示どちらかに切り替える。
     * @param panelType SABIAN_PANEL,BODY_SABIAN_PANELのどちらか
     * @exception IllegalArgumentException 存在しないオプションが指定された時
     */
    private void changePanel(int panelType) {

        if ( panelType == SABIAN_PANEL ) {

            cardLayout.show(getContentPane(),"sabian");
            menuEnabled(true);

        } else if ( panelType == BODY_SABIAN_PANEL ) {

            cardLayout.show(getContentPane(),"table");
            jList2.requestFocus(); //これしないとESCキーが効かない
            editMenu.setEnabled(false);
            String msg = bodyList.isEmpty() ?
                "天体情報がまだセットされていません。" :"";
            bodyMessageLabel.setText(msg);

        } else if ( panelType == EDIT_PANEL ) {

            cardLayout.show(getContentPane(),"edit");
            editTextArea.requestFocus(); //これしないとESCキーが効かない
            menuEnabled(false);

        } else {
            throw new IllegalArgumentException("存在しないオプション");
        }
        this.panelType = panelType;
    }

    /**
     * 現在表示されているパネルの種類を返す。
     * @return SABIAN_PANEL,BODY_SABIAN_PANEL,EDIT_PANELのいずれか。
     */
    private int getPanelType() {
        return panelType;
    }

    /**
     * 言語を切り替える。
     * @param lang Sabian.JPまたはSabian.ENのどちらか。
     */
    private void changeLang(int lang) {
        ej = lang;
        jList.repaint();
        jList2.repaint();
        Config.usr.setInteger("SabianDialog.ej", ej);
    }

    /**
     * 現在選択されている言語コードを返す。
     * @return Sabian.JPまたはSabian.ENのどちらか。
     */
    @Override
    public int getLang() {
        return ej;
    }

    /**
     * 改行やタブなどｺﾝﾄﾛｰﾙｺｰﾄﾞを除去して返す。
     * 編集モードの入力から都合の悪いコードを除去するのに使用。
     */
    private String ejectCtrlCode( String text ) {
        char [] array = text.toCharArray();
        StringBuilder sb = new StringBuilder();
        for ( char c : array ) {
            if ( c >= ' ' ) sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 前の天体または位置に移動
     * @param dir 昇り順で検索するときはtrue、降り順で検索するときはfalseを指定。
     */
    private void nextBody(boolean dir) {
        if ( planetsMap == null ) return;
        int angle = jList.getSelectedIndex();
        if ( angle < 0 ) angle = 360;
        int sign = dir ? 1 : -1;
        for ( int i=0; i < 360; i++ ) {
            angle = (angle + sign) % 360;
            if ( angle < 0 ) angle = 359;
            List<Integer> l = planetsMap.get( angle );
            if ( l != null ) {
                setSelect(angle);
                break;
            }
        }

    }
    // テスト
    static void createAndShowGUI() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            String win = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            String nin = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
            UIManager.setLookAndFeel( win );
        } catch (Exception e) {

        }
        // テスト用の天体リストを作る
        final List<Body> bodyList = new ArrayList<Body>();
        double [] values = new double[6];
        double [] lons   = { 0, 40, 80, 90, 90, 90, 120, 150, 186, 210, 240, 299 };
        for ( int i = Const.SUN; i <= Const.PLUTO; i++ ) {
            values[0] = lons[i];
            Body b = new Body(i,values);
            bodyList.add(b);
        }
        // テストフレームの作成
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        JButton button = new JButton("OPEN");
        JButton button2 = new JButton("SET");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dialog = new SabianDialog(frame);
                dialog.setVisible(true);
                dialog.setBodyList(bodyList);
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dialog.setSelect(186);
            }
        });
        frame.add(button);
        frame.add(button2);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    /**
     * テスト
     */
    public static void main(String args[]) {
        System.setProperty("app.properties", "C:/Users/ohsawa/.AMATERU2.0/properties");
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.ButtonGroup ejButtonGroup = new javax.swing.ButtonGroup();
        javax.swing.ButtonGroup viewButtonGroup = new javax.swing.ButtonGroup();
        javax.swing.JPanel sabianPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel8 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JButton prevButton = new javax.swing.JButton();
        javax.swing.JButton nextButton = new javax.swing.JButton();
        to.tetramorph.widget.ResizeIconPanel resizeIconPanel1 = new to.tetramorph.widget.ResizeIconPanel();
        scrollPane = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        javax.swing.JPanel tablePanel = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        javax.swing.JPanel jPanel10 = new javax.swing.JPanel();
        to.tetramorph.widget.ResizeIconPanel resizeIconPanel2 = new to.tetramorph.widget.ResizeIconPanel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        bodyMessageLabel = new javax.swing.JLabel();
        javax.swing.JPanel editPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel9 = new javax.swing.JPanel();
        javax.swing.JButton saveButton = new javax.swing.JButton();
        javax.swing.JButton cancelButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel6 = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        editTextArea = new javax.swing.JTextArea();
        javax.swing.JPanel jPanel7 = new javax.swing.JPanel();
        signAngleLabel = new javax.swing.JLabel();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        viewRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        viewRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        fontMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem editMenuItem = new javax.swing.JMenuItem();
        langMenu = new javax.swing.JMenu();
        jpRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        enRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();

        ejButtonGroup.add(enRadioButtonMenuItem);
        ejButtonGroup.add(jpRadioButtonMenuItem);

        viewButtonGroup.add(viewRadioButtonMenuItem1);
        viewButtonGroup.add(viewRadioButtonMenuItem2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("サビアン辞書");
        getContentPane().setLayout(new java.awt.CardLayout());

        sabianPanel.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        prevButton.setMnemonic('Y');
        prevButton.setText("△ (Y)");
        prevButton.setToolTipText("前の天体にジャンプ Alt+↑");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        jPanel3.add(prevButton);

        nextButton.setMnemonic('N');
        nextButton.setText("▽ (N)");
        nextButton.setToolTipText("次の天体にジャンプ Alt+↓");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        jPanel3.add(nextButton);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel8.add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel8.add(resizeIconPanel1, java.awt.BorderLayout.EAST);

        sabianPanel.add(jPanel8, java.awt.BorderLayout.SOUTH);

        jList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
        jList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(jList);

        sabianPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(6, 2));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/ari.gif"))); // NOI18N
        jButton1.setActionCommand("1");
        jButton1.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/lib.gif"))); // NOI18N
        jButton2.setActionCommand("7");
        jButton2.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/tau.gif"))); // NOI18N
        jButton3.setActionCommand("2");
        jButton3.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton3);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/sco.gif"))); // NOI18N
        jButton4.setActionCommand("8");
        jButton4.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/gem.gif"))); // NOI18N
        jButton5.setActionCommand("3");
        jButton5.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton5);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/sag.gif"))); // NOI18N
        jButton6.setActionCommand("9");
        jButton6.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/can.gif"))); // NOI18N
        jButton7.setActionCommand("4");
        jButton7.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton7);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/cap.gif"))); // NOI18N
        jButton8.setActionCommand("10");
        jButton8.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton8);

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/leo.gif"))); // NOI18N
        jButton9.setActionCommand("5");
        jButton9.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton9);

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/aqu.gif"))); // NOI18N
        jButton10.setActionCommand("11");
        jButton10.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton10);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/vir.gif"))); // NOI18N
        jButton11.setActionCommand("6");
        jButton11.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton11);

        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/pis.gif"))); // NOI18N
        jButton12.setActionCommand("12");
        jButton12.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jPanel2.add(jButton12);

        sabianPanel.add(jPanel2, java.awt.BorderLayout.EAST);

        getContentPane().add(sabianPanel, "sabian");

        tablePanel.setLayout(new java.awt.BorderLayout());

        jList2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList2);

        tablePanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel10.setLayout(new java.awt.BorderLayout());
        jPanel10.add(resizeIconPanel2, java.awt.BorderLayout.EAST);

        bodyMessageLabel.setText("jLabel1");
        jPanel4.add(bodyMessageLabel);

        jPanel10.add(jPanel4, java.awt.BorderLayout.WEST);

        tablePanel.add(jPanel10, java.awt.BorderLayout.SOUTH);

        getContentPane().add(tablePanel, "table");

        editPanel.setLayout(new java.awt.BorderLayout());

        saveButton.setMnemonic('Y');
        saveButton.setText("保存(Y)");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel9.add(saveButton);

        cancelButton.setMnemonic('N');
        cancelButton.setText("中止(N)");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel9.add(cancelButton);

        editPanel.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4));
        jPanel6.setLayout(new java.awt.GridLayout(1, 0));

        editTextArea.setColumns(20);
        editTextArea.setLineWrap(true);
        editTextArea.setRows(5);
        editTextArea.setMargin(new java.awt.Insets(0, 4, 0, 0));
        jScrollPane1.setViewportView(editTextArea);

        jPanel6.add(jScrollPane1);

        editPanel.add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));

        signAngleLabel.setText(" ");
        signAngleLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        signAngleLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel7.add(signAngleLabel);

        editPanel.add(jPanel7, java.awt.BorderLayout.NORTH);

        getContentPane().add(editPanel, "edit");

        viewMenu.setMnemonic('V');
        viewMenu.setText("表示(V)");

        viewRadioButtonMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        viewRadioButtonMenuItem1.setSelected(true);
        viewRadioButtonMenuItem1.setText("全サビアン表示");
        viewRadioButtonMenuItem1.setActionCommand("sabian");
        viewRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRadioButtonMenuItem1ActionPerformed(evt);
            }
        });
        viewMenu.add(viewRadioButtonMenuItem1);

        viewRadioButtonMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        viewRadioButtonMenuItem2.setText("天体順に表示");
        viewRadioButtonMenuItem2.setActionCommand("table");
        viewRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRadioButtonMenuItem2ActionPerformed(evt);
            }
        });
        viewMenu.add(viewRadioButtonMenuItem2);
        viewMenu.add(jSeparator1);

        fontMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK));
        fontMenuItem.setText("フォントサイズ変更");
        fontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(fontMenuItem);
        viewMenu.add(jSeparator2);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        exitMenuItem.setText("サビアン辞書を閉じる");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(exitMenuItem);

        jMenuBar1.add(viewMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("編集(E)");

        editMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        editMenuItem.setText("テキストを編集");
        editMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editMenuItem);

        jMenuBar1.add(editMenu);

        langMenu.setMnemonic('L');
        langMenu.setText("言語(L)");

        jpRadioButtonMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        jpRadioButtonMenuItem.setSelected(true);
        jpRadioButtonMenuItem.setText("日本語");
        jpRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpRadioButtonMenuItemActionPerformed(evt);
            }
        });
        langMenu.add(jpRadioButtonMenuItem);

        enRadioButtonMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        enRadioButtonMenuItem.setText("英語");
        enRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enRadioButtonMenuItemActionPerformed(evt);
            }
        });
        langMenu.add(enRadioButtonMenuItem);

        jMenuBar1.add(langMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    //メニューからサビアン辞書を閉じるが選択された。
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    //メニューから編集が選択された
    private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
        int angle = jList.getSelectedIndex();
        if ( angle < 0 ) {
            JOptionPane.showMessageDialog(
                this,"編集するサビアンシンボルを選択してください");
            return;
        }
        signAngleLabel.setIcon( new SignIcon( angle ) );
        editTextArea.setText( Sabian.getText( angle, ej ) );
        changePanel( EDIT_PANEL );
    }//GEN-LAST:event_editMenuItemActionPerformed

    //メニューから全サビアン表示が選択された

    private void viewRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewRadioButtonMenuItem1ActionPerformed
        changePanel( SABIAN_PANEL );
    }//GEN-LAST:event_viewRadioButtonMenuItem1ActionPerformed

    //メニューから天体順で表示が選択された

    private void viewRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewRadioButtonMenuItem2ActionPerformed
        changePanel( BODY_SABIAN_PANEL );
    }//GEN-LAST:event_viewRadioButtonMenuItem2ActionPerformed

    //メニューから英語表示が選択された

    private void enRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enRadioButtonMenuItemActionPerformed
        changeLang( Sabian.EN );
    }//GEN-LAST:event_enRadioButtonMenuItemActionPerformed

    //メニューから日本語表示が選択された

    private void jpRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpRadioButtonMenuItemActionPerformed
        changeLang( Sabian.JP );
    }//GEN-LAST:event_jpRadioButtonMenuItemActionPerformed


  private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        nextBody( false );
  }//GEN-LAST:event_prevButtonActionPerformed

  // 次の天体または位置に移動

  private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        nextBody( true );
  }//GEN-LAST:event_nextButtonActionPerformed

  //編集モードの保存ボタン

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
      String text = editTextArea.getText();
      text = ejectCtrlCode(text);
      int angle = jList.getSelectedIndex();
      if ( ! text.equals( Sabian.getText(angle,ej) ) ) {
          int result = JOptionPane.showConfirmDialog(this,
              "<html>編集されたサビアンシンボルを保存します。よろしいですか？<br>" +
              "(改行コードなど制御コードは強制的に除去されます。)</html>",
              "サビアンシンボルの保存",JOptionPane.YES_NO_OPTION);
          if ( result == JOptionPane.YES_OPTION ) {
              String backup = Sabian.getText( angle, ej ); //保存前に元ネタを保管
              Sabian.setText( angle, text, ej ); //編集されたものをセット
              if ( ! Sabian.save( this ) ) {
                  Sabian.setText(angle,backup,ej); //保存にしっぱいしたときは元ネタに戻す
              } else
                  System.out.println("サビアンを保存した");
          }
      }
      changePanel( SABIAN_PANEL );
  }//GEN-LAST:event_saveButtonActionPerformed

  //編集モードの中止ボタン

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
      changePanel( SABIAN_PANEL );
  }//GEN-LAST:event_cancelButtonActionPerformed

  private void fontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontMenuItemActionPerformed
      Font font = FontChooser.showDialog(this, defaultFont );
      if ( font != null ) {
          setListFont(font);
      }
  }//GEN-LAST:event_fontMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bodyMessageLabel;
    private javax.swing.JMenu editMenu;
    private javax.swing.JTextArea editTextArea;
    private javax.swing.JRadioButtonMenuItem enRadioButtonMenuItem;
    private javax.swing.JMenuItem fontMenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JList jList;
    private javax.swing.JList jList2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JRadioButtonMenuItem jpRadioButtonMenuItem;
    private javax.swing.JMenu langMenu;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel signAngleLabel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JRadioButtonMenuItem viewRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem viewRadioButtonMenuItem2;
    // End of variables declaration//GEN-END:variables

}
