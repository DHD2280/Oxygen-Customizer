package android.view;

import android.graphics.HardwareRenderer;
import android.graphics.RecordingCanvas;

public class ThreadedRenderer extends HardwareRenderer {

    /**
     * Interface used to receive callbacks whenever a view is drawn by
     * a threaded renderer instance.
     */
    interface DrawCallbacks {
        /**
         * Invoked before a view is drawn by a threaded renderer.
         * This method can be used to apply transformations to the
         * canvas but no drawing command should be issued.
         *
         * @param canvas The Canvas used to render the view.
         */
        void onPreDraw(RecordingCanvas canvas);

        /**
         * Invoked after a view is drawn by a threaded renderer.
         * It is safe to invoke drawing commands from this method.
         *
         * @param canvas The Canvas used to render the view.
         */
        void onPostDraw(RecordingCanvas canvas);
    }

}
