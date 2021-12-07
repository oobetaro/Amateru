/*
 * Preference.java
 *
 * Created on 2006/09/17, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TimeZone;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Place;

/**
 * Propertiesを拡張したクラスで、ColorやAspectTypeなどを文字列表現でset/get、
 * ファイルにもload/storeできる。
 * DBへの登録はPropertiesクラスで行っているので、それをcopyすることで
 * Preferenceオブジェクトを作成する。
 * <pre>
 * Properties p = db.getProperties("Default");
 * Preference pref = new Preference();
 * pref.copy(p);
 * </pre>
 * @author 大澤義鷹
 */
public class Preference extends Properties {
    private static final String SEPARATOR = "┃";

    /**
     * 空のプレファランスを作成
     */
    public Preference() {
        super();
    }
    /**
     * デフォルトのプレファランスをもつオブジェクトを作成
     */
    public Preference(Properties prop) {
        super(prop);
    }
    /**
     * Integerをセットする。
     * @param key Integerの名前
     * @param value Integerの値
     */
    public void setInteger(String key,Integer value) {
        setProperty(key,value.toString());
    }
    /**
     * keyに対応するIntegerを取得する。
     */
    public Integer getInteger(String key) {
        if(getProperty(key) == null) return null;
        return new Integer(getProperty(key));
    }
    /**
     * keyに対応するIntegerを取得する。keyが存在しなければdefValueを返す。
     */
    public Integer getInteger(String key,Integer defValue) {
        Integer i = getInteger(key);
        return ( i == null ) ? defValue : i;
    }
    /**
     * int配列を文字列に変換してカンマで区切ってプロパティにセット
     * @param key プロパティキー
     * @param array 登録するint配列
     */
    public void setIntArray(String key,int [] array) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<array.length; i++)
            sb.append("").append(array[i]).append(",");
        sb.deleteCharAt( sb.length() - 1 );
        setProperty( key,sb.toString() );
    }
    /**
     * カンマで区切られた文字配列を分解してint配列にして返す。
     * @param key プロパティキー
     */
    public int [] getIntArray(String key) {
        if ( getProperty( key ) == null ) return null;
        String [] strval = getProperty( key ).split( "," );
        int [] result = new int[strval.length];
        for( int i = 0; i < strval.length; i++ )
            result[i] = Integer.parseInt( strval[i] );
        return result;
    }
    /**
     * カンマで区切られた文字配列を分解してint配列にして返す。
     * @param key プロパティキー
     * @param defArray キーが存在しない場合に返すデフォルトのint配列。
     */
    public int [] getIntArray(String key, int [] defArray) {
        if(getProperty(key) == null) return defArray;
        return getIntArray(key);
    }
    /**
     * Booleanをセットする。
     */
    public void setBoolean(String key,boolean value) {
        setProperty(key, "" + value );
    }
    /**
     * keyに対応するBooleanを取得する。
     * キーが存在しないならnullを返す。
     */
    public Boolean getBoolean( String key ) {
        String b = getProperty(key);
        return ( b == null) ?
                null : Boolean.valueOf(b);
    }
    /**
     * keyに対応するBooleanを取得するが、keyが存在しないならdefの値を返す。
     */
    public Boolean getBoolean( String key, boolean def ) {
        Boolean b = getBoolean(key);
        return ( b == null ) ? def : b;
    }
    /**
     * Doubleをセットする。
     */
    public void setDouble(String key,Double value) {
        setProperty(key,value.toString());
    }
    /**
     * keyに対応するDoubleを取得する。
     */
    public Double getDouble(String key) {
        if(getProperty(key) == null) return null;
        return new Double(getProperty(key));
    }

    /**
     * Longをセットする。
     * @param key Integerの名前
     * @param value Integerの値
     */
    public void setLong( String key, Long value) {
        setProperty( key, value.toString() );
    }
    /**
     * keyに対応するLongを取得する。
     */
    public Long getLong( String key ) {
        if ( getProperty( key ) == null ) return null;
        return new Long( getProperty( key ) );
    }
    /**
     * keyに対応するLongを取得する。keyが存在しなければdefValueを返す。
     */
    public Long getLong( String key, Long defValue ) {
        Long i = getLong( key );
        return ( i == null ) ? defValue : i;
    }


    /**
     * Rectangleをセットする。
     */
    public void setRectangle(String key,Rectangle value) {
        setProperty( key,
                String.format( "%d,%d,%d,%d",
                value.x, value.y, value.width, value.height) );
    }


    /**
     * keyに対応するRectangleを取得する。
     */
    public Rectangle getRectangle( String key ) {
        String value = getProperty( key );
        if ( value == null ) return null;
        int [] v = toInt(value.split(","));
        return new Rectangle( v[0], v[1], v[2], v[3] );
    }

    /**
     * String配列をセットする。セパレータに罫線のコード"┃"を使用しているので、
     * 文字列中にこの文字は混入してはいけない。
     */
    public void setStringArray( String key, String [] values ) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < values.length; i++ )
            sb.append(values[i]).append( SEPARATOR );
        sb.deleteCharAt( sb.length() - 1 );
        setProperty( key, sb.toString() );
    }

    /**
     * keyに対応するString配列を取得する。
     */
    public String [] getStringArray(String key) {
        return getProperty( key ).split( SEPARATOR );
    }

    /**
     * Colorをプロパティにセット
     */
    public void setColor( String key, Color c ) {
        setProperty( key, String.format("%d,%d,%d,%d",
                c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() ) );
    }
    /**
     * プロパティからカラーを取得
     */
    public Color getColor(String key) {
        String color = getProperty(key);
        if ( color == null ) return null;
        String [] array = color.split(",");
        int [] c = new int[4];
        for ( int i = 0; i < 4; i++ ) c[i] = Integer.parseInt(array[i]);
        return new Color( c[0], c[1], c[2], c[3] );
    }
    /**
     * プロパティからカラーを取得。取得できないときはdefaultColorを返す。
     */
    public Color getColor(String key,Color defaultColor) {
        Color color = getColor(key);
        return ( color == null ) ? defaultColor : color;
    }
    /**
     * Color配列をプロパティとして保存する。色情報はR,G,B,Aに分解され十進数として
     * カンマで区切られた形式で保管される。かならず4チャンネル分保管される。
     * なお配列の中にnullの要素がある場合、それは"-1,-1,-1,-1,"として表現され、
     * getColors()では、nullに復元される。
     */
    public void setColors(String key,Color [] c) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<c.length; i++) {
            if ( c[i] == null ) {
                sb.append("-1,-1,-1,-1,");
            } else {
                sb.append( String.format( "%d,%d,%d,%d,",
                        c[i].getRed(),
                        c[i].getGreen(),
                        c[i].getBlue(),
                        c[i].getAlpha() ) );
            }
        }
        sb.deleteCharAt( sb.length() - 1 );
        setProperty( key, sb.toString() );
    }

    /**
     * プロパティキーに対応する複数のカラーを返す。
     * @param key "255,128,64,255, ....."というように、1色につき4つの数値(r,g,b,a)
     * によって構成される文字列を指定する。カンマで分割したとき要素数は4で割り切れ
     * なければならない。割り切れないときはIllegalArgumentExceptionが出る。
     * また数値に変換できない文字列が入った場合、一つの数値が256以上の場合も同様。
     * keyにnullまたは""が与えられたときはnullを返す。
     */
    public Color [] getColors( String key ) {
        String value = getProperty(key);
        if( value == null || value.length() == 0 ) return null;
        String [] array = value.split(",");

        if ( (array.length % 4) != 0 )
            throw new IllegalArgumentException("Illegal format : " + key);

        int [] values = new int[ array.length ];
        Color [] colors = new Color[ array.length / 4 ];

        try {
            for( int i=0; i < array.length; i++ )
                values[i] = Integer.parseInt( array[i] );
        } catch( NumberFormatException e ) {
            throw new IllegalArgumentException("Illegal format : " + key);
        }

        for( int i=0,j=0; i<values.length; i += 4) {
            if(values[i] < 0)
                colors[ j++ ] = null;
            else if((values[i] >= 256))
                throw new IllegalArgumentException("Illegal format : " + key);
            else
                colors[ j++ ] = new Color(
                    values[ i ], values[ i + 1 ], values[ i + 2 ], values[ i + 3 ] );
        }
        return colors;
    }

    /**
     * プロパティキーに対応する複数のColorを返す。
     * 取得できなかったときは、defaultColorsを返す。
     */
    public Color [] getColors( String key, Color [] defaultColors ) {
        Color [] c = getColors( key );
        return ( c == null ) ? defaultColors : c;
    }

    /**
     * AspectType配列をセットする。
     */
    public void setAspectTypes( String key, AspectType [] values ) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < values.length; i++) {
            sb.append( values[i].aid ).append( "," );
            sb.append( values[i].tightOrb ).append( "," );
            sb.append( values[i].looseOrb ).append( "," );
        }
        sb.deleteCharAt( sb.length() - 1 );
        setProperty( key,sb.toString() );
    }

    /**
     * keyに対応するAspectType配列を取得する。
     */
    public AspectType[] getAspectTypes(String key) {
        String [] values = getProperty(key).split(",");
        AspectType [] types = new AspectType[ values.length / 3 ];
        for(int i=0; i<types.length; i++) {
            types[i] = new AspectType(
                Integer.parseInt(values[ i * 3 ]),
                Double.parseDouble(values[ i * 3 + 1 ]),
                Double.parseDouble(values[ i * 3 + 2 ]));
        }
        return types;
    }

    /**
     * 名前をつけてPlaceオブジェクトを保管する
     */
    public void setPlace(String key,Place place) {
        StringBuilder sb = new StringBuilder();
        sb.append( place.getPlaceName()).append( SEPARATOR );
        sb.append( place.getLatitude().toString() ).append( SEPARATOR );
        sb.append( place.getLongitude().toString() ).append( SEPARATOR );
        sb.append( place.getTimeZone().getID() );
        setProperty( key,sb.toString() );
    }

    /**
     * キーに対応するPlaceオブジェクトを返す。
     */
    public Place getPlace(String key) {
        if ( getProperty(key) == null ) return null;
        String [] value = getProperty(key).split( SEPARATOR );
        return new Place( value[0],
                           new Double( value[1] ),
                           new Double( value[2] ),
                           TimeZone.getTimeZone( value[3] ) );
    }

    /**
     * キーに対応するPlaceオブジェクトを返す。キーが無いときはdefPlaceを返す。
     */
    public Place getPlace( String key, Place defPlace ) {
        Place p = getPlace( key );
        return ( p == null ) ? defPlace : p;
    }

    /**
     * フォントを記憶する。floatのサイズは保管できない。Font#getSize()で取得した
     * 値を保管する。
     * @param key
     * @param font
     */
    public void setFont( String key, Font font ) {
        setProperty( key,
                      font.getFamily() + ","
                    + font.getStyle() + ","
                    + font.getSize());
    }

    /**
     * フォントを返す。
     * @param key
     * @param defFont keyがみつからないときのデフォルトフォント。
     * @return フォント
     */
    public Font getFont( String key, Font defFont ) {
        if ( getProperty( key,"").isEmpty() ) return defFont;
        String [] v = getProperty( key ).split(",");
        return new Font( v[0],
                         Integer.parseInt(v[1]),
                         Integer.parseInt(v[2])   );
    }
    public Font getFont( String key ) {
        return getFont( key, null );
    }
    /**
     * propをこのオブジェクトにコピーする。
     */
    public void copy( Properties prop ) {
        for ( Enumeration enu = prop.keys(); enu.hasMoreElements(); ) {
            String key = (String)enu.nextElement();
            String value = prop.getProperty( key );
            setProperty( key, value );
        }
    }

    //整数の数字が入っている文字列配列をint配列に変換。
    private static int [] toInt(String [] v) {
        int [] tmp = new int[ v.length ];
        for ( int i = 0; i < tmp.length; i++ ) {
            tmp[i] = Integer.parseInt( v[i] );
        }
        return tmp;
    }
    /**
     * Preferenceのディープコピーによる複製を返す。
     * @param prop コピー元のPreference
     * @return 複製されたPreference
     */
    public static Preference getNewPreference( Preference prop ) {
        Preference res = new Preference();
        for ( Enumeration enu = prop.keys(); enu.hasMoreElements(); ) {
            String key = (String)enu.nextElement();
            String value = prop.getProperty( key );
            res.setProperty( key, value );
        }
        return res;
    }
}
