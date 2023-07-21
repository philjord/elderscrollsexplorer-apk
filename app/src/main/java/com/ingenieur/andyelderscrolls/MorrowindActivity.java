package com.ingenieur.andyelderscrolls;

import static android.widget.Toast.LENGTH_LONG;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_GAME;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_START_CONFIG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.andyesexplorer.AndySimpleWalkSetup;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import org.jogamp.java3d.JoglesPipeline;
import org.jogamp.vecmath.Vector3f;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

import esmj3dtes3.ai.Tes3AICREA;
import nif.j3d.J3dNiTriBasedGeom;
import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.tes3.Tes3Extensions;
import simpleandroid.JoglHelloWorldActivity;
import tools3d.utils.YawPitch;

/**
 * Created by phil on 7/15/2016.
 */
public class MorrowindActivity extends Activity {
    private static final String WELCOME_SCREEN_UNWANTED = "WELCOME_SCREEN_UNWANTED";
    private static final String OPTIMIZE = "OPTIMIZE";
    private static final String ANTIALIAS = "ANTIALIAS";
    private static final String GYROSCOPE = "GYROSCOPE";

    // Note we are unlinked from the bigger ESE prefs as that wants multi game etc
    public static final String MORROWIND_PREFS_NAME = "MorrowindActivityDefault";

    public static final String MORROWIND_BASE_FOLDER = "MorrowindLastSelectedFile";
    public static final String MORROWIND_GAME_FOLDER = "MORROWIND_GAME_FOLDER";

    private GameConfig gameSelected;

