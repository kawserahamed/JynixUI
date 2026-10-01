package io.javaui.samples;

import io.javaui.navigation.NavController;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UberAppTest {

    @Test
    void navigationTransitionsBetweenScreensCorrectly() {
        Scope scope = new Scope("TestApp", () -> {});
        scope.startPass();

        NavController nav = new NavController("home");
        assertThat(nav.getCurrentRoute()).isEqualTo("home");

        // Navigate to ride select
        nav.navigate("ride_select");
        assertThat(nav.getCurrentRoute()).isEqualTo("ride_select");

        // Navigate to active ride
        nav.navigate("active_ride");
        assertThat(nav.getCurrentRoute()).isEqualTo("active_ride");

        // Pop back to ride select
        boolean popped = nav.popBack();
        assertThat(popped).isTrue();
        assertThat(nav.getCurrentRoute()).isEqualTo("ride_select");

        // Pop back to home
        nav.popBack();
        assertThat(nav.getCurrentRoute()).isEqualTo("home");
    }

    @Test
    void allScreensMaterializeWithoutExceptions() {
        Scope scope = new Scope("AllScreensTest", () -> {});
        NavController nav = new NavController("home");

        String[] routes = {
                "home", "ride_select", "matching", "active_ride", "driver_profile",
                "fare_breakdown", "payments", "add_card", "history", "receipt",
                "safety", "scheduled", "profile", "notifications", "theme",
                "saved_places", "family", "eats", "support", "multi_stop", "promos"
        };

        assertThat(routes).hasSize(21);

        for (String r : routes) {
            scope.startPass();
            UI ui = switch (r) {
                case "home" -> UberApp.HomeScreen(scope, nav);
                case "ride_select" -> UberApp.RideSelectionScreen(scope, nav);
                case "matching" -> UberApp.RideMatchingScreen(scope, nav);
                case "active_ride" -> UberApp.ActiveRideTrackingScreen(scope, nav);
                case "driver_profile" -> UberApp.DriverProfileScreen(scope, nav);
                case "fare_breakdown" -> UberApp.FareBreakdownScreen(scope, nav);
                case "payments" -> UberApp.PaymentMethodsScreen(scope, nav);
                case "add_card" -> UberApp.AddCreditCardScreen(scope, nav);
                case "history" -> UberApp.TripHistoryScreen(scope, nav);
                case "receipt" -> UberApp.TripDetailReceiptScreen(scope, nav);
                case "safety" -> UberApp.SafetyToolkitScreen(scope, nav);
                case "scheduled" -> UberApp.ScheduledRidesScreen(scope, nav);
                case "profile" -> UberApp.UserProfileSettingsScreen(scope, nav);
                case "notifications" -> UberApp.NotificationSettingsScreen(scope, nav);
                case "theme" -> UberApp.DarkModePreferencesScreen(scope, nav);
                case "saved_places" -> UberApp.SavedPlacesScreen(scope, nav);
                case "family" -> UberApp.FamilyProfileScreen(scope, nav);
                case "eats" -> UberApp.UberEatsIntegrationScreen(scope, nav);
                case "support" -> UberApp.HelpSupportCenterScreen(scope, nav);
                case "multi_stop" -> UberApp.MultiStopRideConfigScreen(scope, nav);
                case "promos" -> UberApp.PromotionsAndDiscountsScreen(scope, nav);
                default -> throw new IllegalStateException();
            };

            UINode node = ui.materialize(scope);
            assertThat(node).isNotNull();
            assertThat(node.getType()).isNotEmpty();
        }
    }
}
