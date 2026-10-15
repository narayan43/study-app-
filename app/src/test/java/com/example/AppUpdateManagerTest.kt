package com.example

import com.example.util.AppUpdateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AppUpdateManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testVersionComparison_higherCodeIsNewer() {
        assertTrue(
            AppUpdateManager.isRemoteVersionNewer(
                remoteCode = 105,
                remoteName = "1.0.3",
                localCode = 103,
                localName = "1.0.3"
            )
        )
    }

    @Test
    fun testVersionComparison_lowerCodeIsNotNewer() {
        assertFalse(
            AppUpdateManager.isRemoteVersionNewer(
                remoteCode = 102,
                remoteName = "1.0.5",
                localCode = 103,
                localName = "1.0.3"
            )
        )
    }

    @Test
    fun testVersionComparison_equalCodeFallsBackToSemver() {
        // Equal code, higher semver
        assertTrue(
            AppUpdateManager.isRemoteVersionNewer(
                remoteCode = 103,
                remoteName = "1.0.4",
                localCode = 103,
                localName = "1.0.3"
            )
        )

        // Equal code, equal semver
        assertFalse(
            AppUpdateManager.isRemoteVersionNewer(
                remoteCode = 103,
                remoteName = "1.0.3",
                localCode = 103,
                localName = "1.0.3"
            )
        )

        // Equal code, lower semver
        assertFalse(
            AppUpdateManager.isRemoteVersionNewer(
                remoteCode = 103,
                remoteName = "1.0.2",
                localCode = 103,
                localName = "1.0.3"
            )
        )
    }

    @Test
    fun testCompareSemver() {
        assertTrue(AppUpdateManager.compareSemver("1.0.4", "1.0.3") > 0)
        assertTrue(AppUpdateManager.compareSemver("1.1.0", "1.0.9") > 0)
        assertTrue(AppUpdateManager.compareSemver("2.0.0", "1.9.9") > 0)
        assertEquals(0, AppUpdateManager.compareSemver("1.0.3", "1.0.3"))
        assertTrue(AppUpdateManager.compareSemver("1.0.2", "1.0.3") < 0)
    }

    @Test
    fun testIsValidUrl_rejectsInsecureOrUntrusted() {
        // Valid HTTPS GitHub URLs
        assertTrue(AppUpdateManager.isValidUrl("https://github.com/narayan43/study-app-/releases/download/v1.0.4/ExamPrep-1.0.4.apk"))
        assertTrue(AppUpdateManager.isValidUrl("https://api.github.com/repos/narayan43/study-app-/releases/latest"))
        assertTrue(AppUpdateManager.isValidUrl("https://objects.githubusercontent.com/github-production-release-asset-2e65be/12345/abc"))
        assertTrue(AppUpdateManager.isValidUrl("https://raw.githubusercontent.com/narayan43/study-app-/main/version.json"))

        // Insecure HTTP rejected
        assertFalse(AppUpdateManager.isValidUrl("http://github.com/narayan43/study-app-/releases/latest"))
        assertFalse(AppUpdateManager.isValidUrl("http://objects.githubusercontent.com/asset.apk"))

        // Untrusted domains rejected
        assertFalse(AppUpdateManager.isValidUrl("https://evil.com/fake.apk"))
        assertFalse(AppUpdateManager.isValidUrl("https://github.com.attacker.com/download.apk"))
        assertFalse(AppUpdateManager.isValidUrl("ftp://github.com/asset"))
        assertFalse(AppUpdateManager.isValidUrl(""))
    }

    @Test
    fun testSha256CalculationAndVerification() {
        val testFile = tempFolder.newFile("test_sample.txt")
        testFile.writeText("ExamPrep APK verification test content\n")

        val hash = AppUpdateManager.calculateSha256(testFile)
        assertEquals(64, hash.length)

        // Known SHA-256 for identical content must match
        val hash2 = AppUpdateManager.calculateSha256(testFile)
        assertEquals(hash, hash2)
    }

    @Test
    fun testParseVersionJson() {
        val jsonStr = """
            {
              "versionCode": 104,
              "versionName": "1.0.4",
              "apkFileName": "ExamPrep-1.0.4.apk",
              "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            }
        """.trimIndent()

        val parsed = AppUpdateManager.parseVersionJson(jsonStr)
        assertEquals(104, parsed.versionCode)
        assertEquals("1.0.4", parsed.versionName)
        assertEquals("ExamPrep-1.0.4.apk", parsed.apkFileName)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", parsed.sha256)
    }

    @Test
    fun testExtractSha256FromText() {
        val standardOutput = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  ExamPrep-1.0.4.apk\n"
        val extracted = AppUpdateManager.extractSha256FromText(standardOutput)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", extracted)

        val rawHash = "A3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"
        assertEquals("a3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", AppUpdateManager.extractSha256FromText(rawHash))

        assertEquals("", AppUpdateManager.extractSha256FromText("invalid_hash"))
    }
}