    private int ACTION_OPEN_DOCUMENT_TREE_CODE = 567;

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);


        // get system out to log
        PrintStream interceptor = new SopInterceptor(System.out, "sysout");
        System.setOut(interceptor);
        PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
        System.setErr(interceptor2);


        // android has a different system from windows
        PropertyLoader.propFile = new File(this.getFilesDir(), "config.ini");
        try {
            PropertyLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameConfig.init();

        setContentView(R.layout.morrowind);


        // Possibly show welcome screen
        SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
        boolean welcomeScreenUnwanted = settings.getBoolean(WELCOME_SCREEN_UNWANTED, false);

        if (welcomeScreenUnwanted) {
            getFilePermissions();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the buttons
            builder.setPositiveButton(R.string.welcometextyes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // do remind again so no prefs
                    getFilePermissions();
                }
            });
            builder.setNegativeButton(R.string.welcometextno, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // don't remind again
                    SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(WELCOME_SCREEN_UNWANTED, true);
                    editor.apply();
                    getFilePermissions();
                }
            });

            String welcomeMessage = this.getString(R.string.welcometext);
            TextView textView = new TextView(this);
            textView.setPadding(10, 10, 10, 10);
            textView.setText(Html.fromHtml(welcomeMessage));
            textView.setMovementMethod(LinkMovementMethod.getInstance());

            builder.setView(textView);

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    private void getFilePermissions() {

        //CAREFEUL!! shared prefs can be not wiped on uninstall, see AndroidManifest.xml
        //https://stackoverflow.com/questions/35517239/sharedpreferences-are-not-being-cleared-when-i-uninstall
        SharedPreferences preferences = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
        String baseFolder = preferences.getString(MORROWIND_BASE_FOLDER, null);
        Uri baseFolderUri = null;
        if (baseFolder != null) {
            baseFolderUri = Uri.parse(baseFolder);
            if (baseFolderUri != null) {
                attemptLaunchMorrowind();
            }
        }

        if (baseFolderUri == null) {
            Toast.makeText(this, "Please select the folder containing the Morrowind game files", LENGTH_LONG).show();
            // ok find a root folder for morrowind at least
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            this.startActivityForResult(intent, ACTION_OPEN_DOCUMENT_TREE_CODE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
        boolean optimize = settings.getBoolean(OPTIMIZE, false);
        menu.findItem(R.id.menu_optomize).setChecked(optimize);
        setOptimize(optimize);
        boolean antialias = settings.getBoolean(ANTIALIAS, false);
        menu.findItem(R.id.menu_anti_alias).setChecked(antialias);
        AndyESExplorerActivity.antialias = antialias;
        boolean gyroscope = settings.getBoolean(GYROSCOPE, false);
        menu.findItem(R.id.menu_gyroscope).setChecked(antialias);
        AndyESExplorerActivity.gyroscope = gyroscope;


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_test_3d:
                test3d();
                return true;
            case R.id.menu_optomize:
                item.setChecked(!item.isChecked());
                setOptimize(item.isChecked());
                return true;
            case R.id.menu_anti_alias:
                item.setChecked(!item.isChecked());
                AndyESExplorerActivity.antialias = item.isChecked();
                SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(ANTIALIAS, item.isChecked());
                editor.apply();
                return true;
            case R.id.menu_gyroscope:
                item.setChecked(!item.isChecked());
                AndyESExplorerActivity.gyroscope = item.isChecked();
                SharedPreferences settings2 = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
                SharedPreferences.Editor editor2 = settings2.edit();
                editor2.putBoolean(GYROSCOPE, item.isChecked());
                editor2.apply();
                return true;
            case R.id.menu_start_tools:
                startESEActivity(null);
                return true;
            case R.id.menu_help_screen:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String welcomeMessage = this.getString(R.string.welcometext);
                TextView textView = new TextView(this);
                textView.setPadding(10, 10, 10, 10);
                textView.setText(Html.fromHtml(welcomeMessage));
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                builder.setView(textView);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menu_privacy:
                String urlStr = "https://sites.google.com/view/corm-privacy/home";
                Uri webpage = Uri.parse(urlStr);
                Intent intent2 = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent2.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startESEActivity(View view) {
        Intent intent = new Intent(this, ElderScrollsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_OPEN_DOCUMENT_TREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri baseDocumentTreeUri = Objects.requireNonNull(data).getData();

                // record the folder details in the config, if the esm can be found
                if (setGameESMFileSelect(DocumentFile.fromTreeUri(this, baseDocumentTreeUri))) {
                    final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // take persistable Uri Permission for future use
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                    SharedPreferences preferences = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
                    preferences.edit().putString(MORROWIND_BASE_FOLDER, data.getData().toString()).apply();
                    attemptLaunchMorrowind();
                } else {
                    //try again?
                    //possibly let's not infinite loop in case they really want ESE
                }
            } else {
                Toast.makeText(this, "ACTION_OPEN_DOCUMENT_TREE_CODE error", LENGTH_LONG).show();
            }
        }
    }


    private void test3d() {
        Intent myIntent = new Intent(this, JoglHelloWorldActivity.class);//JoglStatusActivity.class);
        this.startActivity(myIntent);
    }



    /**
     * new version for getting a whole directory
     *
     * @param folder
     */
    private boolean setGameESMFileSelect(DocumentFile folder) {
        boolean validESM = false;
        for (DocumentFile file : folder.listFiles()) {
            // let's see if this guy is one of our game configs
            String fileName = file.getName();

            for (GameConfig gameConfig : GameConfig.allGameConfigs) {
                if (gameConfig.mainESMFile.equals(fileName)) {
                    SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(MORROWIND_GAME_FOLDER + gameConfig.folderKey, folder.getUri().toString());
                    editor.apply();

                    validESM = true;
                }
            }
            if (validESM) {
                break;
            }
        }
        if (!validESM) {
            Toast.makeText(this, "Selected file not a valid game esm", LENGTH_LONG).show();
            return false;
        }

        return validESM;
    }


    private void attemptLaunchMorrowind() {
        SharedPreferences preferences = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
        String baseFolder = preferences.getString(MORROWIND_BASE_FOLDER, null);
        if (baseFolder != null) {
            Uri basefolderUri = Uri.parse(baseFolder);

            for (GameConfig gameConfig : GameConfig.allGameConfigs) {
                //System.out.println("looking for game folder " + gameConfig.folderKey);

                // id this key the morrowind one?
                if ("MorrowindFolder".equals(gameConfig.folderKey)) {
                    // does it in fact contain the esm file (perhaps the data has been deleted for example)
                    DocumentFile df = DocumentFile.fromTreeUri(this, basefolderUri);
                    DocumentFile checkEsmDF = df.findFile(gameConfig.mainESMFile);

                    if (!checkEsmDF.exists()) {
                        // if no esm clear this settings so we don't waste time with it
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove(MORROWIND_GAME_FOLDER + gameConfig.folderKey);
                        editor.apply();
                    } else {
                        gameConfig.scrollsFolder = basefolderUri.toString();
                        gameSelected = gameConfig;

                        // debug shaders like this to externalize from jars
                        //ShaderPrograms.fileSystemFolder = new DFFile(df.findFile("shaders"));//new File(basefolderUri.getPath(), "shaders");

                        break;
                    }
                }
            }


        }


        if (gameSelected != null) {
            showStartConfigPicker();
        } else {
            Toast.makeText(this, "Please select the morrowind.esm file", Toast.LENGTH_LONG)
                    .show();
        }
    }


    private void showStartConfigPicker() {
        final ListView gameConfigSelectorList = (ListView) findViewById(R.id.gameConfigSelectView);

        gameConfigSelectorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, configNames) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });


        gameConfigSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                // send the which through and hope they match up

                Intent intent = new Intent(MorrowindActivity.this, AndyESExplorerActivity.class);
                // note we use strings from ElderScrollsActivity because that's how AndyESExplorerActivity unloads it
                intent.putExtra(SELECTED_GAME, gameSelected.gameName);
                intent.putExtra(SELECTED_START_CONFIG, which);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void setOptimize(boolean optimize) {
        J3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = optimize;
        JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = optimize;
        JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = optimize;

        // these don't seem to cause trouble often, but the above three do constantly
        //JoglesPipeline.LATE_RELEASE_CONTEXT = optimize;
        //JoglesPipeline.MINIMISE_NATIVE_CALLS_TRANSPARENCY = optimize;
        //JoglesPipeline.MINIMISE_NATIVE_CALLS_TEXTURE = optimize;

        SharedPreferences settings = getSharedPreferences(MORROWIND_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(OPTIMIZE, optimize);
        editor.apply();
    }

    public static final String[] configNames = new String[]
            {
                    "Inside starting ship",
                    "Seyda Neen on ship deck",
                    "Last session",
                    "Combat in a cave",
                    "Vivec",
                    "Ald Rhun",
                    "Tel Mora",
                    "Azura's cave",
                    "Ghost gate",
                    "Nice green land",
                    "Dwarf ruins",
                    "Ebonheart, Imperial Commission",
                    "Vivec, Palace of Vivec",
                    "Telasero, Propylon Chamber",
                    "Molag Mar",
                    "Vos",
            };

    public static void organiseMorrowindPreselectedConfigs(int gameConfigId) {
        GameConfig morrowindConfig = GameConfig.allGameConfigs.get(0);


       // musicToPlay = 0;//0=none,1=explore,2=battle

        if (gameConfigId == 0) {
            //scene  Imperial prison ship id 22668
            morrowindConfig.startCellId = 22668;
            morrowindConfig.startLocation = new Vector3f(1, -0.3f, 2);
            morrowindConfig.startYP = new YawPitch(Math.PI / 4, 0);
            morrowindConfig.musicToPlayId = 1;//explore
        } else if (gameConfigId == 1) {
            // deck of start ship
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(-108, 3, 936);
            morrowindConfig.startYP = new YawPitch(0, 0);
            morrowindConfig.musicToPlayId = 1;//explore
        } else if (gameConfigId == 2) {
            //Freeform
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(-108, 3, 936);
            morrowindConfig.startYP = new YawPitch(0, 0);
        } else if (gameConfigId == 3) {
            //dwarwen ruin for combat but odd sound issue
            morrowindConfig.startCellId = 23903;//23042;
            morrowindConfig.startLocation = new Vector3f(2, -1, 18);//(57, 0, -17);
            morrowindConfig.startYP = new YawPitch(Math.PI / 8, 0);
            morrowindConfig.musicToPlayId = 2;//battle
            Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
            Tes3AICREA.combatDemo = true;
        } else if (gameConfigId == 4) {
            //vivec for third person view
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(423, 8, 1079);
            morrowindConfig.startYP = new YawPitch(0, 0);
            morrowindConfig.musicToPlayId = 1;//explore
            AndySimpleWalkSetup.TRAILER_CAM = true;
        } else if (gameConfigId == 5) {
            // ald rhun
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(-152, 31, -682);
            morrowindConfig.startYP = new YawPitch(0, 0);
            morrowindConfig.musicToPlayId = 1;//explore
            Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
        } else if (gameConfigId == 6) {
            //tel mora  , cast spell in third
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(1387, 18, -1438);
            morrowindConfig.startYP = new YawPitch(Math.PI / 8, 0);
            morrowindConfig.musicToPlayId = 1;//explore
            Tes3Extensions.HANDS = Tes3Extensions.hands.SPELL;
            AndySimpleWalkSetup.TRAILER_CAM = true;
        } else if (gameConfigId == 7) {
            //inside cavern with azura
            morrowindConfig.startCellId = 22087;
            morrowindConfig.startLocation = new Vector3f(0, 0, 16);
            morrowindConfig.startYP = new YawPitch(0, 0);
            morrowindConfig.musicToPlayId = 1;//explore
        } else if (gameConfigId == 8) {
            //  ghost gate, look the walk down gully
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(256, 11, -460);
            morrowindConfig.startYP = new YawPitch(0, 0);
            Tes3Extensions.HANDS = Tes3Extensions.hands.SPELL;
            morrowindConfig.musicToPlayId = 1;//explore
        } else if (gameConfigId == 9) {
            //nice green land walk along a road, transition to next
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(896, 12, -1472);
            morrowindConfig.startYP = new YawPitch(0, 0);
            morrowindConfig.musicToPlayId = 1;//explore
        } else if (gameConfigId == 10) {
            //   dwarf ruins outside along a bridge walk up behind crea
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(-183, 49, -1059);
            morrowindConfig.startYP = new YawPitch(0, 0);
            Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
            morrowindConfig.musicToPlayId = 2;//battle
        } else if (gameConfigId == 11) {
            //Ebonheart, Imperial Commission
            morrowindConfig.startCellId = 22302;
            morrowindConfig.startLocation = new Vector3f(0, 2, -6);
            morrowindConfig.startYP = new YawPitch(Math.PI, 0);
        } else if (gameConfigId == 12) {
            //Vivec, Palace of Vivec
            morrowindConfig.startCellId = 24230;
            morrowindConfig.startLocation = new Vector3f(0, -4, 5);
            morrowindConfig.startYP = new YawPitch(Math.PI, 0);
        } else if (gameConfigId == 13) {
            //Telasero, Propylon Chamber
            morrowindConfig.startCellId = 23850;
            morrowindConfig.startLocation = new Vector3f(5, -6, -9);
            morrowindConfig.startYP = new YawPitch(Math.PI / 4, 0);
        } else if (gameConfigId == 14) {
            //Molag Mar
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(1405, 23, 758);
            morrowindConfig.startYP = new YawPitch(0, 0);
        } else if (gameConfigId == 15) {
            //Vos
            morrowindConfig.startCellId = 0;
            morrowindConfig.startLocation = new Vector3f(1225, 19, -1465);
            morrowindConfig.startYP = new YawPitch(0, 0);
        }
    }
}
