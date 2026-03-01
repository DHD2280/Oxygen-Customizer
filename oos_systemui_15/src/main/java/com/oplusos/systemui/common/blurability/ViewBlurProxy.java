package com.oplusos.systemui.common.blurability;

public class ViewBlurProxy {

    public BlurConfig getBlurConfig() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void applyBlurConfig() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlurAmount(float f2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final BlurType getBlurType() {
        throw new UnsupportedOperationException("Stub!");
    }

    public abstract class BlurType {
        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
        public BlurType() {
        }


        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeNon extends BlurType {

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeNon);
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public int hashCode() {
                return -1046104676;
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public String toString() {
                return "BlurTypeNon";
            }

            public BlurTypeNon() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeMotion extends BlurType {

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeMotion);
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public int hashCode() {
                return -250048889;
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public String toString() {
                return "BlurTypeMotion";
            }

            public BlurTypeMotion() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeWindowBg extends BlurType {

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeWindowBg);
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public int hashCode() {
                return -737016090;
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public String toString() {
                return "BlurTypeWindowBg";
            }

            public BlurTypeWindowBg() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypePlatformStatic extends BlurType {

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypePlatformStatic);
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public int hashCode() {
                return -2035994990;
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public String toString() {
                return "BlurTypePlatformStatic";
            }

            public BlurTypePlatformStatic() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeStatic extends BlurType {

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeStatic);
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public int hashCode() {
                return -74212033;
            }

            /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
            public String toString() {
                return "BlurTypeStatic";
            }

            public BlurTypeStatic() {
                super();
            }
        }
    }


}
