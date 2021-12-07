/*
 * SplitNode.java
 *
 * Created on 2007/10/26, 3:43
 *
 */

package split;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Stack;
import javax.swing.text.html.CSS;

/**
 * このノードを使って画面をデバイダで自在に分割して、その中にJPanelなどの部品を
 * 張り込むことができる。
 * @author 大澤義鷹
 */
public class SplitNode extends SplitPane {
  /** 
   * 挿入ターゲットのコンポーネントが置かれている区画の、どの位置に挿入するかを
   * 指定する定数。
   */
  public static final int INSERT_TOP = 0;
  public static final int INSERT_LEFT = 1;
  public static final int INSERT_BOTTOM = 2;
  public static final int INSERT_RIGHT = 3;
  protected SplitNode parent = null;
  /**  
   * SplitNode オブジェクトを作成する
   */
  public SplitNode()  {
    super();
    setOrientation( VERTICAL_SPLIT );
    removeAll();
  }
  public SplitNode( int orientation, Component left, Component right) {
    super( orientation, left, right );
  }
  /**
   * ノードにコンポーネントが挿入された際に、挿入されたコンポーネントのサイズを
   * 設定(挿入区画のサイズの幅/高さどちらかのサイズの約30%に設定)し、それまで
   * 挿入区画一杯にしめていたコンポーネントまたはノードのサイズを同様に70%に設定
   * し、それに合わせて挿入区画にあったデバイダの位置を30:70の位置に再設定する。
   * flagの値によっては、挿入されたノード自身に挿入区画のサイズをセットする。
   * (まぁ、この説明はとてもわかりにくいと思う。)
   * @param s     デバイダがあるノード
   * @param t     挿入ターゲットとなったコンポーネント(全体の広さ)
   * @param c     挿入されたコンポーネント
   * @param ins   挿入位置
   * @param flag  falseのときは、sのデバイダ位置を設定するだけだが、trueにすると、
   *              t.getSize()の値をsに書きこむオプション。
   */
  private static void setNewSize(SplitNode s, Component t, Component c, 
                                                    int ins,boolean flag) {
    Dimension size = t.getSize();
    if(flag)
      s.setSize(new Dimension(size));
    int w = size.width;
    int h = size.height;
    Dimension csize = new Dimension();
    Dimension tsize = new Dimension();
    int dvloc = 0;
    if(ins == INSERT_TOP || ins == INSERT_BOTTOM) {
      h -= 10;
      csize.width = w; 
      csize.height = h * 30 / 100;
      tsize.width = w;
      tsize.height = h * 70 / 100;
      dvloc = ( ins == INSERT_TOP ) ? csize.height + 5 : tsize.height - 5;
    } else {
      w -= 10;
      csize.width = w * 30 / 100;
      csize.height = h;
      tsize.width = w * 70 / 100;
      tsize.height = h;
      dvloc = ( ins == INSERT_LEFT ) ? csize.width + 5 : tsize.width - 5;
    }
    c.setSize(csize);
    t.setSize(tsize);
    s.setDividerLocation(dvloc);
  }

