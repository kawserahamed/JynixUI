package io.javaui.samples;

import io.javaui.navigation.NavController;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RideFlowAppTest {

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
                "saved_places", "family", "delivery", "support", "multi_stop", "promos"
        };

        assertThat(routes).hasSize(21);

        for (String r : routes) {
            scope.startPass();
            UI ui = switch (r) {
                case "home" -> RideFlowApp.HomeScreen(scope, nav);
                case "ride_select" -> RideFlowApp.RideSelectionScreen(scope, nav);
                case "matching" -> RideFlowApp.RideMatchingScreen(scope, nav);
                case "active_ride" -> RideFlowApp.ActiveRideTrackingScreen(scope, nav);
                case "driver_profile" -> RideFlowApp.DriverProfileScreen(scope, nav);
                case "fare_breakdown" -> RideFlowApp.FareBreakdownScreen(scope, nav);
                case "payments" -> RideFlowApp.PaymentMethodsScreen(scope, nav);
                case "add_card" -> RideFlowApp.AddCreditCardScreen(scope, nav);
                case "history" -> RideFlowApp.TripHistoryScreen(scope, nav);
                case "receipt" -> RideFlowApp.TripDetailReceiptScreen(scope, nav);
                case "safety" -> RideFlowApp.SafetyToolkitScreen(scope, nav);
                case "scheduled" -> RideFlowApp.ScheduledRidesScreen(scope, nav);
                case "profile" -> RideFlowApp.UserProfileSettingsScreen(scope, nav);
                case "notifications" -> RideFlowApp.NotificationSettingsScreen(scope, nav);
                case "theme" -> RideFlowApp.DarkModePreferencesScreen(scope, nav);
                case "saved_places" -> RideFlowApp.SavedPlacesScreen(scope, nav);
                case "family" -> RideFlowApp.FamilyProfileScreen(scope, nav);
                case "delivery" -> RideFlowApp.DeliveryIntegrationScreen(scope, nav);
                case "support" -> RideFlowApp.HelpSupportCenterScreen(scope, nav);
                case "multi_stop" -> RideFlowApp.MultiStopRideConfigScreen(scope, nav);
                case "promos" -> RideFlowApp.PromotionsAndDiscountsScreen(scope, nav);
                default -> throw new IllegalStateException();
            };

            UINode node = ui.materialize(scope);
            assertThat(node).isNotNull();
            assertThat(node.getType()).isNotEmpty();
        }
    }
}
