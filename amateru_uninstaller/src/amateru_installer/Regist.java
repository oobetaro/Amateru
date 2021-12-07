package amateru_installer;

/*
 * Regist.java
 * Created on 2011/08/10, 19:52:33.
 */


import java.util.prefs.Preferences;

/**
 * インストール情報をレジストリに保管したり、インストールしようとしているユーザ
 * が管理者権限をもっているかどうかをチェックするメソッド群。
 * このクラスはアンインストーラにも必要になるが、このクラスをアンインストーラの
 * プロジェクトにコピーするとき、パッケージ名やクラス名を変更してはならない。
 * 共通のノードにアクセスできなくなってしまう。
 * @author ohsawa
 */
public class Regist {
    private Regist() {
    }

    public static Preferences sys = Preferences.systemNodeForPackage(Regist.class);
    /**
     * 管理者権限ではないアカウントで、このメソッドを実行するとfalseを返す。
     * レジストリのシステムノードにテスト書き込みを行い、書き込みできるか、
     * エラーになるかで判定している。
     * テストで書き込むキーは"AMATERUインストール権限保持者"、値は"YES"を
     * 書きこむ。書き込んだあとはキーと値を削除する。
     * 書き込みでエラーになったときは、書きこまれないわけだから削除はしない。
     * @return
     */
    public static boolean isAdminUser() {
        String TEST_KEY = "AMATERUインストール権限保持者";
        boolean isAdmin = true;
        try {
            sys.put(TEST_KEY, "YES");
            sys.remove(TEST_KEY);
        } catch ( Exception e ) {
            isAdmin = false;
        }
        return isAdmin;
    }
}
