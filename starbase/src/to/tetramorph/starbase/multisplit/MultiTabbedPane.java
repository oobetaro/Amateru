/*
 * MultiTabbedPane.java
 *
 * Created on 2007/10/30, 15:29
 *
 */

package to.tetramorph.starbase.multisplit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TabbedPaneUI;

/**
 * マルチ分割パネル。タブをダブクリするとシングル表示モードとなり、一番大きな
 * 画面で閲覧可能になる。再度ダブクリすると、もとのマルチ表示モードに戻る。
 * マルチ表示モードが初期値。
 *
 * シングルモードでタブをすべて閉じると、マルチモードに復帰する。
 * @author 大澤義鷹
 */
public class MultiTabbedPane extends JComponent {
  SplitNode rootNode = new SplitNode();
  GlassPane gpan = new GlassPane();
  MouseHandler mh = new MouseHandler();
  JFrame frame;
  List<InnerTabbedPane> tabpanList = new ArrayList<InnerTabbedPane>();
  boolean multiMode = true;
  TabbedPaneHandler tabHandler = new TabbedPaneHandler();
  Component selectedComponent = null;
  MultiTabbedListener mtl;
  /**
   *  MultiTabbedPane オブジェクトを作成する
   */
  public MultiTabbedPane(JFrame owner) {
    frame = owner;
    frame.setGlassPane( gpan );
    setLayout(new GridLayout(0,1));
    add(rootNode);
  }

  public void setMultiTabbedListener(MultiTabbedListener mtl) {
    this.mtl = mtl;
  }
  
  /**
   * 現在選択されているコンポーネントを返す。一枚もコンポーネントが無い状態のとき
   * はnullを返す。
   */
  public Component getSelectedComponent() {
    return selectedComponent;
  }
//  /**
//   * 指定されたコンポーネントを選択状態にする。選択されたコンポーネントにはフォー
//   * カスが移動する。指定されたコンポーネントがみつからない場合はなにもしない。
//   * @exception IllegalArgumentException compがnullの場合。
//   */
//  public void setSelectedComponent(Component comp) {
//      if(comp == null) throw new IllegalArgumentException(
//          "ターゲットのコンポーネントがnullです。");
//      for ( InnerTabbedPane tabpan : tabpanList ) {
//          for ( int i=0; i <tabpan.getTabCount(); i++ ) {
//              Component c = tabpan.getComponentAt(i);
//              if ( c == comp ) {
//                  tabpan.setSelectedComponent(comp);
//                  selectedComponent(comp);
//                  return;
//              }
//          }
//      }
//  }
  //選択されたコンポーネントをセットする。リスナ発火メソッドを呼び出す。
  private void selectedComponent(Component c) {
    selectedComponent = c;
    if ( mtl != null ) mtl.tabSelected(c);
  }
  /**
   * すべてのタベッドペインに格納されているすべてのコンポーネントのりストを返す。
   */
  public List<Component> getAllComponents() {
    List<Component> list = new ArrayList<Component>();
    for(InnerTabbedPane tabpan : tabpanList) {
      for(int i=0; i <tabpan.getTabCount(); i++) {
        Component c = tabpan.getComponentAt(i);
        list.add(c);
      }
    }
    return list;
  }
  /**
   * 指定さたれコンポーネントのタブにアイコンとタイトルをセットする。
   */
  public void setTitle(Icon icon,String title,Component comp) {
    if(comp == null) throw 
      new IllegalArgumentException("ターゲットのコンポーネントがnullです。");
    for(InnerTabbedPane tabpan : tabpanList) {
      for(int i=0; i <tabpan.getTabCount(); i++) {
        Component c = tabpan.getComponentAt(i);
        if(c == comp) {
          if(icon != null) tabpan.setIconAt(i,icon);
          if(title != null) tabpan.setTitleAt(i,title);
          return;
        }
      }
    }
  }
  Component contentComponent;
  
