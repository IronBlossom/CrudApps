package ironblossom.csemock.experimental.ex_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ironblossom.csemock.R;
import ironblossom.csemock.experimental.ExMainActivity;
import ironblossom.csemock.experimental.utils.ShareItem;
import ironblossom.csemock.experimental.utils.ViewHolder;

public class ExFragWatchlist extends Fragment {
    public static final ArrayList<ShareItem> shareItemList = new ArrayList<ShareItem>();
    ListView shareListView;

    public ExFragWatchlist() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ex_frag_watchlist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        shareListView = (ListView) view.findViewById(R.id.shareListView);
        ShareItemAdapter adapter = new ShareItemAdapter(shareItemList);
        shareListView.setAdapter(adapter);
    }

    private class ShareItemAdapter extends BaseAdapter {
        ArrayList<ShareItem> localAdapter;
        LayoutInflater inflater;

        private ShareItemAdapter(ArrayList<ShareItem> localAdapter) {
            this.localAdapter = localAdapter;
            inflater = getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return localAdapter.size();
        }

        @Override
        public Object getItem(int position) {
            return localAdapter.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.ex_watchlist_row, parent, false);
            }
            TextView tvShareName = ViewHolder.get(convertView, R.id.shareName);
            TextView tvCompanyName = ViewHolder.get(convertView, R.id.companyName);
            TextView tvTotalPoint = ViewHolder.get(convertView, R.id.totalPoint);
            TextView tvIndex = ViewHolder.get(convertView, R.id.sharePointHiLo);
            TextView tvAlert = ViewHolder.get(convertView, R.id.alertTv);
            TextView tvDay = ViewHolder.get(convertView, R.id.dayTv);

            tvShareName.setText(localAdapter.get(position).getShareName());
            tvCompanyName.setText(localAdapter.get(position).getCompanyName());
            tvTotalPoint.setText(String.valueOf(localAdapter.get(position).getTotalPoint()));
            tvIndex.setText(String.valueOf(localAdapter.get(position).getIndex1()) + "\n" + String.valueOf(localAdapter.get(position).getIndex2()) + "%");
            tvAlert.setText("Alert" + "\n" + "Hi:" + String.valueOf(localAdapter.get(position).getAlertHi()) + "\n" + "Lo:" + String.valueOf(localAdapter.get(position).getAlertLo()));
            tvDay.setText("Day(Hi/Lo)" + "\n" + String.valueOf(localAdapter.get(position).getDayHi()) + "\n" + String.valueOf(localAdapter.get(position).getDayLo()));


            return convertView;
        }
    }

    private void populateItem() {
        shareItemList.clear();
        for (int i = 0; i < 10; i++) {
            ShareItem shareItem = new ShareItem();
            shareItem.setShareName(ExMainActivity.shareNames[i]);
            shareItem.setCompanyName(ExMainActivity.companyNames[i]);
            shareItem.setTotalPoint((float) ((i + 1) * 0.33f));
            shareItem.setIndex1((float) ((i + 1) * 0.5f));
            shareItem.setIndex2((float) ((i + 1) * 1.5f));
            shareItem.setAlertHi((float) ((i + 1) * 500f));
            shareItem.setAlertLo((float) ((i + 1) * 230f));
            shareItem.setDayHi((float) ((i + 1) * 230f));
            shareItem.setDayLo((float) ((i + 1) * 210f));
            shareItemList.add(shareItem);
        }

    }
}
