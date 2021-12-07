/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

/**
 * TreeShellで実行するツリー編集用のコマンドクラスを作成するときに実装する
 * インターフェイス。
 * @author 大澤義鷹
 */
interface ShellCommand {
    void redo();
    void undo();
}
