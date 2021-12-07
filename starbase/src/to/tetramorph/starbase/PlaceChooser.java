/*
 * PlaceChooser.java
 *
 * Created on 2006/05/29, 16:33
 */

package to.tetramorph.starbase;

import java.util.logging.Logger;
import to.tetramorph.starbase.util.WindowMoveHandler;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.util.ParentWindow;
import static java.lang.System.getProperty;

/**
 * 土地の地名・緯度・経度の情報を選択するためのダイアログ。
 * 2011-07-28 設定やデータファイルの置き場所を変更。レジストリの使用をやめた。
 * @author  大澤義孝
 */
class PlaceChooser extends javax.swing.JDialog {
    /**
     * showDialogメソッドから返った配列の地名を指す要素番号
     */
    public static final int PLACENAME = 0;
    /**
     * showDialogメソッドから返った配列の緯度を指す要素番号
     */
    public static final int LATITUDE = 1;
    /**
     * showDialogメソッドから返った配列の経度を指す要素番号
     */
    public static final int LONGITUDE = 2;
    /**
     * showDialogメソッドから返った配列のタイムゾーンを指す要素番号
     */
    public static final int TIMEZONE = 3;
    // 観測地データのフォルダ
    private File placedir = new File( getProperty("app.place") );
    //private String homedir = System.getProperty("user.home");
    // 検索され選択された土地データの履歴
    private File historyFile = new File( getProperty( "app.userdict" ),
                                                          "PlaceHistory.csv");
    // 個人のお気に入りのデータ
    private File myDataFile  = new File( getProperty( "app.userdict" ),
                                                          "PlaceMyData.csv");
    // 国内と海外のデータ名と、そのファイル名が書かれた表
    private File indexFile   = new File( placedir, "index.csv");
    // このクラスが使用するプロパティ
    //private Properties prop;

    Vector<String> indexVector = new Vector<String>(); //地名データファイルのリスト
    Vector<Node> placeNameVector = new Vector<Node>(); //地名リスト
    Vector<Node> historyVector = new Vector<Node>();   //履歴用リスト
    Vector<Node> myDataVector = new Vector<Node>();    //マイデータのリスト
    Vector<Node> clipBoardVector = new Vector<Node>(); //コピペ用のリスト
    Vector<Node> resultVector = new Vector<Node>();    //検索結果用のリスト
    JRadioButtonMenuItem [] searchMenuItems;
    JList [] jLists = new JList[3];
    private Place result = null;


    //土地の緯度経度情報を格納するためのクラス
    class Node {
        static final String
            CSV_HEADDER = "地名,ちめい,緯度,経度,地方名,地方時";
        String placeName,placeNameKana;
        String lon,lat;
        String zoneName = "";	//検索でヌルポが出るのを防ぐため""を入れておく。
        String zoneID = "";
        //通常のインスタンス作成
        Node() { }
        //デープコピーで複製を作成
        Node(Node n) {
            this.placeName = n.placeName;
            this.placeNameKana = n.placeNameKana;
            this.lon = n.lon;
            this.lat = n.lat;
            this.zoneName = n.zoneName;
            this.zoneID = n.zoneID;
        }
        /*
         * 文字配列で値をセットする。データの順番は次のとおり。
         * String array[] = { placeName,placeNameKana,lat,lon,zoneName,zoneID };
         */
        void setValues(String [] args) {
            //中には列数が少ない行があることが予想される。
            //一つづつ検査してから代入しないと、ArrayOutBoundExceptionが出てしまう。
            if(args.length >= 1) placeName = args[0];
            if(args.length >= 2) placeNameKana = args[1];
            if(args.length >= 3) lat = args[2];
            if(args.length >= 4) lon = args[3];
            if(args.length >= 5) zoneName = args[4];
            if(args.length >= 6) zoneID = args[5];
        }
        //placeNameを返す。
        @Override
        public String toString() {
            if( zoneName.length() == 0 ) return placeName;
            return zoneName + " " + placeName;
        }
        // CSVファイル用で、1行分のデータを返す。タブ区切り。
        String getValue() {
            return   placeName + "," + placeNameKana + ","
                        +   lat + "," + lon   + ","
                        + zoneName + "," + zoneID;
        }
    }


