package com.ingenieur.andyelderscrolls.utils;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.ingenieur.andyelderscrolls.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.displayables.Displayable;


public class BSAArchiveFileChooser {
    private BSArchiveSet bsArchiveSet;

    private final FragmentActivity activity;
    private BsaTreeFragment bsaTreeFragment;

    // filter on file extension
    private String extension = null;
    private BSAArchiveFileChooserFilter bsaArchiveFileChooserFilter;

    private List<TreeNode> fileRoots = new ArrayList<TreeNode>();

    private BsaFileSelectedListener fileListener;

    // file selection event handling
    public interface BsaFileSelectedListener extends TreeViewAdapter.OnTreeNodeClickListener, TreeViewAdapter.OnTreeNodeLongClickListener {
    }

    public BSAArchiveFileChooser(FragmentActivity activity) {
        this(activity, null);
    }

    public BSAArchiveFileChooser(FragmentActivity activity, BSArchiveSet bsArchiveSet) {
        this.activity = activity;
        this.bsArchiveSet = bsArchiveSet;
    }

    public BSAArchiveFileChooser setExtension(String extension) {
        this.extension = (extension == null) ? null :
                extension.toLowerCase();
        return this;
    }

    public void setFilter(BSAArchiveFileChooserFilter bsaArchiveFileChooser) {
        this.bsaArchiveFileChooserFilter = bsaArchiveFileChooser;
    }

    public BSAArchiveFileChooser setFileListener(BsaFileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }

    public void showDialog() {
        // must recreate each time as it doesn't reshow!
        if (bsaTreeFragment != null) {
            bsaTreeFragment.dismiss();
            bsaTreeFragment = new BsaTreeFragment(bsaTreeFragment);// create it from the same dataset
        } else {
            bsaTreeFragment = new BsaTreeFragment();
        }
        bsaTreeFragment.updateTreeNodes(fileRoots);
        bsaTreeFragment.setTreeNodeClickListener(fileListener);
        bsaTreeFragment.setTreeNodeLongClickListener(fileListener);
        bsaTreeFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void dismiss() {
        if (bsaTreeFragment != null)
            bsaTreeFragment.dismiss();
    }

    /**
     * called when the data is altered or the extension is changed
     */
    public BSAArchiveFileChooser load() {
        fileRoots.clear();
        for (ArchiveFile archiveFile : bsArchiveSet) {
            if (archiveFile.hasNifOrKf()) {
                //R.layout.list_item_room maybe?
                TreeNode archiveFileRoot = new TreeNode(archiveFile, R.layout.list_item_file);
                fileRoots.add(archiveFileRoot);

                HashMap<String, FolderNode> foldersByName = new HashMap<String, FolderNode>();

                List<ArchiveEntry> entries = archiveFile.getEntries();
                TreeNode parentNode;

                for (ArchiveEntry entry : entries) {
                    parentNode = archiveFileRoot;

                    //only process the ones we want
                    if (extension != null && !entry.getFileName().endsWith(extension))
                        continue;
                    if (bsaArchiveFileChooserFilter != null && !bsaArchiveFileChooserFilter.accept(entry))
                        continue;

                    String path = ((Displayable) entry).getFolderName();
                    if (foldersByName.get(path) != null) {
                        parentNode = foldersByName.get(path);
                    } else {

                        int length = path.length();

                        int pos = 0;
                        int index1;
                        while (pos < length) {
                            String name;
                            int sep = path.indexOf('\\', pos);
                            if (sep < 0) {
                                name = path.substring(pos);
                                pos = length;
                            } else {
                                name = path.substring(pos, sep);
                                pos = sep + 1;
                            }

                            if (foldersByName.get(name) != null) {
                                parentNode = foldersByName.get(name);
                                break;
                            }

                            int count = parentNode.getChildren().size();
                            boolean insert = true;
                            index1 = 0;
                            while (index1 < count) {
                                TreeNode compare = parentNode.getChildren().get(index1);
                                if (!(compare instanceof FolderNode))
                                    break;
                                FolderNode folderNode = (FolderNode) compare;
                                int diff = name.compareTo(folderNode.getName());
                                if (diff <= 0) {
                                    if (diff == 0) {
                                        insert = false;
                                        parentNode = folderNode;
                                    }
                                    break;
                                }
                                index1++;
                            }

                            if (insert) {
                                FolderNode folderNode = new FolderNode(name, R.layout.list_item_file);
                                parentNode.addChild(index1, folderNode);
                                parentNode = folderNode;
                                foldersByName.put(path, folderNode);
                            }
                        }

                    }

                    int count = parentNode.getChildren().size();
                    String name = entry.getFileName();
                    int index2;
                    for (index2 = 0; index2 < count; index2++) {
                        TreeNode compare = parentNode.getChildren().get(index2);
                        if (compare instanceof FolderNode)
                            continue;
                        if (name.compareTo(((ArchiveEntry) compare.getValue()).getFileName()) < 0)
                            break;
                    }
                    TreeNode fileNode = new TreeNode(entry, R.layout.list_item_file);
                    parentNode.addChild(index2, fileNode);
                }
            }
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


    public interface BSAArchiveFileChooserFilter {
        boolean accept(ArchiveEntry ae);
    }
}