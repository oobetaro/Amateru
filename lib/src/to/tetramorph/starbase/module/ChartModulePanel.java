/*
 * ChartModulePanel.java
 *
 * Created on 2007/02/19, 16:33
 */

package to.tetramorph.starbase.module;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.util.Preference;


/**
 * チャートモジュールを作成する際の抽象クラス。ChartInternalFrameの中で複数の
 * チャートモジュールがロードされ、ユーザからのメニュー操作によって選択的に
 * 使用される。
 * チャートモジュールはこのクラスに用意されているサビアンダイアログや色設定
 * ダイアログを使うことができ、モジュール内で選択した天体のサビアンを表示したり、
 * モジュール固有の設定情報を登録・編集できる。
 */
public abstract class ChartModulePanel extends javax.swing.JPanel {
    
    /**
     * モジュールの親である内部フレームChartPaneはChartTabを実装している。
     * アイコンをセットしたり、モジュール画面が選択されたりしたことを
     * アマテルに通達するメソッドを持つ。
     */
    protected ChartTab iframe;
    
    /**
     * プラグインの側でJDaialogやJFrameを使いたい場合にオーナーとして使用するFrame。
     */
    protected Frame parentFrame;
    
    /**
     * システムの設定情報。デフォルトタイムや使用ノードタイプ等。
     */
    protected Preference config;   //configDataの複製品    
    
    private Preference configData; //Config.dataの参照
    private SabianDialogHandler sabianDialogHandler;
    private String className;
    //選択されている色設定名。""ならﾃﾞﾌｫﾙﾄ色
    private String selectedColorConfName = "";
    //private Preference selectedColorPreference;
    private Preference colorPreference;
    //計算仕様のﾌﾟﾛﾊﾟﾃｲ
    private String selectedSpecificConfName = "";
    //private Preference selectedSpecificPreference;
    private Preference specificPreference;
    private Dictionary dictionary;
    /**
     * 動的にロードするプラグインのためのコンストラクタ。
     */
    public ChartModulePanel() {
        //initComponents();
    }
    
    /**
     * このモジュールに必須のパラメターを登録する。動的にクラスをロードし、
     * インスタンスを作成したあとこのメソッドで登録する。というのは、動的なロード
     * の場合、コンストラクタで引数を渡すことができないため。
     * ChartPane#createChartModulePanel()からのみ使用されている。
     *
     * @param iframe ChartModulePanelが格納されている内部フレーム
     * @param sabianDialogHandler
     * @param configData 基本設定情報を格納したPreference
     * @param className チャートモジュールのクラス名
     * @param parentFrame チャートモジュール側でJDialogを使うときに指定する親Frame。
     * 実はMainFrameを指定。
     */
    public final void setConstructArgs( ChartTab iframe,
                                           SabianDialogHandler sabianDialogHandler,
                                           Dictionary dictionary,
                                           Preference configData,
                                           String className,
                                           Frame parentFrame ) {
        this.iframe = iframe;
        this.sabianDialogHandler = sabianDialogHandler;
        this.configData = configData;
        this.className = className;
        this.parentFrame = parentFrame;
        this.dictionary = dictionary;
        // たとえばここで下行のようなコードを書いても、MainFrameは別パッケージなので
        // コンパイルは通らない。つまりChartModule側からMainFrameにアクセスはできない
        // 構造になっていて、セキュリティは保たれる。
        //MainFrame mf = (MainFrame)parentFrame;
        selectedColorConfName = configData.getProperty(
                                className + ".DefaultColorConfName", "" );
        config = new Preference();
        config.copy( configData );
        init();
    }
    
    public void getChartConfig( ChartConfig cc ) {
        cc.setPreference( config );
    }
    