  /**
   * このノードにコンポーネントを追加する。
   * @param insert 挿入場所を指定する。
   * INSERT_TOP,INSERT_LEFT,INSERT__BOTTOM,INSERT_BOTTOMのいずれか
   * @param comp 挿入するコンポーネント
   * @param targetComp 挿入する標的コンポーネント。ルートノードに一つ目の
   * コンポーネントを追加するときはnullを指定する。
   */
  public void addComponent(Component comp,int insert,Component targetComp) {
    if ( comp == null) 
       throw new IllegalArgumentException( "nullは追加できません。" );
    if ( insert < INSERT_TOP || insert > INSERT_RIGHT )
       throw new IllegalArgumentException( "挿入位置定数ではありません。" );
    if ( comp instanceof SplitNode || targetComp instanceof SplitNode)
       throw new IllegalArgumentException( "SplitNodeは追加できません。" );
    if ( leftComponent instanceof SplitNode && 
          rightComponent instanceof SplitNode )
       throw new IllegalArgumentException( "このノードには追加できません。" );
    
    Component       top    = getTopComponent();
    Component       bottom = getBottomComponent();
    
    int            orientation = ((insert & 1) == 0) ? 
                    VERTICAL_SPLIT    :    HORIZONTAL_SPLIT ;
    boolean       isFront = (insert >> 1) == 0;

    if( top == null && bottom == null ) { //両方空いている場合
      setOrientation( orientation );
      if (isFront)                       // TOP | LEFT
          setTopComponent(comp);
      else                               // BOTTOM | RIGHT
          setBottomComponent(comp);
      
      hideDivider();                     //入っているのは1枚なのでデバイダを隠す
      return;
    }
    //topがnullでbottomがSplitNodeではない場合
    if (top == null && ! ( bottom instanceof SplitNode) ) {
      setOrientation( orientation );
      if ( isFront ) {
           setTopComponent(comp);
      } else {
        setBottomComponent(null);
        setTopComponent(bottom);
        setBottomComponent(comp);
      }
      showDivider();
      setNewSize(this, targetComp, comp, insert, false );
      return;
    }
    //bottomがnullでtopがSplitNodeではない場合
    if(bottom == null && ! ( top instanceof SplitNode) ) {
      setOrientation( orientation );
      if( isFront ) {
        setTopComponent(null);
        setBottomComponent( top );
        setTopComponent( comp );
      } else {
        setBottomComponent(comp);
      }
      showDivider();
      setNewSize(this, targetComp, comp, insert, false );
      return;
    }
    if(targetComp == null) 
      throw new IllegalArgumentException("targetCompがnullです");
    
    //両方がにユーザーコンポーネントが詰まっている場合。ノードを拡張
    
    SplitNode sn = null;
    int dvloc = getDividerLocation();
    
    if( top == targetComp ) {
      
      if( isFront )               // 前方挿入
        sn = new SplitNode( orientation, comp, top );
      else                       // 後方挿入
        sn = new SplitNode( orientation, top, comp );
      
      setTopComponent( null );
      setTopComponent(sn);
      setNewSize( sn, top, comp, insert, true);
      
    } else if( bottom == targetComp) {
      
      if( isFront )               // 前方挿入
        sn = new SplitNode( orientation, comp, bottom );
      else                       // 後方挿入
        sn = new SplitNode( orientation, bottom, comp );
      
      setBottomComponent( null );
      setBottomComponent(sn);
      setNewSize( sn, bottom, comp, insert, true);
    }
    setDividerLocation( dvloc );
    sn.parent = this;           //親への参照を与える
    sn.showDivider();
  }
  // ノードの子に親を教える
  private void teachParent( SplitNode node ) {
    if ( node.leftComponent instanceof SplitNode )
          ((SplitNode)node.leftComponent).parent = node;
    if ( node.rightComponent instanceof SplitNode)
          ((SplitNode)node.rightComponent).parent = node;
  }
//  /**
//   * 指定されたコンポーネントを削除する。
//   * @exception IllegalArgumentException 引数にnullを指定したとき
//   */
//  public void removeComponent(Component c) {
//    if ( c == null ) throw new IllegalArgumentException("null禁止");
//    SplitNode node = findNode(c);
//    if ( node == null ) return;
//    SplitNode par = node.parent;
//    // ルートノードでの削除
//    if( par == null) {
//      System.out.println("★削除1" + node + "の" + c +"を削除");
//
//      Component c2 = ( node.leftComponent == c) ? 
//        node.rightComponent : node.leftComponent;
//      if ( node.bothNodesAreComponent() ) {
//        //この削除どうしてもわずかに隙間があく
//        node.removeAll();
//        node.setLeftComponent(c2); //最後の1枚なのでLeft/Rightはどちらでもよい
//        node.hideDivider();
//        //node.updateUI();
//        System.out.println("              1-1");
//      } else if( c2 instanceof SplitNode ) { //下のノードを上に引き上げる
//        SplitNode sn = (SplitNode)c2;
//        int loc = sn.getDividerLocation();
//        node.removeAll();
//        node.setOrientation( sn.getOrientation() );
//        node.setLeftComponent( sn.leftComponent );
//        node.setRightComponent( sn.rightComponent );
//        node.showDivider();
//        node.setDividerLocation(loc);
//        teachParent(node);
//        System.out.println("              1-2");
//      } else {
//        if( node.leftComponent == c ) node.setLeftComponent(null);
//        else node.setRightComponent(null);
//        System.out.println("              1-3");
//      }
//      return;
//    }
//    //子ノードで両方が葉ノードの場合
//    if( node.bothNodesAreComponent() ) {
//      System.out.println("★削除2" + node + "の" + c + "を削除");
//      // 削除対象とは別のコンポーネントを取得
//      Component c2 = ( node.leftComponent == c ) ?
//                       node.rightComponent    :    node.leftComponent;
//      
////      if( node.leftComponent == c) c2 = node.rightComponent;
////      else if(node.rightComponent == c) c2 = node.leftComponent;
////      else throw new IllegalArgumentException("ありえない！");
//      System.out.println("parent = " + par);
//      Component pc = par.leftComponent == node ?
//        par.rightComponent : par.leftComponent;
//      
//      node.removeAll();
//      System.out.println("pc = " + pc);
//      int loc = par.getDividerLocation();
//      if ( par.leftComponent == node ) {
//           par.removeAll();
//           par.updateUI();
//           par.setLeftComponent( c2 );
//           par.setRightComponent( pc );
//      } else {
//           par.removeAll();
//           par.updateUI();
//           par.setLeftComponent( pc );
//           par.setRightComponent( c2 );
//      }
//      if ( par.getNodeCount() == 2)
//        par.setDividerLocation(loc);
//      return;
//    }
//    
//    if ( node.containSplitNode() ) {             //子ノードをもつ幹ノードの場合
//        System.out.println("★削除4 " + node + "の" + c + "を削除");      
//
//        SplitNode chaild = ( node.leftComponent instanceof SplitNode ) ?
//          (SplitNode) node.leftComponent   :   (SplitNode) node.rightComponent;
//
//        node.removeAll();
//        node.setOrientation( chaild.getOrientation() );
//        node.setLeftComponent( chaild.leftComponent );
//        node.setRightComponent( chaild.rightComponent );
//        node.setDividerLocation( chaild.getDividerLocation() );
//        teachParent(node);      
//    } else
//      throw new IllegalArgumentException(
//             "ありえないデータ構造。なにかバグがあります。");
//    
//  }
//  
  /**
   * 指定されたコンポーネントを削除する。ここで指定されるComponentは、
   * 非SplitNodeで、SplitNodeで編んだノードの中に登録したコンポーネントでなければ
   * ならない。
   * 
   * @param c 削除するコンポーネント。
   * 
   * @exception IllegalArgumentException 引数にnullを指定したとき
   */
  public void removeComponent(Component c) {
    if ( c == null ) throw new IllegalArgumentException("null禁止");
    SplitNode node = findNode(c);
    if ( node == null ) return;
    Component left = node.leftComponent;
    Component right = node.rightComponent;
    
    //どちらも葉である場合( nullの場合もtrueとなる事に注意! )
    if( ! (left instanceof SplitNode ) && ! ( right instanceof SplitNode )) {
      if( node.parent == null) {                  //親がないルートノードの場合
        System.out.println("★削除1 " + node + "の " + c + "を削除");      
        if ( left == c )
          node.setLeftComponent(null);
        else
          node.setRightComponent(null);
        if(node.getNodeCount() <= 1) node.hideDivider();
      } else {                                    //親がいる子ノードの場合
        //System.out.println("★削除2 " + node + "の " + c + "を削除");      
        SplitNode par = node.parent;
        Component c2 = ( node.leftComponent == c ) ?
                         node.rightComponent    :    node.leftComponent;
        //System.out.println("parent = " + par);
        Component pc = ( par.leftComponent == node ) ?
                         par.rightComponent     :     par.leftComponent;

        node.removeAll();
        //System.out.println("pc = " + pc);
        int loc = par.getDividerLocation();
        if ( par.leftComponent == node ) {
             par.removeAll();
             par.setLeftComponent( c2 );
             par.setRightComponent( pc );
        } else {
             par.removeAll();
             par.setLeftComponent( pc );
             par.setRightComponent( c2 );
        }
        if ( par.getNodeCount() == 2)     par.setDividerLocation(loc);
      }
    } else {
      //System.out.println("★削除4 " + node + "の " + c + "を削除");      
      SplitNode chaild = ( node.leftComponent instanceof SplitNode ) ?
        (SplitNode) node.leftComponent   :   (SplitNode) node.rightComponent;
      node.removeAll();
      node.setOrientation( chaild.getOrientation() );
      node.setLeftComponent( chaild.leftComponent );
      node.setRightComponent( chaild.rightComponent );
      node.setDividerLocation( chaild.getDividerLocation() );
      teachParent(node);
    }
  }
  
