/*
 * TreeOfLifePanel.java
 *
 * Created on 2007/10/26, 0:28
 */

package split;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author  ���V�`��
 */
public class TreeOfLifePanel extends javax.swing.JPanel {
  
  //�e�Z�t�B���̍��W (�e�B�t�@���g�����_�Ƃ���)
  Point2D [] sephiroth = {
    new Point2D.Double(      0, -0.25),  // �_�[�g
    new Point2D.Double(      0, -0.5),  // �P�e��
    new Point2D.Double( 0.2165, -0.375), // �R�N�}�[
    new Point2D.Double(-0.2165, -0.375), // �r�i�[
    new Point2D.Double( 0.2165, -0.125),//�@�P�Z�h
    new Point2D.Double(-0.2165, -0.125),// �Q�u���[
    new Point2D.Double(      0,  0),    // �e�B�t�@���g
    new Point2D.Double( 0.2165,  0.125),// �l�c�A�N
    new Point2D.Double(-0.2165,  0.125),// �z�h
    new Point2D.Double(      0,  0.25), // �C�G�\�h
    new Point2D.Double(      0,  0.5),  // �}���N�g
  };
  //�Z�t�B�������ԃp�X (�ԍ����ɂȂ��ł���)
  Point2D [][] paths = {
    {  },
    { sephiroth[1],sephiroth[2] },
    { sephiroth[1],sephiroth[3] }, // 2
    { sephiroth[1],sephiroth[6] }, // 3
    { sephiroth[2],sephiroth[3] }, // 4
    { sephiroth[2],sephiroth[6] }, // 5
    { sephiroth[2],sephiroth[4] }, // 6
    { sephiroth[3],sephiroth[6] }, // 7
    { sephiroth[3],sephiroth[5] }, // 8
    { sephiroth[4],sephiroth[5] }, // 9
    { sephiroth[4],sephiroth[6] }, // 10
    { sephiroth[4],sephiroth[7] }, // 11
    { sephiroth[5],sephiroth[6] }, // 12
    { sephiroth[5],sephiroth[8] }, // 13
    { sephiroth[6],sephiroth[7] }, // 14
    { sephiroth[6],sephiroth[9] }, // 15
    { sephiroth[6],sephiroth[8] }, // 16
    { sephiroth[7],sephiroth[8] }, // 17
    { sephiroth[7],sephiroth[9] }, // 18
    { sephiroth[7],sephiroth[10] },// 19
    { sephiroth[8],sephiroth[9]  }, // 20
    { sephiroth[8],sephiroth[10] },// 21
    { sephiroth[9],sephiroth[10] },// 22
  };
  //�Z�t�B���̉~��Shape��ۊǂ���z��B(�e���|����)
  Ellipse2D [] sephiraDisks = new Ellipse2D[11];
  
  /** Creates new form TreeOfLife */
  public TreeOfLifePanel() {
    initComponents();
  }
  int num = 0;
  public void setNumber(int num) {
    this.num = num;
  }
  public String toString() {
    return "No."+num;
  }
  //���̃p�l���̐�Έʒu�ƃT�C�Y��ۊǂ��邽�߂̕ϐ�
  protected Rectangle glassBounds = new Rectangle();
  protected boolean onCursor = false;
  
  @Override
  public void paintComponent(Graphics graphics) {
    Graphics2D g = (Graphics2D)graphics;
    g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    Rectangle rect = getBounds();
    Dimension size = getSize();
    g.setPaint(this.getBackground());
    //g.fillRect(0,0,size.width-1,size.height-1);
    draw(g,rect.width,rect.height);
    g.drawString("No." + num,8,20);
  }
  
  private void draw(Graphics2D g,double width,double height) {
    double treeHeight = height * 0.85;
    double treeWidth = treeHeight * 0.55; //0.433;
    if(treeWidth > width) {
      treeWidth = width * 0.7;
      treeHeight = treeWidth * 2.3;
    }
    double zx = width / 2d;
    double zy = height / 2d;
    double diskWidth = treeHeight * 0.1;
    double ofs = diskWidth / 2d;
    float strk0 = 1f; //(float)(treeHeight * 0.0035);
    float strk1 = (float)(treeHeight * 0.025);
    if(strk1 < 4) strk1 = 4f;
    float strk2 = strk1 - 2f;
    Stroke stroke0 = new BasicStroke(strk0);
    Stroke stroke1 = new BasicStroke(strk1);
    Stroke stroke2 = new BasicStroke(strk2);
    Stroke brokenStroke = new BasicStroke(1f,BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_BEVEL,1f,new float [] {3f,4f,3f,4f},0f);
    Line2D line = new Line2D.Double();
    for(int i=1; i<paths.length; i++) {
      line.setLine(
        paths[i][0].getX() * treeHeight + zx,
        paths[i][0].getY() * treeHeight + zy,
        paths[i][1].getX() * treeHeight + zx,
        paths[i][1].getY() * treeHeight + zy);
      g.setPaint(Color.BLACK);
      g.setStroke(stroke1);
      g.draw(line);
      g.setPaint(Color.WHITE);
      g.setStroke(stroke2);
      g.draw(line);
    }
    Ellipse2D sephiraEllipse = new Ellipse2D.Double();
    for(int i=0; i<sephiroth.length; i++) {
      double x = treeHeight * sephiroth[i].getX() + zx;
      double y = treeHeight * sephiroth[i].getY() + zy;
      sephiraEllipse.setFrame( x-ofs,y-ofs,diskWidth,diskWidth );
      if(i==0) { //�_�[�g��p
        g.setStroke(brokenStroke);
        g.setPaint(Color.BLACK);
        g.draw(sephiraEllipse);
      } else {
        g.setPaint(Color.WHITE);
        g.fill(sephiraEllipse);
        g.setStroke(stroke0);
        g.setPaint(Color.BLACK);
        g.draw(sephiraEllipse);
      }
    }
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setOpaque(false);
    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 300, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
  
}
