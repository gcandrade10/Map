package com.example.realtimamap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MyInfoWindowAdapter(context:Context) : GoogleMap.InfoWindowAdapter {
    private var myContentsView: View? = null
    init {
        myContentsView = LayoutInflater.from(context).inflate(R.layout.custom_info_contents,null)

    }
    override fun getInfoContents(p0: Marker?): View {
        val tvTitle = myContentsView?.findViewById(R.id.title) as TextView


        return myContentsView as View
    }

    override fun getInfoWindow(p0: Marker?): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

