package com.browserblock.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class BrowserDetector {
    private static final Set<String> KNOWN_BROWSER_PACKAGES = new HashSet<>(Arrays.asList(
        "com.android.browser", "com.android.chrome", "com.chrome.beta", "com.chrome.canary",
        "com.chrome.dev", "org.chromium.chrome", "org.chromium.webview_shell",
        "com.brave.browser", "com.brave.browser_beta", "com.brave.browser_nightly",
        "com.kiwibrowser.browser", "org.mozilla.firefox", "org.mozilla.firefox_beta",
        "org.mozilla.fenix", "org.mozilla.fennec_fdroid", "org.mozilla.focus",
        "org.mozilla.klar", "org.mozilla.reference.browser", "com.microsoft.emmx",
        "com.microsoft.emmx.beta", "com.microsoft.emmx.canary", "com.microsoft.emmx.dev",
        "com.opera.browser", "com.opera.browser.beta", "com.opera.mini.native",
        "com.opera.mini.native.beta", "com.opera.touch", "com.sec.android.app.sbrowser",
        "com.sec.android.app.sbrowser.beta", "com.duckduckgo.mobile.android",
        "com.vivaldi.browser", "com.vivaldi.browser.snapshot", "com.vivaldi.browser.sopranos",
        "com.yandex.browser", "com.yandex.browser.beta", "com.yandex.browser.alpha",
        "com.yandex.browser.lite", "com.UCMobile.intl", "com.uc.browser.en",
        "com.uc.browser.hd", "com.ucturbo", "com.cloudmosa.puffinFree",
        "com.cloudmosa.puffin", "com.cloudmosa.puffinIncognito", "com.amazon.cloud9",
        "com.amazon.cloud9.kids", "com.mi.globalbrowser", "com.mi.globalbrowser.mini",
        "com.heytap.browser", "com.coloros.browser", "com.huawei.browser",
        "com.huawei.browser.beta", "com.hicloud.browser", "com.zui.browser",
        "com.lenovo.browser", "com.vivo.browser", "com.transsion.phoenix",
        "com.transsion.phoenix.lite", "com.aloha.browser", "com.aloha.browser.lite",
        "com.ecosia.android", "com.qwant.liberty", "com.startpage.app",
        "com.avast.android.secure.browser", "com.avg.android.secure.browser",
        "com.cake.browser", "com.stoutner.privacybrowser.standard",
        "com.stoutner.privacybrowser.free", "com.cookiegames.smartcookie",
        "com.cookiegames.smartcookiepro", "acr.browser.lightning", "acr.browser.barebones",
        "mark.via.gp", "mark.via", "com.xbrowser.play", "com.xbrowser.pro",
        "com.oh.bro", "com.oh.brop", "com.fevdev.nakedbrowser",
        "com.fevdev.nakedbrowserpro", "org.bromite.bromite", "org.bromite.chromium",
        "com.celzero.bravedns", "org.torproject.torbrowser", "org.torproject.torbrowser_alpha",
        "info.guardianproject.orfox", "org.gnu.icecat", "org.ironfoxoss.ironfox",
        "io.github.forkmaintainers.iceraven", "com.github.kiwibrowser.src.next",
        "org.adblockplus.browser", "com.adblockbrowser", "com.ghostery.android.ghostery",
        "com.ghostery.android.ghostery_private_browser", "com.kaspersky.safekids",
        "com.kaspersky.browser", "com.naver.whale", "com.naver.whale.beta",
        "com.naver.whale.dev", "com.lge.browser", "com.sonymobile.browser",
        "com.asus.browser", "com.htc.sense.browser", "com.motorola.browser",
        "com.blackberry.browser", "com.jio.web", "com.jio.browser",
        "com.tencent.mtt", "com.tencent.mtt.intl", "com.baidu.browser.inter",
        "com.baidu.browser.apps", "com.ksmobile.cb", "com.mx.browser",
        "com.mx.browser.tablet", "com.apusapps.browser", "com.apusapps.browser.turbo",
        "com.ijinshan.browser_fast", "com.fast.browser", "com.talpa.hibrowser",
        "com.talpa.pibrowser", "com.generalmobi.go.browser", "com.rainsee.create",
        "com.quark.browser", "com.lemurbrowser.exts", "com.hsv.freeadblockerbrowser",
        "com.rocket.browser", "com.bharat.browser", "com.fulldive.extension",
        "com.fulldive.mobile", "com.appsverse.privatebrowser", "com.appsverse.photon",
        "com.appsverse.photonbrowser", "com.ksmobile.browser", "com.pure.browser",
        "com.purebrowser.mini", "com.veera.browser", "com.tempest.browser",
        "com.sidekick.browser", "com.arc.browser", "company.thebrowser.browser",
        "net.waterfox.android.release", "net.waterfox.android.beta",
        "org.mozilla.fennec_aurora", "org.mozilla.firefox.vpn", "org.lineageos.jelly",
        "com.android.browser2", "com.google.android.apps.chrome", "com.google.android.apps.chrome_dev"
    ));

    private static final Set<String> KNOWN_YOUTUBE_PACKAGES = new HashSet<>(Arrays.asList(
        "com.google.android.youtube",
        "com.google.android.youtube.tv",
        "com.google.android.youtube.go",
        "com.google.android.apps.youtube.kids",
        "com.google.android.apps.youtube.music",
        "com.google.android.apps.youtube.creator",
        "com.google.android.apps.youtube.gaming",
        "com.google.android.apps.youtube.vr",
        "com.vanced.android.youtube",
        "com.vanced.android.apps.youtube.music",
        "com.vanced.android.youtube.tv",
        "com.vanced.android.youtube.kids",
        "app.revanced.android.youtube",
        "app.revanced.android.apps.youtube.music",
        "app.rvx.android.youtube",
        "app.rvx.android.apps.youtube.music",
        "app.rvx.android.youtube.tv",
        "app.rvx.android.apps.youtube.kids",
        "app.revanced.extended.youtube",
        "app.revanced.extended.apps.youtube.music",
        "com.reyoutube.android.youtube",
        "com.reyoutube.android.apps.youtube.music",
        "com.google.android.youtube.revanced",
        "com.google.android.apps.youtube.music.revanced"
    ));

    private BrowserDetector() {}

    static Set<String> installedBrowsers(Context context) {
        Set<String> packages = new HashSet<>(KNOWN_BROWSER_PACKAGES);
        packages.addAll(KNOWN_YOUTUBE_PACKAGES);
        PackageManager pm = context.getPackageManager();

        Intent browserCategory = Intent.makeMainSelectorActivity(
            Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
        addResolvedPackages(pm, browserCategory, packages);

        Intent http = new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com/"));
        http.addCategory(Intent.CATEGORY_BROWSABLE);
        addResolvedPackages(pm, http, packages);

        Intent https = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/"));
        https.addCategory(Intent.CATEGORY_BROWSABLE);
        addResolvedPackages(pm, https, packages);

        Intent youtubeHttps = new Intent(
            Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        youtubeHttps.addCategory(Intent.CATEGORY_BROWSABLE);
        addResolvedPackages(pm, youtubeHttps, packages);

        Intent youtubeScheme = new Intent(
            Intent.ACTION_VIEW, Uri.parse("vnd.youtube://watch/dQw4w9WgXcQ"));
        youtubeScheme.addCategory(Intent.CATEGORY_BROWSABLE);
        addResolvedPackages(pm, youtubeScheme, packages);

        addInstalledYouTubeLikePackages(pm, packages);

        packages.remove(context.getPackageName());
        return packages;
    }

    private static void addInstalledYouTubeLikePackages(
            PackageManager pm, Set<String> packages) {
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.MATCH_ALL);
        for (ApplicationInfo app : apps) {
            String packageName = app.packageName == null ? "" : app.packageName;
            CharSequence labelText = app.loadLabel(pm);
            String label = labelText == null ? "" : labelText.toString();
            String searchable = (packageName + " " + label).toLowerCase(Locale.US);
            if ((searchable.contains("youtube")
                    || searchable.contains("yt music")
                    || searchable.contains("vanced")
                    || searchable.contains("revanced"))
                    && !searchable.contains("browserblock")) {
                packages.add(packageName);
            }
        }
    }

    private static void addResolvedPackages(
            PackageManager pm, Intent intent, Set<String> packages) {
        List<ResolveInfo> results = pm.queryIntentActivities(
            intent, PackageManager.MATCH_ALL);
        for (ResolveInfo result : results) {
            if (result.activityInfo != null) {
                packages.add(result.activityInfo.packageName);
            }
        }
    }
}
