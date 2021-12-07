/*
 * NPTRingsColorSettingPanel.java
 *
 * Created on 2007/10/11, 17:54
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel;
import to.tetramorph.util.ColorCalcurator;

/**
 * NPT三重円の獣帯リングの色設定を行うパネルで、
 * NPTColorConfPanelの中に組み込まれる。
 * @author  大澤義鷹
 */
public class NPTRingsColorSettingPanel extends javax.swing.JPanel {
    private static final Color COLOR1 = new Color(120,120,120);
    // 参照を保管できるように配列を使用している
    public Color[] _backgroundColor = new Color[]{ Color.WHITE };
    public Color[] _zodiacBGColors;
    public Color[] _zodiacFGColors;
    public Color[] _zodiacSymbolBorderColors = new Color[12];
    public Color[] _zodiacRingBorderColor = new Color[]{ Color.BLACK };
    public Color[] _zodiacGaugeColor = new Color[]{ Color.BLACK };
    public boolean[] _isNoZodiacGauge = new boolean[]{ false };
    public boolean[] _isNoZodiacSymbolsBorder = new boolean[]{ false };
    public boolean[] _isNoZodiacBG = new boolean[]{ false };
    public boolean[] _isNoZodiacRingBorder = new boolean[]{ false };
    /**
     * NPT三枚分のZodiacHouseColorDisplayPanelのインスタンス配列。
     * 獣帯の配色設定は三枚ともこのクラスのフィールドが設定されているので、この
     * 配列から参照するのではなく、このクラスフィールドのものを参照すること。
     */
    public ZodiacHouseColorDisplayPanel[] _displayPanels;

    private static final int NATAL = 0;
    private static final int PROGRESS = 1;
    private static final int TRANSIT = 2;
    private static final String DISPLAY = "DISPLAY";
    private static final String COLOR = "COLOR";
    private static final String NO_COLOR = "NO_COLOR";
    private static final String BG_OPTION = "BG_OPTION";
    private static final String GAUGE_OPTION = "GAUGE_OPTION";
    private static final String BORDER_OPTION = "BORDER_OPTION";
    private static final String EFFECT_OPTION = "EFFECT_OPTION";
    private static final String NO_OPTION = "NO_OPTION";
    private static final String REPEAT_TOOL = "REPEAT_TOOL";
    private static final String NO_TOOL = "NO_TOOL";
    private static final String COLOR_PALETTE = "COLOR_PALETTE";
    private static final String NO_PALETTE = "NO_PALETTE";
//  private static final String TEXT_DISPLAY  = "TEXT_DISPLAY";
    private JPanel[] cardPanels;
    private JCheckBox[] checkBoxs;

