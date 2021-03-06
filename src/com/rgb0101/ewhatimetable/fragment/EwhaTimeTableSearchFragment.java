package com.rgb0101.ewhatimetable.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.rgb0101.ewhatimetable.R;
import com.rgb0101.ewhatimetable.SpinnerAdapter;
import com.rgb0101.ewhatimetable.home.EwhaHomeActivity;
import com.rgb0101.ewhatimetable.home.EwhaHomeInterface;
import com.rgb0101.ewhatimetable.searchdata.EwhaAdapter;
import com.rgb0101.ewhatimetable.searchdata.EwhaParse;
import com.rgb0101.ewhatimetable.searchdata.EwhaResult;
import com.rgb0101.ewhatimetable.searchdata.EwhaServer;
import com.rgb0101.ewhatimetable.searchdata.SearchData;
import com.rgb0101.ewhatimetable.timetabledata.EwhaTimeTableMyTimeTable;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.AbsListView.OnScrollListener;

public class EwhaTimeTableSearchFragment extends Fragment {
	private boolean tutorialHidden= false;
	private EwhaHomeInterface homeInterface= null;
	
	private static final String TAG= "EwhaTimeTable";
	public static final String site= "http://eureka.ewha.ac.kr/eureka/hs/sg/openHssg504021q.do?popupYn=Y";
	
	private SharedPreferences pref= null;
	
	private static View wholeView= null;
	// search no menu
	private ViewAnimator menu= null;
	private TranslateAnimation anim= null;
	private TextView mShowMenu= null;
	private TextView mSearch= null;
	private ListView mList= null;
	private boolean isMenuVisible= true;
	
	// search menu
	private EditText yearEdit= null;
	private Spinner semesterSpin= null;
	private Spinner semKindSpin= null;
	private Spinner univSpin= null;
	private Spinner majSpin= null;
	private Spinner subKindSpin= null;
	private Spinner gradeSpin= null;
	private Spinner daySpin= null;
	private Spinner timeSpin= null;
	private EditText subNumberEdit= null;
	private EditText subNameEdit= null;
	private EditText profNameEdit= null;
	
	// for data
	private ArrayList<String> semesterList= new ArrayList<String>();
	private ArrayList<String> semKindList= new ArrayList<String>();
	private ArrayList<String> univList= null;
	private ArrayList<ArrayList<String>> majList= new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> majValueList= new ArrayList<ArrayList<String>>();
	private ArrayList<String> subKindList= null;
	private ArrayList<String> subKindValueList= null;
	private ArrayList<String> dayList= null;
	private ArrayList<String> timeList= null;
	private ArrayList<String> gradeList= null;
	private ArrayList<String> defaultList= new ArrayList<String>();
	
	private ArrayList<SpinnerAdapter> majAdapterList= new ArrayList<SpinnerAdapter>();
	private ArrayList<String> majPostValue= null;
	private String[] majValue= {"01", "12", "02", "13", "20", "21", "06", "15", "08", "18", "10", "19", "17", "03", "04", "14", "05", "09", "11", "16"};
	private int[] isMajListExist= {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -1, -1, 12, -1, -1, -1, -1};
	private SpinnerAdapter defaultAdapter= null;
	private PopupWindow mPopup= null;
	
	private EwhaServer mServer= null;
	
	private EwhaParse mParse= null;
	
	private SearchData searchData= null;
	private ArrayList<EwhaResult> mResult= null;
	private EwhaResult mSelected= null;
	private EwhaAdapter resultContent= null;
	
	private InputMethodManager imm= null;
	private EwhaTimeTableMyTimeTable mTimeTable= null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		wholeView= inflater.inflate(R.layout.fragment_search, null);
		return wholeView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		makeView();
		makeResource();
		
