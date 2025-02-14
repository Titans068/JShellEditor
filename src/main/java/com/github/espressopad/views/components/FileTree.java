package com.github.espressopad.views.components;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileTree extends JTree {
    private final DefaultTreeModel defaultTreeModel;
    private final File dir;

    public FileTree(File dir) {
        this.dir = dir;
        this.defaultTreeModel = new DefaultTreeModel(this.addNodes(null, dir));
        this.setLayout(new BorderLayout());

        // Make a tree list with all the nodes, and make it a JTree
        this.setModel(this.defaultTreeModel);

        // Lastly, put the JTree into a JScrollPane.
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.getViewport().add(this);
        this.setCellRenderer(new FileTreeCellRenderer());
    }

    /**
     * Add nodes from under "dir" into curTop. Highly recursive.
     */
    private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
        String curPath = dir.getPath();
        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
        // should only be null at root
        if (curTop != null)
            curTop.add(curDir);
        String[] ol = Arrays.stream(Objects.requireNonNullElse(dir.list(), new String[0]))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toArray(String[]::new);
        File f;
        List<String> files = new ArrayList<>();
        // Make two passes, one for Dirs and one for Files. This is #1.
        for (String thisObject : ol) {
            String newPath;
            if (curPath.equals("."))
                newPath = thisObject;
            else
                newPath = Path.of(curPath, thisObject).toString();
            if ((f = new File(newPath)).isDirectory())
                this.addNodes(curDir, f);
            else
                files.add(thisObject);
        }
        // Pass two: for files.
        for (String file : files)
            curDir.add(new DefaultMutableTreeNode(file));
        return curDir;
    }

    @Override
    public DefaultTreeModel getModel() {
        return this.defaultTreeModel;
    }

    public void refreshTree() {
        this.setModel(null);
        this.setModel(new DefaultTreeModel(this.addNodes(null, this.dir)));
        this.setCellRenderer(new FileTreeCellRenderer());
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(200, 400);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {

        private final FileSystemView fileSystemView;

        private final JLabel label;

        FileTreeCellRenderer() {
            this.label = new JLabel();
            this.label.setOpaque(true);
            this.fileSystemView = FileSystemView.getFileSystemView();
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            String userObject = String.valueOf(((DefaultMutableTreeNode) value).getUserObject());
            if (!leaf)
                this.setText(new File(userObject).getName());
            this.setToolTipText(userObject);
            return this;
        }
    }
}