    /**
     * このモジュールパネルのクラス名を返す。クラス名はコンストラクタから与えら
     * れた名前。
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * サビアンダイアログを返す。サブクラスとなるチャートモジュール(プラグイン)は
     * このメソッドを使ってシステム共通のサビアンダイアログにアクセスできる。
     */
    protected SabianDialogHandler getSabianDialogHandler() {
        return sabianDialogHandler;
    }
    /**
     * 汎用辞書を返す。（辞書ダイアログとのインターフェイス）
     */
    protected Dictionary getDictionary() {
        return dictionary;
    }
    /**
     * モジュール初期化時に呼び出される。モジュール作成者は必要に応じて
     * オーバーライドして初期化用の処理を書く。普通はコンストラクタの中で行う処理
     * をこのメソッドの中に書く。モジュールのインスタンスが作成されたのち、
     * 一番最初にこのメソッドが呼び出される。
     */
    protected void init() { }
    
    /**
     * Natalのリストをチャートモジュールにセットする。
     * データベースがこのメソッドを使ってNatalを渡して来るので、
     * チャートモジュールはこのデータをもとにホロスコープや、各種グラフを描画する
     * よう処理を実装する。
     * isNeedTransit()がtrueを返すように実装したモジュールの場合はtransitに値が
     * 入ってコールされるが、falseを返すようにした場合はnullが渡される。
     * @param channelData チャンネルデータ
     */
    public abstract void setData( ChannelData channelData );
    
    /**
     * このチャートモジュールがフォーカスを受け取る場合はtrueを返す。
     * またtrueをセットしてある場合は、ChartInternalFrameによって、キーリスナが登録
     * されるので、addKeyListenerもオーバーライドしてリスナの登録を各ウィジェトに
     * 施すようにすること。
     * このメソッドはオーバーライドしなけれぱfalseを返す。
     */
    public boolean isFocusable() {
        return false;
    }
    
    /**
     * トランジットチャンネルを必要としてるモジュールならtrueを返す。
     * falseにするとTimePanelのトランジットタブがDisenabledになる。
     */
    public abstract boolean isNeedTransit();
    
    /**
     * このモジュールがいくつチャンネルをもっているか返す。TimePanelはこの値
     * を元に切替スイッチ(日時場所データをどの何番目の円にストアするかの切替)を
     * 用意する。
     * 二重円なら、二つ。三重円でプログレスつきなら、二つ。三重円にそれぞれネイタル
     * を入れるなら三つ。二重円でネイタルとプログレスというなら一つ。
     */
    public abstract int getChannelSize();
    
    /**
     * チャンネル(複数)の名前を返す。getChannelSize()が返す数と同じ要素数でなけれ
     * ばならない。[0]から第一チャンネルと続く。
     */
    public abstract String [] getChannelNames();
    
    /**
     * デフォルトカラー設定を登録する。チャートモジュールの中でデフォルトで使用する
     * 固有の色設定情報をPreference形式で与える。どのようなパラメターでどのような
     * キーと値であってもかまわずモジュール製作者にゆだねられている。
     * システムはこのPreferenceの登録、追加、順列入替、更新、削除、改名の手続きを
     * サポートする。
     */
    public void setDefaultColor( Preference colorPref ) {
        this.colorPreference = colorPref;
    }
    
    /**
     * デフォルトカラー設定を返す。初期値はnull。setDefaultColor()でセットされた
     * 値を返すだけ。(getColorPreference()のほうが良い名前だと思う。Default不要)
     */
    public Preference getDefaultColor() {
        return colorPreference;
    }
    
    /**
     * カラー設定が変更されたときに呼び出される。
     * また初期化時にも呼び出される。色設定をカスタマイズ可能とする場合、サブクラス
     * でこのメソッドをオーバーライドする。オーバーライドしない場合、このメソッド
     * はなにもしない。
     * init()の次に呼び出されるメソッド。
     */
    public void updateColorSetting() {
    }
    
    /**
     * デフォルト仕様を登録する。チャートモジュールの中でデフォルトで使用する
     * 固有の仕様設定情報をPreference形式で与える。どのようなパラメターでどのような
     * キーと値であってもかまわずモジュール製作者にゆだねられている。
     * システムはこのPreferenceの登録、追加、順列入替、更新、削除、改名の手続きを
     * サポートする。
     */
    public void setDefaultSpecific( Preference specificPreference ) {
        this.specificPreference = specificPreference;
    }
    
