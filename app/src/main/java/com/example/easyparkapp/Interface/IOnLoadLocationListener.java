package com.example.easyparkapp.Interface;

import com.example.easyparkapp.MLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<MLatLng> latLngs);
    void onLoadLocationFailed(String message);

}
