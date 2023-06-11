package com.ingenieur.andyelderscrolls;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.jbullet.JBulletActivity;
import com.ingenieur.andyelderscrolls.kfdisplay.KfDisplayActivity2;
import com.ingenieur.andyelderscrolls.nifdisplay.NifDisplayActivity;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import org.jogamp.java3d.JoglesPipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import nif.j3d.J3dNiTriBasedGeom;
import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;

public class ElderScrollsActivity extends Activity {
//TODO: this guy is not up with the hip kids on the new SAF URI FileChannel world but the below is still true
    /**
     * Ok no more single root folder, all games must be added by selecting there esm file
     * bsa files MUST be sibling files or possibly an obb file with teh game name in it
     * esm files will be identified by filename these are fixed
     * Tools will ahve an add button to add as many as you like, if a different location
     * is selected for a current folder it is simply overwritten
     * Morrowind will look for it's folder and ask for it if not found, ic_tools is how you change this if needed
     * the esm names that are stored come from the gameconfig list key is folderKey esm name is mainESMFile
     * So note Andy root must disappear totally? yes I think that's right
     */
    public static final String PREFS_NAME = "ElderScrollsActivityDefault";

    public final static String SELECTED_GAME = "SELECTED_GAME";
    public static final String SELECTED_START_CONFIG = "SELECTED_START_CONFIG";

    public static final String SELECTED_ROOT_FOLDER = "LastSelectedFile";
    public static final String GAME_FOLDER = "GAME_FOLDER";

    private GameConfig gameSelected;

    private int ACTION_OPEN_DOCUMENT_TREE_CODE = 569;

