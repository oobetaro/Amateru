@if (1==1) /*
@ECHO OFF

REM       �A�}�e���E�A���C���X�g�[���N���p�o�b�`�X�N���v�g
REM       ���̃t�@�B����Microsoft Windows��p
REM       2011-08-16 ���V�`�F

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

	/* Vista�ȍ~��6�A�܂�WinXP��Win2K�Ȃ炱�̏���
       (UAC�𐧌䂵�ĊǗ��҃��[�h�ɏ��i)�͍s��Ȃ� */

    if ( WScript.Arguments.length == 0 ) {

        /* ���ڂɂ��̃X�N���v�g���Ăт��ꂽ�Ƃ���
           ����������̂ł���if���̒��ɂ͓���Ȃ� */

        var sh = WScript.CreateObject("Shell.Application");

        /* ������x���̃X�N���v�g�����s����B
           �X�N���v�g�̋N����//E�I�v�V����������Ɗg���q��bat�ł��ʂ�  */

        sh.ShellExecute( "wscript.exe", 
                         "//E:JScript \"" + WScript.ScriptFullName + "\" \""
                       + Wsh.CurrentDirectory + "\"", 
                         "", "runas", 1 ); 

		/* �Ăяo������A�X�N���v�g�͂����ŏI�� */
        WScript.Quit(0); 
    }
}

/*
 * �X�N���v�g�̂���t�H���_�ɂ���amateru_uninsaller.jar���N������
 */

var path = WScript.ScriptFullName;
path = path.substring( 0, path.lastIndexOf("\\") ); 
Wsh.Run( "javaw.exe -jar \"" + path + "\\amateru_uninstaller.jar\"", 1, false );
