package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

import com.ingenieur.andyelderscrolls.R;

import org.jogamp.vecmath.Vector3f;

import esmj3d.j3d.BethRenderSettings;
import scrollsexplorer.simpleclient.GlobalGameSettings;

public class OptionsDialog extends Dialog implements BethRenderSettings.UpdateListener, GlobalGameSettings.UpdateListener
{
	protected Activity activity;
	protected ViewGroup rootView;
	protected Button closeButton;
	CheckBox optionsMouseLock;
	SeekBar optionsFarLoadGridCount;
	SeekBar optionsNearLoadGridCount;
	SeekBar optionsObjectFadeDistance;
	CheckBox optionsShowPathGrid;
	CheckBox optionsFogEnabled;
	SeekBar optionsAmbientLightLevel;
	SeekBar optionsDirectionalLightLevel;
	CheckBox optionsPlacedLightsEnabled;
	CheckBox optionsLightOutlines;
	CheckBox optionsCharacterOutlines;
	CheckBox optionsDoorOutlines;
	CheckBox optionsContainerOutlines;
	CheckBox optionsParticlesOutlines;
	CheckBox optionsFocusedObjectOutlines;



	public OptionsDialog(final Activity activity, final AndySimpleWalkSetup simpleWalkSetup)
	{
		super(activity);
		this.activity = activity;
		rootView = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_options, null);
		this.setContentView(rootView);

		this.setTitle("Options");