  /**
   * 指定されたノードから、ツリーを全検索して、指定されたコンポーネントが含まれて
   * いるノード(つまりgetTopComponent()やgetRightComponent()でcが返る)を見つけて
   * 返す。みつからないときはnullを返す。
   * 
   * @param root 検索を始めるノード。nullを指定するとこのノード自身が返る。
   * @param c 検索ターゲットのコンポーネント
   */
  public SplitNode findNode(Component c) {
    Stack<SplitNode> stack = new Stack<SplitNode>();
    return findNode( this, c, stack );
  }

  
  /**
   * 指定されたノードから、ツリーを全検索して、指定されたコンポーネントを見つけて
   * 返す。みつからないときはnullを返す。
   * @param node 検索するノード
   * @param c 検索ターゲットのコンポーネント
   * @param stack 全ノードを再帰トレースするために必要なスタック。最初は空のスタ
   * ックを渡す。
   */
  private static SplitNode findNode( SplitNode node, 
                                          Component c, 
                               Stack<SplitNode> stack ) {
    
    if( node.contain(c) ) return node;
    Component left = node.getLeftComponent();
    Component right = node.getRightComponent();
    if( left != null ) { //左から先に検索する
                // 右にノードがあるときはスタックに積む
      if( right instanceof SplitNode )
          stack.push( (SplitNode) right );
                // 左にもノードがあるときはそのノードをさらに検索
      if( left instanceof SplitNode ) 
          return findNode( (SplitNode)left, c, stack ); //再帰
          //ここで下まで落ちるのは、たとえleftがコンポーネントだったとしても、
          //それは探しているものではない。なぜなら最初のcontainsで判定されている。
    } else if( right != null) {
                // 右ノードがあればそれを検索
      if(right instanceof SplitNode)
          return findNode( (SplitNode)right, c, stack ); //再帰
          //ここで下まで落ちるのは(以下略)
    }
    if(stack.empty()) return null; //スタックが空なら検索終了
    //残りがあるなら、スタックから下ろし、検索続行
    return findNode( stack.pop(), c, stack ); //再帰
  }
  
