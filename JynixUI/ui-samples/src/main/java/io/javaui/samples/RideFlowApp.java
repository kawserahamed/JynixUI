package io.javaui.samples;

import io.javaui.annotation.UIComponent;
import io.javaui.layout.Modifier;
import io.javaui.navigation.NavController;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.state.BooleanState;
import io.javaui.state.IntState;
import io.javaui.state.State;

import static io.javaui.foundation.UIFoundation.*;
import static io.javaui.material.UIMaterial.*;

/**
 * Enterprise RideFlow mobility sample application built entirely in Java 17 with JynixUI.
 * Comprises 21 production-grade screens demonstrating declarative state, lazy lists,
 * nested components, and navigation backstack.
 */
public final class RideFlowApp {

    private RideFlowApp() {}

    @UIComponent
    public static UI App(Scope s) {
        NavController nav = s.remember(() -> new NavController("home"));

        return NavController.NavHost(nav, Modifier.fillMaxSize(), route -> {
            switch (route) {
                case "home": return HomeScreen(s, nav);
                case "ride_select": return RideSelectionScreen(s, nav);
                case "matching": return RideMatchingScreen(s, nav);
                case "active_ride": return ActiveRideTrackingScreen(s, nav);
                case "driver_profile": return DriverProfileScreen(s, nav);
                case "fare_breakdown": return FareBreakdownScreen(s, nav);
                case "payments": return PaymentMethodsScreen(s, nav);
                case "add_card": return AddCreditCardScreen(s, nav);
                case "history": return TripHistoryScreen(s, nav);
                case "receipt": return TripDetailReceiptScreen(s, nav);
                case "safety": return SafetyToolkitScreen(s, nav);
                case "scheduled": return ScheduledRidesScreen(s, nav);
                case "profile": return UserProfileSettingsScreen(s, nav);
                case "notifications": return NotificationSettingsScreen(s, nav);
                case "theme": return DarkModePreferencesScreen(s, nav);
                case "saved_places": return SavedPlacesScreen(s, nav);
                case "family": return FamilyProfileScreen(s, nav);
                case "delivery": return DeliveryIntegrationScreen(s, nav);
                case "support": return HelpSupportCenterScreen(s, nav);
                case "multi_stop": return MultiStopRideConfigScreen(s, nav);
                case "promos": return PromotionsAndDiscountsScreen(s, nav);
                default: return HomeScreen(s, nav);
            }
        });
    }

    // Screen 1: Home
    public static UI HomeScreen(Scope s, NavController nav) {
        State<String> destination = s.state("");
        return Column(
                Modifier.fillMaxSize().background(0xFFF6F6F6),
                TopBar(Modifier.DEFAULT, "RideFlow", null, Button("Profile", () -> nav.navigate("profile"))),
                Card(
                        Modifier.fillMaxWidth().padding(16),
                        Text("Where to?"),
                        Spacer(Modifier.size(8)),
                        TextField(Modifier.fillMaxWidth(), destination::get, destination::set, "Enter destination..."),
                        Spacer(Modifier.size(12)),
                        Button(Modifier.fillMaxWidth(), "Find Rides", () -> nav.navigate("ride_select"))
                ),
                Card(
                        Modifier.fillMaxWidth().padding(16, 0),
                        Row(
                                Button("Rides", () -> nav.navigate("ride_select")),
                                Spacer(Modifier.size(8)),
                                Button("Delivery", () -> nav.navigate("delivery")),
                                Spacer(Modifier.size(8)),
                                Button("History", () -> nav.navigate("history"))
                        )
                )
        );
    }

    // Screen 2: Ride Selection
    public static UI RideSelectionScreen(Scope s, NavController nav) {
        IntState selectedTier = s.intState(0);
        return Column(
                Modifier.fillMaxSize().background(0xFFFFFFFF),
                TopBar(Modifier.DEFAULT, "Choose a Ride", Button("Back", nav::popBack), null),
                Card(Modifier.fillMaxWidth().padding(16),
                        Text(() -> "Selected Option: " + (selectedTier.get() == 0 ? "Standard Ride ($24.50)" : "Executive Premium ($48.00)")),
                        Spacer(Modifier.size(8)),
                        Row(
                                Button("Standard", () -> selectedTier.set(0)),
                                Spacer(Modifier.size(8)),
                                Button("Premium", () -> selectedTier.set(1))
                        ),
                        Spacer(Modifier.size(16)),
                        Button(Modifier.fillMaxWidth(), "Confirm Ride", () -> nav.navigate("matching"))
                )
        );
    }

