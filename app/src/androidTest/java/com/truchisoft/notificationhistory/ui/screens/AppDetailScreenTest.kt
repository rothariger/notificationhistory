package com.truchisoft.notificationhistory.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performLongClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truchisoft.notificationhistory.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Assume this is a package name that will have notifications on the test device
    // This is a common app that might exist. Tests might be flaky if not.
    private val targetAppPackageNameForTest = "com.android.settings" 
    private val targetAppNameForTest = "Settings" // Or whatever it's displayed as

    @Before
    fun navigateToAppDetailScreen() {
        // This is a simplified navigation.
        // It assumes AppListScreen is the first screen and items are identifiable.
        // We are looking for a common app like "Settings" or any app that is likely to have notifications.
        // This part is highly dependent on the actual AppListScreen implementation.
        // Wait for the app list to load - use a timeout
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(targetAppNameForTest, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(targetAppNameForTest, ignoreCase = true).performClick()
        } catch (e: AssertionError) {
            // Fallback: Try clicking the first available app if "Settings" is not found or has no notifications
            // This is very brittle. A proper test would use test tags or ensure data exists.
            System.err.println("Warning: Target app '$targetAppNameForTest' not found or timed out. Attempting to click first app.")
            try {
                 composeTestRule.waitUntil(timeoutMillis = 5000) {
                    // A more generic way to find an app item if specific text fails
                    // This assumes app items might have a "app_icon" content description or similar
                    composeTestRule.onAllNodesWithContentDescription("App icon", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                composeTestRule.onAllNodesWithContentDescription("App icon", substring = true)[0].performClick()
                System.err.println("Clicked on the first app found as a fallback.")
            } catch (e2: Exception) {
                System.err.println("Critical: Could not navigate to any AppDetailScreen. Tests will likely fail. ${e2.message}")
                // This test will likely fail if navigation doesn't happen.
            }
        }

        // Wait for AppDetailScreen to load by checking for a common element
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithText("Mostrar solo no leídos").isDisplayed()
        }
    }

    private fun findNotificationCards() = composeTestRule.onAllNodesWithText("Sin Título", substring = true)
    private fun findCheckboxes() = composeTestRule.onAllNodesWithContentDescription("Checkbox", substring = true) // Assuming Checkbox has this CD

    @Test
    fun testEnterSelectionMode_And_VerifyAppBarAndCheckboxes() {
        // Ensure there's at least one notification to long-press
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            findNotificationCards().fetchSemanticsNodes().isNotEmpty()
        }
        if (findNotificationCards().fetchSemanticsNodes().isEmpty()) {
            System.err.println("Warning: No notifications found for testEnterSelectionMode. Skipping test.")
            return
        }

        // 1. Long-press on the first notification item
        findNotificationCards()[0].performLongClick()

        // 2. Verify that the top app bar changes
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Seleccionar todo").assertIsDisplayed()
        // Check that the title shows "1 seleccionada" (or similar for one item selected)
        composeTestRule.onNodeWithText("1 seleccionada", substring = true).assertIsDisplayed()


        // 3. Verify that checkboxes are displayed for notification items
        // This is tricky without unique tags. We'll assume if one checkbox is shown, others are too.
        // A more robust way would be to use test tags on the Checkbox within NotificationCard.
        // For now, check if at least one checkbox is present.
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            findCheckboxes().fetchSemanticsNodes().isNotEmpty()
        }
        findCheckboxes()[0].assertIsDisplayed()
    }

    @Test
    fun testSelectAndDeselectItems() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            findNotificationCards().fetchSemanticsNodes().size >= 2 // Need at least 2 items
        }
         if (findNotificationCards().fetchSemanticsNodes().size < 2) {
            System.err.println("Warning: Less than 2 notifications found for testSelectAndDeselectItems. Skipping test.")
            return
        }

        // Enter selection mode by long-clicking the first item
        findNotificationCards()[0].performLongClick()
        composeTestRule.onNodeWithText("1 seleccionada", substring = true).assertIsDisplayed() // Initial selection

        // Tap on the second notification item to select it
        findNotificationCards()[1].performClick()
        composeTestRule.onNodeWithText("2 seleccionadas", substring = true).assertIsDisplayed()

        // Verify checkboxes (assuming test tags are not used, so we rely on finding them)
        // This is not directly testing which checkbox corresponds to which item easily without tags.
        // However, the selection count is a good indicator.

        // Tap on the first selected item to deselect it
        findNotificationCards()[0].performClick()
        composeTestRule.onNodeWithText("1 seleccionada", substring = true).assertIsDisplayed()

        // Tap on the second selected item to deselect it (now only one should be selected)
        findNotificationCards()[1].performClick() // This should deselect the last item
        // After deselecting the last item, selection mode should exit
        composeTestRule.onNodeWithText("Mostrar solo no leídos").assertIsDisplayed() // Check for normal app bar element
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertDoesNotExist()
    }


    @Test
    fun testSelectAllAndDeselectAll() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            findNotificationCards().fetchSemanticsNodes().isNotEmpty()
        }
        val notificationCount = findNotificationCards().fetchSemanticsNodes().size
        if (notificationCount == 0) {
            System.err.println("Warning: No notifications found for testSelectAllAndDeselectAll. Skipping test.")
            return
        }

        findNotificationCards()[0].performLongClick() // Enter selection mode

        // Tap "Select All"
        composeTestRule.onNodeWithContentDescription("Seleccionar todo").performClick()
        composeTestRule.onNodeWithText("$notificationCount seleccionada", substring = true).assertIsDisplayed()
        // Verify "Deselect All" button is now shown (content description might change)
        composeTestRule.onNodeWithContentDescription("Deseleccionar todo").assertIsDisplayed()


        // Tap "Deselect All"
        composeTestRule.onNodeWithContentDescription("Deseleccionar todo").performClick()
        // After deselecting all, selection mode should exit
        composeTestRule.onNodeWithText("Mostrar solo no leídos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertDoesNotExist()
    }

    @Test
    fun testBatchDeletion() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            findNotificationCards().fetchSemanticsNodes().size >= 2
        }
        val initialCount = findNotificationCards().fetchSemanticsNodes().size
         if (initialCount < 2) {
            System.err.println("Warning: Less than 2 notifications found for testBatchDeletion. Skipping test.")
            return
        }

        // Select first two items
        findNotificationCards()[0].performLongClick() // Selects first and enters mode
        findNotificationCards()[1].performClick()    // Selects second
        composeTestRule.onNodeWithText("2 seleccionadas", substring = true).assertIsDisplayed()

        // Tap "Delete"
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").performClick()

        // Verify selection mode is exited
        composeTestRule.onNodeWithText("Mostrar solo no leídos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertDoesNotExist()

        // Verify items are removed - count should decrease by 2
        // This check can be flaky if other notifications arrive during the test.
        // A more robust way is to check for specific items if they have unique identifiers accessible to tests.
        composeTestRule.waitUntil(timeoutMillis = 3000) { // Wait for UI to update
            findNotificationCards().fetchSemanticsNodes().size == initialCount - 2
        }
        findNotificationCards().assertCountEquals(initialCount - 2)
    }

    @Test
    fun testExitSelectionModeWithBackPress() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            findNotificationCards().fetchSemanticsNodes().isNotEmpty()
        }
         if (findNotificationCards().fetchSemanticsNodes().isEmpty()) {
            System.err.println("Warning: No notifications found for testExitSelectionModeWithBackPress. Skipping test.")
            return
        }

        // Enter selection mode
        findNotificationCards()[0].performLongClick()
        composeTestRule.onNodeWithText("1 seleccionada", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertIsDisplayed()

        // Press back button
        Espresso.pressBack()

        // Verify selection mode is exited
        composeTestRule.onNodeWithText("Mostrar solo no leídos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Eliminar seleccionadas").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Seleccionar todo").assertDoesNotExist()
    }
}
