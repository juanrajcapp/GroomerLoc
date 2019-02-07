package com.juanrajc.groomerloc.adaptadores;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.juanrajc.groomerloc.fragmentsCita.FragChatCitaCli;
import com.juanrajc.groomerloc.fragmentsCita.FragDatosCitaCli;

public class AdaptadorPagCitaCli extends FragmentPagerAdapter {

    private int numTabs;

    public AdaptadorPagCitaCli(FragmentManager fm, int numTabs) {
        super(fm);

        this.numTabs = numTabs;

    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                return new FragDatosCitaCli();

            case 1:
                return new FragChatCitaCli();

            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return numTabs;
    }

}
