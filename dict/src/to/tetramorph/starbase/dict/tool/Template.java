/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict.tool;

import to.tetramorph.starbase.util.Sabian;

/**
 * 辞書のテンプレートを作るためのプログラム。
 * @author 大澤義鷹
 */
public class Template {
    static final String [] planets = {
        "sun","mon","mer","ven","mar","jup","sat","ura","nep","plu","nod",
        "ac","mc","dc","ic","apg","v",
        "cer","pal","jun","ves","chi","pho",
        "snd","aapg","av"
    };
    static final String [] planet_names = {
        "太陽","月","水星","金星","火星","木星","土星","天王星","海王星","冥王星",
        "ノード","AC","MC","DC","IC","リリス","バーテックス",
        "セレス","パラス","ジュノー","ベスタ","キローン","フォーラス",
        "サウスノード","アンチリリス","アンチバーテックス"
    };
    static final String [] signs = {
        "ari","tau","gem","can","leo","vir","lib","sco","sag","cap","aqu","pis"
    };
    static final String [] sign_names = {
        "牡羊座","牡牛座","双子座","蟹座","獅子座","乙女座",
        "天秤座","蠍座","射手座","山羊座","水瓶座","魚座"
    };
    static final String [] aspects = {
        "conjunction","sextile","square","trine","opposition","quincunx",
        "quintile","semi-sextile","semi-square","sesquiquadrate","bi-quintile",
        "decile"
    };
    static final String [] aspect_names = {
        "コンジャクション","セクスタイル","スクエア","トライン","オポジション",
        "クインカンクス","クインタイル","セミセクスタイル","セミスクエア",
        "セスクアドレイト","バイクインタイル","デシル"
    };
    static final String [] house_names = {
        "１室","２室","３室","４室","５室","６室",
        "７室","８室","９室","１０室","１１室","１２室",
    };
    static final String [] houses = {
        "house1","house2","house3","house4","house5","house6","house7","house8",
        "house9","house10","house11","house12"
    };
    /**
     * 星座と惑星の組合せを列挙する。
     */
    static void サインの天体() {
        System.out.println("<page folder=\"各サインの天体の意味\">");
        for ( int i=0; i < planets.length; i++ ) {
            String p = String.format("<page folder=\"%s\">", planet_names[i]);
            System.out.println(p);
            for ( int j=0; j < signs.length; j++ ) {
                //String s = String.format("%sの%s,%s,%s", sign_names[j],planet_names[i],signs[j],planets[i]);
                String s = String.format("<page action=\"SelectedBody\" title=\"%s／%s\" sign=\"%s\" planet=\"%s\"></page>", sign_names[j],planet_names[i],signs[j],planets[i]);
                System.out.println( s );
            }
            System.out.println("</page>");
        }
        System.out.println("</page>");
    }
    static void ハウスの天体() {
        System.out.println("<page folder=\"各ハウスの天体の意味\">");
        for ( int j=0; j < signs.length; j++ ) {
            String p = String.format("<page folder=\"%s\">", house_names[j]);
            System.out.println(p);
            for ( int i=0; i < planets.length; i++ ) {
                String s = String.format("<page action=\"SelectedBody\" " +
                        "title=\"%s／%s\" house=\"%s\" planet=\"%s\"></page>",
                        house_names[j],planet_names[i],houses[j],planets[i]);
                System.out.println( s );
            }
            System.out.println("</page>");
        }
        System.out.println("</page>");
    }
    static void ハウス() {
        System.out.println("<page folder=\"各ハウスの意味\">");
        for ( int i=0; i < houses.length; i++ ) {
            String s = String.format("<page action=\"SelectedBody\" title=\"%s\" house=\"%s\"></page>", house_names[i], houses[i]);
            System.out.println(s);
        }
        System.out.println("</page>");
    }

    static void サイン() {
        System.out.println("<page folder=\"各サインの意味\">");
        for ( int i=0; i < signs.length; i++ ) {
            String s = String.format("<page action=\"SelectedBody\" title=\"%s\" sign=\"%s\"></page>", sign_names[i], signs[i]);
            System.out.println(s);
        }
        System.out.println("</page>");
    }

    static void 天体() {
        System.out.println("<page folder=\"各天体の意味\">");
        for ( int i=0; i < planets.length; i++ ) {
            String s = String.format("<page action=\"SelectedBody\" title=\"%s\" planet=\"%s\"></page>", planet_names[i], planets[i]);
            System.out.println(s);
        }
        System.out.println("</page>");
    }
    static void アスペクト() {
        System.out.println("<page folder=\"アスペクトの意味\">");
        for ( int i=0; i < aspects.length; i++ ) {
            String s = String.format("<page action=\"SelectedAspect\" title=\"%s\" aspect=\"%s\"></page>", aspect_names[i], aspects[i]);
            System.out.println(s);
        }
        System.out.println("</page>");
    }
    static void 天体のアスペクト() {
        System.out.println("<page folder=\"各天体のアスペクトの意味\">");
        int p = 1;
        for ( int i=0; i < planets.length-1; i++ ) {
            String subj = String.format("<page folder=\"%s\">", planet_names[i]);
            System.out.println(subj);
            for ( int j=p; j < planets.length; j++ ) {
                String title = planet_names[i] +"／"+planet_names[j];
                String s=String.format("<page action=\"SelectedAspect\" " +
                        "title=\"%s\" planet=\"%s\" planet2=\"%s\"></page>",
                        title,planets[i],planets[j]);
                System.out.println(s);
            }
            p++;
            System.out.println("</page>");
        }
        System.out.println("</page>");
    }
    static void sabian() {
        System.out.println("<page folder=\"サビアンの意味\">");
        for ( int sign=0; sign < 12; sign++ ) {
            System.out.println("<page folder=\"" + sign_names[sign] + "\">");
            for ( int angle = 0; angle < 30; angle++) {
                String title = String.format("%s %d度", sign_names[sign], angle);
                String s = String.format("<page action=\"SelectedBody\" title=\"%s\" sign=\"%s\" angle=\"%d\">「%s」</page>",
                        title,signs[sign],angle,Sabian.getText(sign, angle, Sabian.JP));
                System.out.println( s );
            }
            System.out.println("</page>");
        }
        System.out.println("</page>");

    }
    public static void exec1() {
        天体();
        サイン();
        サインの天体();
        ハウス();
        ハウスの天体();
        アスペクト();
        天体のアスペクト();
    }
    public static void main(String [] args) {
        exec1();
        //sabian();
    }
}
