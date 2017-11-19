
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Gui extends JFrame {

    private JPanel contentPane;
    private JTextField textField;
    private JTextField textField_1;
    private JTable theTable;
    private JLabel mipsCode;
    private JLabel binaryLabel;
    private JLabel binary;
    private JLabel hexLabel;
    private JLabel hex_value;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        HexMIPSConverter.init();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Gui frame = new Gui();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Gui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel output = new JPanel();
        contentPane.add(output, BorderLayout.SOUTH);
        output.setLayout(new BoxLayout(output, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        output.add(panel);

        mipsCode = new JLabel("");
        panel.add(mipsCode);

        JPanel panel_2 = new JPanel();
        output.add(panel_2);

        binaryLabel = new JLabel("");
        panel_2.add(binaryLabel);

        binary = new JLabel("");
        panel_2.add(binary);

        JPanel panel_3 = new JPanel();
        output.add(panel_3);

        hexLabel = new JLabel("");
        panel_3.add(hexLabel);

        hex_value = new JLabel("");
        panel_3.add(hex_value);

        JPanel panel_5 = new JPanel();
        output.add(panel_5);

        theTable = new JTable();
        theTable.setBackground(UIManager.getColor("Label.background"));
        theTable.setEnabled(false);
        theTable.setGridColor(Color.BLACK);
        MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
        theTable.setBorder(border);
        panel_5.add(theTable);

        JPanel panel_1 = new JPanel();
        contentPane.add(panel_1, BorderLayout.CENTER);
        panel_1.setLayout(new GridLayout(1, 0, 0, 0));

        JPanel instruction = new JPanel();
        panel_1.add(instruction);

        JLabel lblInstructionToHex = new JLabel("Instruction to Hex");
        instruction.add(lblInstructionToHex);

        textField = new JTextField();
        instruction.add(textField);
        textField.setColumns(10);

        JButton btnNewButton = new JButton("Convert");
        instruction.add(btnNewButton);
        btnNewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String mips = textField.getText();
                ArrayList<String> table = HexMIPSConverter.mipsToHex(mips);
                if (table != null) {
                    hexLabel.setText("Hex: ");
                    binaryLabel.setText("Binary: ");
                    mipsCode.setText(mips);
                    hex_value.setText(table.get(1));
                    binary.setText(table.get(0));
                    toTable(table);
                } else {
                    error();
                }
            }
        });

        JPanel hex = new JPanel();
        panel_1.add(hex);

        JLabel lblNewLabel = new JLabel("Hex to Instruction");
        hex.add(lblNewLabel);

        textField_1 = new JTextField();
        hex.add(textField_1);
        textField_1.setColumns(10);

        JButton btnNewButton_1 = new JButton("Convert");
        hex.add(btnNewButton_1);
        btnNewButton_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String hexString = textField_1.getText();
                ArrayList<String> table = HexMIPSConverter.hexToMips(hexString);
                if (table != null) {
                    hex_value.setText(hexString);
                    hexLabel.setText("Hex: ");
                    binaryLabel.setText("Binary: ");
                    binary.setText(table.get(0));
                    mipsCode.setText(table.get(1));
                    toTable(table);
                } else {
                    error();
                }
            }
        });
    }

    public void toTable(ArrayList<String> table) {
        String data[][] = new String[2][(table.size() - 2) / 2];
        for (int i = 2; i < table.size(); i++) {
            if (i % 2 == 0) {
                data[0][i / 2 - 1] = table.get(i);
            } else {
                data[1][i / 2 - 1] = table.get(i);
            }
        }
        String cols[] = new String[data[0].length];
        DefaultTableModel model = new DefaultTableModel(data, cols);
        theTable.setModel(model);
        for(int i = 0; i < cols.length; i++){
            if(i == cols.length-1 && cols.length != 6){
                if(cols.length == 2){
                    theTable.getColumnModel().getColumn(i).setPreferredWidth(225);
                } else {
                    theTable.getColumnModel().getColumn(i).setPreferredWidth(200);
                }
            } else {
                theTable.getColumnModel().getColumn(i).setPreferredWidth(60);
            }
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment( JLabel.CENTER );
            theTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public void error() {
        mipsCode.setText("Error");
        hex_value.setText("");
        hexLabel.setText("");
        binaryLabel.setText("");
        binary.setText("");
        ((DefaultTableModel)theTable.getModel()).setRowCount(0);
    }

}