  public void contentSelected(Component comp) {
    //System.out.println("コンテンツクリック" + comp);
    for(InnerTabbedPane tabpan : tabpanList) {
      for(int i=0; i <tabpan.getTabCount(); i++) {
        Component c = tabpan.getComponentAt(i);
        if(c == comp) {
          if(tabpan.isFocusOwner()) {
            //System.out.println("フォーカスオーナー");
          } else
            //System.out.println("フォーカスなし");
            tabpan.setSelectedComponent(comp);
            tabpan.requestFocusInWindow();
            selectedComponent(comp);
        }
      }
    }    
  }
  /**
   * 指定されたコンポーネントのタブにタイトルをセットする。
   */
  public void setTitle(String title,Component comp) {
    setTitle(null,title,comp);
  }
  /**
   * 指定されたコンポーネントのタブにアイコンをセットする。
   */
  public void setIcon(Icon icon,Component comp) {
    setTitle(icon,null,comp);
  }
  /**
   * コンポーネントをすべて削除する。
   */
  public void removeAll() {
    multiMode = true;
    rootNode.removeAll();
    tabpanList.clear();
    revalidate();
  }
  /**
   * コンポーネントを追加する。(これはTabbedPaneに入れるもの)
   */
  public void insert( Icon icon, String title, Component comp) {
    InnerTabbedPane tabpan = new InnerTabbedPane();
    if(tabpanList.size() == 0) {
      // マウスリスナやタブクローズのリスナを登録
      tabpan.addMouseListener(mh);
      tabpan.addMouseMotionListener(mh);
      tabpan.setTabbedPaneListener(tabHandler);
      tabpan.addTab( title, icon, comp);
      rootNode.addComponent( tabpan, SplitNode.INSERT_LEFT, null );
      tabpanList.add( tabpan );
    } else { //最後のタブペインに追加
      tabpan = tabpanList.get( tabpanList.size() - 1);
      tabpan.addTab( title, icon, comp);
    }
    tabpan.setSelectedComponent( comp );
    tabpan.requestFocusInWindow();
    selectedComponent(comp);
  }
  
  
  int count = 0;
  
  //Aのn番を、Aのエリアのaに挿入
  void insertTabbedPane(InnerTabbedPane src, int srcIndex, int align) {
    //System.out.println("Aのn番を、Aのエリアのaに挿入");
    insertTabbedPane(src,srcIndex,src,align);
  }
  
  //Aのn番を、Bのエリアのaに挿入
  void insertTabbedPane(InnerTabbedPane src, int srcIndex,
    InnerTabbedPane dist, int align) {
    //System.out.println("Aのn番を、Bのエリアのaに挿入");
    InnerTabbedPane tabpan = new InnerTabbedPane();
    tabpan.addMouseListener(mh);
    tabpan.addMouseMotionListener(mh);
    tabpan.setTabbedPaneListener(tabHandler);
    Component srcComp = src.getComponentAt( srcIndex );
    String title = src.getTitleAt( srcIndex );
    Icon icon = src.getIconAt(srcIndex);
    
    //addTabした時点でsrcから削除されるので元のタブパンから削除不要
    tabpan.addTab(title,icon,srcComp);
    SplitNode n = rootNode.findNode( dist );
    n.addComponent( tabpan, align, dist);
    tabpanList.add(tabpan);
    if(src.getTabCount() == 0)
      removeTabbedPane( src );
    tabpan.setSelectedComponent(srcComp);
    tabpan.requestFocusInWindow();
  }
  
  //Aのn番をBのm番に移動
  private void moveTab(InnerTabbedPane src, int srcIndex,
    InnerTabbedPane dist, int distIndex) {
    //System.out.println("Aのn番をBのm番に移動");
    Component srcComp = src.getComponentAt( srcIndex );
    String title = src.getTitleAt( srcIndex );
    Icon icon = src.getIconAt(srcIndex);
    src.removeTabAt(srcIndex);
    dist.addTab(title,icon,srcComp);
    dist.setSelectedComponent(srcComp);
    moveTab(dist,dist.getTabCount()-1,distIndex);
    if(src.getTabCount() == 0)
      removeTabbedPane( src );
    dist.setSelectedComponent(srcComp);
    dist.requestFocusInWindow();
  }
  
  //Aのn番を、Aのm番に移動
  
