package tictactoe;

import javax.swing.*;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.TRAILING;

public class WelcomePage extends JPanel {
    private JLabel ipLabel   = new JLabel("IP:");
    private JLabel portLabel = new JLabel("Port:");

    private JTextField ipField   = new JTextField("127.0.0.1", 15);
    private JTextField portField = new JTextField("8888");

    private JButton enterButton = new JButton("Enter");

    public WelcomePage() {
        this.initUI();
    }

    private void initUI() {
        GroupLayout gl = new GroupLayout(this);
        this.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup(TRAILING)
                            .addComponent(ipLabel)
                            .addComponent(portLabel)
                            .addComponent(enterButton))
                    .addGroup(gl.createParallelGroup()
                            .addComponent(ipField)
                            .addComponent(portField))
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(BASELINE)
                        .addComponent(ipLabel)
                        .addComponent(ipField))
                .addGroup(gl.createParallelGroup(BASELINE)
                        .addComponent(portLabel)
                        .addComponent(portField))
                .addComponent(enterButton)
        );

    }

}
