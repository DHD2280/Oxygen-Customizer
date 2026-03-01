package com.android.systemui.plugins.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.service.quicksettings.Tile;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public interface QSTile {

    public static class State {
        public static final int VERSION = 1;
        public static final int DEFAULT_STATE = Tile.STATE_ACTIVE;

        public Icon icon;
        public Supplier<Icon> iconSupplier;
        public int state = DEFAULT_STATE;
        public CharSequence label;
        @Nullable
        public CharSequence secondaryLabel;
        public CharSequence contentDescription;
        @Nullable public CharSequence stateDescription;
        public CharSequence dualLabelContentDescription;
        public boolean disabledByPolicy;
        public boolean dualTarget = false;
        public boolean isTransient = false;
        public String expandedAccessibilityClassName;
        public boolean handlesLongClick = true;
        public boolean handlesSecondaryClick = false;
        public boolean hasLongClickEffect = true;
        @Nullable
        public Drawable sideViewCustomDrawable;
        public String spec;

        /** Get the state text. */
        public CharSequence getStateText(int arrayResId, Resources resources) {
            if (state == Tile.STATE_UNAVAILABLE || this instanceof QSTile.BooleanState) {
                String[] array = resources.getStringArray(arrayResId);
                return array[state];
            } else {
                return "";
            }
        }

        /** Get the text for secondaryLabel. */
        public CharSequence getSecondaryLabel(CharSequence stateText) {
            // Use a local reference as the value might change from other threads
            CharSequence localSecondaryLabel = secondaryLabel;
            if (TextUtils.isEmpty(localSecondaryLabel)) {
                return stateText;
            }
            return localSecondaryLabel;
        }

        public boolean copyTo(State other) {
            if (other == null) throw new IllegalArgumentException();
            if (!other.getClass().equals(getClass())) throw new IllegalArgumentException();
            final boolean changed = !Objects.equals(other.spec, spec)
                    || !Objects.equals(other.icon, icon)
                    || !Objects.equals(other.iconSupplier, iconSupplier)
                    || !Objects.equals(other.label, label)
                    || !Objects.equals(other.secondaryLabel, secondaryLabel)
                    || !Objects.equals(other.contentDescription, contentDescription)
                    || !Objects.equals(other.stateDescription, stateDescription)
                    || !Objects.equals(other.dualLabelContentDescription,
                    dualLabelContentDescription)
                    || !Objects.equals(other.expandedAccessibilityClassName,
                    expandedAccessibilityClassName)
                    || !Objects.equals(other.disabledByPolicy, disabledByPolicy)
                    || !Objects.equals(other.state, state)
                    || !Objects.equals(other.isTransient, isTransient)
                    || !Objects.equals(other.dualTarget, dualTarget)
                    || !Objects.equals(other.handlesLongClick, handlesLongClick)
                    || !Objects.equals(other.handlesSecondaryClick, handlesSecondaryClick)
                    || !Objects.equals(other.hasLongClickEffect, hasLongClickEffect)
                    || !Objects.equals(other.sideViewCustomDrawable, sideViewCustomDrawable);
            other.spec = spec;
            other.icon = icon;
            other.iconSupplier = iconSupplier;
            other.label = label;
            other.secondaryLabel = secondaryLabel;
            other.contentDescription = contentDescription;
            other.stateDescription = stateDescription;
            other.dualLabelContentDescription = dualLabelContentDescription;
            other.expandedAccessibilityClassName = expandedAccessibilityClassName;
            other.disabledByPolicy = disabledByPolicy;
            other.state = state;
            other.dualTarget = dualTarget;
            other.isTransient = isTransient;
            other.handlesLongClick = handlesLongClick;
            other.handlesSecondaryClick = handlesSecondaryClick;
            other.hasLongClickEffect = hasLongClickEffect;
            other.sideViewCustomDrawable = sideViewCustomDrawable;
            return changed;
        }

        @Override
        public String toString() {
            return toStringBuilder().toString();
        }

        // Used in dumps to determine current state of a tile.
        // This string may be used for CTS testing of tiles, so removing elements is discouraged.
        protected StringBuilder toStringBuilder() {
            final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
            sb.append("spec=").append(spec);
            sb.append(",icon=").append(icon);
            sb.append(",iconSupplier=").append(iconSupplier);
            sb.append(",label=").append(label);
            sb.append(",secondaryLabel=").append(secondaryLabel);
            sb.append(",contentDescription=").append(contentDescription);
            sb.append(",stateDescription=").append(stateDescription);
            sb.append(",dualLabelContentDescription=").append(dualLabelContentDescription);
            sb.append(",expandedAccessibilityClassName=").append(expandedAccessibilityClassName);
            sb.append(",disabledByPolicy=").append(disabledByPolicy);
            sb.append(",dualTarget=").append(dualTarget);
            sb.append(",isTransient=").append(isTransient);
            sb.append(",handlesSecondaryClick=").append(handlesSecondaryClick);
            sb.append(",hasLongClickEffect=").append(hasLongClickEffect);
            sb.append(",state=").append(state);
            sb.append(",sideViewCustomDrawable=").append(sideViewCustomDrawable);
            return sb.append(']');
        }

        public State copy() {
            State state = new State();
            copyTo(state);
            return state;
        }
    }

    /**
     * Distinguished from [BooleanState] for use-case purposes such as allowing null secondary label
     */
    class AdapterState extends State {
        public static final int VERSION = 1;
        public boolean value;
        public boolean forceExpandIcon;

        @Override
        public boolean copyTo(State other) {
            final AdapterState o = (AdapterState) other;
            final boolean changed = super.copyTo(other)
                    || o.value != value
                    || o.forceExpandIcon != forceExpandIcon;
            o.value = value;
            o.forceExpandIcon = forceExpandIcon;
            return changed;
        }

        @Override
        protected StringBuilder toStringBuilder() {
            final StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",value=" + value);
            rt.insert(rt.length() - 1, ",forceExpandIcon=" + forceExpandIcon);
            return rt;
        }

        @Override
        public State copy() {
            AdapterState state = new AdapterState();
            copyTo(state);
            return state;
        }
    }

    class BooleanState extends AdapterState {
        public static final int VERSION = 1;

        @Override
        public State copy() {
            BooleanState state = new BooleanState();
            copyTo(state);
            return state;
        }
    }

    public abstract class Icon {
        public static final int VERSION = 1;

        public abstract Drawable getDrawable(Context context);

        public Drawable getInvisibleDrawable(Context context) {
            return getDrawable(context);
        }

        public int getPadding() {
            return 0;
        }

        public int hashCode() {
            return Icon.class.hashCode();
        }

        @NonNull
        public String toString() {
            return "Icon";
        }
    }


}
