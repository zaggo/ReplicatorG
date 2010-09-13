package replicatorg.app.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import replicatorg.drivers.OnboardParameters;
import replicatorg.drivers.Driver;

public class ExtruderOnboardParameters extends JFrame {
	private static final long serialVersionUID = 6353987389397209816L;
	private OnboardParameters target;
	
	class ThermistorTablePanel extends JPanel {
		private static final long serialVersionUID = 7765098486598830410L;
		private JTextField betaField = new JTextField();
		private JTextField r0Field = new JTextField();
		private JTextField t0Field = new JTextField();
		private int which;
		ThermistorTablePanel(int which, String titleText) {
			super(new MigLayout());
			this.which = which;
			setBorder(BorderFactory.createTitledBorder(titleText));
			final int FIELD_WIDTH = 20;
			betaField.setColumns(FIELD_WIDTH);
			r0Field.setColumns(FIELD_WIDTH);
			t0Field.setColumns(FIELD_WIDTH);
			
			double beta = target.getBeta(which);
			if (beta == -1) { beta = 4066; }
			betaField.setText(Integer.toString((int)beta));
			add(new JLabel("Beta"));
			add(betaField,"wrap");

			double r0 = target.getR0(which);
			if (r0 == -1) { r0 = 100000; }
			r0Field.setText(Integer.toString((int)r0));
			add(new JLabel("Thermistor Resistance"));
			add(r0Field,"wrap");

			double t0 = target.getT0(which);
			if (t0 == -1) { t0 = 25; }
			t0Field.setText(Integer.toString((int)t0));
			add(new JLabel("Base Temperature"));
			add(t0Field,"wrap");
		}

		void commit() {
			int beta = Integer.parseInt(betaField.getText());
			int r0 = Integer.parseInt(r0Field.getText());
			int t0 = Integer.parseInt(t0Field.getText());
			target.createThermistorTable(which,r0,t0,beta);
		}
	}

	private void commit() {
		for (ThermistorTablePanel p : thermistorTablePanels) {
			p.commit();
		}
		backoffPanel.commit();
		pidPanel.commit();
		JOptionPane.showMessageDialog(this,
				"Changes will not take effect until the extruder board is reset.  You can \n" +
				"do this by turning your machine off and then on, or by disconnecting and \n" +
				"reconnecting the extruder cable.  Make sure you don't still have a USB2TTL \n" +
				"cable attached to the extruder controller, as the cable will keep the board \n" +
				"from resetting.",
			    "Extruder controller reminder",
			    JOptionPane.INFORMATION_MESSAGE);
	}

	private class BackoffPanel extends JPanel {
		private static final long serialVersionUID = 6593800743174557032L;
		private JTextField stopMsField = new JTextField();
		private JTextField reverseMsField = new JTextField();
		private JTextField forwardMsField = new JTextField();
		private JTextField triggerMsField = new JTextField();
		BackoffPanel() {
			setLayout(new MigLayout());
			setBorder(BorderFactory.createTitledBorder("Reversal parameters"));
			add(new JLabel("<html>These parameters effect the amount of time the extruder reverses " +
					"when it goes from a forward state to a stopped state.</html>"),
					"span");
			final int FIELD_WIDTH = 20;
			stopMsField.setColumns(FIELD_WIDTH);
			reverseMsField.setColumns(FIELD_WIDTH);
			forwardMsField.setColumns(FIELD_WIDTH);
			triggerMsField.setColumns(FIELD_WIDTH);

			add(new JLabel("Time to pause (ms)"));
			add(stopMsField,"wrap");
			add(new JLabel("Time to reverse (ms)"));
			add(reverseMsField,"wrap");
			add(new JLabel("Time to advance (ms)"));
			add(forwardMsField,"wrap");
			add(new JLabel("Min. extrusion time before reversal (ms)"));
			add(triggerMsField,"wrap");
			OnboardParameters.BackoffParameters bp = target.getBackoffParameters();
			stopMsField.setText(Integer.toString(bp.stopMs));
			reverseMsField.setText(Integer.toString(bp.reverseMs));
			forwardMsField.setText(Integer.toString(bp.forwardMs));
			triggerMsField.setText(Integer.toString(bp.triggerMs));
		}

		public void commit() {
			OnboardParameters.BackoffParameters bp = new OnboardParameters.BackoffParameters();
			bp.forwardMs = Integer.parseInt(forwardMsField.getText());
			bp.reverseMs = Integer.parseInt(reverseMsField.getText());
			bp.stopMs = Integer.parseInt(stopMsField.getText());
			bp.triggerMs = Integer.parseInt(triggerMsField.getText());
			target.setBackoffParameters(bp);
		}
	}

	BackoffPanel backoffPanel;

