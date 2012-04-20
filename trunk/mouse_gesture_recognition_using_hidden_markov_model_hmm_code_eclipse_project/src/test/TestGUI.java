/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package test;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.gt.gesture.features.GestureFeatureExtractor;
import com.gt.gesture.features.RawFeature;
import com.gt.gesture.mouseCapture.DataCapturePanel;

/**
 * 
 * @author Ganesh
 * 
 */
public class TestGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	DataCapturePanel dcp = new DataCapturePanel();
	private JButton jButton = null;
	private JButton repaintBTN = null;

	/**
	 * This method initializes repaintBTN
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getRepaintBTN() {
		if (repaintBTN == null) {
			repaintBTN = new JButton();
			repaintBTN.setText("Replay");
			repaintBTN.setBounds(new Rectangle(534, 142, 119, 24));
			repaintBTN.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("animate call");
					// copy values
					RawFeature param = null;
					try {
						param = (RawFeature) dcp.getCapturedRawFeature().clone();
					} catch (CloneNotSupportedException e1) {
						System.out.println(e1.toString());
					}
					dcp.animateCaptured(param);
				}
			});
		}
		return repaintBTN;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TestGUI thisClass = new TestGUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public TestGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		dcp.setBounds(new Rectangle(10, 8, 379, 237));
		this.setSize(691, 309);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(dcp, null);
			jContentPane.add(getRepaintBTN(), null);

			JButton extractFeatureBTN = new JButton("Extract Feature");
			extractFeatureBTN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					GestureFeatureExtractor gfe = new GestureFeatureExtractor((RawFeature) dcp.getCapturedRawFeature());
				}
			});
			extractFeatureBTN.setBounds(534, 177, 119, 23);
			jContentPane.add(extractFeatureBTN);
		}
		return jContentPane;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
