package com.juanrajc.groomerloc.adaptadores;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.juanrajc.groomerloc.fragmentsCita.FragChatCitaPelu;
import com.juanrajc.groomerloc.fragmentsCita.FragDatosCitaPelu;

public class AdaptadorPagCitaPelu extends FragmentPagerAdapter {

    private int numTabs;

    public AdaptadorPagCitaPelu(FragmentManager fm, int numTabs) {
        super(fm);

        this.numTabs = numTabs;

    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                return new FragDatosCitaPelu();

            case 1:
                return new FragChatCitaPelu();

                default:
                    return null;

        }

    }

    @Override
    public int getCount() {
        return numTabs;
    }

}
