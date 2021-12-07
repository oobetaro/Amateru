/*
 * ChartConfig.java
 *
 * Created on 2007/07/12, 1:20
 *
 */

package to.tetramorph.starbase.util;

import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.util.Preference;

/**
 * チャート計算設定オブジェクトを作成する。
 * @author 大澤義鷹
 */
public class ChartConfig {
    private boolean isUseMeanApogee = false; //ﾐｰﾝｱﾎﾟｼﾞｰを使用するときはtrue
    private boolean isUseMeanNode = false; //ﾐｰﾝﾉｰﾄﾞを使用するときはtrue
    private char cuspUnknownHouseSystem = '1'; //ｶｽﾌﾟ不明時のﾊｳｽ分割法(1 or 2)
    private char houseSystemCode = 'P'; //使用するﾊｳｽ分割法
    private boolean prioritizeSolar = false;
    
    public String toString() {
        return "isUseMeanNode="            + isUseMeanNode 
              + ", isUseMeanApogee="        + isUseMeanApogee 
              + ", cuspUnknownHouseSystem=" + cuspUnknownHouseSystem
              + " ,houseSystemCode="        + houseSystemCode
              + " ,PrioritizeSolar="        + prioritizeSolar
              + " ,DefaultTransitPlace="    + place.toString();
    }
    
    /**  
     * ChartConfig オブジェクトを作成する 
     */
    public ChartConfig() {
    }
    
    /**
     * ミーンアポジーを使用する場合はtrueをセット。
     * falseを指定するとオスカレーションアポジー(密接アポジー)が使用される。
     * デフォルトはfalse。
     */
    public void setUseMeanApogee( boolean b ) {
        this.isUseMeanApogee = b;
    }
    
    /**
     * ミーンアポジーが設定されている場合はtrueを返す。
     */
    public boolean isUseMeanApogee() {
        return isUseMeanApogee;
    }
    
    /**
     * ミーンノードを使用する場合はtrueをセット。falseを指定するとトルーノードが
     * 使用される。デフォルトはfalse。
     */
    public void setUseMeanNode( boolean b ) {
        this.isUseMeanNode = b;
    }
    
    /**
     * ミーンノード使用が設定されている場合はtrueを返す。
     */
    public boolean isUseMeanNode() {
        return isUseMeanNode;
    }
    
    /**
     * カスプが求められないとき(場所が不明のとき。時刻未設定はデフォルト時刻が
     * 使用される)のハウス分割法を指定。1ならソーラー、2ならソーラーサイン。
     * それ以外のものはIllegalArgumentException()
     */
    public void setCuspUnkownHouseSystem( char code ) {
        if(code >= '1' && code <= '2') cuspUnknownHouseSystem = code;
        else throw 
            new IllegalArgumentException("サポートされていないハウス分割法");
    }
    
    /**
     * カスプが計算できないときのハウス分割法を返す。"1"か"2"どちらかが返る。
     */
    public char getCuspUnknownHouseSystem() {
        return cuspUnknownHouseSystem;
    }
    /**
     * 「ソーラーまたはソーラーサインを使用する」を選択する場合は"true"
     * 「デフォルトの地方時と観測地とハウス分割法で計算する。」場合は"false"
     * をセットする。
     */
    public void setPrioritizeSolar( boolean b ) {
        prioritizeSolar = b;
    }
    
    /**
     * 「ソーラーまたはソーラーサインを使用する」場合はtrueを返す。
     * 「デフォルトの地方時と観測地とハウス分割法で計算する。」場合はfalseを返す。
     *
     */
    public boolean isPrioritizeSolar() {
        return prioritizeSolar;
    }
    /**
     * ハウス分割法を指定する。指定できるコードは、'P','K','O','R','C','E','1','2'
     * のいずれか。デフォルトは(int)'P'でプラシーダス。
     */
    public void setHouseSystemCode( char code ) {
        houseSystemCode = code;
    }
    
    /**
     * ハウス分割法を返す。
     */
    public char getHouseSystemCode() {
        return houseSystemCode;
    }
    
    private Place place = TestConst.getImperialPalaceOfJapan();
    /**
     * デフォルトの観測地を返す。まったく未設定の状態では皇居の位置がデフォルト。
     */
    public Place getDefaultPlace() {
        return place;
    }
    public void setDefaultPlace( Place place ) {
        this.place = place; 
    }
    /**
     * システムのプロパティに従ってこのオブジェクトに値を設定する。
     * "UseMeanApogee","UseMeanNode","CuspUnknownHouseSystem","HouseSystemIndex"
     * "PrioritizeSolar"のキーを認識する。
     * HouseSystemIndexはHouseSystemCodeに変換して解釈される。
     */
    public void setPreference( Preference pref ) {
        isUseMeanApogee = pref.getBoolean( "UseMeanApogee", false );
        isUseMeanApogee = pref.getBoolean( "UseMeanNode", false );
        cuspUnknownHouseSystem = 
            pref.getProperty( "CuspUnknownHouseSystem", "1" ).charAt( 0 );
        houseSystemCode = pref.getProperty( "HouseSystemCode", "P" ).charAt( 0 );
        prioritizeSolar = pref.getBoolean( "PrioritizeSolar", false );
        place = pref.getPlace( "DefaultTransitPlace", TestConst.getGreenwitchPlace() );
    }
    
    /**
     * このオブジェクトの設定内容を指定されたprefに書きこむ。
     * "UseMeanApogee","UseMeanNode","CuspUnknownHouseSystem","HouseSystemIndex"
     * "PrioritizeSolar"のキーが書きこまれる。
     * HouseSystemCodeはHouseSystemIndexに変換して書き出される。
     * 戻り値はprefのインスタンスと同じ参照を返す。
     */
    public Preference getPreference(Preference pref) {
        pref.setBoolean( "UseMeanApogee", isUseMeanApogee );
        pref.setBoolean( "UseMeanNode", isUseMeanNode );
        pref.setProperty( "CuspUnknownHouseSystem", "" + cuspUnknownHouseSystem );
        pref.setProperty( "HouseSystemCode","" + houseSystemCode );
        pref.setProperty( "PrioritizeSolar", "" + prioritizeSolar );
        pref.setPlace( "DefaultTransitPlace", place );
        return pref;
    }
}
