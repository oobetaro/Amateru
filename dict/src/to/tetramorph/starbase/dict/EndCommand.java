/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;


/**
 * コマンドがここで終了（一区切り）したことを表すためのもので、実装されたメソッド
 * はすべてUnsupportedOperationExceptionを出す。
 * @author 大澤義鷹
 */
class EndCommand implements ShellCommand {
    
    public EndCommand() {

    }

    @Override
    public void redo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void undo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
