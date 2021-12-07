/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

/**
 * シェルがコマンドを実行したタイミングでこのリスナが呼び出される。
 * これはメニューやボタンのEnabled/Disenabledを切り替えるために用意されていて、
 * 他の用途は一切考慮してない。
 *
 * @author 大澤義鷹
 */
interface ShellListener {
    /**
     * execute,undo,redoが行われた時に呼び出される。
     */
    void commandExecuted( TreeShell shell );
}
