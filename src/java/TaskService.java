/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author erdigurbuz
 */
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import org.primefaces.component.column.Column;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.treetable.TreeTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@Named("taskService")
@SessionScoped
public class TaskService implements Serializable {

    private TreeNode root = new DefaultTreeNode(new Task());
    private TreeNode selectedTask;
    private int keyCode;

    @PostConstruct
    public void init() {
        DefaultTreeNode newTask = new DefaultTreeNode(new Task("", "0", true, false, true));
        root.getChildren().add(newTask);
        selectedTask = newTask;
        RequestContext.getCurrentInstance().update("tasksForm:tasks");

    }

    private void setStatusTreeNode(TreeNode node, String type, Boolean t) {
        if (type.equals("check")) {
            node.setSelected(t);
        } else {
            node.setExpanded(t);
        }

        if (type.equals("check")) {
            ((Task) node.getData()).setCompleted(t);
        } else {
            ((Task) node.getData()).setExpand(t);
        }
    }

    private void setCheckTreeNodesRecursively(TreeNode node, Boolean check) {
        for (TreeNode treeNode : node.getChildren()) {
            if (!treeNode.isLeaf()) {
                setCheckTreeNodesRecursively(treeNode, check);
            }
            treeNode.setSelected(check);
            ((Task) treeNode.getData()).setCompleted(check);
        }
        node.setSelected(check);
        ((Task) node.getData()).setCompleted(check);

    }

    public TreeNode getRoot() {
        return root;
    }

    public void setSelectedTask(AjaxBehaviorEvent event) {
        TreeTable selectedTreeTable = (TreeTable) ((Column) ((InputTextarea) event.getSource()).getParent()).getParent();
        selectedTask = selectedTreeTable.getRowNode();
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        TreeNode node = event.getTreeNode();
        if (!node.isLeaf()) {
            setCheckTreeNodesRecursively(node, false);
        } else {
            setStatusTreeNode(node, "check", false);
        }

        selectedTask = node;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        if (!node.isLeaf()) {
            setCheckTreeNodesRecursively(node, true);
        } else {
            setStatusTreeNode(node, "check", true);
        }

        selectedTask = node;
    }

    public void nodeExpand(NodeExpandEvent event) {
        TreeNode node = event.getTreeNode();
        setStatusTreeNode(node, "expand", true);
    }

    public void nodeCollapse(NodeCollapseEvent event) {
        TreeNode node = event.getTreeNode();
        setStatusTreeNode(node, "expand", false);
    }

    public String getSelectedRowKey() {
        if (selectedTask == null) {
            selectedTask = root.getChildren().get(0);
        }
        return selectedTask.getRowKey();
    }

    private void setNewRowkey(TreeNode node) {
        for (TreeNode treeNode : node.getChildren()) {
            if (!treeNode.isLeaf()) {
                setNewRowkey(treeNode);
            }
            ((Task) treeNode.getData()).setRowKey(treeNode.getRowKey());
            //Parent'lar son duruma gore ayarlaniyor
            if (!treeNode.getRowKey().contains("_")) {
                ((Task) treeNode.getData()).setParent(true);
            } else {
                ((Task) treeNode.getData()).setParent(false);
            }
        }
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void taskInputListener() {
        switch (keyCode) {
            //Enter
            case 13:
                addNewTask(false);
                break;

            //Tab
            case 9:
                indentTask();
                break;

            //Alt
            case 18:
                outdentTask();
                break;

            //Up
            case 38:
                upTaskArrow();
                break;

            //Ctrl
            case 40:
                downTaskArrow();
                break;

            default:
                break;
        }
    }

    public void upTaskArrow() {
        TreeNode parent = selectedTask.getParent();
        int lastIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
        if (lastIndex == 0) {
            selectedTask = parent;
        } else {
            selectedTask = parent.getChildren().get(lastIndex - 1);
        }

        RequestContext.getCurrentInstance().update("tasksForm:tasks");
    }

    public void downTaskArrow() {
        TreeNode parent = selectedTask.getParent();
        int lastIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);

        if (selectedTask.isLeaf()) {
            try {
                selectedTask = parent.getChildren().get(lastIndex + 1);
            } catch (IndexOutOfBoundsException ex) {
            }
        } else {
            selectedTask = selectedTask.getChildren().get(0);
        }

        RequestContext.getCurrentInstance().update("tasksForm:tasks");
    }

    public void upTask() {
        TreeNode parent = selectedTask.getParent();
        int selectedTaskIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
        int lastIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
        if (lastIndex == 0) {
            if (!parent.getRowKey().equals("root")) {
                int parentTaskIndex = Integer.parseInt(parent.getRowKey().split("\\_")[parent.getRowKey().split("\\_").length - 1]);
                parent.getParent().getChildren().add(parentTaskIndex, selectedTask);
            }
        } else {
            parent.getChildren().add(selectedTaskIndex - 1, selectedTask);
        }

        RequestContext.getCurrentInstance().update("tasksForm:tasks");
        setNewRowkey(root);
    }

