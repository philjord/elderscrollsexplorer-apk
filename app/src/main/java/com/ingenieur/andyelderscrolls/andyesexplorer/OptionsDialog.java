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

public class OptionsDialog extends Dialog
{
	protected Activity activity;
	protected ViewGroup rootView;
	protected Button closeButton;

	public OptionsDialog(final Activity activity, final AndySimpleWalkSetup simpleWalkSetup)
	{
		super(activity);
		this.activity = activity;
		rootView = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_options, null);
		this.setContentView(rootView);

		this.setTitle("Options");



		SeekBar optionsFarLoadGridCount = (SeekBar)rootView.findViewById(R.id.optionsFarLoadGridCount);
		optionsFarLoadGridCount.setProgress(BethRenderSettings.getFarLoadGridCount());
		optionsFarLoadGridCount.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				AndyESExplorerActivity.logFireBaseContent("setFarLoadGridCount", ""+ progress);
				BethRenderSettings.setFarLoadGridCount(progress);
			}
		});


		SeekBar optionsNearLoadGridCount = (SeekBar)rootView.findViewById(R.id.optionsNearLoadGridCount);
		optionsNearLoadGridCount.setProgress(BethRenderSettings.getNearLoadGridCount());
		optionsNearLoadGridCount.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				AndyESExplorerActivity.logFireBaseContent("setNearLoadGridCount", ""+ progress);
				BethRenderSettings.setNearLoadGridCount(progress);
			}
		});
		SeekBar optionsObjectFadeDistance = (SeekBar)rootView.findViewById(R.id.optionsObjectFadeDistance);
		optionsObjectFadeDistance.setProgress(BethRenderSettings.getObjectFade());
		optionsObjectFadeDistance.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				AndyESExplorerActivity.logFireBaseContent("setObjectFade", ""+ progress);
				BethRenderSettings.setObjectFade(progress);
			}
		});
		CheckBox optionsShowPathGrid = (CheckBox)rootView.findViewById(R.id.optionsShowPathGrid);
		optionsShowPathGrid.setChecked(BethRenderSettings.isShowPathGrid());
		optionsShowPathGrid.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setShowPathGrid", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setShowPathGrid(((CheckBox)v).isChecked());
			}
		});


		CheckBox optionsFogEnabled = (CheckBox)rootView.findViewById(R.id.optionsFogEnabled);
		optionsFogEnabled.setChecked(BethRenderSettings.isFogEnabled());
		optionsFogEnabled.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setFogEnabled", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setFogEnabled(((CheckBox)v).isChecked());
			}
		});

		SeekBar optionsAmbientLightLevel = (SeekBar)rootView.findViewById(R.id.optionsAmbientLightLevel);
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
		SeekBar optionsDirectionalLightLevel = (SeekBar)rootView.findViewById(R.id.optionsDirectionalLightLevel);
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
		CheckBox optionsPlacedLightsEnabled = (CheckBox)rootView.findViewById(R.id.optionsPlacedLightsEnabled);
		optionsPlacedLightsEnabled.setChecked(BethRenderSettings.isEnablePlacedLights());
		optionsPlacedLightsEnabled.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setEnablePlacedLights", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setEnablePlacedLights(((CheckBox)v).isChecked());
			}
		});


		CheckBox optionsLightOutlines = (CheckBox)rootView.findViewById(R.id.optionsLightOutlines);
		optionsLightOutlines.setChecked(BethRenderSettings.isOutlineLights());
		optionsLightOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineLights", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineLights(((CheckBox)v).isChecked());
			}
		});
		CheckBox optionsCharacterOutlines = (CheckBox)rootView.findViewById(R.id.optionsCharacterOutlines);
		optionsCharacterOutlines.setChecked(BethRenderSettings.isOutlineChars());
		optionsCharacterOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineChars", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineChars(((CheckBox)v).isChecked());
			}
		});
		CheckBox optionsDoorOutlines = (CheckBox)rootView.findViewById(R.id.optionsDoorOutlines);
		optionsDoorOutlines.setChecked(BethRenderSettings.isOutlineDoors());
		optionsDoorOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineDoors", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineDoors(((CheckBox)v).isChecked());
			}
		});
		CheckBox optionsContainerOutlines = (CheckBox)rootView.findViewById(R.id.optionsContainerOutlines);
		optionsContainerOutlines.setChecked(BethRenderSettings.isOutlineConts());
		optionsContainerOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineConts", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineConts(((CheckBox)v).isChecked());
			}
		});
		CheckBox optionsParticlesOutlines = (CheckBox)rootView.findViewById(R.id.optionsParticlesOutlines);
		optionsParticlesOutlines.setChecked(BethRenderSettings.isOutlineParts());
		optionsParticlesOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineParts", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineParts(((CheckBox)v).isChecked());
			}
		});
		CheckBox optionsFocusedObjectOutlines = (CheckBox)rootView.findViewById(R.id.optionsFocusedObjectOutlines);
		optionsFocusedObjectOutlines.setChecked(BethRenderSettings.isOutlineFocused());
		optionsFocusedObjectOutlines.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AndyESExplorerActivity.logFireBaseContent("setOutlineFocused", ""+ ((CheckBox)v).isChecked());
				BethRenderSettings.setOutlineFocused(((CheckBox)v).isChecked());
			}
		});
		this.closeButton = (Button) rootView.findViewById(R.id.optionsClose);
		closeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				dismiss();
			}
		});
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
