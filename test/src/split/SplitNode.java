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
 * ���̃m�[�h���g���ĉ�ʂ��f�o�C�_�Ŏ��݂ɕ������āA���̒���JPanel�Ȃǂ̕��i��
 * ���荞�ނ��Ƃ��ł���B
 * @author ���V�`��
 */
public class SplitNode extends SplitPane {
  /** 
   * �}���^�[�Q�b�g�̃R���|�[�l���g���u����Ă�����́A�ǂ̈ʒu�ɑ}�����邩��
   * �w�肷��萔�B
   */
  public static final int INSERT_TOP = 0;
  public static final int INSERT_LEFT = 1;
  public static final int INSERT_BOTTOM = 2;
  public static final int INSERT_RIGHT = 3;
  protected SplitNode parent = null;
  /**  
   * SplitNode �I�u�W�F�N�g���쐬����
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
   * �m�[�h�ɃR���|�[�l���g���}�����ꂽ�ۂɁA�}�����ꂽ�R���|�[�l���g�̃T�C�Y��
   * �ݒ�(�}�����̃T�C�Y�̕�/�����ǂ��炩�̃T�C�Y�̖�30%�ɐݒ�)���A����܂�
   * �}������t�ɂ��߂Ă����R���|�[�l���g�܂��̓m�[�h�̃T�C�Y�𓯗l��70%�ɐݒ�
   * ���A����ɍ��킹�đ}�����ɂ������f�o�C�_�̈ʒu��30:70�̈ʒu�ɍĐݒ肷��B
   * flag�̒l�ɂ���ẮA�}�����ꂽ�m�[�h���g�ɑ}�����̃T�C�Y���Z�b�g����B
   * (�܂��A���̐����͂ƂĂ��킩��ɂ����Ǝv���B)
   * @param s     �f�o�C�_������m�[�h
   * @param t     �}���^�[�Q�b�g�ƂȂ����R���|�[�l���g(�S�̂̍L��)
   * @param c     �}�����ꂽ�R���|�[�l���g
   * @param ins   �}���ʒu
   * @param flag  false�̂Ƃ��́As�̃f�o�C�_�ʒu��ݒ肷�邾�������Atrue�ɂ���ƁA
   *              t.getSize()�̒l��s�ɏ������ރI�v�V�����B
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
   * ���̃m�[�h�ɃR���|�[�l���g��ǉ�����B
   * @param insert �}���ꏊ���w�肷��B
   * INSERT_TOP,INSERT_LEFT,INSERT__BOTTOM,INSERT_BOTTOM�̂����ꂩ
   * @param comp �}������R���|�[�l���g
   * @param targetComp �}������W�I�R���|�[�l���g�B���[�g�m�[�h�Ɉ�ڂ�
   * �R���|�[�l���g��ǉ�����Ƃ���null���w�肷��B
   */
  public void addComponent(Component comp,int insert,Component targetComp) {
    if ( comp == null) 
       throw new IllegalArgumentException( "null�͒ǉ��ł��܂���B" );
    if ( insert < INSERT_TOP || insert > INSERT_RIGHT )
       throw new IllegalArgumentException( "�}���ʒu�萔�ł͂���܂���B" );
    if ( comp instanceof SplitNode || targetComp instanceof SplitNode)
       throw new IllegalArgumentException( "SplitNode�͒ǉ��ł��܂���B" );
    if ( leftComponent instanceof SplitNode && 
          rightComponent instanceof SplitNode )
       throw new IllegalArgumentException( "���̃m�[�h�ɂ͒ǉ��ł��܂���B" );
    
    Component       top    = getTopComponent();
    Component       bottom = getBottomComponent();
    
    int            orientation = ((insert & 1) == 0) ? 
                    VERTICAL_SPLIT    :    HORIZONTAL_SPLIT ;
    boolean       isFront = (insert >> 1) == 0;

    if( top == null && bottom == null ) { //�����󂢂Ă���ꍇ
      setOrientation( orientation );
      if (isFront)                       // TOP | LEFT
          setTopComponent(comp);
      else                               // BOTTOM | RIGHT
          setBottomComponent(comp);
      
      hideDivider();                     //�����Ă���̂�1���Ȃ̂Ńf�o�C�_���B��
      return;
    }
    //top��null��bottom��SplitNode�ł͂Ȃ��ꍇ
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
    //bottom��null��top��SplitNode�ł͂Ȃ��ꍇ
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
      throw new IllegalArgumentException("targetComp��null�ł�");
    
    //�������Ƀ��[�U�[�R���|�[�l���g���l�܂��Ă���ꍇ�B�m�[�h���g��
    
    SplitNode sn = null;
    int dvloc = getDividerLocation();
    
    if( top == targetComp ) {
      
      if( isFront )               // �O���}��
        sn = new SplitNode( orientation, comp, top );
      else                       // ����}��
        sn = new SplitNode( orientation, top, comp );
      
      setTopComponent( null );
      setTopComponent(sn);
      setNewSize( sn, top, comp, insert, true);
      
    } else if( bottom == targetComp) {
      
      if( isFront )               // �O���}��
        sn = new SplitNode( orientation, comp, bottom );
      else                       // ����}��
        sn = new SplitNode( orientation, bottom, comp );
      
      setBottomComponent( null );
      setBottomComponent(sn);
      setNewSize( sn, bottom, comp, insert, true);
    }
    setDividerLocation( dvloc );
    sn.parent = this;           //�e�ւ̎Q�Ƃ�^����
    sn.showDivider();
  }
  // �m�[�h�̎q�ɐe��������
  private void teachParent( SplitNode node ) {
    if ( node.leftComponent instanceof SplitNode )
          ((SplitNode)node.leftComponent).parent = node;
    if ( node.rightComponent instanceof SplitNode)
          ((SplitNode)node.rightComponent).parent = node;
  }
//  /**
//   * �w�肳�ꂽ�R���|�[�l���g���폜����B
//   * @exception IllegalArgumentException ������null���w�肵���Ƃ�
//   */
//  public void removeComponent(Component c) {
//    if ( c == null ) throw new IllegalArgumentException("null�֎~");
//    SplitNode node = findNode(c);
//    if ( node == null ) return;
//    SplitNode par = node.parent;
//    // ���[�g�m�[�h�ł̍폜
//    if( par == null) {
//      System.out.println("���폜1" + node + "��" + c +"���폜");
//
//      Component c2 = ( node.leftComponent == c) ? 
//        node.rightComponent : node.leftComponent;
//      if ( node.bothNodesAreComponent() ) {
//        //���̍폜�ǂ����Ă��킸���Ɍ��Ԃ�����
//        node.removeAll();
//        node.setLeftComponent(c2); //�Ō��1���Ȃ̂�Left/Right�͂ǂ���ł��悢
//        node.hideDivider();
//        //node.updateUI();
//        System.out.println("              1-1");
//      } else if( c2 instanceof SplitNode ) { //���̃m�[�h����Ɉ����グ��
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
//    //�q�m�[�h�ŗ������t�m�[�h�̏ꍇ
//    if( node.bothNodesAreComponent() ) {
//      System.out.println("���폜2" + node + "��" + c + "���폜");
//      // �폜�ΏۂƂ͕ʂ̃R���|�[�l���g���擾
//      Component c2 = ( node.leftComponent == c ) ?
//                       node.rightComponent    :    node.leftComponent;
//      
////      if( node.leftComponent == c) c2 = node.rightComponent;
////      else if(node.rightComponent == c) c2 = node.leftComponent;
////      else throw new IllegalArgumentException("���肦�Ȃ��I");
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
//    if ( node.containSplitNode() ) {             //�q�m�[�h�������m�[�h�̏ꍇ
//        System.out.println("���폜4 " + node + "��" + c + "���폜");      
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
//             "���肦�Ȃ��f�[�^�\���B�Ȃɂ��o�O������܂��B");
//    
//  }
//  
  /**
   * �w�肳�ꂽ�R���|�[�l���g���폜����B�����Ŏw�肳���Component�́A
   * ��SplitNode�ŁASplitNode�ŕ҂񂾃m�[�h�̒��ɓo�^�����R���|�[�l���g�łȂ����
   * �Ȃ�Ȃ��B
   * 
   * @param c �폜����R���|�[�l���g�B
   * 
   * @exception IllegalArgumentException ������null���w�肵���Ƃ�
   */
  public void removeComponent(Component c) {
    if ( c == null ) throw new IllegalArgumentException("null�֎~");
    SplitNode node = findNode(c);
    if ( node == null ) return;
    Component left = node.leftComponent;
    Component right = node.rightComponent;
    
    //�ǂ�����t�ł���ꍇ( null�̏ꍇ��true�ƂȂ鎖�ɒ���! )
    if( ! (left instanceof SplitNode ) && ! ( right instanceof SplitNode )) {
      if( node.parent == null) {                  //�e���Ȃ����[�g�m�[�h�̏ꍇ
        System.out.println("���폜1 " + node + "�� " + c + "���폜");      
        if ( left == c )
          node.setLeftComponent(null);
        else
          node.setRightComponent(null);
        if(node.getNodeCount() <= 1) node.hideDivider();
      } else {                                    //�e������q�m�[�h�̏ꍇ
        //System.out.println("���폜2 " + node + "�� " + c + "���폜");      
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
      //System.out.println("���폜4 " + node + "�� " + c + "���폜");      
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
   * �w�肳�ꂽ�m�[�h����A�c���[��S�������āA�w�肳�ꂽ�R���|�[�l���g���܂܂��
   * ����m�[�h(�܂�getTopComponent()��getRightComponent()��c���Ԃ�)��������
   * �Ԃ��B�݂���Ȃ��Ƃ���null��Ԃ��B
   * 
   * @param root �������n�߂�m�[�h�Bnull���w�肷��Ƃ��̃m�[�h���g���Ԃ�B
   * @param c �����^�[�Q�b�g�̃R���|�[�l���g
   */
  public SplitNode findNode(Component c) {
    Stack<SplitNode> stack = new Stack<SplitNode>();
    return findNode( this, c, stack );
  }

  
  /**
   * �w�肳�ꂽ�m�[�h����A�c���[��S�������āA�w�肳�ꂽ�R���|�[�l���g��������
   * �Ԃ��B�݂���Ȃ��Ƃ���null��Ԃ��B
   * @param node ��������m�[�h
   * @param c �����^�[�Q�b�g�̃R���|�[�l���g
   * @param stack �S�m�[�h���ċA�g���[�X���邽�߂ɕK�v�ȃX�^�b�N�B�ŏ��͋�̃X�^
   * �b�N��n���B
   */
  private static SplitNode findNode( SplitNode node, 
                                          Component c, 
                               Stack<SplitNode> stack ) {
    
    if( node.contain(c) ) return node;
    Component left = node.getLeftComponent();
    Component right = node.getRightComponent();
    if( left != null ) { //�������Ɍ�������
                // �E�Ƀm�[�h������Ƃ��̓X�^�b�N�ɐς�
      if( right instanceof SplitNode )
          stack.push( (SplitNode) right );
                // ���ɂ��m�[�h������Ƃ��͂��̃m�[�h������Ɍ���
      if( left instanceof SplitNode ) 
          return findNode( (SplitNode)left, c, stack ); //�ċA
          //�����ŉ��܂ŗ�����̂́A���Ƃ�left���R���|�[�l���g�������Ƃ��Ă��A
          //����͒T���Ă�����̂ł͂Ȃ��B�Ȃ��Ȃ�ŏ���contains�Ŕ��肳��Ă���B
    } else if( right != null) {
                // �E�m�[�h������΂��������
      if(right instanceof SplitNode)
          return findNode( (SplitNode)right, c, stack ); //�ċA
          //�����ŉ��܂ŗ�����̂�(�ȉ���)
    }
    if(stack.empty()) return null; //�X�^�b�N����Ȃ猟���I��
    //�c�肪����Ȃ�A�X�^�b�N���牺�낵�A�������s
    return findNode( stack.pop(), c, stack ); //�ċA
  }
  
  //�f�o�C�_���B��
  void hideDivider() {
    setDividerSize(1);
  }
 
  //�f�o�C�_���o��
  void showDivider() {
    setDividerSize( SplitPane.DIVIDER_SIZE );
  }
  
  
  /**
   * �w�肳�ꂽ�R���|�[�l���gc�����̃m�[�h�Ɋ܂܂�Ă���Ƃ���true��Ԃ��B
   * ���w�̃m�[�h�܂ł͌��o���Ȃ��B
   */
  private boolean contain(Component c) {
    return getTopComponent() == c || getBottomComponent() == c;
  }

  // �o�^����Ă���m�[�h�̐���Ԃ��B0�`2�܂ł̒l���Ԃ�B
  private int getNodeCount() {
    int i=0;
    if(leftComponent != null) i++;
    if(rightComponent != null) i++;
    return i;
  }
  /**
   * ���̃m�[�h�ɓo�^����Ă���R���|�[�l���g���폜����B�����ł͒P����
   * <pre>
   * setBottomComponent(null);
   * setTopComponent(null);
   * </pre>
   * �Ƃ��Ă邾�������A�X�[�p�[�N���X��removeAll()�Ƃ͔����ɐU�镑�����قȂ�A
   * ���̃m�[�h�̎q��S�č폜�������ꍇ�͂��̃��\�b�h���ĂԂ��ƁB
   */
  public void removeAll() {
    setBottomComponent(null);
    setTopComponent(null);
  }
  /**
   * ��̎q�̕�����\����Ԃ��B
   */
  public String toString() {
    return "( " + getTopComponent() + ", " + getBottomComponent() + " )";
  }
}