    //検索イベント処理(検索窓内でEnter)
    private void search() {
        String key = searchTextField.getText().trim();
        System.out.println(key);
        if(key.length() == 0) return;
        int type = 0;
        if(key.startsWith("*")) {
            type = type | 2;
            key = key.substring( 1,key.length() );
        }
        if(key.endsWith("*")) {
            type = type | 1;
            key = key.substring( 0,key.length() - 1 );
        }
        //System.out.println("type = " + type);
        //中間一致のみのほうがなにかとわかりやすいので今はそれに固定している。
        type = 3;
        resultVector.clear();
        for(int i=0; i<placeNameVector.size(); i++) {
            Node node = placeNameVector.elementAt(i);
            if(type <= 1) {
                if( ( node.placeName.startsWith  ( key ) ) ||
                    ( node.placeNameKana.startsWith( key ) ) ||
                    ( node.zoneName.startsWith  ( key ) ) ){
                    resultVector.add(node);
                }
            } else if(type == 2) {
                if( (node.placeName.endsWith  ( key ) ) ||
                    (node.placeNameKana.endsWith( key ) ) ||
                    (node.zoneName.endsWith  ( key ) ) ){
                    resultVector.add(node);
                }
            } else if(type == 3) {
                if( (node.placeName.indexOf  ( key ) >= 0 ) ||
                    (node.placeNameKana.indexOf( key ) >= 0 ) ||
                    (node.zoneName.indexOf  ( key ) >= 0 ) ) {
                    resultVector.add(node);
                }
            }
        }
        locationTextField.setText("");
        searchJList.setListData(resultVector);
        //searchJList.setSelectedIndex(0);
        searchJList.clearSelection(); // *1
    }
    // *1 Lumi氏から指摘があったので、そのように変更してみた。
    //   「また地名を検索する所はリターンキーで検索されて、１個しかヒットしないと
    //    それで確定してしまうようですが、検索ボタンを付けた上で、１個だけの場合
    //    でも、表示した上で、それを利用者が選択するようにしたほうが直感的だと
    //     思います。」
    //    ただそのままEnterで確定できるのも操作を知っている人には快適なような
    //    気もする。

