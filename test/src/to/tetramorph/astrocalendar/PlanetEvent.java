
package to.tetramorph.astrocalendar;

import java.util.*;
/**
 * �����������f���̃C���O���X�̎�����A�t�s�⏇�s�ɓ]���鎞����ۊǂ���N���X�B
 */
class PlanetEvent {
  int sign;
  int state;
  int planet;
  Calendar date;
  /**
   * ���t�A�V�̔ԍ�(SweConst�ɏ�����)�A
   * state��0,1�Ȃ炱�̃f�[�^���C���O���X�̎�����\�����Ă��āA1�Ȃ�t�s�ł��邱�Ƃ�\���B
   * state��2,3�Ȃ炱�̃f�[�^���t�s���s�̐ؑ֎�����\�����Ă��āA
   * 3�Ȃ�t�s��2�Ȃ珇�s�ɓ]��������\���B
   * sign�͂��̎��̐����T�C����\���B
   *
   * ���̃N���X�̓t�B�[���h�ϐ��ɒ��ڃA�N�Z�X���Ďg���B
   */
  public PlanetEvent(Calendar date,int planet,int state,int sign) {
    this.date = date;
    this.state = state;
    this.sign = sign;
    this.planet = planet;
  }
}
