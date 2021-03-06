package com.rgb0101.ewhatimetable.home;

import java.util.ArrayList;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rgb0101.ewhatimetable.R;
import com.rgb0101.ewhatimetable.fragment.EwhaTimeTableAboutDeveloperFragment;
import com.rgb0101.ewhatimetable.fragment.EwhaTimeTableCalculatorFragment;
import com.rgb0101.ewhatimetable.fragment.EwhaTimeTableGridFragment;
import com.rgb0101.ewhatimetable.fragment.EwhaTimeTableMainFragment;
import com.rgb0101.ewhatimetable.fragment.EwhaTimeTableSearchFragment;
import com.rgb0101.ewhatimetable.timetabledata.EwhaTimeTableMyTimeTable;

public class EwhaHomeActivity extends FragmentActivity implements EwhaHomeInterface{
	public static final int MAIN= 0;
	public static final int SEARCH= 1;
	public static final int GRID= 2;
	public static final int CALC= 3;
	public static final int ABOUT_DEVELOPER= 4;
	
	private SharedPreferences pref= null;

	private static long backPressedTime= 0;
	private static Toast toast= null;
	
	private DrawerLayout mDrawer= null;
	private ListView mDrawerList= null;
	private ArrayList<String> mDrawerItemString= new ArrayList<String>();
	public static ArrayList<Integer> mColor= new ArrayList<Integer>();
	public static ArrayList<Boolean> mColorUsed= new ArrayList<Boolean>();
	
	private FragmentTransaction frgTransaction= null;
	private EwhaTimeTableMainFragment mMainFragment= null;
	private EwhaTimeTableSearchFragment mSearchFragment= null;
	private EwhaTimeTableGridFragment mGridFragment= null;
	private EwhaTimeTableCalculatorFragment mCalcFragment= null;
	private EwhaTimeTableAboutDeveloperFragment mAboutDeveloperFragment= null;
	
	private EwhaTimeTableMyTimeTable mTimeTable= null;
	
	// after
	private ProgressDialog dialog= null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
	}
	
	private void makeView(){
		mDrawer= (DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerList= (ListView)findViewById(R.id.drawer);
		
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long long_position){
				frgTransaction= getFragmentManager().beginTransaction();
				moveFragment(position);
				mDrawer.closeDrawer(mDrawerList);
			}
		});
		
		mMainFragment= new EwhaTimeTableMainFragment();
		mSearchFragment= new EwhaTimeTableSearchFragment();
		mGridFragment= new EwhaTimeTableGridFragment();
		mCalcFragment= new EwhaTimeTableCalculatorFragment();
		mAboutDeveloperFragment= new EwhaTimeTableAboutDeveloperFragment();
		
		frgTransaction= getFragmentManager().beginTransaction();
		frgTransaction.replace(R.id.container, mMainFragment);
		frgTransaction.commit();
	}
	
	public void moveFragment(int index){
		frgTransaction= getFragmentManager().beginTransaction();
		switch(index){
		case MAIN: frgTransaction.replace(R.id.container, mMainFragment); break;
		case SEARCH: frgTransaction.replace(R.id.container, mSearchFragment); break;
		case GRID: frgTransaction.replace(R.id.container, mGridFragment); break;
		case CALC: frgTransaction.replace(R.id.container, mCalcFragment); break;
		case ABOUT_DEVELOPER: frgTransaction.replace(R.id.container, mAboutDeveloperFragment); break;
		}
		frgTransaction.addToBackStack(null);
		frgTransaction.commit();
	}
	
	private void makeResources(){
		for(int i=0; i<4; i++) mDrawerItemString.add(getResources().getString(R.string.drawer01+i));
		mDrawerList.setAdapter(new ArrayAdapter(this, R.layout.draweritem, mDrawerItemString));
		for(int i=0; i<48; i++) {
			mColor.add(getResources().getColor(R.color.cellColor01+i));
			mColorUsed.add(false);
		}
		
		pref= getSharedPreferences("rgb0101", Context.MODE_PRIVATE);
		refreshTimeTable();
	}
	
	public void refreshTimeTable(){
		saveTimeTable();
		mTimeTable= new EwhaTimeTableMyTimeTable(this, pref);
	}
	
	public void saveTimeTable(){
		if(mTimeTable != null) mTimeTable.saveData(pref);
	}
	
	public ProgressDialog makeDialog(){
		dialog= new ProgressDialog(this);
		dialog.setMessage("Loading...");
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		
		return dialog;
    }
    public ProgressDialog getDialog(){
    	if(dialog != null) return dialog;
    	else return null;
    }
	
    public static int makeColor(){
		int color= 0;
		while(color < 48){
			if(!EwhaHomeActivity.mColorUsed.get(color)){
				EwhaHomeActivity.mColorUsed.set(color, true);
				return color;
			}
			color++;
		}
		return -1;
	}
    
	@Override
	public void onResume(){
		super.onResume();
		
		makeView();
		makeResources();
	}
	
	@Override
	public void onPause(){
		if(mDrawerItemString != null && mDrawerItemString.size() > 0) mDrawerItemString.clear();
		mTimeTable.saveData(pref);
		super.onPause();
	}
	
	@Override
  	public void onBackPressed(){
  		if(System.currentTimeMillis() > backPressedTime + 2000){
  			backPressedTime= System.currentTimeMillis();
  			toast= Toast.makeText(getApplicationContext(), getResources().getString(R.string.back_finish), Toast.LENGTH_SHORT);
  			toast.show();
  			return ;
  		}
  		if(System.currentTimeMillis() <= backPressedTime+2000){
  			toast.cancel();
  			super.onBackPressed();
  		}
  	}
	
	public EwhaTimeTableMainFragment getMainFragment(){
		if(mMainFragment != null) return mMainFragment;
		else return null;
	}
	public EwhaTimeTableSearchFragment getSearchFragment(){
		if(mSearchFragment != null) return mSearchFragment;
		else return null;
	}
	public EwhaTimeTableGridFragment getGridFragment(){
		if(mGridFragment != null) return mGridFragment;
		else return null;
	}
	public EwhaTimeTableAboutDeveloperFragment getAboutDeveloperFragment(){
		if(mAboutDeveloperFragment != null) return mAboutDeveloperFragment;
		else return null;
	}
	
	public EwhaTimeTableMyTimeTable getTimeTable(){ return mTimeTable; }
	public ArrayList<Integer> getColors(){ return mColor; }
	public SharedPreferences getPref(){ return pref; }
}