    /**
     * 編集イベント処理 (切り抜き、コピー)
     */
    class CopyPasteHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            int tabIndex = tabbedPane.getSelectedIndex();
            //選択されているノード(複数)を取得
            Object [] array = jLists[tabIndex].getSelectedValues();
            if ( command.equals("CUT") ) {
                Vector v = null;
                if ( tabIndex == 0 ) return;
                if ( tabIndex == 1 ) v = historyVector;
                if ( tabIndex == 2 ) v = myDataVector;
                for(int i=0; i<array.length; i++) {
                    Node node = (Node)array[i];
                    v.remove(node);
                }
                jLists[tabIndex].setListData(v);
                saveDict(historyFile,historyVector);
                saveDict(myDataFile,myDataVector);
            } else if ( command.equals("COPY") ) {
                clipBoardVector.clear();
                for(int i=0; i<array.length; i++) {
                    Node node = (Node)array[i];
                    clipBoardVector.add(node);
                }
            } else if ( command.equals("PASTE") ) {
                if(tabIndex == 2) {
                    for(int i=0; i<clipBoardVector.size(); i++) {
                        myDataVector.add(
                            new Node(clipBoardVector.elementAt(i)) );
                    }
                    myDataJList.setListData(myDataVector);
                    myDataJList.setSelectedIndex(myDataVector.size()-1);
                    saveDict(myDataFile,myDataVector);
                }
            } else if ( command.equals("MYDATA") ) {
                for(int i=0; i<array.length; i++) {
                    Node node = (Node)array[i];
                    myDataVector.add(new Node(node));
                    myDataJList.setListData(myDataVector);
                    myDataJList.setSelectedIndex(myDataVector.size()-1);
                    saveDict(myDataFile,myDataVector);
                }
            }
            limitaryEditMenu(tabIndex);
        }
    }
    /**
     * 登録と編集イベント
     */
    class EditHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            int tabIndex = tabbedPane.getSelectedIndex();
            //選択されているノード(複数)を取得
            Object [] array = jLists[tabIndex].getSelectedValues();
            Node n = null;
            if ( command.equals("EDIT") ) {
                n = (Node)array[0];
                Place place = getPlace(n);
                place = PlaceDialog.showDialog( PlaceChooser.this, place );
                if ( place == null ) return;
                // 地方名が半角スペースで区切られている場合は最初の節を地方名とみなす
                placeToNode( place, n );
            } else if ( command.equals("REGIST") ) {
                Place place = PlaceDialog.showDialog( PlaceChooser.this );
                if ( place == null ) return;
                n = new Node();
                placeToNode( place, n );
                myDataVector.add(n);
                myDataJList.setListData(myDataVector);
                myDataJList.setSelectedIndex(myDataVector.size()-1);
            }
            myDataJList.revalidate();
            locationMessage(n);
            saveDict(myDataFile,myDataVector);
        }
        // NodeをPlaceオブジェクトに変換して返す。
        Place getPlace(Node n) {
            Double lon = new Double(n.lon);
            Double lat = new Double(n.lat);
            TimeZone tz = TimeZone.getTimeZone(n.zoneID);
            // 地方名が存在するなら地名と連結して地名として扱う
            String name = n.zoneName;
            if ( name.length() != 0 ) name = name + " ";
            name = name + n.placeName;
            return new Place(name,lat,lon,tz);
        }
        // placeの内用をnに複写する。
        void placeToNode(Place place,Node n) {
            String name = place.getPlaceName();
            String zoneName = "";
            int spc = name.indexOf(" ");
            if ( spc > 0 ) {
                zoneName = name.substring(0,spc);
                name = name.substring(spc).trim();
            }
            n.lon = place.getLongitude().toString();
            n.lat = place.getLatitude().toString();
            n.zoneID = place.getTimeZone().getID();
            n.zoneName = zoneName;
            n.placeName = name;
        }
    }
    //Enterキーイベント (リストの選択項目をEnterキーで「決定」)
    class TypeEnterEvent extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            //System.out.println("TypeEnterKey Event");
            int keycode = (int)e.getKeyChar();
            //System.out.println( "keycode = " + KeyEvent.getKeyText(keycode) );
            if( keycode == KeyEvent.VK_ENTER ) {
                //System.out.println("Enterキーが押された");
                accept((JList)e.getSource());
            }
        }
    }

    // JListで項目がダブルクリックされた状態を検出するためのリスナ
    class JListMouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if ( e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                JPopupMenu menu = copyPopupMenu();
                menu.show((Component)e.getSource(), e.getX(), e.getY());
            } else if(e.getClickCount() == 2) {
                accept((JList)e.getSource());
            }
        }
        // マウスボタンが離れた時点でメニューのenabledを設定
        @Override
        public void mouseReleased(MouseEvent e) {
            int index = tabbedPane.getSelectedIndex();
            limitaryEditMenu(index);
        }
        //編集メニューのアイテムをPopupMenuとして複製する。*1
        JPopupMenu copyPopupMenu() {
            JPopupMenu menu = new JPopupMenu();
            for ( Component c : editMenu.getMenuComponents()) {
                if( c instanceof JSeparator ) {
                    menu.add(new JSeparator());
                } else if( c instanceof JMenuItem ) {
                    JMenuItem srcItem = (JMenuItem)c;
                    JMenuItem item = new JMenuItem();
                    item.setText(srcItem.getText());
                    item.setActionCommand(srcItem.getActionCommand());
                    item.setEnabled( srcItem.isEnabled() );
                    for ( ActionListener al : srcItem.getActionListeners() ) {
                        item.addActionListener(al);
                    }
                    menu.add(item);
                }
            }
            return menu;
        }
        // *1 editMenu.getPopupMenu()で取得したものを使えると簡単で良いのだが、
        //    それをしてshow()すると、メニューバーの編集メニューが次から表示
        //    されなくなってしまう。うまくリカバリする手があるのかもしれないが、
        //    よくわからないので、そのまま複製する方法を使った。
    }
    // タブが切り替えられたときのイベント処理
    class TabChangeHandler implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent evt) {
            JTabbedPane tpan = (JTabbedPane)evt.getSource();
            int index = tpan.getSelectedIndex();
            JList jlist = jLists[index];
            jlist.requestFocus();
            Node n = (Node)jlist.getSelectedValue();
            locationMessage( n );
            Config.usr.setInteger("PlaceChooser.tabIndex", index);
            //pref.putInt("PlaceChooser.tabIndex",index);
            limitaryEditMenu(index);
        }
    }

    /**
     * 決定イベント処理 (決定ボタン、リスト項目のダブクリ＆Enterキー入力)
     */
    private void accept(JList list) {
        Object [] node = list.getSelectedValues();
        if(node == null)    { showError(); return; }
        if(node.length != 1) { showError(); return; }
        Node n = (Node)node[0];
        if(n.zoneID.trim().equals("")) n.zoneID = "UTC";
        TimeZone tz = TimeZone.getTimeZone(n.zoneID);
        if ( !tz.getID().equals(n.zoneID)) {
            showError("サポートされていないタイムゾーンID : " + n.zoneID);
            return;
        }
        if ( n.toString().length()==0 ) {
            showError("地名がありません。");
            return;
        }
        try {
            result = new Place(n.toString(),new Double(n.lat),new Double(n.lon),
                TimeZone.getTimeZone(n.zoneID));
        } catch( Exception e ) {
            showError(e.toString());
            return;
        }
        if ( list != historyJList ) {
            boolean contain = false;
            for(int i=0; i<historyVector.size(); i++) {
                String temp = historyVector.elementAt(i).toString();
                if(temp.equals(n.toString())) { contain = true; break; }
            }
            if ( ! contain ) {
                historyVector.insertElementAt(n,0);
                historyJList.setListData(historyVector);
                saveDict(historyFile,historyVector);
            }
        }
        dispose();
        Config.save();
    }
    private void showError() {
        String errMsg = "観測地名を選択してください。";
        JOptionPane.showMessageDialog(this,errMsg,"観測地選択エラー",
            JOptionPane.ERROR_MESSAGE);
    }
    private void showError(String err) {
        String errMsg = "<HTML>異常な観測地情報<BR>" + err + "</HTML>";
        JOptionPane.showMessageDialog(this,errMsg,"観測地選択エラー",
            JOptionPane.ERROR_MESSAGE);
    }

    private void setOrientation( String command, int orientation ) {
        myDataJList.setLayoutOrientation( orientation );
        searchJList.setLayoutOrientation( orientation );
        historyJList.setLayoutOrientation( orientation );
        //pref.put("PlaceChooser.view",command);
        Config.usr.setProperty("PlaceChooser.view", command);
    }

    //表示方式の切替イベント処理
    private void viewSelected(ActionEvent e) {
        String command = e.getActionCommand();
        if ( command.equals("縦に")) {
            setOrientation( command, JList.VERTICAL );
        } else if(command.equals("縦から横に")) {
            setOrientation( command, JList.VERTICAL_WRAP );
        } else if(command.equals("横から縦に")) {
            setOrientation( command, JList.HORIZONTAL_WRAP );
        }
    }

    // コンボボックスによる国内/国外の辞書切替
    private void dictSelected() {
        int dictnum = searchComboBox.getSelectedIndex();
        dictnum = dictnum < 0 ? 0 : dictnum;
        String temp = indexVector.elementAt(dictnum);
        // 処理してるデータ↓
        // 国内,gaiku_japane.csv
        // 国外,foreign3.csv
        String [] array = temp.split(","); //国内,gaiku_japane.csv等を分解
        File file = new File(placedir, array[1] );
        placeNameVector.clear();
        loadDict(file,placeNameVector);
        //pref.put( "PlaceChooser.country", "" + dictnum );
        Config.usr.setProperty("PlaceChooser.country", "" + dictnum);
        searchTargetLabel.setText( array[0]+ "の");
        resultVector.clear();
        searchJList.setListData(resultVector);
        locationTextField.setText("検索対象を「" + array[0] + "」にしました。");
        searchTextField.setText("");
    }

    // 検索リスト(JList)のセレクションイベント
    private void jlistValueChanged(ListSelectionEvent e) {
        int i = e.getFirstIndex();
        JList list = (JList)e.getSource();
        Object [] node = list.getSelectedValues();
        if(node == null) return;
        if(node.length != 1) return;
        Node n = (Node)node[0];
        locationMessage( n );
        list.requestFocus();
    }

    // Nodeの観測地情報をlocationTextFieldに表示する。
    private void locationMessage(Node n) {
        String text = "";
        if ( n != null ) {
            String lat = AngleConverter.getFormattedLatitude(new Double(n.lat));
            String lon = AngleConverter.getFormattedLongitude(new Double(n.lon));
            text = n.toString() + " " + lat + ",  " + lon + ", " + n.zoneID;
        }
        locationTextField.setText(text);
    }

    // 地名データが入ったベクタを地名CSVファイルにセーブする
    private void saveDict(File dictFile,Vector<Node> dictVector ) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dictFile,"SJIS");
            pw.println( Node.CSV_HEADDER );
            for(int i=0; i<dictVector.size(); i++) {
                pw.println(dictVector.elementAt(i).getValue());
            }
            pw.flush();
        } catch( IOException e ) {
            Logger.getLogger( PlaceChooser.class.getName() )
                    .log( Level.SEVERE, null, e );
        } finally {
            try { pw.close(); } catch(Exception e) {}
        }
    }

    // 地名データが入ったファイルをVectorにロードする
    private void loadDict( File dictFile, Vector<Node> dictVector ) {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(dictFile);
            InputStreamReader isr = new InputStreamReader(fis,"SJIS");
            br = new BufferedReader(isr);
            if ( br.ready() ) br.readLine();
            while(br.ready()) {
                String [] locdata = br.readLine().split(",");
                Node n   = new Node();
                n.setValues(locdata);
                dictVector.add(n);
            }
        } catch ( Exception e ) {
            Logger.getLogger( PlaceChooser.class.getName() )
                    .log( Level.SEVERE, null, e );
        } finally {
            try { br.close(); } catch ( Exception e ) { }
        }
    }

    //地名CSVインデックスをベクタに読み込む
    private void loadIndex() {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(indexFile);
            InputStreamReader isr = new InputStreamReader(fis,"SJIS");
            br = new BufferedReader(isr);
            if ( br.ready() ) br.readLine(); //ヘッダー行は無視
            while(br.ready()) {
                String temp = br.readLine();
                indexVector.add(temp.trim());
            }
            br.close();
        } catch ( IOException e ) {
            Logger.getLogger( PlaceChooser.class.getName() )
                    .log( Level.SEVERE, null, e );
        } finally {
            try { br.close(); } catch(Exception e) { }
        }
    }

    // 親がDialogのときのコンストラクタ。直接は使わない。
    private PlaceChooser(java.awt.Dialog owner) {
        super(owner, true);
        initComponents();
        init1();
    }

    // 親がFrameのときのコンストラクタ。直接newはさせない。
    private PlaceChooser(java.awt.Frame owner) {
        super(owner,true);
        initComponents();
        init1();
    }

    //国内,国外の切替コンボの初期設定
    private void initComboBox() {
        searchComboBox.removeAllItems();
        for(int i=0; i<indexVector.size(); i++) {
            String temp = indexVector.elementAt(i);
            String [] array = temp.split(",");
            searchComboBox.addItem(array[0]);
        }
        searchComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                dictSelected();
            }
        });
    }

    private void init1() {
        loadIndex();
        ///地名CSVファイルロード
        for(int i=0; i<indexVector.size(); i++) {
            String temp = indexVector.elementAt(i);
            String [] array = temp.split(",");
//            if(array[0].equals(pref.get("PlaceChooser.view","縦に"))) {
            if(array[0].equals( Config.usr.getProperty("PlaceChooser.view","縦に"))) {
                File file = new File( placedir, array[1]);
                loadDict(file,placeNameVector);
                break;
            }
        }
        if(historyFile.exists()) loadDict(historyFile,historyVector);
        if(myDataFile.exists()) loadDict(myDataFile,myDataVector);
        //}
        //private void init2() {
        jLists[0] = searchJList;
        jLists[1] = historyJList;
        jLists[2] = myDataJList;
        initComboBox();
        //リスト表示方式の切替メニューを設定
        buttonGroup1.add(viewRadioButtonMenuItem1);
        buttonGroup1.add(viewRadioButtonMenuItem2);
        buttonGroup1.add(viewRadioButtonMenuItem3);
        //タブが切り替えられたときのイベント処理
        tabbedPane.addChangeListener( new TabChangeHandler() );
        //タブのショートカットキーを設定
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_S );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_H );
        tabbedPane.setMnemonicAt( 2, KeyEvent.VK_M );

        searchJList.setListData( resultVector );
        historyJList.setListData( historyVector );
        myDataJList.setListData( myDataVector );

        //ウィンドウのサイズと位置を復元
        WindowMoveHandler wmh =
            new WindowMoveHandler("PlaceChooser.BOUNDS", this);
        addComponentListener( wmh );
        wmh.setBounds();
        //JList内でのダブルクリックイベント
        JListMouseHandler jlmh = new JListMouseHandler();
        searchJList.addMouseListener(jlmh);
        historyJList.addMouseListener(jlmh);
        myDataJList.addMouseListener(jlmh);

        TypeEnterEvent tev = new TypeEnterEvent();
        searchJList.addKeyListener(tev);
        historyJList.addKeyListener(tev);
        myDataJList.addKeyListener(tev);
        ListSelectionListener selListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                jlistValueChanged(e);
            }
        };
        searchJList.addListSelectionListener(selListener);
        historyJList.addListSelectionListener(selListener);
        myDataJList.addListSelectionListener(selListener);

        //コピペ、削除のイベントリスナの登録
        CopyPasteHandler cph = new CopyPasteHandler();
        cutMenuItem.addActionListener(cph);
        copyMenuItem.addActionListener(cph);
        pasteMenuItem.addActionListener(cph);
        mydataMenuItem.addActionListener(cph);

        //マイデータの編集と登録
        EditHandler ehand = new EditHandler();
        editMenuItem.addActionListener(ehand);
        registMenuItem.addActionListener(ehand);

        //検索方式を復元
