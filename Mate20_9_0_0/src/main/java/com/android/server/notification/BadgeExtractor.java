package com.android.server.notification;

import android.content.Context;

public class BadgeExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "BadgeExtractor";
    private RankingConfig mConfig;

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    /* JADX WARNING: Missing block: B:26:0x0065, code skipped:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null || this.mConfig == null) {
            return null;
        }
        boolean userWantsBadges = this.mConfig.badgingEnabled(record.sbn.getUser());
        boolean appCanShowBadge = this.mConfig.canShowBadge(record.sbn.getPackageName(), record.sbn.getUid());
        if (!userWantsBadges || !appCanShowBadge) {
            record.setShowBadge(false);
        } else if (record.getChannel() != null) {
            boolean z = record.getChannel().canShowBadge() && appCanShowBadge;
            record.setShowBadge(z);
        } else {
            record.setShowBadge(appCanShowBadge);
        }
        if (record.isIntercepted() && (record.getSuppressedVisualEffects() & 64) != 0) {
            record.setShowBadge(false);
        }
        return null;
    }

    public void setConfig(RankingConfig config) {
        this.mConfig = config;
    }

    public void setZenHelper(ZenModeHelper helper) {
    }
}
