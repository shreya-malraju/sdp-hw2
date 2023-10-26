// import necessary packages

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

// table app is the update table use case
//it extends JFrame
public class TableApp extends JFrame {

    // initialize
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton undoButton;
    private JButton redoButton;

    private CommandController commandController = new CommandController();
    private Stack<Command> history = new Stack<>();
    private Stack<Command> redoHistory = new Stack<>();

    // main method
    public TableApp() {
        setTitle("Table App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableModel = new DefaultTableModel(new String[] { "ID", "Content" }, 0);
        table = new JTable(tableModel);

        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");

        Command addCommand = new AddCommand(tableModel);
        Command deleteCommand = new DeleteCommand(table, tableModel);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);

        // action listener for add

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandController.execute(addCommand);
                history.push(addCommand);
                undoButton.setEnabled(true);
                redoButton.setEnabled(false);
            }
        });
        // action listener for delete

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    commandController.execute(deleteCommand);
                    history.push(deleteCommand);
                    undoButton.setEnabled(true);
                    redoButton.setEnabled(false);
                }
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!history.isEmpty()) {
                    Command lastCommand = history.pop();
                    lastCommand.undo();
                    redoHistory.push(lastCommand);
                    undoButton.setEnabled(!history.isEmpty());
                    redoButton.setEnabled(true);
                }
            }
        });

        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!redoHistory.isEmpty()) {
                    Command lastUndoneCommand = redoHistory.pop();
                    lastUndoneCommand.redo();
                    history.push(lastUndoneCommand);
                    undoButton.setEnabled(true);
                    redoButton.setEnabled(!redoHistory.isEmpty());
                }
            }
        });
//panel for adding buttons

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(buttonPanel, BorderLayout.SOUTH);
        containerPanel.add(tablePanel, BorderLayout.CENTER);

        getContentPane().add(containerPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableApp app = new TableApp();
                app.setVisible(true);
            }
        });
    }
}
//command interface
//it has undo and redo methods

interface Command {
    void execute();
    void undo();
    void redo();
}

//add command implements command interface
class AddCommand implements Command {
    private DefaultTableModel tableModel;
    private int lastRow;
    private Object[] rowData;

    public AddCommand(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void execute() {
        int id = (int) (Math.random() * 1000);
        String content = "Item " + id;
        rowData = new Object[] { id, content };
        lastRow = tableModel.getRowCount();
        tableModel.addRow(rowData);
    }

    @Override
    public void undo() {
        if (lastRow != -1 && lastRow < tableModel.getRowCount()) {
            tableModel.removeRow(lastRow);
        }
    }

    @Override
    public void redo() {
        if (rowData != null) {
            tableModel.addRow(rowData);
        }
    }
}
//delete command implements command interface
class DeleteCommand implements Command {
    private JTable table;
    private DefaultTableModel tableModel;
    private Object[] deletedRow;
    private int deletedRowIndex;

    public DeleteCommand(JTable table, DefaultTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
    }

    @Override
    public void execute() {
        deletedRowIndex = table.getSelectedRow();
        if (deletedRowIndex != -1) {
            deletedRow = new Object[2];
            deletedRow[0] = tableModel.getValueAt(deletedRowIndex, 0);
            deletedRow[1] = tableModel.getValueAt(deletedRowIndex, 1);
            tableModel.removeRow(deletedRowIndex);
        }
    }

    @Override
    public void undo() {
        if (deletedRow != null) {
            tableModel.insertRow(deletedRowIndex, deletedRow);
        }
    }

    @Override
    public void redo() {
        if (deletedRowIndex != -1) {
            deletedRow = new Object[2];
            deletedRow[0] = tableModel.getValueAt(deletedRowIndex, 0);
            deletedRow[1] = tableModel.getValueAt(deletedRowIndex, 1);
            tableModel.removeRow(deletedRowIndex);
        }
    }
}
//to start execution of the command

class CommandController {
    public void execute(Command command) {
        command.execute();
    }
}