    /**
     * NPTRingsColorSettingPanelオブジェクトを作成する。
     */
    public NPTRingsColorSettingPanel() {
        initComponents();
        checkBoxs = new JCheckBox[]{
            noBGCheckBox, noGaugeCheckBox, noSymbolBorderCheckBox };
        palettePanel.setChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                showAcceptColor(palettePanel.getSelectedColor());
            }
        });
        cardPanels = new JPanel[]{ colorPanel, optionPanel, toolPanel,
                                    signDisplayPanel, colorPalettePanel};
        menuTree.getSelectionModel().setSelectionMode(
                        TreeSelectionModel.SINGLE_TREE_SELECTION );
        hideAllCards();
        createMenu();
        _displayPanels = new ZodiacHouseColorDisplayPanel[]{
            displayPanel1, displayPanel2, displayPanel3 };
        this._zodiacBGColors = new Color[12];
        this._zodiacFGColors = new Color[12];
        for ( int i = 0; i < _zodiacBGColors.length; i++) {
            _zodiacBGColors[i] = Const.SIGN_COLORS[i];
            _zodiacFGColors[i] = Color.WHITE;
            _zodiacSymbolBorderColors[i] = Color.BLACK;
        }
        //3つのパネルに同じ配列を割り当てる
        for (int i = 0; i < _displayPanels.length; i++) {
            _displayPanels[i]._zodiacBGColors = _zodiacBGColors;
            _displayPanels[i]._zodiacFGColors = _zodiacFGColors;
            _displayPanels[i]._zodiacGaugeColor = _zodiacGaugeColor;
            _displayPanels[i]._zodiacRingBorderColor = _zodiacRingBorderColor;
            _displayPanels[i]._isNoZodiacGauge = _isNoZodiacGauge;
            _displayPanels[i]._isNoZodiacSymbolsBorder = _isNoZodiacSymbolsBorder;
            _displayPanels[i]._zodiacSymbolBorderColors = _zodiacSymbolBorderColors;
            _displayPanels[i]._isNoZodiacBG = _isNoZodiacBG;
            _displayPanels[i]._isNoZodiacRingBorder = _isNoZodiacRingBorder;
            _displayPanels[i]._backgroundColor = _backgroundColor;
        }
        menuTree.addTreeSelectionListener(new TreeHandler());
    }

    /**
     * 背景色を要素数1の配列で与える。
     */
    public void setBGColor(Color[] color) {
        _backgroundColor = color;
        for (int i = 0; i < _displayPanels.length; i++) {
            _displayPanels[i]._backgroundColor = color;
        }
    }

    /**
     * メニューのアクションリスナで、NPTの識別IDや、ハウスやサインの番号を
     * 保管するフィールドをもつ。またリピート配色用のメソッドをもっており、
     * 必要に応じてオーバーライドして使用する。リピート配色が不要のメニューは
     * オーバーライドも不要。
     */
    private abstract class MenuListener {

        int npt;
        int num;

        MenuListener(int npt, int num) {
            this.npt = npt;
            this.num = num;
        }

        MenuListener(int npt) {
            this(npt, 0);
        }

        MenuListener() {
            this(0, 0);
        }

        //リピート配色したい場合は以下二つのメソッドをオーバーライドする
        void setColor(Color color) {
            throw new UnsupportedOperationException("");
        }

        //このメニューが担当している部品の色を返す。
        Color getColor() {
            throw new UnsupportedOperationException("");
        }

        void setButtonListener() {
            palettePanel.setChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent evt) {
                    showAcceptColor(palettePanel.getSelectedColor());
                    accept(palettePanel.getSelectedColor());
                    repaint();
                }
            });
            for (JCheckBox cb : checkBoxs) {
                for (ActionListener al : cb.getActionListeners()) {
                    cb.removeActionListener(al);
                }
            }
            ActionListener a = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    checked();
                    repaint();
                }
            };
            for (JCheckBox cb : checkBoxs) {
                cb.addActionListener(a);
            }
        }

        //このメニューが選択されたときの処理を書く
        abstract void actionPerformed();

        //確定ボタンが押されたときの処理を書く
        void accept(Color selectedColor) {
        }

        //チェックボックスが選択されたときの処理を書く
        void checked() {
        }
    }

    /**
     * JTreeに格納されるノード。ノード選択時に実行するリスナを登録できる。
     */
    class MenuNode extends DefaultMutableTreeNode {

        MenuListener l;

        //名前のみに選択されても配色機能はないノードを作る
        MenuNode(String name) {
            super(name);
            l = new MenuListener() {

                void actionPerformed() {
                    hideAllCards();
                }

                void accept(Color selcol) {
                } //個ノードのリスナをコール
            };
        }

        //名前と、選択されたときのリスナでノードを作る。
        MenuNode(String name, MenuListener l) {
            super(name);
            this.l = l;
        }
    }

