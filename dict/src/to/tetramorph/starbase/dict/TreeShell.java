/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.ArrayList;
import java.util.List;

/**
 * ツリーの削除・挿入・リネームをShellCommandオブジェクトにしたがって実行する。
 * また要求に応じてアンドゥ、リドゥを行う。
 * @see RenameCommand,InsertCommand,DeleteCommand,EndCommand
 * @author 大澤義鷹
 */
class TreeShell {
    private static final EndCommand EOC = new EndCommand();
    private List<ShellCommand> history = new ArrayList<ShellCommand>();
    private int pos = -1;
    /**
     * ツリーシェルを作成する。
     */
    public TreeShell() {
        end();
    }

    // posより後ろの要素をbufferから捨てる。
    private void truncate() {
        for ( int i = history.size()-1; i > pos; i--) {
            history.remove(i);
        }
    }

    /**
     * コマンドを実行(redo())しコマンドをヒストリーに記憶する。
     * undo(),redo()によってツリーの復元を行える。
     */
    public void execute( ShellCommand cmd ) {
        truncate();
        cmd.redo();
        history.add(cmd);
        pos = history.size()-1;
    }

    /**
     * コマンド終了をシェルに通達する。アンドゥ／リドゥのときの区切りを打つため
     * のもので、execute()を繰り返し実行したあとこのメソッドで区切りを打つ。
     * undo(),redo()はこの区切り単位で、コマンドを再実行する。
     */
    public void end() {
        truncate();
        history.add( EOC );
        pos = history.size()-1;
        if ( sl != null ) sl.commandExecuted(this);
    }


    /**
     * 記憶されているコマンドを読み出し復元操作を行う。削除されていたものは復活し
     * 移動されたものは元の位置に戻る。
     */
    public void undo() {
        if ( pos == 0 ) return;
        ShellCommand cmd = history.get(pos);
        if ( cmd != EOC )
            throw new java.lang.IllegalArgumentException("EOCで閉じていない");
        while( pos-- >= 1 ) {
            cmd = history.get( pos );
            if ( cmd == EOC ) break;
            cmd.undo();
        }
        if ( sl != null ) sl.commandExecuted(this);
    }
    /**
     * まだアンドゥできるコマンドがヒストリーに残っている場合はtrueを返す。
     */
    public boolean canUndo() {
        return pos > 1;
    }

    /**
     * undo()とは反対に、保存されているコマンドを再実行する。
     */
    public void redo() {
        if ( (pos+1) == history.size() ) return;
        ShellCommand cmd = history.get(pos);
        if ( cmd != EOC )
            throw new java.lang.IllegalArgumentException("EOCで閉じていない");
        while ( pos++ >= 0 && pos < history.size()) {
            cmd = history.get(pos);
            if ( cmd == EOC ) break;
            cmd.redo();
        }
        if ( sl != null ) sl.commandExecuted(this);
    }
    /**
     * まだリドゥできるコマンドがヒストリーにのこっている場合はtrueを返す。
     */
    public boolean canRedo() {
        return ( pos < history.size() - 1 );
    }
    /**
     * コマンドのヒストリーをすべて抹消する。
     */
    public void clear() {
        history.clear();
        end();
    }
    private ShellListener sl = null;
    public void setShellListener( ShellListener l ) {
        sl = l;
    }
}
