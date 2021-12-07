/*
 * DataExplorer.java
 *
 * Created on 2007/12/15, 18:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.util.List;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.Natal;

/**
 * DatabasePanelに別名を与えるためのインターフェイス。DatabasePanelはExplorer風
 * にデータをツリーとテーブルで、出生データを視覚的に操作・編集する機能をもって
 * いるが、これを作り替えるとかDBを別の製品に変更するなどするケースが考えられ、
 * その時には別クラス名で新たに作ることになる。そのときこのインターフェイスを
 * implementすればよい。DatabasePanelを使用するクラスは、このインターフェイス
 * を通じてアクセスするようにしておく。
 * @author 大澤義鷹
 */
interface DataExplorer {
    /**
     * ダイアログを開いてコピー先フォルダを取得し、Natalのリストをそこにコピーする。
     * SearchResultFrameから検索結果の保存で使用する。
     */
    public void copyToFolder(List<Natal> list);
    /**
     * 文字列表現のパス名からJTree内で正しく機能するTreePathを作成して返す。
     * setSelectionPath(treePath)として、コードからフォルダをセレクトしても、
     * フォルダが開かない事がある。その理由はTreePathはTreeNode[]から作成するが、
     * JTreeに表示させるためにsetModelで与えたTreeNodeで作成されたものでなければ
     * ならず、名前だけ同じにして適当にこしらえたTreeNode[]からTreePathを作っても
     * 正しく機能しない。文字列で表現されたパス名から、TreePathを作るときに、
     * JTree内のTreeNodeを配列に入れて、それを元にTreePathを作らなければならない。
     */
    public TreePath foundTreePath(String path);
    /**
     * 現在選択されているカレントフォルダーへのパスを返す。
     * @return フォルダへのパス
     */
    public TreePath getCurrentFolder();
    /**
     * 指定IDのNatalデータを返す。存在しないときはnullを返す。
     */
    public Natal getNatal(int id);
    /**
     * 現在選択されているカレントフォルダーの親のパスを返す。
     * @return フォルダへのパス
     */
    public TreePath getParentFolder();
    /** 
     * 検索結果のコンポーネントを返す。
     */
    public Component getSearchResultPanel();
    /**
     * TreeModelをセットする。
     */
    public void setTreeModel(TreeModel model);
    /**
     * 選択中のフォルダ内にあるサブフォルダのリストを返す。
     * @return フォルダへのパス
     */
    public List<TreePath> getSubFolders();
    /**
     * 検索パネル(SearchPanel)が表示されている状態ならtrueを返す。
     */
    public boolean isSearchFrameShowing();
    /**
     * イベントデータ入力ダイアログを開きDBに新規登録。
     */
    public void registEvent();
    /**
     * ネイタルデータ入力ダイアログを開きDBに新規登録。
     */
    public void registNatal();
    /**
     * NatalをDBに登録する。occのgetId()が1以下なら(ID未登録なら)
     * 新規挿入で挿入時にはツリーのダイアログが開き、保存先フォルダを選択させられる。
     * IDが与えられている場合は更新保存とする。ChartInternalFrameから呼びだし。
     * 引数にnullを与えるとNullPointerException。
     * 新規に挿入された場合は、引数のoccにDB上のidが付与される。
     */
    public void registNatal(Natal occ);
    /**
     * 指定されたフォルダを開きテーブルにはフォルダの中のデータを表示。
     */
    public void selectFolder( TreePath path );
    /**
     * テーブル中のデータでoccに一致するものを選択する。
     */
    public void selectTable( Natal occ );
    /**
     * 引数で与えられたNatalが登録されているフォルダを開き、該当するデータを
     * セレクトする。テーブル側のデータもセレクトされる。
     */
    public void selectTree( Natal occ );
    /**
     * 階層構造上に編み上げたノードオブジェクトをツリーにセットする。
     * @param rootNode 階層を表現するノードオブジェクト
     */
    public void setNode( FolderTreeNode rootNode );
    /**
     * フォルダーを選択するダイアログを開き選択されたパスを返す。選択が中止された
     * ときはnullを返す。
     */
    public TreePath showFolderSelectDialog( String message );
    /**
     * 指定されたフォルダの内容を表示する。
     * @param currentTreePath 表示するパス
     */
    public void showList( TreePath currentTreePath );
    /**
     * 検索フレームと検索結果フレーム両方を開く。
     */
    public void showSearchFrame();
    /**
     * インポートが行われたときに、ツリーの表示を更新する。データベースの出生
     * データを、大幅に書き換えた場合に、そのままだとツリーやテーブル表示に
     * 反映されない。このメソッドはTREEPATH表からTreeModelを再構築する。
     */
    public void treeUpdate();
    /**
     * Look and Feelの更新用だが、まだ中途半端
     */
    public void updateLookAndFeel();

}