    private SopInterceptor sysoutInterceptor;
    private SopInterceptor syserrInterceptor;
    private boolean setLogFile = false;


    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);

        // get system out to log
        sysoutInterceptor = new SopInterceptor(System.out, "sysout");
        System.setOut(sysoutInterceptor);
        syserrInterceptor = new SopInterceptor(System.err, "syserr");
        System.setErr(syserrInterceptor);

        //DEBUG to fix Nexus 5
        J3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = false;
        JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = false;
        JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = false;

        //Debugishness to investigate egl error 300e
        JoglesPipeline.LATE_RELEASE_CONTEXT = false;

        System.err.println("Tools\n" +
                "\t\tJ3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = false;\n" +
                "\t\tJoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = false;\n" +
                "\t\tJoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = false;\n" +
                "\t\tJoglesPipeline.LATE_RELEASE_CONTEXT = false;");


        // android has a different system from windows
        PropertyLoader.propFile = new File(this.getFilesDir(), "config.ini");
        try {
            PropertyLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameConfig.init();

        setContentView(R.layout.main);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        String baseFolder = preferences.getString(SELECTED_ROOT_FOLDER, null);
        Uri baseFolderUri = null;
        if (baseFolder != null) {
            baseFolderUri = Uri.parse(baseFolder);
            if (baseFolderUri != null) {
                setAllGamesRoot(DocumentFile.fromTreeUri(this, baseFolderUri));
            }
        }

        if (baseFolderUri == null) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            this.startActivityForResult(intent, ACTION_OPEN_DOCUMENT_TREE_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_OPEN_DOCUMENT_TREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri baseDocumentTreeUri = Objects.requireNonNull(data).getData();
                final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // take persistable Uri Permission for future use
                getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                preferences.edit().putString(SELECTED_ROOT_FOLDER, data.getData().toString()).apply();

                setAllGamesRoot(DocumentFile.fromTreeUri(this, baseDocumentTreeUri));
            } else {
                Toast.makeText(this, "ACTION_OPEN_DOCUMENT_TREE_CODE error", LENGTH_LONG).show();
            }
        }
    }


    public void toggleWriteLog(View view) {
        setLogFile = !setLogFile;
    }

    /**
     * This allows users to reset the folder root in case of file system changes
     *
     * @param view
     */
    public void resetRootFolder(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        this.startActivityForResult(intent, ACTION_OPEN_DOCUMENT_TREE_CODE);
    }

    /**
     * file should be a permissions granted root folder which will be searched for all esm options
     *
     * @param file
     */
    private void setAllGamesRoot(DocumentFile file) {
        //if slow I'm told x20 as fast this guy
        //DocumentsContract

        searchFolder(file);

        // update the game list
        fillGameList();
    }

    /**
     * returns count of esm files found
     *
     * @param folder
     * @return count of ESM file found in here or sub folders
     */
    private int searchFolder(DocumentFile folder) {
        int returnCount = 0;
        // are we a directory? if a file complain and leave
        if (folder.isDirectory()) {
            // if so check for an esm in us record it, and leave (do not dig deeper)
            for (DocumentFile file : folder.listFiles()) {
                // let's see if this guy is one of our game configs
                String fileName = file.getName();
                for (GameConfig gameConfig : GameConfig.allGameConfigs) {
                   // System.out.println("checking " + folder.getName() + " against " + gameConfig.gameName);
                    if (gameConfig.mainESMFile.equals(fileName)) {
                        System.out.println("Matched esm file name! " + gameConfig.gameName);

                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        // note we record the folder not the esm file itself
                        editor.putString(GAME_FOLDER + gameConfig.folderKey, folder.getUri().toString());
                        editor.apply();
                        System.out.println("Saved to Prefs " + GAME_FOLDER + gameConfig.folderKey + " : " + folder.getUri().toString());
                        // leave the method, do not check siblings, do not check sub folders
                        return 1;
                    }
                }
            }

            // if none then search each sub folder
            for (DocumentFile file : folder.listFiles()) {
                if (file.isDirectory()) {
                    returnCount += searchFolder(file);
                }
            }

        } else {
            // can't pass a  regular file in here
            throw new UnsupportedOperationException("Can't give a file to searchFolder " + folder.getName());
        }
        return returnCount;
    }


    private void fillGameList() {
        final ListView gameSelectorList = (ListView) findViewById(R.id.gameSelectView);

        final ArrayList<GameConfig> gamesWithFoldersSet = new ArrayList<GameConfig>();
        for (GameConfig gameConfig : GameConfig.allGameConfigs) {
            System.out.println("looking for game folder of " + gameConfig.folderKey);
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            String gameFolder = settings.getString(GAME_FOLDER + gameConfig.folderKey, "");
            if (gameFolder.length() > 0) {
                System.out.println("has game folder " + gameFolder);
                gameConfig.scrollsFolder = gameFolder;
                gamesWithFoldersSet.add(gameConfig);
            }
        }

        String[] gameNames = new String[gamesWithFoldersSet.size()];
        int i = 0;
        for (GameConfig gameConfig : gamesWithFoldersSet) {
            gameNames[i++] = gameConfig.gameName;
        }

        gameSelectorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gameNames) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });

        gameSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String selection = (String) gameSelectorList.getItemAtPosition(which);
                for (GameConfig gameConfig : gamesWithFoldersSet) {
                    if (selection.equals(gameConfig.gameName)) {
                        gameSelected = gameConfig;
                        // enable the other buttons now we have a valid game selected
                        Button esExplorerButton = (Button) findViewById(R.id.esExplorerButton);
                        esExplorerButton.setEnabled(true);
                        Button nifButton = (Button) findViewById(R.id.nifButton);
                        nifButton.setEnabled(true);
                        Button kfButton = (Button) findViewById(R.id.kfButton);
                        kfButton.setEnabled(true);
                        Button jBulletButton = (Button) findViewById(R.id.jBulletButton);
                        jBulletButton.setEnabled(true);
                        // now await user deciding to select explore or nif display etc on selection
                        break;
                    }
                }
            }
        });
    }

    public void showESExplorer(View view) {
        if (gameSelected != null) {
            setUpLogFile(gameSelected);
            Intent intent = new Intent(this, AndyESExplorerActivity.class);
            intent.putExtra(SELECTED_GAME, gameSelected.gameName);
            startActivity(intent);
        } else {
            Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void showNifDisplay(View view) {
        if (gameSelected != null) {
            setUpLogFile(gameSelected);
            Intent intent = new Intent(this, NifDisplayActivity.class);
            intent.putExtra(SELECTED_GAME, gameSelected.gameName);
            startActivity(intent);
        } else {
            Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void showKfDisplay(View view) {
        if (gameSelected != null) {
            setUpLogFile(gameSelected);
            Intent intent = new Intent(this, KfDisplayActivity2.class);
            intent.putExtra(SELECTED_GAME, gameSelected.gameName);
            startActivity(intent);
        } else {
            Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void showJBulletDisplay(View view) {
        if (gameSelected != null) {
            setUpLogFile(gameSelected);
            Intent intent = new Intent(this, JBulletActivity.class);
            intent.putExtra(SELECTED_GAME, gameSelected.gameName);
            startActivity(intent);
        } else {
            Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
                    .show();
        }
    }



    private void setUpLogFile(GameConfig gameSelected) {
        if (setLogFile) {
            // just go for downloads, always there
            File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CallOfRedMountainLog.log");
            sysoutInterceptor.setLogFile(logFile);
            syserrInterceptor.setLogFile(logFile);
        }
    }

    public static GameConfig getGameConfig(String gameName) {
        for (GameConfig gameConfig : GameConfig.allGameConfigs) {
            //System.out.println("checking " + gameName + " against " + gameConfig.gameName);
            if (gameConfig.gameName.equals(gameName)) {
                //System.out.println("Found game to load! " + gameConfig.gameName);
                return gameConfig;
            }
        }

        System.out.println("No game found for! " + gameName);
        return null;
    }

}
