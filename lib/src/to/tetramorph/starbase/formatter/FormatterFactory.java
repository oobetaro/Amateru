/*
 * FormatterFactory.java
 *
 * Created on 2006/06/02, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.formatter;

import javax.swing.JFormattedTextField;

/**
 * JFormattedTextFieldに自前のフォーマッタをセットするためのラッパークラス。
 * NetBeansのGUIデザイナを使わないのであれば、このようなクラスは本来必要ない。
 * GUIデザイナは最初に必ず引数無しのコンストラクタでウィジェトのインスタンスを作成
 * し、後からメソッドによって必要なパラメターを設定することが前提になっている。
 * <br>
 * JFormattedTextField用に自前で用意した日付や時刻のフォーマッタがたくさんあり、
 * それらはJFormattedTextField.AbstractFormatterをextendsして作成されたものだ。
 * 普通ならそれらのフォーマッタをコンストラクタで指定してJFormattedTextFieldの
 * インスタンスを作ればよいのだが、GUIデザイナの制約から指定することができない。
 * <br>
 * ではメソッドによってフォーマッタを後から設定してやれば良いかというと、
 * JFormattedTextField#setFormatter()はprotectedなので、
 * そのままでは同.AbstractFormatterをこのメソッドでセットすることはできない。
 * 子クラスを作りsetFormatterをオーバーライドすればよさそうだが、
 * 実際にやってみるとsetFormatter呼び出しと共にスタックオーバーフローのエラーを
 * 出して設定することができない。
 * <br>
 * そこでJFormattedTextField#setFormatterFactory()を使ってセットする方法を
 * ためしてみたらうまくいった。
 * <br>
 * このクラスのコンストラクタに自前のフォーマッタを与えてインスタンスを作り、
 * それをJFormattedTextField#setFormatterFactory(..)を使ってセットする。
 * <pre>
 * 例
 * JFormattedTextField date;
 * date.setFormatterFactory(new FormatterFactory(new MyDateFormatter()));
 * </pre>
 * @author 大澤義鷹
 */
public class FormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
  private JFormattedTextField.AbstractFormatter fmt;
  /**
   * 自前のFormatterを与えて、インスタンスを作成する。
   * @param fmt JFormattedTextField.AbstractFormatterをextendsして作成したフォーマッタ。
   */
  public FormatterFactory(JFormattedTextField.AbstractFormatter fmt) {
    this.fmt = fmt;
  }
  /**
   * JFormattedTextField.AbstractFormatterFactory#getFormatterの実装で
   * コンストラクタで与えられたAbstractFormatterのインスタンスを返す。
   * @param tf プログラマは意識する必要なし。
   * @return フォーマッタを返す。
   */
  public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
    return fmt;
  }
}
