/*
 *
 */
package to.tetramorph.starbase.formatter;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * テキストフィールドの入力文字数に制限をかけるためのもの。
 * <pre>
 * textfield.setDocument( new LimitedDocument( 10 ) );
 * などとして使う。
 * </pre>
 * @see javax.swing.JTextField
 */
public class LimitedDocument extends PlainDocument {
  
  int limit;
  /**
   * 指定された文字数でオブジェクトを作成する。これをセットされたJTextFieldは、
   * 入力文字数に制限がかかる。
   * @see javax.swing.JTextField
   */
  public LimitedDocument( int limit ){
    this.limit = limit;
  }
  /**
   * プログラマがこのメソッドを呼び出す必要は無い。
   */
  public void insertString( int offset, String str, AttributeSet a ) {   
    if( getLength() >= limit ) return;
    try {
      super.insertString( offset, str, a );
    } catch( BadLocationException e ) {
      System.out.println( e );
    }
  }
}