		optionsMouseLock = (CheckBox)rootView.findViewById(R.id.optionsMouseLock);
		optionsMouseLock.setChecked(simpleWalkSetup.isMouseLock());
		optionsMouseLock.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				simpleWalkSetup.setMouseLock(((CheckBox)v).isChecked());
			}
		});

		optionsFarLoadGridCount = (SeekBar)rootView.findViewById(R.id.optionsFarLoadGridCount);
		optionsFarLoadGridCount.setProgress(BethRenderSettings.getFarLoadGridCount());
		optionsFarLoadGridCount.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				BethRenderSettings.setFarLoadGridCount(progress);
			}
		});


		optionsNearLoadGridCount = (SeekBar)rootView.findViewById(R.id.optionsNearLoadGridCount);
		optionsNearLoadGridCount.setProgress(BethRenderSettings.getNearLoadGridCount());
		optionsNearLoadGridCount.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				BethRenderSettings.setNearLoadGridCount(progress);
			}
		});
		optionsObjectFadeDistance = (SeekBar)rootView.findViewById(R.id.optionsObjectFadeDistance);
		optionsObjectFadeDistance.setProgress(BethRenderSettings.getObjectFade());
		optionsObjectFadeDistance.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				BethRenderSettings.setObjectFade(progress);
			}
		});
		optionsShowPathGrid = (CheckBox)rootView.findViewById(R.id.optionsShowPathGrid);
		optionsShowPathGrid.setChecked(BethRenderSettings.isShowPathGrid());
		optionsShowPathGrid.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setShowPathGrid(((CheckBox)v).isChecked());
			}
		});


		optionsFogEnabled = (CheckBox)rootView.findViewById(R.id.optionsFogEnabled);
		optionsFogEnabled.setChecked(BethRenderSettings.isFogEnabled());
		optionsFogEnabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setFogEnabled(((CheckBox)v).isChecked());
			}
		});

		optionsAmbientLightLevel = (SeekBar)rootView.findViewById(R.id.optionsAmbientLightLevel);
		optionsAmbientLightLevel.setProgress((int)(BethRenderSettings.getGlobalAmbLightLevel()* 100));
		optionsAmbientLightLevel.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				BethRenderSettings.setGlobalAmbLightLevel(progress / 100f);
				if(simpleWalkSetup!=null)
					simpleWalkSetup.setGlobalAmbLightLevel(progress / 100f);
			}
		});
		optionsDirectionalLightLevel = (SeekBar)rootView.findViewById(R.id.optionsDirectionalLightLevel);
		optionsDirectionalLightLevel.setProgress((int)(BethRenderSettings.getGlobalDirLightLevel()* 100));
		optionsDirectionalLightLevel.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				BethRenderSettings.setGlobalDirLightLevel(progress / 100f);
				if(simpleWalkSetup!=null)
					simpleWalkSetup.setGlobalDirLightLevel(progress / 100f);
			}
		});
		optionsPlacedLightsEnabled = (CheckBox)rootView.findViewById(R.id.optionsPlacedLightsEnabled);
		optionsPlacedLightsEnabled.setChecked(BethRenderSettings.isEnablePlacedLights());
		optionsPlacedLightsEnabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setEnablePlacedLights(((CheckBox)v).isChecked());
			}
		});


		optionsLightOutlines = (CheckBox)rootView.findViewById(R.id.optionsLightOutlines);
		optionsLightOutlines.setChecked(BethRenderSettings.isOutlineLights());
		optionsLightOutlines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setOutlineLights(((CheckBox)v).isChecked());
			}
		});
		optionsCharacterOutlines = (CheckBox)rootView.findViewById(R.id.optionsCharacterOutlines);
		optionsCharacterOutlines.setChecked(BethRenderSettings.isOutlineChars());
		optionsCharacterOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				BethRenderSettings.setOutlineChars(((CheckBox)v).isChecked());
			}
		});
		optionsDoorOutlines = (CheckBox)rootView.findViewById(R.id.optionsDoorOutlines);
		optionsDoorOutlines.setChecked(BethRenderSettings.isOutlineDoors());
		optionsDoorOutlines.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				BethRenderSettings.setOutlineDoors(((CheckBox)v).isChecked());
			}
		});
		optionsContainerOutlines = (CheckBox)rootView.findViewById(R.id.optionsContainerOutlines);
		optionsContainerOutlines.setChecked(BethRenderSettings.isOutlineConts());
		optionsContainerOutlines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)	{
				BethRenderSettings.setOutlineConts(((CheckBox)v).isChecked());
			}
		});
		optionsParticlesOutlines = (CheckBox)rootView.findViewById(R.id.optionsParticlesOutlines);
		optionsParticlesOutlines.setChecked(BethRenderSettings.isOutlineParts());
		optionsParticlesOutlines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setOutlineParts(((CheckBox)v).isChecked());
			}
		});
		optionsFocusedObjectOutlines = (CheckBox)rootView.findViewById(R.id.optionsFocusedObjectOutlines);
		optionsFocusedObjectOutlines.setChecked(BethRenderSettings.isOutlineFocused());
		optionsFocusedObjectOutlines.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BethRenderSettings.setOutlineFocused(((CheckBox)v).isChecked());
			}
		});
		this.closeButton = (Button) rootView.findViewById(R.id.optionsClose);
		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view)
			{
				dismiss();
			}
		});

		// listen out for render setting changes
		BethRenderSettings.addUpdateListener(this);
		GlobalGameSettings.addUpdateListener(this);
	}


	@Override
	public void renderSettingsUpdated() {
		 //TODO:

		/*
		optionsFarLoadGridCount.setProgress(BethRenderSettings.isEnableDirLight());
		optionsNearLoadGridCount.setProgress(BethRenderSettings.isEnableDirLight());
		optionsObjectFadeDistance.setProgress(BethRenderSettings.isEnableDirLight());
		optionsShowPathGrid.setChecked(BethRenderSettings.isEnableDirLight());
		optionsFogEnabled.setChecked(BethRenderSettings.isEnableDirLight());
		optionsAmbientLightLevel.setProgress(BethRenderSettings.isEnableDirLight());
		optionsDirectionalLightLevel.setProgress(BethRenderSettings.isEnableDirLight());
		optionsPlacedLightsEnabled.setChecked(BethRenderSettings.isEnableDirLight());
		optionsLightOutlines.setChecked(BethRenderSettings.isEnableDirLight());
		optionsCharacterOutlines.setChecked(BethRenderSettings.isEnableDirLight());
		optionsDoorOutlines.setChecked(BethRenderSettings.isEnableDirLight());
		optionsContainerOutlines.setChecked(BethRenderSettings.isEnableDirLight());
		optionsParticlesOutlines.setChecked(BethRenderSettings.isEnableDirLight());
		optionsFocusedObjectOutlines.setChecked(BethRenderSettings.isEnableDirLight());

		 */
	}

	@Override
	public void gameSettingsUpdated() {
	}


	public void display()
	{
		AndyESExplorerActivity.logFireBaseContent("OptionsShown");
		this.setOnDismissListener(new DialogInterface.OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				//TODO: anything?
			}
		});
		this.show();
	}


	private abstract class OnSeekBarChangeAdapter implements SeekBar.OnSeekBarChangeListener
	{
		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{

		}
	}
}