  void moveTab(InnerTabbedPane src, int srcIndex, int distIndex) {
    if ( srcIndex == distIndex ) return;      //同じ場所ならキャンセル
    if ( (srcIndex + 1) == distIndex) return; //右隣へ移動もキャンセル
    //System.out.println("Aのn番を、Aのm番に移動");
    Component srcComp = src.getComponentAt( srcIndex );
    String title = src.getTitleAt( srcIndex );
    Icon icon = src.getIconAt(srcIndex);
    int srcCount = src.getTabCount();
    src.removeTabAt(srcIndex);
    if( distIndex == (srcCount - 1) || (distIndex > srcIndex)) { //最後に移動
      src.insertTab(title,icon,srcComp,distIndex - 1);
    } else { //(二つ以上)左に移動
      src.insertTab(title,icon,srcComp,distIndex);
    }
    src.setSelectedComponent(srcComp);
    src.requestFocusInWindow();
  }
  
  
  //指定されたタブパンをクローズ
  public void removeTabbedPane(InnerTabbedPane tabpan) {
    rootNode.removeComponent( tabpan );
    tabpanList.remove( tabpan );
    if(tabpanList.size() >= 1)
      selectedComponent(tabpanList.get(0).getSelectedComponent());
    else
      selectedComponent(null);
    rootNode.validate();
  }

  //タブペインのリスナ。タブの×ボタンが押されたらタブやタブペインを消去したり、
  //マルチ/シングルのモード切替処理を行う。
  
  class TabbedPaneHandler implements TabbedPaneListener {
    //タブのボタンでクローズされたときの処理。
    public void closedTab(InnerTabbedPane tabpan, Component c) {
      if ( mtl != null ) mtl.tabClosed(c);
      if ( multiMode ) {
        if(tabpan.getTabCount() == 0) {
          removeTabbedPane(tabpan);
        } else {
          selectedComponent(tabpan.getSelectedComponent());
        }
      } else {
        if(tabpan.getTabCount() == 0) {
          removeTabbedPane(tabpan);
          if(getTabpanCount() == 0) {
            if(orgNode.getLeftComponent() == tabpan) {
              orgNode.setLeftComponent(null);
            } else if( orgNode.getRightComponent() == tabpan) {
              orgNode.setRightComponent(null);
            }
            orgNode.setDividerSize(1);
            orgNode.setDividerLocation(-1);
            remove(rootNode);
            rootNode = rootNode2;
            for(InnerTabbedPane tp : tabpanList)
              tp.hide = false;
            add(rootNode);
            multiMode = true;
            revalidate();
            repaint();
          }
        }
      }
    }
    
