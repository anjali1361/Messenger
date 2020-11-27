package util

import android.content.Context
import android.net.ConnectivityManager

class ConnectionManager {

    fun checkConnectivity(context: Context): Boolean{
        val connectivityManager= context.getSystemService(Context.CONNECTIVITY_SERVICE )as ConnectivityManager//used to get notification about hardware functionality of network devices
        val activeNetwork = connectivityManager.activeNetworkInfo//used to fetch data about active ntwrks

        if(activeNetwork?.isConnected != null){//isconnected mthod would return 3 values i.e. true(if ntwrk hs internet cnnection),false,null(if ntwrk is broken or inactive)
            return activeNetwork.isConnected

        }else{
            return false
        }
    }
}