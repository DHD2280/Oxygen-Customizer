package it.dhd.oxygencustomizer.services.tiles;

import static it.dhd.oxygencustomizer.utils.Constants.ACTION_TILE_REMOVED;

import android.content.Intent;
import android.service.quicksettings.TileService;

public class CaffeineTileService extends TileService {

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        sendBroadcast(new Intent(ACTION_TILE_REMOVED));
    }

}
