package com.ingenieur.andyelderscrolls.utils;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.ingenieur.andyelderscrolls.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BsaTreeFragment extends DialogFragment implements TreeViewAdapter.OnTreeNodeClickListener, TreeViewAdapter.OnTreeNodeLongClickListener {

    private TreeViewAdapter treeViewAdapter;

    /**
     * Custom OnClickListener to be invoked when a TreeNode has been clicked.
     */
    private TreeViewAdapter.OnTreeNodeClickListener treeNodeClickListener;

    /**
     * Custom OnLongClickListener to be invoked when a TreeNode has been clicked and hold.
     */
    private TreeViewAdapter.OnTreeNodeLongClickListener treeNodeLongClickListener;

    private static final String TAG = "BsaTreeFragment";

    // if we are not set up hang on the the nodes the users wants loaded
    private List<TreeNode> nodesToLoad;


    public BsaTreeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_tree, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        TreeViewHolderFactory factory = (v, layout) -> new BsaNodeViewHolder(v);

        treeViewAdapter = new TreeViewAdapter(factory);
        recyclerView.setAdapter(treeViewAdapter);

      /*  TreeNode javaDirectory = new TreeNode("Java", R.layout.list_item_file);
        javaDirectory.addChild(new TreeNode("FileJava1.java", R.layout.list_item_file));
        javaDirectory.addChild(new TreeNode("FileJava2.java", R.layout.list_item_file));
        javaDirectory.addChild(new TreeNode("FileJava3.java", R.layout.list_item_file));

        TreeNode gradleDirectory = new TreeNode("Gradle", R.layout.list_item_file);
        gradleDirectory.addChild(new TreeNode("FileGradle1.gradle", R.layout.list_item_file));
        gradleDirectory.addChild(new TreeNode("FileGradle2.gradle", R.layout.list_item_file));
        gradleDirectory.addChild(new TreeNode("FileGradle3.gradle", R.layout.list_item_file));

        javaDirectory.addChild(gradleDirectory);

        TreeNode lowLevelRoot = new TreeNode("LowLevel", R.layout.list_item_file);

        TreeNode cDirectory = new TreeNode("C", R.layout.list_item_file);
        cDirectory.addChild(new TreeNode("FileC1.c", R.layout.list_item_file));
        cDirectory.addChild(new TreeNode("FileC2.c", R.layout.list_item_file));
        cDirectory.addChild(new TreeNode("FileC3.c", R.layout.list_item_file));

        TreeNode cppDirectory = new TreeNode("Cpp", R.layout.list_item_file);
        cppDirectory.addChild(new TreeNode("FileCpp1.cpp", R.layout.list_item_file));
        cppDirectory.addChild(new TreeNode("FileCpp2.cpp", R.layout.list_item_file));
        cppDirectory.addChild(new TreeNode("FileCpp3.cpp", R.layout.list_item_file));

        TreeNode goDirectory = new TreeNode("Go", R.layout.list_item_file);
        goDirectory.addChild(new TreeNode("FileGo1.go", R.layout.list_item_file));
        goDirectory.addChild(new TreeNode("FileGo2.go", R.layout.list_item_file));
        goDirectory.addChild(new TreeNode("FileGo3.go", R.layout.list_item_file));

        lowLevelRoot.addChild(cDirectory);
        lowLevelRoot.addChild(cppDirectory);
        lowLevelRoot.addChild(goDirectory);

        TreeNode cSharpDirectory = new TreeNode("C#", R.layout.list_item_file);
        cSharpDirectory.addChild(new TreeNode("FileCs1.cs", R.layout.list_item_file));
        cSharpDirectory.addChild(new TreeNode("FileCs2.cs", R.layout.list_item_file));
        cSharpDirectory.addChild(new TreeNode("FileCs3.cs", R.layout.list_item_file));

        TreeNode gitFolder = new TreeNode(".git", R.layout.list_item_file);

        List<TreeNode> fileRoots = new ArrayList<>();
        fileRoots.add(javaDirectory);
        fileRoots.add(lowLevelRoot);
        fileRoots.add(cSharpDirectory);
        fileRoots.add(gitFolder);

        treeViewAdapter.updateTreeNodes(fileRoots);

       */

        if(nodesToLoad != null) {
            treeViewAdapter.updateTreeNodes(nodesToLoad);
            nodesToLoad = null;
        }

        treeViewAdapter.setTreeNodeClickListener(this);
        treeViewAdapter.setTreeNodeLongClickListener(this);

        return view;
    }

    public void onTreeNodeClick(TreeNode treeNode, View view) {
        this.treeNodeClickListener.onTreeNodeClick(treeNode, view);
    }

    public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
        this.treeNodeLongClickListener.onTreeNodeLongClick(treeNode, view);
        this.dismiss();
        return false;
    }

    /**
     * Register a callback to be invoked when this TreeNode is clicked
     *
     * @param listener The callback that will run
     */
    public void setTreeNodeClickListener(TreeViewAdapter.OnTreeNodeClickListener listener) {
        this.treeNodeClickListener = listener;
    }

    /**
     * Register a callback to be invoked when this TreeNode is clicked and held
     *
     * @param listener The callback that will run
     */
    public void setTreeNodeLongClickListener(TreeViewAdapter.OnTreeNodeLongClickListener listener) {
        this.treeNodeLongClickListener = listener;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int menuId = item.getItemId();
        if (menuId == R.id.expand_all_action) {
            treeViewAdapter.expandAll();
        } else if (menuId == R.id.collapse_all_action) {
            treeViewAdapter.collapseAll();
        } else if (menuId == R.id.expand_selected_action) {
            treeViewAdapter.expandNode(treeViewAdapter.getSelectedNode());
        } else if (menuId == R.id.collapse_selected_action) {
            treeViewAdapter.collapseNode(treeViewAdapter.getSelectedNode());
        } else if (menuId == R.id.expand_selected_branch_action) {
            treeViewAdapter.expandNodeBranch(treeViewAdapter.getSelectedNode());
        } else if (menuId == R.id.collapse_selected_branch_action) {
            treeViewAdapter.collapseNodeBranch(treeViewAdapter.getSelectedNode());
        } else if (menuId == R.id.expand_selected_level_action) {
            treeViewAdapter.expandNodesAtLevel(2);
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ExtensionTable {

        public static int getExtensionIcon(String extension) {
            switch (extension) {
                case ".c":
                    return R.drawable.ic_c;
                case ".cpp":
                    return R.drawable.ic_cpp;
                case ".cs":
                    return R.drawable.ic_cs;
                case ".git":
                    return R.drawable.ic_git;
                case ".go":
                    return R.drawable.ic_go;
                case ".gradle":
                    return R.drawable.ic_gradle;
                case ".java":
                    return R.drawable.ic_java;
                default:
                    return R.drawable.ic_file;
            }
        }
    }
    /**
     * Update the list of tree nodes
     * @param fileRoots The new tree nodes
     */
    public void updateTreeNodes(List<TreeNode> fileRoots) {
        if(treeViewAdapter != null)
        treeViewAdapter.updateTreeNodes(fileRoots);
        else
            nodesToLoad = fileRoots;
    }

    public class BsaNodeViewHolder extends TreeViewHolder {

        private TextView fileName;
        private ImageView fileStateIcon;
        private ImageView fileTypeIcon;

        public BsaNodeViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews() {
            fileName = itemView.findViewById(R.id.file_name);
            fileStateIcon = itemView.findViewById(R.id.file_state_icon);
            fileTypeIcon = itemView.findViewById(R.id.file_type_icon);
        }

        @Override
        public void bindTreeNode(TreeNode node) {
            super.bindTreeNode(node);

            String fileNameStr = node.getValue().toString();
            fileName.setText(fileNameStr);

            int dotIndex = fileNameStr.indexOf('.');
            if (dotIndex == -1) {
                fileTypeIcon.setImageResource(R.drawable.ic_folder);
            } else {
                String extension = fileNameStr.substring(dotIndex);
                int extensionIcon = ExtensionTable.getExtensionIcon(extension);
                fileTypeIcon.setImageResource(extensionIcon);
            }

            if (node.getChildren().isEmpty()) {
                fileStateIcon.setVisibility(View.INVISIBLE);
            } else {
                fileStateIcon.setVisibility(View.VISIBLE);
                int stateIcon = node.isExpanded() ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right;
                fileStateIcon.setImageResource(stateIcon);
            }
        }
    }


}
