@if (1==1) /*
@ECHO OFF

REM       アマテル・アンインストーラ起動用バッチスクリプト
REM       このファィルはMicrosoft Windows専用
REM       2011-08-16 大澤義孝

CScript //Nologo //E:JScript "%~f0" %*
GOTO :EOF
rem */
@end
var Wsh = WScript.CreateObject("WScript.Shell"); 
var SystemFolder = 1; 
var Fs = WScript.CreateObject("Scripting.FileSystemObject"); 

var kernel = Fs.BuildPath( Fs.GetSpecialfolder(SystemFolder).Path,
                           "kernel32.dll" );
var os_version = parseInt( Fs.GetFileVersion( kernel ) );

if ( os_version >= 6 ) {

	/* Vista以降は6、つまりWinXPやWin2Kならこの処理
       (UACを制御して管理者モードに昇格)は行わない */

    if ( WScript.Arguments.length == 0 ) {

        /* 二回目にこのスクリプトが呼びされたときは
           引数があるのでこのif文の中には入らない */

        var sh = WScript.CreateObject("Shell.Application");

        /* もう一度このスクリプトを実行する。
           スクリプトの起動で//Eオプションをつけると拡張子がbatでも通る  */

        sh.ShellExecute( "wscript.exe", 
                         "//E:JScript \"" + WScript.ScriptFullName + "\" \""
                       + Wsh.CurrentDirectory + "\"", 
                         "", "runas", 1 ); 

		/* 呼び出した後、スクリプトはここで終了 */
        WScript.Quit(0); 
    }
}

/*
 * スクリプトのあるフォルダにあるamateru_uninsaller.jarを起動する
 */

var path = WScript.ScriptFullName;
path = path.substring( 0, path.lastIndexOf("\\") ); 
Wsh.Run( "javaw.exe -jar \"" + path + "\\amateru_uninstaller.jar\"", 1, false );
