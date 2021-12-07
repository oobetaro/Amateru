/*
 * Main.java
 * Created on 2011/07/17, 22:27:21.
 */
package amateru_installer;

/**
 * インストーラーのエントリー。
 * amateru_installerというパッケージにインストーラのプログラムが入っている。
 * distというフォルダを作りそこに配布するアプリの構成ファイルを入れる。
 * "/dist"内にアマテルの構成ファイルをコピーするのはけっこう面倒な作業なので、
 * C:\Users\ohsawa\Documents\Amateru\Packの中にPerlで書いたスクリプトを用意
 * している。
 * resourcesの中にamateru_uninstaller.jarを入れる。(中のINSTALL.LOGは空のもの)
 * ライセンス文は/resourcesの中に入れる。
 * この中のniwatori.png等のアイコンはインストーラのフレームに表示するためのもの。
 * つまり/resourcesはインストーラのリソース。/distは配布物。
 * ビルドするとamateru_installer.jarができて、これがアマテルのインストーラとなる。
 *
 * インストーラーを実行すると、まず/resourcesの中のamateru_uninstaller.jarが
 * インストール先のフォルダに配置される。続いて/distの中身が展開される。
 * 展開されるときに、どこのフォルダにコピーしたかログをとっている。
 * 最後にアンインストーラの書庫の中にログを書きだす。
 * アンインストーラはそのログをリソースとして読み出し、アンインストールの処理を行う。
 *
 * インストーラのjar書庫の中身は、自己参照によって自動取得する仕組みを用意してる。
 *
 *
 * アンインストールのとき、ログに書かれているフォルダを末節から削除していくが、
 * 内部にファイルが残っているときは削除されない。インストールのとき、すでにその
 * フォルダが存在し中にファイルが存在した場合は削除されない。しかしこれは正常な
 * 処理である。
 * @author 大澤義孝
 */
public class Main {

    /**
     * インストーラーを起動する。引数は無い。
     */
    public static void main(String[] args) throws Exception {

        if ( OSType.isWindows() && OSType.getOSVersion() >= 6F ) {
            /* Windows Vista,Windows 7以上なら昇格が必要。
               Vista以降のOSバージョンは6以上の値を取る
               greadup()のあとJScriptから別のJVMが起動しMain2.mainが呼ばれる */
            WinShortcut.greadup("/resources/uac.js", Main2.class.getName() );
        } else {
            // それ以外のOSは昇格せずにMain2を実行
            Main2.main(null);
        }
    }

}