//        int countryNum = pref.getInt("PlaceChooser.country",0);
        int countryNum = Config.usr.getInteger("PlaceChooser.country",0);
        searchComboBox.setSelectedIndex( countryNum );

        //リスト表示方式を復元
//        String layout = pref.get("PlaceChooser.view","縦に");
        String layout = Config.usr.getProperty("PlaceChooser.view","縦に");
        if(layout.equals("縦に"))
            viewRadioButtonMenuItem1.doClick();
        else if(layout.equals("縦から横に"))
            viewRadioButtonMenuItem2.doClick();
        else
            viewRadioButtonMenuItem3.doClick();
        try {
//            int index = pref.getInt("PlaceChooser.tabIndex",0);
            int index = Config.usr.getInteger("PlaceChooser.tabIndex",0);
            tabbedPane.setSelectedIndex(index);
            limitaryEditMenu(index);
        } catch(NumberFormatException e) {
            System.out.println(e.toString());
        }
        //ESCキーでダイアログをクローズできるようにキーを割り当てる
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abortButton.doClick();
            }
        });
    }

    ///編集機能に制限をかけるメソッド
    private void limitaryEditMenu(int index) {
        if(index == 0) {             // 検索用
            boolean e = searchJList.getSelectedIndices().length >= 1;
            copyMenuItem.setEnabled(e);
            pasteMenuItem.setEnabled(false);
            registMenuItem.setEnabled(false);
            editMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
            mydataMenuItem.setEnabled(e);
        }
        if(index == 1) {             // ヒストリー用
            boolean e = historyJList.getSelectedIndices().length >= 1;
            copyMenuItem.setEnabled(e);
            pasteMenuItem.setEnabled(false);
            registMenuItem.setEnabled(false);
            editMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(e);
            mydataMenuItem.setEnabled(e);
        }
        if(index == 2) {             // マイデータ用
            boolean e = myDataJList.getSelectedIndices().length >= 1;
            copyMenuItem.setEnabled(e);
            pasteMenuItem.setEnabled( clipBoardVector.size() > 0 );
            registMenuItem.setEnabled(true);
            editMenuItem.setEnabled(e);
            cutMenuItem.setEnabled(e);
            mydataMenuItem.setEnabled(false);
        }
    }
    /**
     * PlaceChooserダイアログを開き、ユーザが選択した土地情報を返す。
     * 戻り値はString[]で、すべて文字列で返す。
     * <pre>
     * values[PlaceChooser.PLACENAME] 土地の名前
     * values[PlaceChooser.LATITUDE]  土地の緯度
     * values[PlaceChooser.LONGITUDE] 土地の経度
     * values[PlaceChooser.TIMEZONE]　TimeZoneクラスで使用できるTimeZoneID
     * </pre>
     * 選択が中止された場合はnullを返す。
     * @param parent このダイアログを使役する親コンポーネント
     * @return 文字配列で先頭から[地名,緯度,経度,TimeZone ID]
     */
    public static Place showDialog(Component parent) {
        Window window = ParentWindow.getWindowForComponent(parent);
        final PlaceChooser dialog;
        if(window instanceof Frame) {
            dialog = new PlaceChooser((Frame)window);
        } else {
            dialog = new PlaceChooser((Dialog)window);
        }
        dialog.setComponentOrientation(window.getComponentOrientation());
        dialog.pack();
        //親コンポーネントに対してセンタリング
        // 表示位置も保存されているのだが、デスクトップ外になったときのことを
        // 考慮している。
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
        return dialog.result;
    }
    /**
     * テスト。引数不要。
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                System.setProperty("app.topounit","60");
                Place place = showDialog(null);
                System.out.println(place);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        searchTargetLabel = new javax.swing.JLabel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        searchComboBox = new javax.swing.JComboBox();
        searchTextField = new javax.swing.JTextField();
        searchTextField.setColumns(10);
        searchButton = new javax.swing.JButton();
        javax.swing.JScrollPane searchScrollPane = new javax.swing.JScrollPane();
        searchJList = new javax.swing.JList();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyJList = new javax.swing.JList();
        javax.swing.JPanel myDataPanel = new javax.swing.JPanel();
        myDataScrollPane = new javax.swing.JScrollPane();
        myDataJList = new javax.swing.JList();
        locationTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        acceptButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();
        resizeIconPanel1 = new to.tetramorph.widget.ResizeIconPanel();
        menuBar = new javax.swing.JMenuBar();
        editMenu = new javax.swing.JMenu();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        cutMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mydataMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        editMenuItem = new javax.swing.JMenuItem();
        registMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        viewRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        viewRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();

        searchTargetLabel.setText("国内の");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("場所の選択");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(350, 300));

        jPanel2.setLayout(new java.awt.BorderLayout());

        searchPanel.setName("検索(S)"); // NOI18N
        searchPanel.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("検索対象");
        jPanel3.add(jLabel2);

        searchComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        searchComboBox.setPreferredSize(new java.awt.Dimension(90, 19));
        jPanel3.add(searchComboBox);

        searchTextField.setColumns(12);
        searchTextField.setText("検索キー");
        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchTextFieldActionPerformed(evt);
            }
        });
        searchTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchTextFieldFocusGained(evt);
            }
        });
        jPanel3.add(searchTextField);

        searchButton.setText("検索");
        searchButton.setMargin(new java.awt.Insets(1, 8, 1, 8));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        jPanel3.add(searchButton);

        searchPanel.add(jPanel3, java.awt.BorderLayout.NORTH);

        searchJList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        searchJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        searchJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        searchJList.setVisibleRowCount(0);
        searchScrollPane.setViewportView(searchJList);

        searchPanel.add(searchScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("検索(S)", searchPanel);

        historyPanel.setLayout(new java.awt.BorderLayout());

        historyScrollPane.setName("ﾋｽﾄﾘｰ(H)"); // NOI18N

        historyJList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        historyJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        historyJList.setVisibleRowCount(0);
        historyScrollPane.setViewportView(historyJList);

        historyPanel.add(historyScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("検索履歴(H)", historyPanel);

        myDataPanel.setLayout(new java.awt.BorderLayout());

        myDataScrollPane.setName("ﾏｲﾃﾞｰﾀ(M)"); // NOI18N

        myDataJList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        myDataJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        myDataJList.setVisibleRowCount(0);
        myDataScrollPane.setViewportView(myDataJList);

        myDataPanel.add(myDataScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("マイデータ(M)", myDataPanel);

        jPanel2.add(tabbedPane, java.awt.BorderLayout.CENTER);

        locationTextField.setColumns(8);
        locationTextField.setEditable(false);
        locationTextField.setText("緯度経度窓");
        locationTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        jPanel2.add(locationTextField, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.BorderLayout());

        acceptButton.setMnemonic('Y');
        acceptButton.setText("決定(Y)");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });
        jPanel1.add(acceptButton);

        abortButton.setMnemonic('N');
        abortButton.setText("中止(N)");
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });
        jPanel1.add(abortButton);

        jPanel4.add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel4.add(resizeIconPanel1, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

        editMenu.setMnemonic('E');
        editMenu.setText("編集(E)");

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setText("コピー");
        copyMenuItem.setActionCommand("COPY");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setText("ペースト");
        pasteMenuItem.setActionCommand("PASTE");
        editMenu.add(pasteMenuItem);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setText("カット");
        cutMenuItem.setActionCommand("CUT");
        editMenu.add(cutMenuItem);
        editMenu.add(jSeparator1);

        mydataMenuItem.setText("マイデータにコピー");
        mydataMenuItem.setActionCommand("MYDATA");
        editMenu.add(mydataMenuItem);
        editMenu.add(jSeparator2);

        editMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        editMenuItem.setText("編集");
        editMenuItem.setActionCommand("EDIT");
        editMenu.add(editMenuItem);

        registMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        registMenuItem.setText("登録");
        registMenuItem.setActionCommand("REGIST");
        editMenu.add(registMenuItem);

        menuBar.add(editMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("表示(V)");

        viewRadioButtonMenuItem1.setText("縦に");
        viewRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRadioButtonMenuItem1ActionPerformed(evt);
            }
        });
        viewMenu.add(viewRadioButtonMenuItem1);

        viewRadioButtonMenuItem2.setText("縦から横に");
        viewRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRadioButtonMenuItem2ActionPerformed(evt);
            }
        });
        viewMenu.add(viewRadioButtonMenuItem2);

        viewRadioButtonMenuItem3.setText("横から縦に");
        viewRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRadioButtonMenuItem3ActionPerformed(evt);
            }
        });
        viewMenu.add(viewRadioButtonMenuItem3);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTextFieldFocusGained
    //検索フォームが選択されたりフォーカスが来たとき、「検索キー」と入っている
    //なら、それを消去する。
        String text = searchTextField.getText();
        if ( text.equals("検索キー") ) {
            searchTextField.setText("");
        }
    }//GEN-LAST:event_searchTextFieldFocusGained

    private void viewRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewRadioButtonMenuItem3ActionPerformed
        //表示方式 横から縦に
        viewSelected(evt);
    }//GEN-LAST:event_viewRadioButtonMenuItem3ActionPerformed

    private void viewRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewRadioButtonMenuItem2ActionPerformed
        //表示方式 縦から横に
        viewSelected(evt);
    }//GEN-LAST:event_viewRadioButtonMenuItem2ActionPerformed

    private void viewRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewRadioButtonMenuItem1ActionPerformed
        //表示方式 縦に
        viewSelected(evt);
    }//GEN-LAST:event_viewRadioButtonMenuItem1ActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        //検索ボタンが押された
        search();
    }//GEN-LAST:event_searchButtonActionPerformed
    //検索テキストフィールドで改行キーが押された
    private void searchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchTextFieldActionPerformed
        search();
    }//GEN-LAST:event_searchTextFieldActionPerformed
//タベットパネルのタブ切替イベント//中止ボタンのアクション処理
    private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
      if(evt.getSource().equals(abortButton)) {
          dispose();
          Config.save();
      }
    }//GEN-LAST:event_abortButtonActionPerformed
// 決定ボタンのアクション処理
  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
      int tabIndex = tabbedPane.getSelectedIndex();
      if(evt.getSource().equals(acceptButton)) {
          if(tabIndex == 0) accept(searchJList);
          if(tabIndex == 1) accept(historyJList);
          if(tabIndex == 2) accept(myDataJList);
      }
  }//GEN-LAST:event_acceptButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JButton acceptButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JList historyJList;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JList myDataJList;
    private javax.swing.JScrollPane myDataScrollPane;
    private javax.swing.JMenuItem mydataMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem registMenuItem;
    private to.tetramorph.widget.ResizeIconPanel resizeIconPanel1;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox searchComboBox;
    private javax.swing.JList searchJList;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JLabel searchTargetLabel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JRadioButtonMenuItem viewRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem viewRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem viewRadioButtonMenuItem3;
    // End of variables declaration//GEN-END:variables

}
