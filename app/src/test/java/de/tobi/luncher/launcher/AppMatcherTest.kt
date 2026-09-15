package de.tobi.luncher.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppMatcherTest {

    private val telegram = app("Telegram", "org.telegram.messenger")
    private val browser = app("Samsung Internet Browser", "com.sec.android.app.sbrowser")
    private val buero = app("Büro", "com.example.buero")
    private val kamera = app("Kamera", "com.example.kamera")

    private val apps = listOf(telegram, browser, buero, kamera)

    private fun app(label: String, pkg: String) =
        InstalledApp(label = label, packageName = pkg, activityName = "$pkg.MainActivity")

    @Test
    fun `exakter Treffer`() {
        assertEquals(telegram, AppMatcher.match("Telegram", apps, emptyMap()))
    }

    @Test
    fun `Grossschreibung egal`() {
        assertEquals(telegram, AppMatcher.match("TELEGRAM", apps, emptyMap()))
        assertEquals(telegram, AppMatcher.match("telegram", apps, emptyMap()))
        assertEquals(telegram, AppMatcher.match("tElEgRaM", apps, emptyMap()))
    }

    @Test
    fun `fuehrende und nachfolgende Leerzeichen werden getrimmt`() {
        assertEquals(telegram, AppMatcher.match("   telegram  ", apps, emptyMap()))
    }

    @Test
    fun `Mehrfach-Leerzeichen werden zusammengezogen`() {
        assertEquals(browser, AppMatcher.match("samsung   internet    browser", apps, emptyMap()))
    }

    @Test
    fun `Umlaute bleiben erhalten`() {
        assertEquals(buero, AppMatcher.match("büro", apps, emptyMap()))
        assertNull(AppMatcher.match("buero", apps, emptyMap()))
        assertNull(AppMatcher.match("buro", apps, emptyMap()))
    }

    @Test
    fun `Grossschreibung mit Umlaut`() {
        assertEquals(buero, AppMatcher.match("BÜRO", apps, emptyMap()))
    }

    @Test
    fun `kein Praefix-Matching`() {
        assertNull(AppMatcher.match("tele", apps, emptyMap()))
        assertNull(AppMatcher.match("samsung", apps, emptyMap()))
    }

    @Test
    fun `kein Teilstring-Matching`() {
        assertNull(AppMatcher.match("internet", apps, emptyMap()))
    }

    @Test
    fun `kein Fuzzy-Matching`() {
        assertNull(AppMatcher.match("telegrma", apps, emptyMap()))
        assertNull(AppMatcher.match("telegramm", apps, emptyMap()))
    }

    @Test
    fun `leere Eingabe trifft nichts`() {
        assertNull(AppMatcher.match("", apps, emptyMap()))
        assertNull(AppMatcher.match("    ", apps, emptyMap()))
    }

    @Test
    fun `unbekannter Name trifft nichts`() {
        assertNull(AppMatcher.match("instagram", apps, emptyMap()))
    }

    @Test
    fun `Alias trifft`() {
        val aliases = mapOf("netz" to browser.packageName)
        assertEquals(browser, AppMatcher.match("netz", apps, aliases))
    }

    @Test
    fun `Alias wird normalisiert verglichen`() {
        val aliases = mapOf("Mein  Netz" to browser.packageName)
        assertEquals(browser, AppMatcher.match("  mein netz ", apps, aliases))
    }

    @Test
    fun `Alias schlaegt gleichnamiges Label`() {
        val aliases = mapOf("Kamera" to telegram.packageName)
        assertEquals(telegram, AppMatcher.match("kamera", apps, aliases))
    }

    @Test
    fun `Alias auf deinstalliertes Paket wird ignoriert`() {
        val aliases = mapOf("kamera" to "com.example.geloescht")
        assertEquals(kamera, AppMatcher.match("kamera", apps, aliases))
    }

    @Test
    fun `Alias auf deinstalliertes Paket ohne Label-Treffer ergibt null`() {
        val aliases = mapOf("netz" to "com.example.geloescht")
        assertNull(AppMatcher.match("netz", apps, aliases))
    }

    @Test
    fun `bei mehrdeutigem Label gewinnt der erste Eintrag`() {
        val ersteUhr = app("Uhr", "com.example.uhr.eins")
        val zweiteUhr = app("uhr", "com.example.uhr.zwei")
        val mitDoppel = apps + ersteUhr + zweiteUhr
        assertEquals(ersteUhr, AppMatcher.match("Uhr", mitDoppel, emptyMap()))
    }

    @Test
    fun `leere App-Liste trifft nichts`() {
        assertNull(AppMatcher.match("telegram", emptyList(), emptyMap()))
    }

    @Test
    fun `normalize zieht Tabs und Zeilenumbrueche zusammen`() {
        assertEquals("a b", AppMatcher.normalize("  a \t\n b  "))
    }
}