    SplitNode rootNode2; //マルチモード時のノード退避用
    SplitNode orgNode;   //シングルモードの対象となったノード
    Dimension orgSize;   //そのノードのサイズ
    int orgLoc;         //そのノードのデバイダ位置(単位 : pixcel)
    /**
     * タブがダブクリされたとき、シングルパネルモードと、マルチパネルモードを切り
     * 替える。
     */
    public void doubleClicked(InnerTabbedPane tabpan, Component c) {
      if( multiMode ) {                               //マルチパネルモードのとき
        if(tabpanList.size() <= 1) return;            //タブペインが一つならやめ
        orgNode = rootNode.findNode(tabpan);//シングル化で対象となったパネルを保管
        orgLoc = orgNode.getDividerLocation();         //デバイダの位置も保管
        orgSize = tabpan.getSize();                    //パネルサイズも保管
        remove(rootNode);           //現在表示されてるマルチ分割されたパネルを消去
        rootNode2 = rootNode;                          //rootNodeを保管
        rootNode = new SplitNode();                //シングル用にノードを新規作成
        //対象となったタブペインを移す。もとのノードからは自動削除される。
        rootNode.addComponent( tabpan, SplitNode.INSERT_LEFT, null);
        //対象タブペイン以外に非表示を意味するフラグを立てる
        for(InnerTabbedPane tp : tabpanList)
          if(tp != tabpan) tp.hide = true;
        //シングル用のノードをこのパネルにセットして再描画。シングルモードの開始。
        add(rootNode);
        multiMode = false; //シングルモードとする
      } else {                                       //シングルパネルモードのとき
        //マルチパネルモードに戻すわけだが、シングルのとき対象となったタブペインは
        //orgNodeから自動的に抜き取られてnullになっている。そのnullのノードを見つけ
        //て、シングルモードのノードを再挿入する。
        Component comp;
        if(orgNode.getLeftComponent() == null) {
          if(rootNode.getNodeCount() == 2) {
            //シングルモード中にタブペインが2つかそれ以上増えたことを意味し、
            //そのときはノードをそのままセット
            rootNode.parent = orgNode;
            orgNode.setLeftComponent(rootNode); //親となるノードを教える
            System.out.println("ノードが増えてますね");
          } else {
            //タブペインは一つのままなのでその一つを取り出し元のノードに戻す。
            //サイズが変わってしまっているので再セット
            comp = rootNode.getWhichComponent();
            System.out.println("comp = " + comp + ", rootCount "
              + rootNode.getComponentCount());
            comp.setSize(orgSize);
            orgNode.setLeftComponent(comp);
          }
        } else if(orgNode.getRightComponent() == null) {
          if(rootNode.getNodeCount() == 2) {
            orgNode.setRightComponent(rootNode);
            rootNode.parent = orgNode;
            System.out.println("ノードが増えてますね");
          } else {
            comp = rootNode.getWhichComponent();
            System.out.println("comp = " + comp + ", rootCount "
              + rootNode.getComponentCount());
            comp.setSize(orgSize);
            orgNode.setRightComponent(comp);
          }
        } else { //ロジックに予想外の事が起きないかぎりこれはありえない。
          throw new java.lang.IllegalStateException(
            "SplitPaneノードが想定外の構造を形成しています。致命的なバグです");
        }
        orgNode.setDividerLocation(orgLoc);             //デバイダの位置も再セット
        remove(rootNode);                 //シングルモードの画面を消去し
        rootNode = rootNode2;             //マルチモードの画面を復帰し、
        //すべてのタブペインの不可視フラグを降ろす
        for(InnerTabbedPane tp : tabpanList) tp.hide = false;
        add(rootNode);
        multiMode = true;
      }
      revalidate();
      repaint();
      tabpan.requestFocusInWindow();
    }
  }
  
  
  /**
   * JFrameのガラス区画。マウスドラッグ中のみ可視化される。
   * タブペインまたはタブの挿入可能位置をガラス区画に赤のガイド線で表示する。
   */
  
