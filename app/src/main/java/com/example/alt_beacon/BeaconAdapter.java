package com.example.alt_beacon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Vector;

public class BeaconAdapter extends BaseAdapter {


    private Vector<MyBeacon> beacons;
    private LayoutInflater layoutInflater;

    public BeaconAdapter(Vector<MyBeacon> beacons, LayoutInflater layoutInflater) {
        this.beacons = beacons;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BeaconHolder beaconHolder;
        if (convertView == null) {
            beaconHolder = new BeaconHolder();
            convertView = layoutInflater.inflate(R.layout.item_beacon, parent, false);
            beaconHolder.address = convertView.findViewById(R.id.address);
            beaconHolder.rssi = convertView.findViewById(R.id.rssi);

            /**  TxPower = 여기서는 TX Level로 수신됨*/
            beaconHolder.txpower = convertView.findViewById(R.id.txpower);
            beaconHolder.time = convertView.findViewById(R.id.time);
            convertView.setTag(beaconHolder);
        } else {
            beaconHolder = (BeaconHolder)convertView.getTag();
        }

        beaconHolder.time.setText("Time :" + beacons.get(position).getNow());
        beaconHolder.address.setText("MAC Addr :"+  beacons.get(position).getAddress());
        beaconHolder.txpower.setText("TXlevel :"+   beacons.get(position).getTxPower());
        beaconHolder.rssi.setText("RSSI :"+ beacons.get(position).getRssi() + "dBm");
        // beaconHolder.location.setText("location is " + beacons.get(position).getLocation());
        return convertView;
    }

    public String getAddress(int pos) {
        return beacons.get(pos).getAddress();
    }
    public int getRssi(int pos) {
        return beacons.get(pos).getRssi();
    }
    public int getTxPower(int pos) {
        return beacons.get(pos).getTxPower();
    }

    private class BeaconHolder {
        TextView address;
        TextView rssi;
        TextView txpower;
        TextView time;
        TextView location;

    }
}
