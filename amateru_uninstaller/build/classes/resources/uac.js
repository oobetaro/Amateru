/*
 * 管理者権限でJScriptからJavaのプログラムを起動する。
 * このスクリプトはWinShortcut#greadup()内で読み込まれ実行される。
 * このソース内には記述されていないが、greadup()内でこのスクリプトが
 * テンポラリファイルにコピーされるとき、スクリプトの末尾にjavaw.exe
 * を呼び出すステートメントが一行追加される。
 */
var Wsh = WScript.CreateObject("WScript.Shell"); 
if ( WScript.Arguments.length == 0 ) {

    /* 二回目にこのスクリプトが呼びされたときは
       引数があるのでこのif文の中には入らない */

    var sh = WScript.CreateObject("Shell.Application");

    /* 引数にカレントフォルダを追加してもう一度このスクリプトを実行する。
       なぜ追加するかというと、昇格したときカレントフォルダの位置が変わ
       ってしまうので元の位置を再実行のスクリプトに伝達するため。
       スクリプトの起動で//Eオプションをつけると拡張子がjsでなくても通る。
       "runas"オプションが管理者権限に昇格する呪文 */

    sh.ShellExecute( "wscript.exe", 
                     "//E:JScript \"" + WScript.ScriptFullName + "\" \""
                   + Wsh.CurrentDirectory + "\"", 
                     "", "runas", 1 ); 

	/* 呼び出した後、スクリプトはここで終了 */
    WScript.Quit(0); 
}
//Wsh.Run( "javaw.exe -cp \"" + classPath + "\" amateru_installer.Main2",1, false );