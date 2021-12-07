/*
 * MultiTabbedPane.java
 *
 * Created on 2007/10/30, 15:29
 *
 */

package multisplit;

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
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TabbedPaneUI;

/**
 *
 * @author ���V�`��
 */
public class MultiTabbedPane extends JComponent implements TabbedPaneListener {
  SplitNode rootNode = new SplitNode();
  GlassPane gpan = new GlassPane();
  MouseHandler mh = new MouseHandler();
  JFrame frame;
  List<InnerTabbedPane> tabpanList = new ArrayList<InnerTabbedPane>();
  TabbedPaneListener l;
  /**
   *  MultiTabbedPane �I�u�W�F�N�g���쐬����
   */
  public MultiTabbedPane(JFrame owner) {
    frame = owner;
    frame.setGlassPane( gpan );
    setLayout(new GridLayout(0,1));
    add(rootNode);
  }

//  MultiTabbedPane(MultiTabbedPane parent) {
//    frame = parent.frame;
//    setLayout(new GridLayout(0,1));
//    add(rootNode);
//  }
  
  public void removeAll() {
    rootNode.removeAll();
    tabpanList.clear();
    validate();
  }
  /**
   * �R���|�[�l���g��ǉ�����B(�����TabbedPane�ɓ�������)
   */
  public void insert( Icon icon, String title, Component comp) {
    InnerTabbedPane tabpan = new InnerTabbedPane();
    if(tabpanList.size() == 0) {
      // �}�E�X���X�i��^�u�N���[�Y�̃��X�i��o�^
      tabpan.addMouseListener(mh);
      tabpan.addMouseMotionListener(mh);
      tabpan.setTabbedPaneListener(l);
      tabpan.addTab( title, icon, comp);
      rootNode.addComponent( tabpan, SplitNode.INSERT_LEFT, null );
      tabpanList.add( tabpan );
    } else { //�Ō�̃^�u�y�C���ɒǉ�
      tabpan = tabpanList.get( tabpanList.size() - 1);
      tabpan.addTab( title, icon, comp);
    }
    tabpan.setSelectedComponent( comp );
    tabpan.requestFocusInWindow(); 
  }
  int count = 0;
  
  //A��n�Ԃ��AA�̃G���A��a�ɑ}�� 
  void insertTabbedPane(InnerTabbedPane src, int srcIndex, int align) {
    System.out.println("A��n�Ԃ��AA�̃G���A��a�ɑ}��");
    insertTabbedPane(src,srcIndex,src,align);
  }