  private static final Color guideColor = new Color(255,0,0,150);
  private static final Stroke guideStroke =
    new BasicStroke(3f, BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
  
  class GlassPane extends JComponent {
    
    Shape shape;
    
    @Override
    protected void paintComponent( Graphics g ) {
      Graphics2D g2 = (Graphics2D)g;
      Dimension size = frame.getSize();
      if(shape != null) {
        g2.setColor(guideColor);
        g2.setStroke(guideStroke);
        g2.draw(shape);
      }
    }
    //シェイプを描画
    void drawShape(Shape shape) {
      this.shape = shape;
      repaint();
    }
    //シェイプを消去する
    void eraseShape() {
      shape = null;
      repaint();
    }
  }
  
  //画面上にあるタブペインの総数を返す。モードに応じて計算法が異なる
  
  int getTabpanCount() {
    if(multiMode) return tabpanList.size();      //マルチモードは全数を返す
    else { //全画面モードなら
      count = 0;
      for(InnerTabbedPane tp : tabpanList) {
        if(! tp.hide) count++;             //可視化されているものだけカウント
      }
    }
    return count;
  }
  
  /**
   * 各タブペインにセットされるマウスハンドラ。タブをマウスで移動したり、新しい
   * タブペインを用意して挿入するような処理をする。
   */
  class MouseHandler extends MouseAdapter implements MouseMotionListener {
    InnerTabbedPane begin; //ドラッグ開始とともにその位置のタブパンが入る。終了するとnull。
    InnerTabbedPane target;
    InnerTabbedPane old;
    int align = -1;
    int srcIndex = -1;
    
    public void mouseDragged(MouseEvent evt) {
      if ( tabpanList.size() <= 0 ) return;
      //タブパン内での座標をグラスパンの座標に変換
      MouseEvent ev = SwingUtilities.
        convertMouseEvent((Component)evt.getSource(),evt, gpan);
      Point p = ev.getPoint();
      if( begin == null ) {
        updateBounds(); //ガラスパン内のタブパンの位置を、すべてのタブパンにセット
        gpan.setVisible(true);
        //どのタブパンでドラッグが開始したか検査しbeginにセット
        for(int i=0; i<tabpanList.size(); i++) {
          InnerTabbedPane pan = tabpanList.get(i);
          if(pan.hide) continue;
          if ( pan.glassBounds.contains( p.getX(), p.getY() ) ) {
            int ti = pan.indexAtLocation(evt.getX(),evt.getY());
            if( ti >= 0 ) {
              srcIndex = pan.getSelectedIndex();
              createDropBounds(pan,p);
              begin = target = pan; //ドラック開始直後のタブパンをセット
              break;
            }
          }
        }
      } else {
        InnerTabbedPane nowpan = null;
        for(InnerTabbedPane tp : tabpanList) {
          if(tp.hide) continue;
          // glassBoundsはcreateBounds()でセットされる値。ガラス区画でのこのペイン
          // の座標やサイズが入っている。
          if(tp.glassBounds.contains(p)) {
            nowpan = tp;
            break;
          }
        }
        if( target == nowpan || nowpan == null) { //他のタブパンには移動してない
          int a = getAlign(p);
          if(align != a) {               //前回の位置と変わっていたら描きなおす
            align = a;
            gpan.drawShape(getGuideShape(align));
          }
        } else {                      //ドラッグ中に他のタブペインにまで移動した
          target = nowpan;
          createDropBounds(target,p);           //区画判定用情報を更新
          align = getAlign(p);                  //カーソル位置から挿入位置を得る
          //挿入位置からそれを示すガイドシェイプを得て描く
          gpan.drawShape(getGuideShape(align));
        }
      }
    }
    //このタイミングでタブの移動や挿入を実行する
    public void mouseReleased( MouseEvent evt) {
      //ドラッグではなくダブクリが発生する場合もある。alignが-1のときはキャンセル
      // 10かそれ以上の値はタブ区画の何番目かの意味
      if( begin != null ) {
        if( begin != target && align >= 10) {
          int distIndex = align - 10;
          moveTab(begin, srcIndex, target, distIndex);
        } else if ( begin == target && align >= 10) {
          int distIndex = align - 10;
          moveTab(begin, srcIndex, distIndex);
        } else if ( begin == target && align >= 0 && align < 4 &&
          !( getTabpanCount() <= 1 && begin.getTabCount() == 1 )) {
          insertTabbedPane( begin, srcIndex, align );
        } else if ( begin != target && align >= 0 && align < 4 ) {
          insertTabbedPane( begin ,srcIndex, target, align );
        }
      }
      //ドラッグ終了。判定用プラグをリセットしガラス区画も非表示にする
      begin = null;
      target = null;
      srcIndex = -1;
      gpan.eraseShape();
      gpan.setVisible(false);
      
    }
    /**
     * 現在選択されているコンポーネントを得る
     */
    public void mousePressed(MouseEvent evt) {
      InnerTabbedPane tabpan = (InnerTabbedPane)evt.getSource();
      int index = tabpan.indexAtLocation(evt.getX(),evt.getY());
      if(index >= 0) {
        selectedComponent(tabpan.getComponentAt(index));
      } else {
        System.out.println("Click = " + tabpan.getSelectedComponent());
        selectedComponent(tabpan.getSelectedComponent());
      }
    }
    public void mouseMoved(MouseEvent evt) {
    }
  }
  //冗長だがメモリ節約のため固定配列を用意
  private Rectangle [] dropRects = new Rectangle [] {
    new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle()
  };
  private Rectangle [] guideRects = new Rectangle [] {
    new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle()
  };
  private Rectangle guideAbort = new Rectangle();
  private List<Rectangle> dropTabList = new ArrayList<Rectangle>();
  private List<Path2D> guideTabList = new ArrayList<Path2D>();
  
  //マウスで挿入できる場所かどうかの判定に必要な矩形領域を複数用意する。
  private void createDropBounds(InnerTabbedPane pan, Point p) {
    //タブ当たり
    dropTabList.clear();
    guideTabList.clear();
    TabbedPaneUI ui = pan.getUI();
    for(int i=0; i < pan.getTabCount(); i++) {
      Rectangle rect = ui.getTabBounds(pan,i);
      Point z = SwingUtilities.convertPoint( pan, rect.getLocation(), gpan );
      rect.setLocation(z);
      dropTabList.add(rect);
    }
    //最後のものを利用してもう一つ作る。なぜならばタブ挿入候補位置は、タブ数より
    //一つ多いから。
    Rectangle r2 = new Rectangle(dropTabList.get(pan.getTabCount()-1));
    r2.x += r2.width;
    dropTabList.add(r2);
    
    Rectangle r = pan.glassBounds;
    int h = r.height * 22 / 100;
    int w = r.width * 22 / 100;
    //コンテンツ矩形当たり(マウスカーソルの当たり判定用)
    dropRects[0].setBounds( r.x, r.y, r.width - 1, h);              // top
    dropRects[1].setBounds( r.x, r.y + h, w, r.height - h * 2 - 1 );    // left
    dropRects[2].setBounds( r.x, r.y + r.height - h, r.width - 1, h - 1);  // bottom
    dropRects[3].setBounds( r.x + r.width - w, r.y + h, w, r.height - h * 2 - 1); //right
    //コンテンツ矩形ガイド(ガラス区画に赤で描くガイド線のShape)
    guideRects[0].setBounds(r.x, r.y, r.width - 1, h);          // top
    guideRects[1].setBounds(r.x, r.y, w, r.height - 1);         // left
    guideRects[2].setBounds(r.x, r.y + r.height - h, r.width - 1, h - 1); // bottom
    guideRects[3].setBounds(r.x + r.width - w - 1, r.y, w, r.height - 1); //right
    for( int i=0; i<guideRects.length; i++ ) resizeRect(guideRects[i]);
    guideAbort.setBounds(r);
    resizeRect(guideAbort);
    //タブガイド(タブの挿入・移動で表示されるガイド線のShape)
    for( int i=0; i<dropTabList.size(); i++ ) {
      Rectangle tr = dropTabList.get(i);
      double xofs = - tr.width / 2;
      Path2D path = new Path2D.Double();
      path.moveTo(  xofs + tr.x + 2,                 tr.y + 2         );
      path.lineTo(  xofs + tr.x + 2,                  r.y +  r.height - 1 - 2 );
      path.lineTo(          r.x +  r.width - 1 - 2,   r.y +  r.height - 1 - 2 );
      path.lineTo(          r.x +  r.width - 1 - 2,  tr.y + tr.height - 1 + 2 );
      path.lineTo(  xofs + tr.x + tr.width - 1 - 2,  tr.y + tr.height - 1 + 2 );
      path.lineTo(  xofs + tr.x + tr.width - 1 - 2,  tr.y + 2                 );
      path.lineTo(  xofs + tr.x + 2,                 tr.y + 2                 );
      //path.closePath(); 閉じてはいけない
      guideTabList.add(path);
    }
  }
  //2ピクセル内側に小さくする。
  private void resizeRect(Rectangle r) {
    r.x += 2;
    r.y += 2;
    r.width -= 4;
    r.height -= 4;
  }
  
  // 挿入位置を返す。10以上の値が返るときは、タブの挿入位置を意味している。
  // 0-3の値が返るときは、矩形領域の挿入位置を意味する。-1は範囲外の意味。
  private int getAlign( Point p ) {
    for(int i=0; i < dropTabList.size(); i++) {
      Rectangle r = dropTabList.get(i);
      if(r.contains(p)) return i + 10;
    }
    for(int i=0; i< dropRects.length; i++) {
      if(dropRects[i].contains(p)) return i;
    }
    return -1;
  }
  
// alignに対応するガイドのShapeを返す
  private Shape getGuideShape(int align ) {
    if(align < 0)   return guideAbort;
    if(align < 4)   return guideRects[align];
    if(align >= 10) return guideTabList.get( align - 10 );
    return null;
  }
  //タブペインにガラスパネルの上での位置を教える
  private void updateBounds() {
    if(tabpanList.size() <= 0) return;
    for(int i=0; i<tabpanList.size(); i++) {
      InnerTabbedPane tabpan = tabpanList.get(i);
      //非可視フラグが立っているものは計算しない
      if(tabpan.hide) continue;
      Point z = SwingUtilities.convertPoint( tabpan, 0, 0, gpan );
      tabpan.glassBounds.setLocation( z );
      tabpan.glassBounds.setSize( tabpan.getSize() );
    }
  }
  
}
