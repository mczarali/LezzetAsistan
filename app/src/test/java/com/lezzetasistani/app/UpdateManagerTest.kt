package com.lezzetasistani.app

import com.lezzetasistani.app.update.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateManagerTest {

    private lateinit var updateManager: UpdateManager

    @Before
    fun setUp() {
        updateManager = UpdateManager()
    }

    @Test
    fun `isNewerVersion returns true when latest version is higher`() {
        val method = UpdateManager::class.java.getDeclaredMethod(
            "isNewerVersion",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val isNewer = method.invoke(updateManager, "1.0.0", "1.0.1") as Boolean
        assertTrue(isNewer)
    }

    @Test
    fun `isNewerVersion returns false when latest version is same or lower`() {
        val method = UpdateManager::class.java.getDeclaredMethod(
            "isNewerVersion",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val isNewerSame = method.invoke(updateManager, "1.0.0", "1.0.0") as Boolean
        assertFalse(isNewerSame)

        val isNewerLower = method.invoke(updateManager, "1.1.0", "1.0.0") as Boolean
        assertFalse(isNewerLower)
    }
}