	private class PIDPanel extends JPanel {
		private JTextField pField = new JTextField();
		private JTextField iField = new JTextField();
		private JTextField dField = new JTextField();
		PIDPanel() {
			setLayout(new MigLayout());
			setBorder(BorderFactory.createTitledBorder("PID parameters"));
			add(new JLabel("<html>These parameters determine the behavior of the PID controller " +
					"that adjusts the temperature of the extruder.</html>"),
					"span");
			final int FIELD_WIDTH = 20;
			pField.setColumns(FIELD_WIDTH);
			iField.setColumns(FIELD_WIDTH);
			dField.setColumns(FIELD_WIDTH);

			add(new JLabel("P parameter"));
			add(pField,"wrap");
			add(new JLabel("I parameter"));
			add(iField,"wrap");
			add(new JLabel("D parameter"));
			add(dField,"wrap");
			OnboardParameters.PIDParameters pp = target.getPIDParameters();
			pField.setText(Float.toString(pp.p));
			iField.setText(Float.toString(pp.i));
			dField.setText(Float.toString(pp.d));
		}

		public void commit() {
			OnboardParameters.PIDParameters pp = new OnboardParameters.PIDParameters();
			pp.p = Float.parseFloat(pField.getText());
			pp.i = Float.parseFloat(iField.getText());
			pp.d = Float.parseFloat(dField.getText());
			target.setPIDParameters(pp);
		}
	}
	
	PIDPanel pidPanel;

	private class ZProbePanel extends JPanel {
		private JTextField disengagedField = new JTextField();
		private JTextField engagedField = new JTextField();
		ZProbePanel() {
			setLayout(new MigLayout());
			setBorder(BorderFactory.createTitledBorder("Z-Probe parameters"));
			add(new JLabel("<html>These parameters determine the behavior of the Z-Probe " +
					"that is used to home the z axis.</html>"),
					"span");
			final int FIELD_WIDTH = 20;
			disengagedField.setColumns(FIELD_WIDTH);
			engagedField.setColumns(FIELD_WIDTH);

			add(new JLabel("Disengaged Angle"));
			add(disengagedField,"wrap");
			add(new JLabel("Engaged Angle"));
			add(engagedField,"wrap");
			
			if(target instanceof Driver)
			{
				add(new JLabel("<html>You can check Z-Probe angles by entering them into the following trial field. Entries in the trial field are not saved!</html>"),
						"span");
				add(new JLabel("Angle trial"));
				JTextField angleTrial = new JTextField();
				angleTrial.setColumns(FIELD_WIDTH);
				add(angleTrial,"wrap");
				angleTrial.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JTextField source = (JTextField) e.getSource();
						((Driver)target).setZProbeAngle(Integer.parseInt(source.getText()));
						source.selectAll();
					}
				});
				angleTrial.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
					}
				
					public void focusLost(FocusEvent e) {
						JTextField source = (JTextField) e.getSource();
						((Driver)target).setZProbeAngle(Integer.parseInt(source.getText()));
						source.selectAll();
					}
				});
			}
			OnboardParameters.ZProbeParameters zp = target.getZProbeParameters();
			disengagedField.setText(Integer.toString(zp.disengagedAngle));
			engagedField.setText(Integer.toString(zp.engagedAngle));
		}

		public void commit() {
			OnboardParameters.ZProbeParameters zp = new OnboardParameters.ZProbeParameters();
			zp.disengagedAngle = Integer.parseInt(disengagedField.getText());
			if(zp.disengagedAngle<0)
				zp.disengagedAngle = 0;
			else if(zp.disengagedAngle>180)
				zp.disengagedAngle = 180;
				
			zp.engagedAngle = Integer.parseInt(engagedField.getText());
			if(zp.engagedAngle<0)
				zp.engagedAngle = 0;
			else if(zp.engagedAngle>180)
				zp.engagedAngle = 180;
			target.setZProbeParameters(zp);
		}
	}
	
	ZProbePanel zprobePanel;

	private JPanel makeButtonPanel() {
		JPanel panel = new JPanel(new MigLayout());
		JButton commitButton = new JButton("Commit Changes");
		panel.add(commitButton);
		commitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ExtruderOnboardParameters.this.commit();
				ExtruderOnboardParameters.this.dispose();				
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ExtruderOnboardParameters.this.dispose();
			}
		});
		panel.add(cancelButton);
		return panel;
	}

	private List<ThermistorTablePanel> thermistorTablePanels = new LinkedList<ThermistorTablePanel>();
	
	public ExtruderOnboardParameters(OnboardParameters target) {
		super("Update onboard machine options");
		this.target = target;

		thermistorTablePanels.add(new ThermistorTablePanel(0,"Extruder thermistor"));
		thermistorTablePanels.add(new ThermistorTablePanel(1,"Heated build platform thermistor"));

		Box panel = Box.createVerticalBox();
		for (ThermistorTablePanel p : thermistorTablePanels) {
			panel.add(p);
		}
		backoffPanel = new BackoffPanel();
		panel.add(backoffPanel);
		pidPanel = new PIDPanel();
		panel.add(pidPanel);
		zprobePanel = new ZProbePanel();
		panel.add(zprobePanel);
		panel.add(makeButtonPanel());
		add(panel);
		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getWidth()) / 2,
				(screen.height - getHeight()) / 2);

	}
}