    /**
     * デフォルト仕様を返す。初期値はnull。setDefaultColor()でセットされた
     * 値を返すだけ。
     */
    public Preference getDefaultSpecific() {
        return specificPreference;
    }
    
    /**
     * 計算仕様の設定が変更されたときに呼び出される。
     * また初期化時にも呼び出される。
     * 計算仕様をカスタマイズ可能とする場合、サブクラスでこのメソッドをオーバー
     * ライドする。カスタマイズイベントが発生するごとにこのメソッドが呼び出される
     * ので、サブクラスはそのタイミングで設定パネルの情報を反映させるようなコード
     * をこのメソッドに実装すれば良い。
     * オーバーライドしない場合、このメソッドはなにもしない。
     * updateColorSetting()の次に呼び出される。
     */
    public void updateSpecificSetting() {
    }
    
    /**
     * 色設定機能を持つプラグインは、このメソッドをオーバーライドして、設定パネル
     * のインスタンスを返すようにする。またsetDefaultColor(Preference p)メソッドに
     * 色設定のデフォルト値をセットする。この二つの操作によって、設定パネルは有効
     * になる。
     * オーバーライドされない場合は、このメソッドはnullを返す。
     * システム側ではこのメソッドを呼び出しSpecificDialogにはめ込んで出力。
     * それによりユーザは設定編集を行える。
     */
    public CustomizePanel getColorCustomizePanel() {
        return null;
    }
    
    /**
     * 計算設定機能を持つプラグインは、このメソッドをオーバーライドして、設定パネル
     * を実装する。またsetDefaultSpecific(Preference p)で、計算設定のデフォルト値
     * をセットする。この二つの手続きによって、計算設定パネルは有効になる。
     * オーバーライドされない場合は、このメソッドはnullを返す。
     * システム側ではこのメソッドを呼び出しSpecificDialoにはめ込んで出力。
     * それによりユーザは設定編集を行える。
     */
    public CustomizePanel getSpecificCustomizePanel() {
        return null;
    }
    
    /**
     * 表示(V)にいれるメニューアイテムのリストを返す。
     * オーバーライドしなければ、このメソッドはつねにnullを返す。
     */
    public List<Component> getViewMenuList() {
        return null;
    }
    
    /**
     * 設定(P)にいれるメニューを返す。
     * オーバーライドしなければ、このメソッドはつねにnullを返す。
     */
    public List<Component> getSpecificMenuList() {
        return null;
    }
    
    /**
     * アマテルからプラグインの(日付のセット以外の)細かい制御をするためのコマンド
     * をセットする。コマンドを受け取るプラグインはこのメソッドをオーバーライド
     * する。コマンドの発行はアマテル側からのみなので、サードパーティのプラグイン
     * はこのメソッドを実装する必要はない。アマテルシステムとなんらかの取り決めが
     * あってはじめてこのメソッドは意味を持つ。<br>
     * プラグイン式にしてあっても、それだけでは不便なケースがあり、その溝を埋める
     * ためにこのメソッドは用意された。(たとえばトランジット円を出す機能。)
     */
    public void setCommand( String [] args ) {
        
    }
    /**
     * このモジュールがどのようなモード設定を持っているか、
     * そのモード名一覧を返す。モードを持たないモジュールの場合は、nullを返す。
     * モード名を持つモジュールなら、モード名をChartModuleModeクラスで名前を
     * 用意して、それを列挙した配列を返す。
     */
    public ChartModuleMode [] getModuleModes() {
        return null;
    }
    
    /**
     * 初期化時に一度呼ばれ、このモジュールの基本動作モードを設定する。
     */
    public void setModuleMode( ChartModuleMode mode ) {
        
    }
    /**
     * 
     */
    public ChartModuleMode getModuleMode() {
        return null;
    }

