package com.ingenieur.andyelderscrolls.utils;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.ingenieur.andyelderscrolls.R;

import java.util.ArrayList;
import java.util.List;

import esfilemanager.common.data.plugin.PluginRecord;
import esfilemanager.loader.FormToFilePointer;
import esfilemanager.loader.IESMManager;


public class ESMCellChooser {
    private IESMManager esmManager;

    private final FragmentActivity activity;
    private EsmTreeFragment esmTreeFragment;

    // filter on file extension
    private ESMArchiveFileChooserFilter esmArchiveFileChooserFilter;

    private List<TreeNode> fileRoots = new ArrayList<TreeNode>();

    private EsmFileSelectedListener fileListener;

    // file selection event handling
    public interface EsmFileSelectedListener extends TreeViewAdapter.OnTreeNodeClickListener, TreeViewAdapter.OnTreeNodeLongClickListener {
    }


    public ESMCellChooser(FragmentActivity activity, IESMManager esmManager) {
        this.activity = activity;
        this.esmManager = esmManager;
    }

    public void setFilter(ESMArchiveFileChooserFilter esmArchiveFileChooserFilter) {
        this.esmArchiveFileChooserFilter = esmArchiveFileChooserFilter;
    }

    public ESMCellChooser setFileListener(EsmFileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }

    public void showDialog() {
        // must recreate each time as it doesn't reshow!
        if (esmTreeFragment != null) {
            esmTreeFragment.dismiss();
            esmTreeFragment = new EsmTreeFragment(esmTreeFragment);// create it from the same dataset
        } else {
            esmTreeFragment = new EsmTreeFragment();
        }
        esmTreeFragment.updateTreeNodes(fileRoots);
        esmTreeFragment.setTreeNodeClickListener(fileListener);
        esmTreeFragment.setTreeNodeLongClickListener(fileListener);
        esmTreeFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void dismiss() {
        if (esmTreeFragment != null)
            esmTreeFragment.dismiss();
    }

    /**
     * called when the data is altered or the extension is changed
     */
    public ESMCellChooser load() {
        fileRoots.clear();

        try {

            String esmName = esmManager.getName();
            //crappy last path thing
            int sep = esmName.lastIndexOf('/');
            if (sep > 0) {
                esmName = esmName.substring(sep + 1);
            }

            TreeNode archiveFileRoot = new TreeNode(esmName, R.layout.list_item_file);
            fileRoots.add(archiveFileRoot);

            // do the worlds first
            FolderNode folderNode = new FolderNode("Exterior", R.layout.list_item_file);
            archiveFileRoot.addChild(folderNode);
            for (Integer formId : esmManager.getAllWRLDTopGroupFormIds()) {
                PluginRecord pr = esmManager.getWRLD(formId);
                //only process the ones we want
                if (esmArchiveFileChooserFilter != null && !esmArchiveFileChooserFilter.accept(pr))
                    continue;

                TreeNode fileNode = new TreeNode(pr, R.layout.list_item_file);
                folderNode.addChild(fileNode);
            }
            // then interiors
            folderNode = new FolderNode("Interior", R.layout.list_item_file);
            archiveFileRoot.addChild(folderNode);
            for (FormToFilePointer cp : esmManager.getAllInteriorCELLFormIds()) {
                int formId = cp.formId;
                PluginRecord pr = esmManager.getInteriorCELL(formId);
                //only process the ones we want
                if (esmArchiveFileChooserFilter != null && !esmArchiveFileChooserFilter.accept(pr))
                    continue;

                TreeNode fileNode = new TreeNode(pr, R.layout.list_item_file);
                folderNode.addChild(fileNode);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }


    public class FolderNode extends TreeNode implements Comparable<FolderNode> {
        private String name;

        public FolderNode(String name, int layoutId) {
            super(name, layoutId);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj != null && (obj instanceof FolderNode) && name.equals(((FolderNode) obj).getName()))
                equal = true;
            return equal;
        }

        public int compareTo(FolderNode compare) {
            return name.compareTo(compare.getName());
        }

    }


    public interface ESMArchiveFileChooserFilter {
        boolean accept(PluginRecord pr);
    }
}