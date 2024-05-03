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
    HashMap<String, FolderNode> foldersByPath = new HashMap<String, FolderNode>();

    private BsaFileSelectedListener fileListener;
    private boolean multiple;


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
        this.extension = (extension == null) ? null : extension.toLowerCase();
        return this;
    }

    public void setFilter(BSAArchiveFileChooserFilter bsaArchiveFileChooser) {
        this.bsaArchiveFileChooserFilter = bsaArchiveFileChooser;
    }

    public BSAArchiveFileChooser setFileListener(BsaFileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
    public BSAArchiveFileChooser setMultiple(boolean multiple) {
        this.multiple = multiple;
        return this;
    }
    public void showDialog() {
        // must recreate each time as it doesn't reshow!
        if (bsaTreeFragment != null) {
            bsaTreeFragment.dismiss();
            bsaTreeFragment = new BsaTreeFragment(bsaTreeFragment, multiple);// create it from the same dataset
        } else {
            bsaTreeFragment = new BsaTreeFragment(multiple);
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

    public void updateNodesState() {
        if (bsaTreeFragment != null)
            bsaTreeFragment.getTreeViewAdapter().notifyDataSetChanged();
    }
    /**
     * MUST be called after showDialog()
     * @param folderNode
     */
    public void expandToTreeNode(TreeNode folderNode) {

        String folderPath = "";

        TreeNode curr = folderNode;
        while (curr != null) {
            if(curr instanceof FolderNode) {
                folderPath = curr.getValue().toString() + (folderPath.length() > 0 ? "\\" : "") + folderPath;
            }
            curr = curr.getParent();
        }

        if (foldersByPath.get(folderPath) != null) {
            TreeNode nodeToExpand = foldersByPath.get(folderPath);
            if(bsaTreeFragment != null) {
                bsaTreeFragment.getTreeViewAdapter().expandToNode(nodeToExpand);
            }
        }
    }
    /**
     * called when the data is altered or the extension is changed
     */
    public BSAArchiveFileChooser load() {
        fileRoots.clear();
        foldersByPath.clear();
        for (ArchiveFile archiveFile : bsArchiveSet) {
            //FIXME need to filter for sound file extensions
            if (   (extension.equals("nif") || extension.equals("kf") && archiveFile.hasNifOrKf())
                || (extension.equals("dds") && archiveFile.hasDDS())
                || (extension.equals("ktx") && archiveFile.hasKTX()) ) {
                //R.layout.list_item_room maybe?
                FolderNode archiveFileRoot = new FolderNode(archiveFile.getName(), R.layout.list_item_file);

                int acceptedItems = 0;
                // both removed if nothing accepted
                fileRoots.add(archiveFileRoot);
                foldersByPath.put(archiveFile.getName(), archiveFileRoot);

                List<ArchiveEntry> entries = archiveFile.getEntries();
                TreeNode parentNode;

                for (ArchiveEntry entry : entries) {
                    parentNode = archiveFileRoot;

                    //only process the ones we want
                    if (extension != null){
                        String name = ((Displayable)entry).getFileName();
                        if(name == null || !name.endsWith(extension))
                            continue;
                    }
                    if (bsaArchiveFileChooserFilter != null && !bsaArchiveFileChooserFilter.accept(entry))
                        continue;

                    acceptedItems++;

                    String folderPath = archiveFile.getName() +"\\" + ((Displayable) entry).getFolderName();
                    if (foldersByPath.get(folderPath) != null) {
                        parentNode = foldersByPath.get(folderPath);
                    } else {
                        // need to build the folderPath

                        int length = folderPath.length();

                        int pos = 0;
                        int index1;
                        while (pos < length) {
                            String name;
                            String parentPath;
                            int sep = folderPath.indexOf('\\', pos);
                            if (sep < 0) {
                                name = folderPath.substring(pos);
                                parentPath = folderPath;
                                pos = length;
                            } else {
                                name = folderPath.substring(pos, sep);
                                parentPath = folderPath.substring(0, sep);
                                pos = sep + 1;
                            }


                            if(foldersByPath.get(parentPath) == null) {
                                // create this one inside the already discovered parent

                                // find the spot within the parent children list by comparison
                                int count = parentNode.getChildren().size();
                                index1 = 0;
                                while (index1 < count) {
                                    TreeNode compare = parentNode.getChildren().get(index1);
                                    if (!(compare instanceof FolderNode))
                                        break;
                                    FolderNode folderNode2 = (FolderNode) compare;
                                    int diff = name.compareTo(folderNode2.getName());
                                    if (diff <= 0) {
                                        break;
                                    }
                                    index1++;
                                }

                                FolderNode folderNode = new FolderNode(name, R.layout.list_item_file);
                                parentNode.addChild(index1, folderNode);
                                parentNode = folderNode;
                                foldersByPath.put(parentPath, folderNode);
                            } else {
                                parentNode = foldersByPath.get(parentPath);
                            }
                        }

                    }

                    int count = parentNode.getChildren().size();
                    String name = ((Displayable)entry).getFileName();
                    int index2;
                    for (index2 = 0; index2 < count; index2++) {
                        TreeNode compare = parentNode.getChildren().get(index2);
                        if (compare instanceof FolderNode)
                            continue;
                        if (name.compareTo(((Displayable) compare.getValue()).getFileName()) < 0)
                            break;
                    }
                    TreeNode fileNode = new TreeNode(entry, R.layout.list_item_file);
                    parentNode.addChild(index2, fileNode);
                }


                // nothing in the bsa file we want
                if(acceptedItems == 0) {
                    fileRoots.remove(archiveFileRoot);
                    foldersByPath.remove(archiveFile.getName());
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