    private ChartCustomizeListener calc_ccl = null;
    private ChartCustomizeListener color_ccl = null;
    
    /**
     * ChartPaneからモジュール初期化時に、このメソッドで引数をセットする。
     * これは設定パネルをモジュール側のコードから呼び出すための仕組みで、
     * このクラスのメソッドshowCalcCustomizeDialog()をサブクラスである
     * モジュール側で呼び出すと、リスナのメソッドがトリガされ、ダイアログが開く。
     * ChartCustomizeListenerはSpecificCustomizerにインプリメントされていて、
     * このインターフェイスにより、SpecificCustomizerの他のメソッドをモジュール
     * 側から操作できないように絶縁している。
     */
    public void setCustomizeListeners( ChartCustomizeListener calc_ccl,
                                        ChartCustomizeListener color_ccl ) {
        this.calc_ccl = calc_ccl;
        this.color_ccl = color_ccl;
    }
    
    /**
     * 計算条件のカスタマイズ用ダイアログを開く。
     * これはgetSpecificCustomizePanel()が返すパネルがはめこまれたダイアログを、
     * モジュール側から可視化したい場合に使用する。
     */
    public void showSpecificCustomizeDialog() {
        calc_ccl.showCustomizeDialog();
    }
    
    /**
     * 配色のカスタマイズ用ダイアログを開く。
     * これはgetColorCustomizePanel()が返すパネルがはめこまれたダイアログを、
     * モジュール側から可視化したい場合に使用する。
     */
    public void showColorCustomizeDialog() {
        color_ccl.showCustomizeDialog();
    }
    
    /**
     * チャート画像を返す。オーバーライドされない場合はnullを返す。
     * クリップボードにコピーしたり、画像ファイルに保存するためのイメージを返す。
     * @param size 要求画像サイズを指定
     */
    public BufferedImage getBufferedImage( Dimension size ) {
        return null;
    }
    
    /**
     * getBufferedImage()が画像を返すとき、このメソッドはtrueを返す。
     * オーバーライドされない場合はfalseを返す。
     */
    public boolean isImageServiceActivated() {
        return false;
    }
    
    /**
     * getBufferedImageがsizeの値を無視して、モジュール独自の画像サイズを返す
     * 場合はtrueを返す。
     */
    public boolean isFixedImageSize() {
        return false;
    }
    
    /**
     * このモジュールパネルの画面比率で、幅を１としたときの縦の比率を返す。
     * デフォルトでは0.707fを返し、これはA4用紙を横向きにした比率と同じ。
     */
    public float getHeightPer() {
        return 0.707f;
    }
    
    /**
     * 印刷用メソッドPrintable#print()を実装したコンポーネントを返す。
     * デフォルトではnullを返すので、印刷機能をサポートするプラグインは
     * オーバーライドし、機能を実装する。
     */
    public Printable getPainter() {
        return null;
    }
    
    /**
     * このモジュールが印刷機能をサポートする場合はtrueを返す。
     */
    public boolean isPrintable() {
        return false;
    }
    
    /**
     * 印刷の際の用紙の向き、印刷部数などを定義したハッシュセットを返す。
     * isPrintable()がfalseのときはnullを返す。
     */
    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        if ( isPrintable() ) {
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add( OrientationRequested.LANDSCAPE );
            aset.add( new Copies(1) );
            aset.add( new JobName("AMATERU", null) );
            return aset;
        }
        return null;
    }
    
    /**
     * このモジュールがマニューバによるアニメーション操作を受け付ける場合は、
     * trueを返す。デフォルトはtrueを返す。
     */
    public boolean isAnimationActivated() {
        return true;
    }

    /**
     * このプラグイン用の辞書アクションファイルのURLを返す。
     * アクションは所定のXMLで記述されたファイル。デフォルトではnullを返す。
     * 必要に応じてオーバーライドする。
     */
    public DictionaryActionFile getDictionaryAction() {
        return null;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * (このメソッドはまったく不使用)
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);

    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
  
}
