package glay.ash.shortcut2.fragment;
import static glay.ash.shortcut2.Constants.*;
import glay.ash.shortcut2.IntentUtil;
import glay.ash.shortcut2.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationDetailFragment extends ListFragment implements OnItemLongClickListener{

	private OnActivitySelectedListener mListener;

	public ApplicationDetailFragment() {
		// Required empty public constructor
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if(activity instanceof OnActivitySelectedListener){
			mListener = (OnActivitySelectedListener) activity;
		}
		
		Context c = activity.getApplicationContext();
		PackageManager pm = c.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(getArguments().getString(BUNDLE_KEY_PACKAGE_NAME), PackageManager.GET_ACTIVITIES);
			
			List<ActivityInfo> infos = Arrays.asList(info.activities);
			Collections.sort(infos, new Comparator<ActivityInfo>() {
				@Override
				public int compare(ActivityInfo lhs, ActivityInfo rhs) {
					if(lhs.exported ^ rhs.exported){
						return lhs.exported ? -1 : 1;
					}else{
						return lhs.name.compareTo(rhs.name);						
					}
				}
			});
			
			setListAdapter(new ActivityListAdapter(infos, c));
		} catch (NameNotFoundException e) {
			
			if(mListener != null){
				mListener.onPackageNotFound(getArguments().getString(BUNDLE_KEY_PACKAGE_NAME));
			}
			
			e.printStackTrace();
		}
	}			

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setOnItemLongClickListener(this);
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		PackageManager pm = getActivity().getPackageManager();
		String packageName = getArguments().getString(BUNDLE_KEY_PACKAGE_NAME);
		
		View header = inflater.inflate(R.layout.list_header, null);
		ImageView iv = (ImageView)header.findViewById(R.id.applicationIcon);
		TextView tv = (TextView)header.findViewById(R.id.applicationLabel);
		try {
			tv.setText(IntentUtil.labelFromPackageName(pm, packageName));
			iv.setImageBitmap(IntentUtil.iconFromPackageName(pm, packageName));
			getListView().addHeaderView(header, null, false);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (null != mListener) {

			 mListener.onItemClicked((ActivityInfo) getListAdapter().getItem((int)id));
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
		if(mListener != null){
			mListener.onItemLongClick((ActivityInfo) getListAdapter().getItem((int)id));
		}
		return true;
	}
	
	private static class ActivityListAdapter extends BaseAdapter{
		final private List<ActivityInfo> aInfos;
		final private LayoutInflater inflater;
		final private PackageManager pm;
		
		private ActivityListAdapter(List<ActivityInfo> aInfos, Context c){
			this.aInfos = aInfos;
			this.inflater = LayoutInflater.from(c);
			this.pm = c.getPackageManager();
		}

		@Override
		public int getCount() {
			return aInfos.size();
		}

		@Override
		public ActivityInfo getItem(int position) {
			return aInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			if(convertView == null){
				convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
			}
			
			TextView tv1 = (TextView)convertView.findViewById(android.R.id.text1);
			TextView tv2 = (TextView)convertView.findViewById(android.R.id.text2);
			tv1.setText(getItem(position).loadLabel(pm));
			tv1.setTextColor(isEnabled(position) ? Color.BLACK : Color.LTGRAY);
			tv2.setText(getItem(position).name);
			tv2.setTextColor(isEnabled(position) ? Color.GRAY : Color.LTGRAY);
			
			return convertView;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return getItem(position).exported;
		}
		
	}
	
	public interface OnActivitySelectedListener {
		public void onItemClicked(ActivityInfo activityInfo);
		public void onItemLongClick(ActivityInfo activityInfo);
		public void onPackageNotFound(String packageName);
	}

}