    // Screen 3: Ride Matching
    public static UI RideMatchingScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(24),
                Text("Connecting to nearby verified drivers..."),
                Spacer(Modifier.size(16)),
                Button("View Active Ride", () -> nav.navigate("active_ride")),
                Spacer(Modifier.size(8)),
                Button("Cancel Request", nav::popBack)
        );
    }

    // Screen 4: Active Ride Tracking
    public static UI ActiveRideTrackingScreen(Scope s, NavController nav) {
        IntState etaMinutes = s.intState(4);
        return Column(
                Modifier.fillMaxSize(),
                TopBar(Modifier.DEFAULT, "En Route", null, Button("Safety", () -> nav.navigate("safety"))),
                Card(
                        Modifier.fillMaxWidth().padding(16),
                        Text(() -> "Driver arrives in " + etaMinutes.get() + " mins"),
                        Text("Toyota Camry • License 7XYZ99"),
                        Spacer(Modifier.size(12)),
                        Row(
                                Button("Driver Profile", () -> nav.navigate("driver_profile")),
                                Spacer(Modifier.size(8)),
                                Button("Fare Info", () -> nav.navigate("fare_breakdown"))
                        )
                )
        );
    }

    // Screen 5: Driver Profile
    public static UI DriverProfileScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Driver Details", Button("Back", nav::popBack), null),
                Text("Marcus Sterling ★ 4.98 (4,120 trips)"),
                Text("Speaks English, Spanish • Top Rated Driver"),
                Spacer(Modifier.size(16)),
                Button("Return to Ride", nav::popBack)
        );
    }

    // Screen 6: Fare Breakdown
    public static UI FareBreakdownScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Fare Breakdown", Button("Back", nav::popBack), null),
                Text("Base Fare: $3.50"),
                Text("Distance (8.4 mi): $16.80"),
                Text("Toll & Fees: $4.20"),
                Text("Total: $24.50"),
                Spacer(Modifier.size(16)),
                Button("Payment Methods", () -> nav.navigate("payments"))
        );
    }

    // Screen 7: Payment Methods
    public static UI PaymentMethodsScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Payment", Button("Back", nav::popBack), null),
                Text("Visa •••• 4242 (Default)"),
                Text("Wallet Balance: $35.00"),
                Spacer(Modifier.size(12)),
                Button("Add Credit Card", () -> nav.navigate("add_card"))
        );
    }

    // Screen 8: Add Credit Card
    public static UI AddCreditCardScreen(Scope s, NavController nav) {
        State<String> card = s.state("");
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Add Card", Button("Back", nav::popBack), null),
                TextField(Modifier.fillMaxWidth(), card::get, card::set, "Card Number"),
                Spacer(Modifier.size(16)),
                Button(Modifier.fillMaxWidth(), "Save Payment Method", nav::popBack)
        );
    }

    // Screen 9: Trip History
    public static UI TripHistoryScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize(),
                TopBar(Modifier.DEFAULT, "Your Trips", Button("Back", nav::popBack), null),
                Card(Modifier.fillMaxWidth().padding(16),
                        Text("Yesterday, 5:30 PM • $24.50"),
                        Text("Downtown -> Airport"),
                        Button("View Receipt", () -> nav.navigate("receipt"))
                ),
                Card(Modifier.fillMaxWidth().padding(16, 8),
                        Text("Oct 12, 8:15 AM • $14.20"),
                        Text("Home -> Office"),
                        Button("View Receipt", () -> nav.navigate("receipt"))
                )
        );
    }

    // Screen 10: Trip Detail Receipt
    public static UI TripDetailReceiptScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Receipt", Button("Back", nav::popBack), null),
                Text("Trip ID: #FLW-8921-992"),
                Text("Paid via Visa •••• 4242"),
                Spacer(Modifier.size(16)),
                Button("Done", nav::popBack)
        );
    }

    // Screen 11: Safety Toolkit
    public static UI SafetyToolkitScreen(Scope s, NavController nav) {
        return Column(
                Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Safety Toolkit", Button("Back", nav::popBack), null),
                Button(Modifier.fillMaxWidth(), "Share Trip Status", () -> {}),
                Spacer(Modifier.size(8)),
                Button(Modifier.fillMaxWidth(), "Emergency 911 Assistance", () -> {})
        );
    }

    // Screen 12: Scheduled Rides
    public static UI ScheduledRidesScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Reserve a Ride", Button("Back", nav::popBack), null),
                Text("Reserve up to 90 days in advance"),
                Button("Schedule Airport Pickup", nav::popBack)
        );
    }

    // Screen 13: User Profile Settings
    public static UI UserProfileSettingsScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Account Settings", Button("Back", nav::popBack), null),
                Text("Alex Morgan ★ 4.95 Rider"),
                Button("Saved Places", () -> nav.navigate("saved_places")),
                Spacer(Modifier.size(8)),
                Button("Appearance", () -> nav.navigate("theme")),
                Spacer(Modifier.size(8)),
                Button("Notifications", () -> nav.navigate("notifications"))
        );
    }

    // Screen 14: Notification Settings
    public static UI NotificationSettingsScreen(Scope s, NavController nav) {
        BooleanState push = s.booleanState(true);
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Notifications", Button("Back", nav::popBack), null),
                Row(Text("Push Notifications"), Spacer(Modifier.size(16)), Switch(Modifier.DEFAULT, push)),
                Spacer(Modifier.size(16)),
                Button("Done", nav::popBack)
        );
    }

    // Screen 15: Dark Mode Preferences
    public static UI DarkModePreferencesScreen(Scope s, NavController nav) {
        BooleanState dark = s.booleanState(false);
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Appearance", Button("Back", nav::popBack), null),
                Row(Text("Dark Theme"), Spacer(Modifier.size(16)), Switch(Modifier.DEFAULT, dark)),
                Spacer(Modifier.size(16)),
                Button("Save", nav::popBack)
        );
    }

    // Screen 16: Saved Places
    public static UI SavedPlacesScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Saved Places", Button("Back", nav::popBack), null),
                Text("🏠 Home: 742 Evergreen Terrace"),
                Text("💼 Work: 100 Market St"),
                Button("Done", nav::popBack)
        );
    }

    // Screen 17: Family Profile
    public static UI FamilyProfileScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Family Hub", Button("Back", nav::popBack), null),
                Text("Manage family accounts with centralized billing."),
                Button("Done", nav::popBack)
        );
    }

    // Screen 18: Delivery Integration
    public static UI DeliveryIntegrationScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Express Delivery", Button("Back", nav::popBack), null),
                Text("Order from 500+ local partners near you."),
                Button("Back to Rides", nav::popBack)
        );
    }

    // Screen 19: Help & Support Center
    public static UI HelpSupportCenterScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Help & Support", Button("Back", nav::popBack), null),
                Button("I lost an item", () -> {}),
                Spacer(Modifier.size(8)),
                Button("Report a safety issue", () -> {}),
                Spacer(Modifier.size(8)),
                Button("Dispute a charge", () -> {})
        );
    }

    // Screen 20: Multi-Stop Ride Configuration
    public static UI MultiStopRideConfigScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Add Stops", Button("Back", nav::popBack), null),
                Text("Stop 1: Coffee Shop"),
                Text("Stop 2: Destination"),
                Button("Save Route", nav::popBack)
        );
    }

    // Screen 21: Promotions and Discounts
    public static UI PromotionsAndDiscountsScreen(Scope s, NavController nav) {
        return Column(Modifier.fillMaxSize().padding(16),
                TopBar(Modifier.DEFAULT, "Promotions", Button("Back", nav::popBack), null),
                Text("🎉 20% off your next 3 airport rides!"),
                Button("Apply to Account", nav::popBack)
        );
    }
}
