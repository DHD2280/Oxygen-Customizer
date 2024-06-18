package it.dhd.oxygencustomizer.weather.receivers;

/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.dhd.oxygencustomizer.utils.UpdateScheduler;
import it.dhd.oxygencustomizer.weather.Config;
import it.dhd.oxygencustomizer.weather.WeatherUpdateService;

public class SystemReceiver extends BroadcastReceiver {
    private static final String TAG = "WeatherService:SystemReceiver";
    private static final boolean DEBUG = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (Config.isEnabled(context)) {
                WeatherUpdateService.scheduleUpdatePeriodic(context);
                WeatherUpdateService.scheduleUpdateNow(context);
            }
            UpdateScheduler.scheduleUpdates(context);
        }
    }
}