		loadPreferences();
	}
	@Override
	public void onPause(){
		super.onPause();
		
		if(mResult != null && mResult.size() > 0){
			savePreferences(false);
			mResult.clear();
		}
		else savePreferences(true);
		
		SharedPreferences.Editor edit= ((EwhaHomeActivity)getActivity()).getPref().edit();
		edit.putBoolean("searchTutorialHidden", tutorialHidden);
		edit.commit();
	}
	
	private void makeView(){
		menu= (ViewAnimator)wholeView.findViewById(R.id.animmator_menu);
		yearEdit= (EditText)wholeView.findViewById(R.id.yearCondition);
		semesterSpin= (Spinner)wholeView.findViewById(R.id.semesterCondition);
		semKindSpin= (Spinner)wholeView.findViewById(R.id.semKindCondition);
		univSpin= (Spinner)wholeView.findViewById(R.id.univCondition);
		majSpin= (Spinner)wholeView.findViewById(R.id.majorCondition);
		subKindSpin= (Spinner)wholeView.findViewById(R.id.subjectCondition);
		subNumberEdit= (EditText)wholeView.findViewById(R.id.subjectNumber);
		subNameEdit= (EditText)wholeView.findViewById(R.id.subjectName);
		profNameEdit= (EditText)wholeView.findViewById(R.id.professorName);
		daySpin= (Spinner)wholeView.findViewById(R.id.dayCondition);
		timeSpin= (Spinner)wholeView.findViewById(R.id.timeCondition);
		gradeSpin= (Spinner)wholeView.findViewById(R.id.gradeCondition);
		mSearch= (TextView)wholeView.findViewById(R.id.search);
		mShowMenu= (TextView)wholeView.findViewById(R.id.showmenu);
		mList= (ListView)wholeView.findViewById(R.id.resultlist);
		
		yearEdit.setText(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		
		menu.bringToFront();
		
		menu.setVisibility(View.INVISIBLE);
		menu.setOnClickListener(click);
    	mSearch.setOnClickListener(click);
    	mShowMenu.setOnClickListener(click);
    	mList.setOnItemClickListener(resultSel);
    	mList.setOnScrollListener(new OnScrollListener(){
    		@Override
    		public void onScroll(AbsListView view, int firstVisible, int visibleItemCount, int totalItemCount){}
    		
    		@Override
    		public void onScrollStateChanged(AbsListView view, int pos){
    			if(isMenuVisible) hideMenu();
    		}
    	});
    	
    	((RelativeLayout)wholeView.findViewById(R.id.tutorial02HiddenOnce)).setOnClickListener(click);
    	((CheckBox)wholeView.findViewById(R.id.tutorial02HiddenForever)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				tutorialHidden= true;
				((RelativeLayout)wholeView.findViewById(R.id.searchFragmentTutorial)).setVisibility(View.INVISIBLE);
			}
		});//.setOnClickListener(click);
    	
    	imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    	mParse= new EwhaParse(getActivity());
	}
	
	private void makeResource(){
    	semesterList.add(Integer.toString(1)+getResources().getString(R.string.semesterText));
    	semesterList.add(Integer.toString(2)+getResources().getString(R.string.semesterText));
    	semKindList.add(getResources().getString(R.string.semesterKind01));
    	semKindList.add(getResources().getString(R.string.semesterKind02));
    	defaultList.add(getResources().getString(R.string.defaultspinner));
    	univList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.universe)));
    	subKindList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.subjectdiv)));
    	subKindValueList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.subjectdivvalue)));
    	gradeList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.grade)));
    	dayList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.days)));
    	timeList= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.time)));
    	defaultAdapter= new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, defaultList);
    	
    	int listId= R.array.univ01;
    	for(int i=0; i<majValue.length; i++){
    		int index= isMajListExist[i];
    		if(index != -1){
    			majList.add(new ArrayList<String>(Arrays.asList(getResources().getStringArray(listId++))));
    			majValueList.add(new ArrayList<String>(Arrays.asList(getResources().getStringArray(listId++))));
    			majAdapterList.add(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, majList.get(index)));
    		}
    		else{
    			majList.add(defaultList);
    			majAdapterList.add(defaultAdapter);
    		}
    	}
    	
    	semesterSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, semesterList));
    	semKindSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, semKindList));
    	univSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, univList));
    	majSpin.setAdapter(defaultAdapter);
    	subKindSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, subKindList));
    	gradeSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, gradeList));
    	daySpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, dayList));
    	timeSpin.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.spinner_text, timeList));
    	univSpin.setOnItemSelectedListener(listener);
    	
    	searchData= new SearchData(getActivity());
    	mServer= new EwhaServer(getActivity());
    	
    	homeInterface= (EwhaHomeInterface)getActivity();
    	
    	tutorialHidden= ((EwhaHomeActivity)getActivity()).getPref().getBoolean("searchTutorialHidden", false); 
    	if(tutorialHidden) ((RelativeLayout)wholeView.findViewById(R.id.searchFragmentTutorial)).setVisibility(View.INVISIBLE);
	}
	
	private void viewPopup(EwhaResult selected){
    	View popup= getActivity().getLayoutInflater().inflate(R.layout.popup_view, null);
    	mPopup= new PopupWindow(popup, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    	mPopup.setAnimationStyle(0);
    	mPopup.setFocusable(true);
    	
    	((TextView)popup.findViewById(R.id.subNamePopup)).setText(selected.getSubName());
    	((TextView)popup.findViewById(R.id.profPopup)).setText(selected.getProf());
    	((TextView)popup.findViewById(R.id.subNumPopup)).setText(selected.getSubNum()+"-"+selected.getClassNum());
    	((TextView)popup.findViewById(R.id.subKindPopup)).setText(selected.getSubKind());
    	((TextView)popup.findViewById(R.id.majorPopup)).setText(selected.getSubKind());
    	((TextView)popup.findViewById(R.id.gradePopup)).setText(selected.getGradeValue() + " / " + selected.getTime());
    	((TextView)popup.findViewById(R.id.lecturePoup)).setText(selected.getLecture());
    	((TextView)popup.findViewById(R.id.isEnglishPopup)).setText(selected.getIsEng());
    	((TextView)popup.findViewById(R.id.studentCountPopup)).setText(selected.getStudent());
    	((TextView)popup.findViewById(R.id.etcmsgPopup)).setText(selected.getEtcmsg());
    	
    	((TextView)popup.findViewById(R.id.close)).setOnClickListener(click);
    	((RelativeLayout)popup.findViewById(R.id.popupBackground)).setOnClickListener(click);
    	((CheckBox)popup.findViewById(R.id.addTimeTable)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int i, j;
				
				if(isChecked){
					boolean isAlready= false, isSame= false;
					int color= EwhaHomeActivity.makeColor();
					if(mTimeTable == null) mTimeTable= ((EwhaHomeActivity)getActivity()).getTimeTable();
					StringTokenizer token= new StringTokenizer(mSelected.getLecture(), "\n");
					while(token.hasMoreTokens()){
						StringTokenizer ins_token= new StringTokenizer(token.nextToken(), "/");
						if(ins_token.hasMoreTokens()){
							StringTokenizer div= new StringTokenizer(ins_token.nextToken(), " ");
							String day= div.nextToken();
							
							for(i=1; i<dayList.size()-1; i++)
								if(day.equals(dayList.get(i)))
									break;
							if(i<dayList.size()-1){
								StringTokenizer timeTokening= new StringTokenizer(div.nextToken(), "~");
								int start= Integer.parseInt(timeTokening.nextToken()), end= Integer.parseInt(timeTokening.nextToken());
								for(j=start; j<=end; j++){
									if(mTimeTable.getSubject(i-1, j-1).getRawData() != null){
										String str= mTimeTable.getSubject(i-1, j-1).getRawData().getSubNum()+"-"+mTimeTable.getSubject(i-1, j-1).getRawData().getClassNum();
										if(!str.equalsIgnoreCase(mSelected.getSubNum()+"-"+mSelected.getClassNum())) buttonView.setChecked(false);
										else isSame= true;
										isAlready= true;
										break;
									}
								}
								if(isAlready && !isSame) Toast.makeText(getActivity(), getResources().getString(R.string.overlayClass), Toast.LENGTH_SHORT).show();
								else if(!isAlready && !isSame){
									String spot= ins_token.nextToken().replace(" ", "");
									for(j=start; j<=end; j++) mTimeTable.addSubject(i, j, color, spot, mSelected);
									mPopup.dismiss();
								}
							}
						}
					}
				} else{
					
					StringTokenizer token= new StringTokenizer(mSelected.getLecture(), "\n");
					while(token.hasMoreTokens()){
						StringTokenizer ins_token= new StringTokenizer(token.nextToken(), "/");
						if(ins_token.hasMoreTokens()){
							StringTokenizer div= new StringTokenizer(ins_token.nextToken(), " ");
							String day= div.nextToken();
							
							for(i=1; i<dayList.size()-1; i++)
								if(day.equals(dayList.get(i))) break;
							if(i<dayList.size()-1){
								StringTokenizer timeTokening= new StringTokenizer(div.nextToken(), "~");
								int start= Integer.parseInt(timeTokening.nextToken()), end= Integer.parseInt(timeTokening.nextToken());
								for(j=start; j<=end; j++) mTimeTable.removeSubject(i-1, j-1);
							}
						}
					}
					
				}
			}
		});
    	
    	((CheckBox)popup.findViewById(R.id.addTimeTable)).setChecked(isExisted());
    	
    	mPopup.showAtLocation(popup, Gravity.CENTER, 0, 0);
    }
	
	private boolean isExisted(){
		if(mSelected != null){
			int i, j;
			if(mTimeTable == null) mTimeTable= ((EwhaHomeActivity)getActivity()).getTimeTable();
			StringTokenizer token= new StringTokenizer(mSelected.getLecture(), "\n");
			while(token.hasMoreTokens()){
				StringTokenizer ins_token= new StringTokenizer(token.nextToken(), "/");
				if(ins_token.hasMoreTokens()){
					StringTokenizer div= new StringTokenizer(ins_token.nextToken(), " ");
					String day= div.nextToken();
					
					for(i=1; i<dayList.size()-1; i++)
						if(day.equals(dayList.get(i))) break;
					if(i<dayList.size()-1){
						StringTokenizer timeTokening= new StringTokenizer(div.nextToken(), "~");
						int index= Integer.parseInt(timeTokening.nextToken()); //, end= Integer.parseInt(timeTokening.nextToken());
						if(mTimeTable.getSubject(i-1, index-1).getRawData() != null){
							EwhaResult cell=mTimeTable.getSubject(i-1, index-1).getRawData(); 
							String str= cell.getSubNum()+"-"+cell.getClassNum();
							if(str.equalsIgnoreCase(mSelected.getSubNum()+"-"+mSelected.getClassNum())) return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	View.OnClickListener click= new View.OnClickListener() {
		public void onClick(View v) {
			int id= v.getId();
			
			if(id == R.id.tutorial02HiddenOnce) ((RelativeLayout)wholeView.findViewById(R.id.searchFragmentTutorial)).setVisibility(View.INVISIBLE);
			else if(id == R.id.showmenu)
				showMenu();
			else if(id != R.id.animmator_menu && id != R.id.relative_menu){
				if(id == R.id.search){
					mResult= null;
					
					int idx;
					searchData.setYearCd(yearEdit.getText().toString());
					searchData.setSemester(semesterSpin.getSelectedItemPosition()+1);
					searchData.setSemKind(semKindSpin.getSelectedItemPosition());
					idx= univSpin.getSelectedItemPosition();
					searchData.setUniv(idx == 0 ? null : majValue[idx-1]);
					idx= majSpin.getSelectedItemPosition();
					searchData.setMaj(idx == 0 ? null : majPostValue.get(idx-1));
					idx= subKindSpin.getSelectedItemPosition();
					searchData.setSubKind(idx == 0 ? null : subKindValueList.get(idx-1));
					searchData.setSubName(subNameEdit.getText().toString());
					searchData.setSubNumber(subNumberEdit.getText().toString());
					searchData.setProfessor(profNameEdit.getText().toString());
					idx= gradeSpin.getSelectedItemPosition();
					if(idx != 0) searchData.setGrade(Integer.toString(idx));
					else searchData.setGrade("");
					searchData.setDay(daySpin.getSelectedItemPosition());
					searchData.setTime(timeSpin.getSelectedItemPosition());
					
					if(imm.isAcceptingText()) imm.hideSoftInputFromInputMethod(yearEdit.getWindowToken(), 0);
					if(isMenuVisible) hideMenu();
					
					if((searchData.getUniv() == null || searchData.getUniv().equalsIgnoreCase("")) && (searchData.getMaj() == null || searchData.getMaj().equalsIgnoreCase("")) &&
    						(searchData.getSubKind() == null || searchData.getSubKind().equalsIgnoreCase("")) && searchData.getDay() == 0 &&
    						searchData.getTime() == 0 && (searchData.getSubNumber() == null || searchData.getSubNumber().equalsIgnoreCase("")) &&
    						(searchData.getSubName() == null || searchData.getSubName().equalsIgnoreCase("")) && (searchData.getProfessor() == null || searchData.getProfessor().equalsIgnoreCase(""))){
    					Toast.makeText( getActivity(), getResources().getString(R.string.err), Toast.LENGTH_SHORT).show();
    				}
    				else{
    					homeInterface.makeDialog().show();
        				mServer.parse(site, searchData);
    				}
				} else if(id == R.id.close || id == R.id.popupBackground){
					if(mPopup != null) mPopup.dismiss();
				} else if(id != R.id.relativePopup)
					if(isMenuVisible) hideMenu();
			}
		}
	};
	
	public EwhaHomeInterface getInterface(){
		return homeInterface;
	}
	
	AdapterView.OnItemClickListener resultSel= new AdapterView.OnItemClickListener() {
    	@Override
    	public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
    		if(adapter.getId() == R.id.resultlist){
    			mSelected= mResult.get(position);
    			Log.d("test", mResult.get(position).toString());
    			viewPopup(mSelected);
    			if(isMenuVisible) hideMenu();
    		}
    	}
	};
    
    AdapterView.OnItemSelectedListener listener= new AdapterView.OnItemSelectedListener() {
    	@Override
    	public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id){
    		Spinner sel= (Spinner)adapter;
    		if(sel.getId() == R.id.univCondition){
    			if(pos != 0){
        			majPostValue= isMajListExist[pos] != -1 ? majValueList.get(pos-1) : null;
            		majSpin.setAdapter(majAdapterList.get(pos-1));
        		} else {
        			majPostValue= null;
        			majSpin.setAdapter(defaultAdapter);
        		}
    		}
    	}
    	
    	@Override
    	public void onNothingSelected(AdapterView<?> adapter){}
	};
	
	private void showMenu(){
		isMenuVisible= true;
		anim= new TranslateAnimation(0, 0, -menu.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { menu.setVisibility(View.VISIBLE); }
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override public void onAnimationEnd(Animation animation) {}
		});
		menu.startAnimation(anim);
	}
	
	private void hideMenu(){
		isMenuVisible= false;
		anim= new TranslateAnimation(0, 0, 0, -menu.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override public void onAnimationEnd(Animation animation) { menu.setVisibility(View.INVISIBLE); }
		});
		menu.startAnimation(anim);
	}
	public void setParse(EwhaParse parse){ mParse= parse; }
    public void setResult(ArrayList<EwhaResult> result){ mResult= result; }
    public void setResultAdpater(EwhaAdapter adapter){ resultContent= null; resultContent= adapter; }
    public EwhaParse getParse(){
    	if(mParse != null) return mParse;
    	else return null; 
    }
    public ArrayList<EwhaResult> getResult(){
    	if(mResult != null) return mResult;
    	else return null;
    }
    public EwhaAdapter getAdapter(){
    	if(resultContent != null) return resultContent;
    	else return null;
    }
    public ListView getListView(){ return mList; }
    public View getView(){ return wholeView; }
    
    private void loadPreferences(){
		pref= getActivity().getSharedPreferences("saving", getActivity().MODE_PRIVATE);
		
		int count= pref.getInt("lastCount", 0);
		if(count > 0){
			for(int i=0; i<count; i++){
				String str= "lastValue"+Integer.toString(i);
				EwhaResult result= new EwhaResult(getActivity());
				result.setSubName(pref.getString(str+"subName", ""));
				result.setSubNum(pref.getString(str+"subNum", ""));
				result.setClassNum(pref.getString(str+"classNum", ""));
				result.setSubKind(pref.getString(str+"subKind", ""));
				result.setMaj(pref.getString(str+"Major", ""));
				result.setGrade(pref.getString(str+"grade", ""));
				result.setProf(pref.getString(str+"prof", ""));
				result.setGradeValue(pref.getString(str+"gradeValue", ""));
				result.setTime(pref.getString(str+"time", ""));
				result.setLecture(pref.getString(str+"lecture", ""));
				result.setClassName(pref.getString(str+"className", ""));
				result.setIsEng(pref.getString(str+"isEnglish", ""));
				result.setStudent(pref.getString(str+"student", ""));
				result.setEtcmsg(pref.getString(str+"etcmsg", ""));
				result.setKorLecturePlan(pref.getString(str+"korLecturePlan", ""));
				result.setEngLecturePlan(pref.getString(str+"engLecturePlan", ""));
				
				if(mResult == null) mResult= new ArrayList<EwhaResult>();
				mResult.add(result);
			}
			
			mList.setAdapter(new EwhaAdapter(getActivity(), R.layout.listitem, mResult));
		}
	}
	private void savePreferences(boolean dataEmpty){
		pref= getActivity().getSharedPreferences("saving", getActivity().MODE_PRIVATE);
		SharedPreferences.Editor edit= pref.edit();
		if(!dataEmpty) {
			edit.putInt("lastCount", mResult.size());
			for(int i=0; i< mResult.size(); i++){
				EwhaResult result= mResult.get(i);
				String str= "lastValue"+Integer.toString(i);
				edit.putString(str+"subName", result.getSubName());
				edit.putString(str+"subNum", result.getSubNum());
				edit.putString(str+"classNum", result.getClassNum());
				edit.putString(str+"subKind", result.getSubKind());
				edit.putString(str+"Major", result.getMaj());
				edit.putString(str+"grade", result.getGrade());
				edit.putString(str+"prof", result.getProf());
				edit.putString(str+"gradeValue", result.getGradeValue());
				edit.putString(str+"time", result.getTime());
				edit.putString(str+"lecture", result.getLecture());
				edit.putString(str+"className", result.getClassName());
				edit.putString(str+"isEnglish", result.getIsEng());
				edit.putString(str+"student", result.getStudent());
				edit.putString(str+"etcmsg", result.getEtcmsg());
				edit.putString(str+"korLecturePlan", result.getKorLecturePlan());
				edit.putString(str+"engLecturePlan", result.getEngLecturePlan());
			}
		}
		else edit.clear();
		edit.commit();
	}
}
