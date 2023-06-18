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

import java.util.List;


public class EsmTreeFragment extends DialogFragment implements TreeViewAdapter.OnTreeNodeClickListener, TreeViewAdapter.OnTreeNodeLongClickListener {

    private TreeViewAdapter treeViewAdapter;

    /**
     * Custom OnClickListener to be invoked when a TreeNode has been clicked.
     */
    private TreeViewAdapter.OnTreeNodeClickListener treeNodeClickListener;

    /**
     * Custom OnLongClickListener to be invoked when a TreeNode has been clicked and hold.
     */
    private TreeViewAdapter.OnTreeNodeLongClickListener treeNodeLongClickListener;

    private static final String TAG = "EsmTreeFragment";

    public EsmTreeFragment() {
        TreeViewHolderFactory factory = (v, layout) -> new EsmNodeViewHolder(v);
        treeViewAdapter = new TreeViewAdapter(factory);
    }

    /**
     * To allow reshowing the dialog fragment we must same the data adapter
     *
     * @param prevTree
     */
    public EsmTreeFragment(EsmTreeFragment prevTree) {
        treeViewAdapter = prevTree.treeViewAdapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        TreeViewHolder.levelIndentPadding = (int) (EsmTreeFragment.this.getResources().getDisplayMetrics().density * 10);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_tree, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(treeViewAdapter);

        treeViewAdapter.setTreeNodeClickListener(this);
        treeViewAdapter.setTreeNodeLongClickListener(this);

        return view;
    }

    public void onTreeNodeClick(TreeNode treeNode, View view) {
        if (treeNodeClickListener != null)
            this.treeNodeClickListener.onTreeNodeClick(treeNode, view);
    }

    public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
        if (treeNodeLongClickListener != null)
            return this.treeNodeLongClickListener.onTreeNodeLongClick(treeNode, view);
        else
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


    /**
     * Update the list of tree nodes
     *
     * @param fileRoots The new tree nodes
     */
    public void updateTreeNodes(List<TreeNode> fileRoots) {
        treeViewAdapter.updateTreeNodes(fileRoots);
    }

    public class EsmNodeViewHolder extends TreeViewHolder {

        private TextView fileName;
        private ImageView fileStateIcon;
        private ImageView fileTypeIcon;

        public EsmNodeViewHolder(@NonNull View itemView) {
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

            if (node.getChildren().isEmpty()) {
                fileStateIcon.setVisibility(View.INVISIBLE);
                fileTypeIcon.setImageResource(R.drawable.ic_c);
            } else {
                fileTypeIcon.setImageResource(R.drawable.ic_folder);
                fileStateIcon.setVisibility(View.VISIBLE);
                int stateIcon = node.isExpanded() ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right;
                fileStateIcon.setImageResource(stateIcon);
            }
        }
    }


}
