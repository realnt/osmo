package osmo.miner;

import osmo.miner.gui.mainform.MainForm;

import javax.swing.UIManager;

/**
 * @author Teemu Kanstren
 */
public class Main {
  public static void main(String[] args) throws Exception {
    new Main().main2();
  }

  public void main2() throws Exception {
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      // if Nimbus does not exist, you are out of luck
      System.out.println("No Nimbus look and feel found. The GUI is not going to be pretty now..");
    }
    MainForm form = new MainForm();

  }
}