  //デバイダを隠す
  void hideDivider() {
    setDividerSize(1);
  }
 
  //デバイダを出す
  void showDivider() {
    setDividerSize( SplitPane.DIVIDER_SIZE );
  }
  
  
  /**
   * 指定されたコンポーネントcがこのノードに含まれているときはtrueを返す。
   * 下層のノードまでは検出しない。
   */
  private boolean contain(Component c) {
    return getTopComponent() == c || getBottomComponent() == c;
  }

  // 登録されているノードの数を返す。0～2までの値が返る。
  private int getNodeCount() {
    int i=0;
    if(leftComponent != null) i++;
    if(rightComponent != null) i++;
    return i;
  }
  /**
   * このノードに登録されているコンポーネントを削除する。内部では単純に
   * <pre>
   * setBottomComponent(null);
   * setTopComponent(null);
   * </pre>
   * としてるだけだが、スーパークラスのremoveAll()とは微妙に振る舞いが異なり、
   * このノードの子を全て削除したい場合はこのメソッドを呼ぶこと。
   */
  public void removeAll() {
    setBottomComponent(null);
    setTopComponent(null);
  }
  /**
   * 二つの子の文字列表現を返す。
   */
  public String toString() {
    return "( " + getTopComponent() + ", " + getBottomComponent() + " )";
  }
}
