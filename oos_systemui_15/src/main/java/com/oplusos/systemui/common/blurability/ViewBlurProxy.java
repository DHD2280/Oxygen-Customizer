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

        public BlurType() {
        }


        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeNon extends BlurType {


            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeNon);
            }


            public int hashCode() {
                return -1046104676;
            }


            public String toString() {
                return "BlurTypeNon";
            }

            public BlurTypeNon() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeMotion extends BlurType {


            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeMotion);
            }


            public int hashCode() {
                return -250048889;
            }


            public String toString() {
                return "BlurTypeMotion";
            }

            public BlurTypeMotion() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeWindowBg extends BlurType {


            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeWindowBg);
            }


            public int hashCode() {
                return -737016090;
            }


            public String toString() {
                return "BlurTypeWindowBg";
            }

            public BlurTypeWindowBg() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypePlatformStatic extends BlurType {


            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypePlatformStatic);
            }


            public int hashCode() {
                return -2035994990;
            }


            public String toString() {
                return "BlurTypePlatformStatic";
            }

            public BlurTypePlatformStatic() {
                super();
            }
        }

        /* compiled from: ViewBlurProxy.kt */
        public final class BlurTypeStatic extends BlurType {


            public boolean equals(Object obj) {
                return this == obj || (obj instanceof BlurTypeStatic);
            }


            public int hashCode() {
                return -74212033;
            }


            public String toString() {
                return "BlurTypeStatic";
            }

            public BlurTypeStatic() {
                super();
            }
        }
    }


}
