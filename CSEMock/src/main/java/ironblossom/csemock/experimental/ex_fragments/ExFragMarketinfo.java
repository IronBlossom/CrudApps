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

public class ExFragMarketinfo extends Fragment {
    public static final ArrayList<ShareItem> gainerLoserList = new ArrayList<ShareItem>();
    ListView gainerLoserListView;

    public ExFragMarketinfo() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ex_frag_marketinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        gainerLoserListView = (ListView) view.findViewById(R.id.gainerLoserListView);
        GainerLoserAdapter adapter = new GainerLoserAdapter(gainerLoserList);
        gainerLoserListView.setAdapter(adapter);
    }

    private class GainerLoserAdapter extends BaseAdapter {
        ArrayList<ShareItem> localAdapter;
        LayoutInflater inflater;

        private GainerLoserAdapter(ArrayList<ShareItem> localAdapter) {
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
                convertView = inflater.inflate(R.layout.ex_market_row, parent, false);
            }
            TextView tvGainerName = ViewHolder.get(convertView, R.id.gainerName);
            TextView tvGainerPercent = ViewHolder.get(convertView, R.id.gainerPercent);
            TextView tvGainerIndex = ViewHolder.get(convertView, R.id.gainerIndex);
            TextView tvLoserName = ViewHolder.get(convertView, R.id.loserName);
            TextView tvLoserPercent = ViewHolder.get(convertView, R.id.loserPercent);
            TextView tvLoserIndex = ViewHolder.get(convertView, R.id.loserIndex);

            tvGainerName.setText(localAdapter.get(position).getShareName());
            tvGainerPercent.setText(String.valueOf(localAdapter.get(position).getIndex1() + localAdapter.get(position).getIndex2()) + "%");
            tvGainerIndex.setText(String.valueOf(localAdapter.get(position).getIndex1()));
            tvLoserName.setText(localAdapter.get(position).getShareName());
            tvLoserPercent.setText(String.valueOf(localAdapter.get(position).getIndex1() - localAdapter.get(position).getIndex2()) + "%");
            tvLoserIndex.setText(String.valueOf(localAdapter.get(position).getIndex2()));


            return convertView;
        }
    }

    private void populateItem() {
        gainerLoserList.clear();
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
            gainerLoserList.add(shareItem);
        }

    }
}
