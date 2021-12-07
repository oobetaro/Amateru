/*
 * ProgressListener.java
 * Created on 2011/07/20, 23:50:13.
 */
package amateru_installer;

/**
 * インストール状況をCopyクラスから受け取るリスナが実装するインターフェイス。
 * @author ohsawa
 */
interface ProgressListener {
    public void addProgress( final double value );
    public void print( final String head, final String name );
    public int getBarMaxValue();
    public void showErrorDialog( final String msg );
    public void installEnd();
}
