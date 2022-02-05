package it.units.fantabasket.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import it.units.fantabasket.ui.NoConnectionActivity;

import static it.units.fantabasket.utils.Utils.MIO_TAG;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.w(MIO_TAG, "active connection --> " + isNetworkAvailable(context));
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Intent intentNew = new Intent(context, NoConnectionActivity.class);
                    intentNew.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentNew);
                    Log.e(MIO_TAG, "losing active connection --> " + isNetworkAvailable(context));
                }
            });
        }
    }
}