  //A��n�Ԃ��AB�̃G���A��a�ɑ}��
  void insertTabbedPane(InnerTabbedPane src, int srcIndex,
                        InnerTabbedPane dist, int align) {
    System.out.println("A��n�Ԃ��AB�̃G���A��a�ɑ}��");
    InnerTabbedPane tabpan = new InnerTabbedPane();
    tabpan.addMouseListener(mh);
    tabpan.addMouseMotionListener(mh);
    tabpan.setTabbedPaneListener(l);
    Component srcComp = src.getComponentAt( srcIndex );
    String title = src.getTitleAt( srcIndex );
    Icon icon = src.getIconAt(srcIndex);
    
    tabpan.addTab(title,icon,srcComp); //���ꂵ�����_��src����폜�����̂Ō��̃^�u�p������폜�s�v
    SplitNode n = rootNode.findNode( dist );
    n.addComponent( tabpan, align, dist);
    tabpanList.add(tabpan);
    if(src.getTabCount() == 0)
      removeTabbedPane( src );
    tabpan.setSelectedComponent(srcComp);
    tabpan.requestFocusInWindow(); 
  }

  
  //A��n�Ԃ�B��m�ԂɈړ�
  private void moveTab(InnerTabbedPane src, int srcIndex,
                         InnerTabbedPane dist, int distIndex) {
    System.out.println("A��n�Ԃ�B��m�ԂɈړ�");
    System.out.println("src = " + src + ", srcIndex = " + srcIndex + ", dist = " + dist + ", distIndex = " + distIndex);
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
						
  //A��n�Ԃ��AA��m�ԂɈړ�

  void moveTab(InnerTabbedPane src, int srcIndex, int distIndex) {
    if ( srcIndex == distIndex ) return;      //�����ꏊ�Ȃ�L�����Z��
    if ( (srcIndex + 1) == distIndex) return; //�E�ׂֈړ����L�����Z��
    System.out.println("A��n�Ԃ��AA��m�ԂɈړ�");
    Component srcComp = src.getComponentAt( srcIndex );
    String title = src.getTitleAt( srcIndex );
    Icon icon = src.getIconAt(srcIndex);
    int srcCount = src.getTabCount();
    src.removeTabAt(srcIndex);
    if( distIndex == (srcCount - 1) || (distIndex > srcIndex)) { //�Ō�Ɉړ�
      src.insertTab(title,icon,srcComp,distIndex - 1);
    }
    else { //(��ȏ�)���Ɉړ�
      src.insertTab(title,icon,srcComp,distIndex);
    }
    src.setSelectedComponent(srcComp);
    src.requestFocusInWindow(); 
  }

  
  //�w�肳�ꂽ�^�u�p�����N���[�Y
  private void removeTabbedPane(InnerTabbedPane tabpan) {
    rootNode.removeComponent( tabpan );
    tabpanList.remove( tabpan );
    rootNode.validate();    
  }
  //�^�u�̃{�^���ŃN���[�Y���ꂽ�Ƃ�
  public void closedTab(InnerTabbedPane tabpan, Component c) {
    if(tabpan.getTabCount() == 0) removeTabbedPane(tabpan);
  }


  boolean normalMode = true;
  SplitNode rootNode2;
  SplitNode orgNode;
  Dimension orgSize;
  int orgLoc;

  public void doubleClicked(InnerTabbedPane tabpan, Component c) {
    if( normalMode ) {
      orgNode = rootNode.findNode(tabpan);
      orgLoc = orgNode.getDividerLocation();
      orgSize = tabpan.getSize();
      remove(rootNode);
      
    } else {
      
    }
  }

//  public void doubleClicked(InnerTabbedPane tabpan, Component c) {
//    if( normalMode ) {
//      orgNode = rootNode.findNode(tabpan);
//      orgLoc = orgNode.getDividerLocation();
//      orgSize = tabpan.getSize();
//      remove(rootNode);
//      rootNode2 = rootNode;
//      rootNode = new SplitNode();
//      rootNode.addComponent( tabpan, SplitNode.INSERT_LEFT, null);
//      for(InnerTabbedPane tp : tabpanList) {
//        if(tp != tabpan) tp.hide = true;
//      }
//      add(rootNode);
//      normalMode = false;
//      revalidate();
//      repaint();
//    } else {
//      Component comp;
//      if(orgNode.getLeftComponent() == null) {
//        if(rootNode.getNodeCount() == 2)
//         orgNode.setLeftComponent(rootNode);
//        else {
//          comp = rootNode.getWhichComponent();
//          System.out.println("comp = " + comp + ", rootCount " 
//            + rootNode.getComponentCount());
//          comp.setSize(orgSize);
//          orgNode.setLeftComponent(comp);
//        }
//        orgNode.setDividerLocation(orgLoc);
//      } else if(orgNode.getRightComponent() == null) {
//        if(rootNode.getNodeCount() == 2)
//          orgNode.setRightComponent(rootNode);
//        else {
//          comp = rootNode.getWhichComponent();
//          System.out.println("comp = " + comp + ", rootCount " 
//            + rootNode.getComponentCount());
//          comp.setSize(orgSize);
//          orgNode.setRightComponent(comp);          
//        }
//        orgNode.setDividerLocation(orgLoc);
//      } else {
//        System.out.println("�ǂ������g�p��");
//      }
//      remove(rootNode);
//      rootNode = rootNode2;
//      for(InnerTabbedPane tp : tabpanList) {
//        if(tp != tabpan) tp.hide = false;
//      }
//      add(rootNode);
//      normalMode = true;
//      revalidate();
//      repaint();
//    }
//  }
    
  
  class GlassPane extends JComponent {
    Shape bounds;
    Color color = new Color(255,0,0,150);
    Stroke stroke = new BasicStroke(3f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
    @Override
    protected void paintComponent( Graphics g ) {
      Graphics2D g2 = (Graphics2D)g;
      Dimension size = frame.getSize();
      if(bounds != null) {
        g2.setColor(color);
        g2.setStroke(stroke);
        g2.draw(bounds);
      }
    }
    void drawShape(Shape bounds) {
      this.bounds = bounds;
      repaint();
    }
    void eraseBounds() {
      bounds = null;
      repaint();
    }
  }
  //�^�u�p���̃}�E�X�n���h��
  class MouseHandler extends MouseAdapter implements MouseMotionListener {
    InnerTabbedPane begin; //�h���b�O�J�n�ƂƂ��ɂ��̈ʒu�̃^�u�p��������B�I�������null�B
    InnerTabbedPane target;
    InnerTabbedPane old;
    int align = -1;
    int srcIndex = -1;
    
    public void mouseDragged(MouseEvent evt) {
      if ( tabpanList.size() <= 0 ) return;
      //�^�u�p�����ł̍��W���O���X�p���̍��W�ɕϊ�
      MouseEvent ev = SwingUtilities.
        convertMouseEvent((Component)evt.getSource(),evt, gpan); 
      Point p = ev.getPoint();
      if( begin == null ) {
        updateBounds(); //�K���X�p�����̃^�u�p���̈ʒu���A���ׂẴ^�u�p���ɃZ�b�g
        gpan.setVisible(true);
        //�ǂ̃^�u�p���Ńh���b�O���J�n������������begin�ɃZ�b�g
        for(int i=0; i<tabpanList.size(); i++) {
          InnerTabbedPane pan = tabpanList.get(i);
          if(pan.hide) continue;
          if ( pan.glassBounds.contains( p.getX(), p.getY() ) ) {
            int ti = pan.indexAtLocation(evt.getX(),evt.getY());
            if( ti >= 0 ) {
              srcIndex = pan.getSelectedIndex();
              createDropBounds(pan,p);
              begin = target = pan; //�h���b�N�J�n����̃^�u�p�����Z�b�g
              break;
            }
          }
        }
      } else {
        InnerTabbedPane nowpan = null;
        for(InnerTabbedPane tp : tabpanList) {
          if(tp.hide) continue;
          if(tp.glassBounds.contains(p)) {
            nowpan = tp;
            break;
          }
        }
        if( target == nowpan || nowpan == null) { //���̃^�u�p���ɂ͈ړ����ĂȂ�
          int a = getAlign(p);
          if(align != a) {
            align = a;
            gpan.drawShape(getGuideShape(align));
          }
        } else {
          target = nowpan;
          createDropBounds(target,p);
          align = getAlign(p);
          gpan.drawShape(getGuideShape(align));
        }
      }
    }

    public void mouseReleased( MouseEvent evt) {
      //�h���b�O�ł͂Ȃ��_�u�N������������ꍇ������Balign��-1�̂Ƃ��̓L�����Z��
      if( begin != null ) {
        if( begin != target && align >= 10) {
          int distIndex = align - 10;
          moveTab(begin, srcIndex, target, distIndex);
        } else if ( begin == target && align >= 10) {
          int distIndex = align - 10;
          moveTab(begin, srcIndex, distIndex);
        } else if ( begin == target && align >= 0 && align < 4 && 
                   !( tabpanList.size() <= 1 && begin.getTabCount() == 1 )) {
          insertTabbedPane( begin, srcIndex, align );
        } else if ( begin != target && align >= 0 && align < 4 ) {
          insertTabbedPane( begin ,srcIndex, target, align );
        }
      }
      begin = null;
      target = null;
      srcIndex = -1;
      gpan.eraseBounds();
      gpan.setVisible(false);
    }
      
    public void mouseMoved(MouseEvent evt) {
    }
  }

  private Rectangle [] dropRects = new Rectangle [] {
            new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle()
  };
  private Rectangle [] guideRects = new Rectangle [] {
            new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle()
  };
  private Rectangle guideAbort = new Rectangle();
  private List<Rectangle> dropTabList = new ArrayList<Rectangle>();
  private List<Path2D> guideTabList = new ArrayList<Path2D>();
  
  //�}�E�X�ő}���ł���ꏊ���ǂ����̔���ɕK�v�ȋ�`�̈�𕡐��p�ӂ���B
  private void createDropBounds(InnerTabbedPane pan, Point p) {
    //�^�u������
    dropTabList.clear();
    guideTabList.clear();
    TabbedPaneUI ui = pan.getUI();
    for(int i=0; i < pan.getTabCount(); i++) {
      Rectangle rect = ui.getTabBounds(pan,i);
      Point z = SwingUtilities.convertPoint( pan, rect.getLocation(), gpan );
      rect.setLocation(z);
      dropTabList.add(rect);
    }
    //�Ō�̂��̂𗘗p���Ă�������
    Rectangle r2 = new Rectangle(dropTabList.get(pan.getTabCount()-1));
    r2.x += r2.width;
    dropTabList.add(r2);
    
    Rectangle r = pan.glassBounds;
    int h = r.height * 22 / 100;
    int w = r.width * 22 / 100;
    //�R���e���c��`������
    dropRects[0].setBounds( r.x, r.y, r.width - 1, h);              // top
    dropRects[1].setBounds( r.x, r.y + h, w, r.height - h * 2 - 1 );    // left
    dropRects[2].setBounds( r.x, r.y + r.height - h, r.width - 1, h - 1);  // bottom
    dropRects[3].setBounds( r.x + r.width - w, r.y + h, w, r.height - h * 2 - 1); //right
    //�R���e���c��`�K�C�h
    guideRects[0].setBounds(r.x, r.y, r.width - 1, h);          // top
    guideRects[1].setBounds(r.x, r.y, w, r.height - 1);         // left
    guideRects[2].setBounds(r.x, r.y + r.height - h, r.width - 1, h - 1); // bottom
    guideRects[3].setBounds(r.x + r.width - w - 1, r.y, w, r.height - 1); //right
    for( int i=0; i<guideRects.length; i++ ) resizeRect(guideRects[i]);
    guideAbort.setBounds(r);
    resizeRect(guideAbort);
    //�^�u�K�C�h
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
      //path.closePath(); ���Ă͂����Ȃ�
      guideTabList.add(path);
    }
  }
  //1�s�N�Z�������ɏ���������B
  private void resizeRect(Rectangle r) {
    r.x += 2;
    r.y += 2;
    r.width -= 4;
    r.height -= 4;
  }

  // �}���ʒu��Ԃ��B10�ȏ�̒l���Ԃ�Ƃ��́A�^�u�̑}���ʒu���Ӗ����Ă���B
  // 0-3�̒l���Ԃ�Ƃ��́A��`�̈�̑}���ʒu���Ӗ�����B-1�͔͈͊O�̈Ӗ��B
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
  
// align�ɑΉ�����K�C�h��Shape��Ԃ�
  private Shape getGuideShape(int align ) {
    if(align < 0) return guideAbort;
    if(align < 4) {
      return guideRects[align];
    }
    if(align >= 10) {
      return guideTabList.get( align - 10 );
    }
    return null;
  }
  //�^�u�p�l���ɃK���X�p�l���̏�ł̈ʒu��������
  private void updateBounds() {
    if(tabpanList.size() <= 0) return;
    for(int i=0; i<tabpanList.size(); i++) {
      InnerTabbedPane tabpan = tabpanList.get(i);
      if(tabpan.hide) continue;
      Point z = SwingUtilities.convertPoint( tabpan, 0, 0, gpan );
      tabpan.glassBounds.setLocation( z );
      tabpan.glassBounds.setSize( tabpan.getSize() );
      //System.out.println(tabpan + "  " + z + " " + tabpan.getSize()  );
    }
  }  

}