    public void downTask() {
        TreeNode parent = selectedTask.getParent();
        int selectedTaskIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
        int lastIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);

        if (lastIndex == parent.getChildCount() - 1) {
            if (!parent.getRowKey().equals("root")) {
                int parentTaskIndex = Integer.parseInt(parent.getRowKey().split("\\_")[parent.getRowKey().split("\\_").length - 1]);
                parent.getParent().getChildren().add(parentTaskIndex + 1, selectedTask);
            }
        } else {
            parent.getChildren().add(selectedTaskIndex + 1, selectedTask);
        }

        RequestContext.getCurrentInstance().update("tasksForm:tasks");
        setNewRowkey(root);
    }

    public void addNewTask(Boolean mainTask) {
        setNewRowkey(root);

        if (mainTask) {
            DefaultTreeNode newTask = new DefaultTreeNode(new Task("", "0", true, false, true));
            root.getChildren().add(root.getChildCount(), newTask);
            selectedTask = newTask;
        } else if (selectedTask == null) {
            DefaultTreeNode newTask = new DefaultTreeNode(new Task("", "0", true, false, true));
            root.getChildren().add(0, newTask);
            selectedTask = newTask;
        } else {
            DefaultTreeNode newTask = new DefaultTreeNode(new Task("", "0", false, false, true));

            if (!selectedTask.getRowKey().equals("root")) {
                int selectedTaskIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);

                if (!selectedTask.isLeaf()) {
                    selectedTask.setExpanded(true);
                    selectedTask.getChildren().add(0, newTask);
                } else {
                    selectedTask.setExpanded(true);
                    try {
                        selectedTask.getParent().getChildren().add(selectedTaskIndex + 1, newTask);
                    } catch (Exception ex) {

                    }
                }
                selectedTask = newTask;
            }
        }
        RequestContext.getCurrentInstance().update("tasksForm:tasks");
        setNewRowkey(root);
    }

    public void removeSelectedTask(Boolean checkLength) {
        if (selectedTask != null) {
            if (checkLength && ((Task) selectedTask.getData()).getValue().length() != 0) {
                return;
            }

            TreeNode parent = selectedTask.getParent();
            int lastIndex = 0;
            try {
                lastIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
            } catch (NumberFormatException ex) {
                return;
            }

            parent.getChildren().addAll(lastIndex, selectedTask.getChildren());
            parent.getChildren().remove(selectedTask);
            if (parent.isLeaf()) {
                selectedTask = parent;
            } else if (lastIndex == 0) {
                selectedTask = parent;
            } else {
                try {
                    selectedTask = parent.getChildren().get(lastIndex);
                } catch (Exception ex) {
                    try {
                        selectedTask = parent.getChildren().get(lastIndex - 1);
                    } catch (Exception e) {
                        selectedTask = null;
                    }
                }
            }

            if (root.isLeaf()) {
                DefaultTreeNode newTask = new DefaultTreeNode(new Task("", "0", true, false, true));
                root.getChildren().add(newTask);
                selectedTask = newTask;
            }
            RequestContext.getCurrentInstance().update("tasksForm:tasks");
            setNewRowkey(root);
        }
    }

    public void indentTask() {
        try {
            int selectedTaskIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
            int count = 0;

            TreeNode aboveNode = null;
            for (TreeNode node : selectedTask.getParent().getChildren()) {
                if (count == selectedTaskIndex - 1) {
                    aboveNode = node;
                }
                count++;
            }
            aboveNode.getChildren().add(selectedTask);
            ((Task) aboveNode.getData()).setExpand(true);
            aboveNode.setExpanded(true);

            RequestContext.getCurrentInstance().update("tasksForm:tasks");
            setNewRowkey(root);
        } catch (Exception ex) {
        }
    }

    public void outdentTask() {
        try {
            TreeNode parent = selectedTask.getParent();
            int selectedTaskIndex = Integer.parseInt(selectedTask.getRowKey().split("\\_")[selectedTask.getRowKey().split("\\_").length - 1]);
            if (parent.getChildCount() - 1 > selectedTaskIndex) {
                int parentTaskIndex = Integer.parseInt(parent.getRowKey().split("\\_")[parent.getRowKey().split("\\_").length - 1]);
                parent.getParent().getChildren().add(parentTaskIndex + 1, selectedTask);
                int childCount = parent.getChildCount();
                for (int i = selectedTaskIndex; i < childCount; i++) {
                    selectedTask.getChildren().add(parent.getChildren().get(selectedTaskIndex));
                }

            } else {
                int newTaskIndex = Integer.parseInt(parent.getRowKey().split("\\_")[parent.getRowKey().split("\\_").length - 1]) + 1;
                selectedTask.getParent().getParent().getChildren().add(newTaskIndex, selectedTask);

            }
            RequestContext.getCurrentInstance().update("tasksForm:tasks");
            setNewRowkey(root);
        } catch (Exception ex) {
        }
    }
}
