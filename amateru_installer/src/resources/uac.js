/*
 * �Ǘ��Ҍ�����JScript����Java�̃v���O�������N������B
 * ���̃X�N���v�g��WinShortcut#greadup()���œǂݍ��܂���s�����B
 * ���̃\�[�X���ɂ͋L�q����Ă��Ȃ����Agreadup()���ł��̃X�N���v�g��
 * �e���|�����t�@�C���ɃR�s�[�����Ƃ��A�X�N���v�g�̖�����javaw.exe
 * ���Ăяo���X�e�[�g�����g����s�ǉ������B
 */
var Wsh = WScript.CreateObject("WScript.Shell"); 
if ( WScript.Arguments.length == 0 ) {

    /* ���ڂɂ��̃X�N���v�g���Ăт��ꂽ�Ƃ���
       ����������̂ł���if���̒��ɂ͓���Ȃ� */

    var sh = WScript.CreateObject("Shell.Application");

    /* �����ɃJ�����g�t�H���_��ǉ����Ă�����x���̃X�N���v�g�����s����B
       �Ȃ��ǉ����邩�Ƃ����ƁA���i�����Ƃ��J�����g�t�H���_�̈ʒu���ς�
       ���Ă��܂��̂Ō��̈ʒu���Ď��s�̃X�N���v�g�ɓ`�B���邽�߁B
       �X�N���v�g�̋N����//E�I�v�V����������Ɗg���q��bat�ł��ʂ�  
       "runas"�I�v�V�������Ǘ��Ҍ����ɏ��i������� */

    sh.ShellExecute( "wscript.exe", 
                     "//E:JScript \"" + WScript.ScriptFullName + "\" \""

                   + Wsh.CurrentDirectory + "\"", 
                     "", "runas", 1 ); 

	/* �Ăяo������A�X�N���v�g�͂����ŏI�� */
    WScript.Quit(0); 
}

/* WinShortcut#greadup()���Ō�Ɉ�s��ǉ����錩�{���R�����g�Ƃ��Ďc���Ă���B
 * -jar�ŋN������̂ł͂Ȃ��A�ʂ̃G���g���[���N���X���Ŏw�肷��B
 * classPath�͂��̃\�[�X���ɂ͐錾����Ă��Ȃ��BJava�����̃\�[�X���t�@�C����
 * �W�J����Ƃ��ɐ擪�ɒǉ�����B
 */
//Wsh.Run( "javaw.exe -cp \"" + classPath + "\" amateru_installer.Main2",1, false );