//ツリーの選択ハンドラ
    class TreeHandler implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {

            TreePath path = e.getNewLeadSelectionPath();
            if (path == null) {
                //選択ﾉｰﾄﾞが存在しないとき(Shiftｷｰ併用選択のときこれは発生しうる
                hideAllCards();
            } else {
                MenuNode n = (MenuNode) path.getLastPathComponent();
                if (n.l != null) {
                    n.l.setButtonListener();
                    //ﾒﾆｭｰﾉｰﾄﾞのﾘｽﾅを呼び出す
                    n.l.actionPerformed();
                }
            }
        }
    }

    /**
     * リピートペイント。
     * repeatButtonのイベントリスナrepeatButtonActionPerformed()から呼び出される。
     * MenuNodeに格納されるリスナ側で、リピートペイントのループを回す処理を書く
     * やり方をすると、リピートペイントを必要とするメニューそれぞれが、数種類
     * のペイント(2色リピート〜6色リピート、虹)方法を実装しなければならなくなる。
     * そこで、リスナ側ではそのメニューが受け持っている1ハウス分の背景色とか文字色
     * をget/setするメソッドのみ実装しておき、リピートペイントの実行ボタンが押され
     * たら、このメソッドの中でリスナのgetColor(),setColor()を呼び出しながら
     * ループを回すようにする。
     * リピートのループはこのメソッドのみでよく、ループにひっかかっているメニュー
     * リスナが、それぞれ受け持っている背景や文字色を変化させる。
     *
     * 背景色やハウス番号以外のメニューでリピート配色が必要ない場合、配色リピート
     * 機能のボタンのあるtoolPanelは画面から隠され、実行ボタンが押されることはない
     * ため、このメソッドが呼び出されることもなく、MenuNodeに格納されているリスナが、
     * set/getColor()をサポートしているかしていないかの識別は必要ない。
     */
    private void repeatPaint() {
        TreePath path = menuTree.getSelectionPath();
        if (path == null) {
            return;
        }
        //親をみつけ、そこから下の子のリストを得る
        TreeNode parent = ((MenuNode) path.getLastPathComponent()).getParent();

        //子ノードのリスナだけ取り出してリストに移す
        List<MenuListener> list = new ArrayList<MenuListener>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            MenuNode node = (MenuNode) parent.getChildAt(i);
            list.add((MenuListener) node.l);
        }
        int count =
            new int[]{ 1, 2, 3, 4, 6, 0 }[ repeatComboBox.getSelectedIndex() ];
        repeatPaint(count, list);
    }

    /**
     * 単一色で塗りつぶす。単一色で塗りつぶすメニューリスナ内から呼ばれる。
     * これが呼ばれるとき、選択されているフォルダには12個のノードがあることが前提
     */
    private void constantPaint() {
        TreePath path = menuTree.getSelectionPath();
        if (path == null) {
            return;
        }
        TreeNode parent = (MenuNode) path.getLastPathComponent();
        //子ノードのリスナだけ取り出してリストに移す
        List<MenuListener> list = new ArrayList<MenuListener>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            MenuNode node = (MenuNode) parent.getChildAt(i);
            list.add((MenuListener) node.l);
        }
        repeatPaint(1, list);
    }

    /**
     * リピートペイントの主要部分。repeatPaint(),constantPaint()が呼び出される。
     * @param count 1,2,3,4,6,0 のいづれかで、0は虹を作る。その他は、指定数の
     * ハウスやサインをくり返す。
     * @param list リピートペイントの対象となる12個のメニューリスナのリスト。
     */
    private void repeatPaint(int count, List<MenuListener> list) {
        if (count >= 1 && count <= 6 && count != 5) {
            for (int i = count; i < 12; i += count) {
                for (int j = 0; j < count; j++) {
                    list.get(i + j).setColor(list.get(j).getColor());
                }
            }
        } else {
            //虹
            Color[] c = new Color[12];
            for (int i = 0; i < list.size(); i++) {
                c[i] = list.get(i).getColor();
            }
            ColorCalcurator.getColorRing(c);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setColor(c[i]);
            }
        }
        repaint();
    }
    //全カードをすべて隠す ( 空白パネルにする )

    private void hideAllCards() {
        setCards(NO_COLOR, NO_OPTION, NO_TOOL, NO_PALETTE);
    }
    // npt,色、オプションなし、ツールなし、パレット有りのカードを表示
    // 一番多く使われているパターン

    private void showHouseColorCards(int npt) {
        setCards(DISPLAY + npt, COLOR, NO_OPTION, NO_TOOL, COLOR_PALETTE);
    }
    //指定カードを各ﾊﾟﾈﾙに設定し、acceptButtonをEnabledにする

    private void setCards(String... cardNames) {
        for (String name : cardNames) {
            for (JPanel pan : cardPanels) {
                CardLayout layout = (CardLayout) pan.getLayout();
                layout.show(pan, name);
            }
        }
    }
    //設定色ラベルに色を反映させる

    private void showAcceptColor(Color color) {
        colorConfLabel.setBackground(color);
        palettePanel.setChooserColor(color);
        rgbLabel.setText(String.format(
            "R=%d,G=%d,B=%d", color.getRed(), color.getGreen(), color.getBlue()));
    }

    // 設定色ラベルの色を返す
    private Color getAcceptColor() {
        return colorConfLabel.getBackground();
    }
    /***************************************************************************
     * ここよりメニューのリスナ
     ***************************************************************************/
    // 全体の背景色ハンドラ

    class BGHandler extends MenuListener {

        //BGHandler() { super(); }
        void actionPerformed() {
            setCards(COLOR, NO_OPTION, NO_TOOL, COLOR_PALETTE);
            showAcceptColor(_backgroundColor[0]);
        }

        void accept(Color color) {
            _backgroundColor[0] = color;
        }
    }
    //サイン背景色設定ハンドラ

    class ZodiacBGHandler extends MenuListener {

        // numは0-11を指定。
        ZodiacBGHandler(int num) {
            super(0, num);
        }

        void actionPerformed() {
            showAcceptColor(_zodiacBGColors[num]);
            if (_isNoZodiacBG[0]) {
                hideAllCards();
            } else {
                setCards(COLOR, NO_OPTION, REPEAT_TOOL, COLOR_PALETTE);
            }
        }

        void accept(Color selcol) {
            _zodiacBGColors[num] = selcol;
        }

        void setColor(Color color) {
            _zodiacBGColors[num] = color;
        }

        Color getColor() {
            return _zodiacBGColors[num];
        }
    }
    //獣帯円の背景色の有無(サイン背景色フォルダのハンドラ)

    class ZodiacNoBGHandler extends MenuListener {

        ZodiacNoBGHandler() {
            super();
        }

        void actionPerformed() {
            setCards(NO_COLOR, BG_OPTION, NO_TOOL, NO_PALETTE);
        }

        void checked() {
            _isNoZodiacBG[0] = noBGCheckBox.isSelected();
        }
    }
    //サインシンボル色

    class ZodiacFGHandler extends MenuListener {

        // numは0-11を指定。
        ZodiacFGHandler(int num) {
            super(0, num);
        }

        void actionPerformed() {
            setCards(COLOR, NO_OPTION, REPEAT_TOOL, COLOR_PALETTE);
            showAcceptColor(_zodiacFGColors[num]);
        }

        void accept(Color selcol) {
            _zodiacFGColors[num] = selcol;
            repaint();
        }

        void setColor(Color color) {
            _zodiacFGColors[num] = color;
        }

        Color getColor() {
            return _zodiacFGColors[num];
        }
    }
    //サインシンボル色の一括セット

    class ZodiacFGFolderHandler extends MenuListener {

        void actionPerformed() {
            setCards(COLOR, NO_OPTION, NO_TOOL, COLOR_PALETTE);
            showAcceptColor(_zodiacFGColors[0]);
        }

        void accept(Color selcol) {
            _zodiacFGColors[0] = selcol;
            constantPaint();
        }
    }
    //サインシンボルの縁取り色

    class ZodiacSymbolBorderHandler extends MenuListener {

        ZodiacSymbolBorderHandler(int num) {
            super(0, num);
        }

        void actionPerformed() {
            showAcceptColor(_zodiacSymbolBorderColors[num]);
            if (_isNoZodiacSymbolsBorder[0]) {
                setCards(COLOR, NO_OPTION, NO_TOOL, COLOR_PALETTE);
            } else {
                setCards(COLOR, NO_OPTION, REPEAT_TOOL, COLOR_PALETTE);
            }
        }

        void accept(Color selcol) {
            _zodiacSymbolBorderColors[num] = selcol;
        }

        @Override
        void checked() {
            _isNoZodiacSymbolsBorder[0] = noSymbolBorderCheckBox.isSelected();
        }

        Color getColor() {
            return _zodiacSymbolBorderColors[num];
        }

        void setColor(Color color) {
            _zodiacSymbolBorderColors[num] = color;
        }
    }
    //サインシンボル縁取り線の有無

    class ZodiacSymbolBorderFolderHandler extends MenuListener {

        ZodiacSymbolBorderFolderHandler() {
            super();
        }

        void actionPerformed() {
            setCards(NO_COLOR, BORDER_OPTION, NO_TOOL, NO_PALETTE);
            noSymbolBorderCheckBox.setSelected(_isNoZodiacSymbolsBorder[0]);
        }

        @Override
        void checked() {
            _isNoZodiacSymbolsBorder[0] = noSymbolBorderCheckBox.isSelected();
        }
    }
    //獣帯円のゲージ色

    class ZodiacGaugeHandler extends MenuListener {

        ZodiacGaugeHandler() {
            super();
        }

        void actionPerformed() {
            setCards(COLOR, GAUGE_OPTION, NO_TOOL, COLOR_PALETTE);
            showAcceptColor(_zodiacGaugeColor[0]);
            noGaugeCheckBox.setSelected(_isNoZodiacGauge[0]);
        }

        void accept(Color selcol) {
            _zodiacGaugeColor[0] = selcol;
        }

        void checked() {
            _isNoZodiacGauge[0] = noGaugeCheckBox.isSelected();
        }
    }
    //獣帯円の輪郭線

    class ZodiacBorderHandler extends MenuListener {

        ZodiacBorderHandler() {
            super();
        }

        void actionPerformed() {
            setCards(COLOR, BORDER_OPTION, NO_TOOL, COLOR_PALETTE);
            showAcceptColor(_zodiacRingBorderColor[0]);
            noSymbolBorderCheckBox.setSelected(_isNoZodiacRingBorder[0]);
        }

        void accept(Color selcol) {
            _zodiacRingBorderColor[0] = selcol;
        }

        void checked() {
            _isNoZodiacRingBorder[0] = noSymbolBorderCheckBox.isSelected();
        }
    }
    //ハウス番号色(単色)(獣帯リングの外)

    class OuterHouseNumberHandler extends MenuListener {

        OuterHouseNumberHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._outerHousesNumberColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._outerHousesNumberColor = selcol;
        }
    }
    //カスプ線色(単色)(獣帯リングの外)

    class OuterCuspsHandler extends MenuListener {

        OuterCuspsHandler(int npt) {
            super(npt);
        }

        public void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._outerCuspsColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._outerCuspsColor = selcol;
        }
    }
    //カスプ度数色(単色)(獣帯リングの外)

    class OuterCuspsDegreeHandler extends MenuListener {

        OuterCuspsDegreeHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._outerCuspsDegreeColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._outerCuspsDegreeColor = selcol;
        }
    }
    // ハウスリングの線色

    class HouseBorderHandler extends MenuListener {

        HouseBorderHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._housesBorderColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._housesBorderColor = selcol;
        }
    }
    //ハウス背景色設定ハンドラ

    class HouseBGHandler extends MenuListener {

        HouseBGHandler(int npt, int num) {
            super(npt, num);
        }

        void actionPerformed() {
            if (_displayPanels[npt]._isNoHousesBG) {
                hideAllCards();
                return;
            }
            setCards(
                DISPLAY + npt, COLOR, NO_OPTION, REPEAT_TOOL, COLOR_PALETTE );
            Color c = _displayPanels[npt]._houseBGColors[num - 1];
            showAcceptColor(c);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._houseBGColors[num - 1] = selcol;
        }

        void setColor(Color color) {
            _displayPanels[npt]._houseBGColors[num - 1] = color;
        }

        Color getColor() {
            return _displayPanels[npt]._houseBGColors[num - 1];
        }
    }
    //ハウス背景の有無

    class HouseBGFolderHandler extends MenuListener {

        HouseBGFolderHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            setCards(DISPLAY + npt, NO_COLOR, BG_OPTION, NO_TOOL, NO_PALETTE);
            noBGCheckBox.setSelected(_displayPanels[npt]._isNoHousesBG);
        }

        void checked() {
            _displayPanels[npt]._isNoHousesBG = noBGCheckBox.isSelected();
        }
    }
    //ハウス番号設定ハンドラ

    class HouseNumbersHandler extends MenuListener {

        //numには1-12を指定。内部で-1している。
        HouseNumbersHandler(int npt, int num) {
            super(npt, num);
        }

        void actionPerformed() {
            setCards(DISPLAY + npt, COLOR, NO_OPTION, REPEAT_TOOL, COLOR_PALETTE);
            Color c = _displayPanels[npt]._houseNumberColors[num - 1];
            showAcceptColor(c);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._houseNumberColors[num - 1] = selcol;
        }

        void setColor(Color color) {
            _displayPanels[npt]._houseNumberColors[num - 1] = color;
        }

        Color getColor() {
            return _displayPanels[npt]._houseNumberColors[num - 1];
        }
    }
    //ハウス番号を12ハウスまとめて塗りつぶし

    class HouseNumberFolderHandler extends MenuListener {

        HouseNumberFolderHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._houseNumberColors[npt]);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._houseNumberColors[0] = selcol;
            constantPaint();
        }
    }
    //天体色設定ハンドラ

    class BodyColorHandler extends MenuListener {

        BodyColorHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._bodysColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._bodysColor = selcol;
            _displayPanels[npt].repaint();
        }
    }
    //天体縁取り色ハンドラ

    class BodyBorderHandler extends MenuListener {

        JRadioButton[] buttons = new JRadioButton[]{
            effectRadioButton1, effectRadioButton2, effectRadioButton3 };

        BodyBorderHandler(int npt) {
            super(npt);
        }

        //このクラスはこのメソッドをオーバーライドして、ラジオボタンの検出を行う
        void setButtonListener() {
            super.setButtonListener(); //ｶﾗｰﾊﾟﾚｯﾄのﾘｽﾅも必要なので呼び出す
            ActionListener a = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    checked();
                    repaint();
                }
            };
            for (JRadioButton rb : buttons) {
                //ﾗｼﾞｵﾎﾞﾀﾝの既存ﾘｽﾅを全削除
                for (ActionListener al : rb.getActionListeners()) {
                    rb.removeActionListener(al);
                }
                rb.addActionListener(a); //新しく登録
            }
        }

        public void actionPerformed() {
            setCards(DISPLAY + npt, COLOR, EFFECT_OPTION, NO_TOOL, COLOR_PALETTE);
            buttons[_displayPanels[npt]._bodysEffect].setSelected(true);
            showAcceptColor(_displayPanels[npt]._bodysBorderColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._bodysBorderColor = selcol;
        }

        void checked() {
            //ﾗｼﾞｵﾎﾞﾀﾝのActionCommandには"0"〜"2"までの文字が登録されている。
            int num = Integer.parseInt(
                effectButtonGroup.getSelection().getActionCommand() );
            _displayPanels[npt]._bodysEffect = num;
        }
    }
    //天体度数色

    class BodyDegreeHandler extends MenuListener {

        BodyDegreeHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            ;
            showAcceptColor(_displayPanels[npt]._bodysDegreeColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._bodysDegreeColor = selcol;
        }
    }
    //カスプ線色

    class CuspLineHandler extends MenuListener {

        CuspLineHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._cuspsColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._cuspsColor = selcol;
        }
    }
    //ハウスのゲージ色

    class HouseGuageHandler extends MenuListener {

        HouseGuageHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            setCards(DISPLAY + npt, COLOR, GAUGE_OPTION, NO_TOOL, COLOR_PALETTE);
            showAcceptColor(_displayPanels[npt]._housesGaugeColor);
            noGaugeCheckBox.setSelected(_displayPanels[npt]._isNoHousesGauge);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._housesGaugeColor = selcol;
        }

        void checked() {
            _displayPanels[npt]._isNoHousesGauge = noGaugeCheckBox.isSelected();
        }
    }
    //ゲージと天体を結ぶ引き出し線

    class LeadingLineHandler extends MenuListener {

        LeadingLineHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._leadingLineColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._leadingLineColor = selcol;
        }
    }
    //天体ハイライト

    class BodyHighLightHandler extends MenuListener {

        BodyHighLightHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._bodysHighLightColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._bodysHighLightColor = selcol;
        }
    }
    //ハウスハイライト

    class HouseHighLightHandler extends MenuListener {

        HouseHighLightHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._housesHighLightColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._housesHighLightColor = selcol;
        }
    }

    //ハウスリングに表示するテキスト文字色(ディスプレイはされない)

    class RingTextHandler extends MenuListener {

        RingTextHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            showHouseColorCards(npt);
            showAcceptColor(_displayPanels[npt]._ringTextColor);
        }

        void accept(Color selcol) {
            _displayPanels[npt]._ringTextColor = selcol;
        }
    }

    //ディスプレイのNPTの切替(N,P,Tのフォルダ用)

    class NPTSelectHandler extends MenuListener {

        NPTSelectHandler(int npt) {
            super(npt);
        }

        void actionPerformed() {
            setCards(DISPLAY + npt, NO_COLOR, NO_OPTION, NO_TOOL, NO_PALETTE );
        }
    }
    //テスト用ハンドラ

    class TestHandler extends MenuListener {

        TestHandler() {
            super();
        }

        void actionPerformed() {
            setCards(COLOR, BORDER_OPTION, REPEAT_TOOL, COLOR_PALETTE);
        }
    }

    /**
     * MenuNodeでメニューのツリーを作成しmenuTreeにセットする。
     */
    private void createMenu() {
        MenuNode rootNode = new MenuNode("配色設定");
        //rootNode.add( new MenuNode("TEST",new TestHandler()));
        rootNode.add(new MenuNode("背景色", new BGHandler()));
        //サインのメニュー
        MenuNode zNode = new MenuNode("サイン");
        MenuNode zBGNode = new MenuNode("背景色", new ZodiacNoBGHandler());
        MenuNode zFGNode = new MenuNode("シンボル色", new ZodiacFGFolderHandler());
        MenuNode zBDNode = new MenuNode("シンボル縁取り色", new ZodiacSymbolBorderFolderHandler());
        zNode.add(zBGNode);
        zNode.add(zFGNode);
        zNode.add(zBDNode);
        zNode.add(new MenuNode("リング線色", new ZodiacBorderHandler()));
        zNode.add(new MenuNode("ゲージの色", new ZodiacGaugeHandler()));
        for (int i = 0; i < 12; i++) {
            zBDNode.add(new MenuNode(Const.SIGN_NAMES[i], new ZodiacSymbolBorderHandler(i)));
            zFGNode.add(new MenuNode(Const.SIGN_NAMES[i], new ZodiacFGHandler(i)));
            zBGNode.add(new MenuNode(Const.SIGN_NAMES[i], new ZodiacBGHandler(i)));
        }
        rootNode.add(zNode);
        //NPT各ハウスのメニュー
        String[] title = {"ネイタル", "プログレス", "トランジット"};
        MenuNode[] nptNodes = new MenuNode[title.length];
        for (int i = 0; i < title.length; i++) {
            nptNodes[i] = new MenuNode(title[i], new NPTSelectHandler(i));
        }
        for (int npt = 0; npt < nptNodes.length; npt++) {
            MenuNode houseBGNode = new MenuNode("ハウス背景色", new HouseBGFolderHandler(npt));
            for (int i = 1; i <= 12; i++) {
                houseBGNode.add(new MenuNode(i + "室", new HouseBGHandler(npt, i)));
            }
            nptNodes[npt].add(houseBGNode);

            MenuNode houseNumNode = new MenuNode("ハウス番号色", new HouseNumberFolderHandler(npt));
            for (int i = 1; i <= 12; i++) {
                houseNumNode.add(new MenuNode(i + "室", new HouseNumbersHandler(npt, i)));
            }
            nptNodes[npt].add(houseNumNode);

            nptNodes[npt].add(new MenuNode("天体色", new BodyColorHandler(npt)));
            nptNodes[npt].add(new MenuNode("天体飾り", new BodyBorderHandler(npt)));
            nptNodes[npt].add(new MenuNode("天体度数色", new BodyDegreeHandler(npt)));
            nptNodes[npt].add(new MenuNode("天体ハイライト色", new BodyHighLightHandler(npt)));
            nptNodes[npt].add(new MenuNode("カスプ線色", new CuspLineHandler(npt)));
            nptNodes[npt].add(new MenuNode("ハウス円線色", new HouseBorderHandler(npt)));
            nptNodes[npt].add(new MenuNode("円外ハウス番号色", new OuterHouseNumberHandler(npt)));
            nptNodes[npt].add(new MenuNode("円外カスプ度数色", new OuterCuspsDegreeHandler(npt)));
            nptNodes[npt].add(new MenuNode("円外カスプ線色", new OuterCuspsHandler(npt)));
            nptNodes[npt].add(new MenuNode("ゲージ色", new HouseGuageHandler(npt)));
            nptNodes[npt].add(new MenuNode("引出し線色", new LeadingLineHandler(npt)));
            nptNodes[npt].add(new MenuNode("ハウスハイライト色", new HouseHighLightHandler(npt)));
            nptNodes[npt].add(new MenuNode("テキストハイライト色", new RingTextHandler(npt)));
            rootNode.add(nptNodes[npt]);
        }

        menuTree.setModel(new DefaultTreeModel(rootNode));
    }

    /***************************************************************************
     * ここよりテスト用のコード
     ***************************************************************************/
    static void createAndShowGUI() {
        if (UIManager.getLookAndFeel().getName().equals("Metal")) {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("サインカラー設定");
        NPTRingsColorSettingPanel panel = new NPTRingsColorSettingPanel();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** テスト */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel2;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;
        javax.swing.JPanel jPanel4;
        javax.swing.JPanel jPanel5;
        javax.swing.JPanel jPanel6;
        javax.swing.JPanel jPanel7;
        javax.swing.JPanel jPanel8;
        javax.swing.JPanel jPanel9;
        javax.swing.JScrollPane jScrollPane1;
        javax.swing.JPanel nullPanel3;

        effectButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        colorPalettePanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        palettePanel = new to.tetramorph.starbase.widget.ColorPalettePanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        menuTree = new javax.swing.JTree();
        jPanel4 = new javax.swing.JPanel();
        colorPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        colorConfLabel = new javax.swing.JLabel();
        rgbLabel = new javax.swing.JLabel();
        nullPanel3 = new javax.swing.JPanel();
        optionPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        noBGCheckBox = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        noGaugeCheckBox = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        noSymbolBorderCheckBox = new javax.swing.JCheckBox();
        nullPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        effectRadioButton1 = new javax.swing.JRadioButton();
        effectRadioButton2 = new javax.swing.JRadioButton();
        effectRadioButton3 = new javax.swing.JRadioButton();
        toolPanel = new javax.swing.JPanel();
        repeatPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        repeatButton = new javax.swing.JButton();
        repeatComboBox = new javax.swing.JComboBox();
        nullPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        signDisplayPanel = new javax.swing.JPanel();
        displayPanel1 = new to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel();
        displayPanel2 = new to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel();
        displayPanel3 = new to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel();

        effectButtonGroup.add(effectRadioButton1);
        effectButtonGroup.add(effectRadioButton2);
        effectButtonGroup.add(effectRadioButton3);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        colorPalettePanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 164, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        colorPalettePanel.add(jPanel7, "NO_PALETTE");

        colorPalettePanel.add(palettePanel, "COLOR_PALETTE");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        jPanel1.add(colorPalettePanel, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(160, 160));
        jScrollPane1.setViewportView(menuTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        colorPanel.setLayout(new java.awt.CardLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("\u8a2d\u5b9a\u8272");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel2.add(jLabel2, gridBagConstraints);

        colorConfLabel.setText("       ");
        colorConfLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorConfLabel.setMaximumSize(new java.awt.Dimension(80, 21));
        colorConfLabel.setMinimumSize(new java.awt.Dimension(80, 21));
        colorConfLabel.setOpaque(true);
        colorConfLabel.setPreferredSize(new java.awt.Dimension(80, 21));
        jPanel2.add(colorConfLabel, new java.awt.GridBagConstraints());

        rgbLabel.setText("R=,G=,B=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(rgbLabel, gridBagConstraints);

        colorPanel.add(jPanel2, "COLOR");

        colorPanel.add(nullPanel3, "NO_COLOR");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel4.add(colorPanel, gridBagConstraints);

        optionPanel.setLayout(new java.awt.CardLayout());

        jPanel6.setLayout(new java.awt.GridBagLayout());

        noBGCheckBox.setText("\u80cc\u666f\u8272\u306a\u3057");
        noBGCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noBGCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel6.add(noBGCheckBox, new java.awt.GridBagConstraints());

        optionPanel.add(jPanel6, "BG_OPTION");

        jPanel8.setLayout(new java.awt.GridBagLayout());

        noGaugeCheckBox.setText("\u76ee\u76db\u308a\u306a\u3057");
        noGaugeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noGaugeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel8.add(noGaugeCheckBox, new java.awt.GridBagConstraints());

        optionPanel.add(jPanel8, "GAUGE_OPTION");

        jPanel9.setLayout(new java.awt.GridBagLayout());

        noSymbolBorderCheckBox.setText("\u7e01\u53d6\u308a\u306a\u3057");
        noSymbolBorderCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noSymbolBorderCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel9.add(noSymbolBorderCheckBox, new java.awt.GridBagConstraints());

        optionPanel.add(jPanel9, "BORDER_OPTION");

        optionPanel.add(nullPanel2, "NO_OPTION");

        jPanel5.setLayout(new java.awt.GridBagLayout());

        effectRadioButton1.setText("\u52b9\u679c\u306a\u3057");
        effectRadioButton1.setActionCommand("0");
        effectRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        effectRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel5.add(effectRadioButton1, gridBagConstraints);

        effectRadioButton2.setText("\u7e01\u53d6\u308a");
        effectRadioButton2.setActionCommand("1");
        effectRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        effectRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel5.add(effectRadioButton2, gridBagConstraints);

        effectRadioButton3.setText("\u30c9\u30ed\u30c3\u30d7\u30b7\u30e3\u30c9\u30a6");
        effectRadioButton3.setActionCommand("2");
        effectRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        effectRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel5.add(effectRadioButton3, gridBagConstraints);

        optionPanel.add(jPanel5, "EFFECT_OPTION");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel4.add(optionPanel, gridBagConstraints);

        toolPanel.setLayout(new java.awt.CardLayout());

        repeatPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("\u30ea\u30d4\u30fc\u30c8\u30da\u30a4\u30f3\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        repeatPanel.add(jLabel1, gridBagConstraints);

        repeatButton.setText("\u5b9f\u884c");
        repeatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeatButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        repeatPanel.add(repeatButton, gridBagConstraints);

        repeatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1\u5ba4\u3092\u30ea\u30d4\u30fc\u30c8", "2\u5ba4\u307e\u3067\u3092\u30ea\u30d4\u30fc\u30c8", "3\u5ba4\u307e\u3067\u3092\u30ea\u30d4\u30fc\u30c8", "4\u5ba4\u307e\u3067\u3092\u30ea\u30d4\u30fc\u30c8", "6\u5ba4\u307e\u3067\u3092\u30ea\u30d4\u30fc\u30c8", "1,4,7,10\u5ba4\u304b\u3089\u8679\u3092\u4f5c\u308b" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        repeatPanel.add(repeatComboBox, gridBagConstraints);

        toolPanel.add(repeatPanel, "REPEAT_TOOL");

        javax.swing.GroupLayout nullPanelLayout = new javax.swing.GroupLayout(nullPanel);
        nullPanel.setLayout(nullPanelLayout);
        nullPanelLayout.setHorizontalGroup(
            nullPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );
        nullPanelLayout.setVerticalGroup(
            nullPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 67, Short.MAX_VALUE)
        );
        toolPanel.add(nullPanel, "NO_TOOL");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel4.add(toolPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel1.add(jPanel4, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        signDisplayPanel.setLayout(new java.awt.CardLayout());

        signDisplayPanel.setPreferredSize(new java.awt.Dimension(400, 100));

        signDisplayPanel.add(displayPanel1, "DISPLAY0");

        signDisplayPanel.add(displayPanel2, "DISPLAY1");

        signDisplayPanel.add(displayPanel3, "DISPLAY2");

        jPanel3.add(signDisplayPanel);

        add(jPanel3, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents

  private void repeatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repeatButtonActionPerformed
      repeatPaint();
  }//GEN-LAST:event_repeatButtonActionPerformed
      // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel colorConfLabel;
    private javax.swing.JPanel colorPalettePanel;
    private javax.swing.JPanel colorPanel;
    private to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel displayPanel1;
    private to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel displayPanel2;
    private to.tetramorph.starbase.widget.ZodiacHouseColorDisplayPanel displayPanel3;
    private javax.swing.ButtonGroup effectButtonGroup;
    private javax.swing.JRadioButton effectRadioButton1;
    private javax.swing.JRadioButton effectRadioButton2;
    private javax.swing.JRadioButton effectRadioButton3;
    private javax.swing.JTree menuTree;
    private javax.swing.JCheckBox noBGCheckBox;
    private javax.swing.JCheckBox noGaugeCheckBox;
    private javax.swing.JCheckBox noSymbolBorderCheckBox;
    private javax.swing.JPanel nullPanel;
    private javax.swing.JPanel nullPanel2;
    private javax.swing.JPanel optionPanel;
    private to.tetramorph.starbase.widget.ColorPalettePanel palettePanel;
    private javax.swing.JButton repeatButton;
    private javax.swing.JComboBox repeatComboBox;
    private javax.swing.JPanel repeatPanel;
    private javax.swing.JLabel rgbLabel;
    private javax.swing.JPanel signDisplayPanel;
    private javax.swing.JPanel toolPanel;
    // End of variables declaration//GEN-END:variables
}