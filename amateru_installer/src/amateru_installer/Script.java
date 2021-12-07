/*
 * Script.java
 * Created on 2011/07/23, 19:59:18.
 */
package amateru_installer;

import java.io.PrintWriter;

/**
 * WinShortcut#execute()にこのインターフェイスを実装したオブジェクトを渡す。
 * executeはこのインターフェイスを実行する。
 * Script#write()の中身は、JScriptを記述する。
 * 記述された内容でexecute()はファイルを作り、Runtime#exec()をつかって、そのファイル
 * を実行し(つまりJScriptを実行し)結果を受け取り、execute()の呼び出し元に値を返す。
 * @author ohsawa
 */
public interface Script {
    /**
     * 実行するJScriptをPrintWriterで書きだす
     */
    public void write( PrintWriter w );
}
