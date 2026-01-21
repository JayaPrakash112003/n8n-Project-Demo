package com.automation.zepto.service;

import com.automation.zepto.dto.OrderRequest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Service;

@Service
public class ZeptoService {

    private static final String DEFAULT_ADDRESS = "Vijaya Nagar - 1st Main Road, Vijaya Nagar, Chennai, Tamil Nadu";

    public String executeOrder(OrderRequest request) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // Headless for Render

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("Navigating to Zepto Home...");
            page.navigate("https://www.zepto.com/");

            // Handle Location Setting First
            handleLocation(page);

            System.out.println("Navigating to Zepto Search...");
            page.navigate("https://www.zepto.com/search");

            // Handle multiple products
            for (String productName : request.getProducts()) {
                System.out.println("Searching for: " + productName);

                Locator searchInput = page.locator("input[placeholder*='Search for']");

                // Allow some time for redirection/load after location set
                try {
                    searchInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
                } catch (TimeoutError e) {
                    System.out.println("Search input not found immediately. Retrying navigation...");
                    page.navigate("https://www.zepto.com/search");
                    searchInput.waitFor();
                }

                searchInput.fill(productName);
                searchInput.press("Enter");

                page.waitForTimeout(3000); // Wait for results

                System.out.println("Adding " + productName + " to cart...");

                // Robust selector for ADD button
                Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD")).first();

                if (addButton.isVisible()) {
                    addButton.click();
                    System.out.println("Added " + productName);
                } else {
                    System.out.println("Could not find ADD button for " + productName);
                }

                // Prepare for next item
                if (request.getProducts().indexOf(productName) < request.getProducts().size() - 1) {
                    page.navigate("https://www.zepto.com/search");
                }
            }

            return "Order process initiated for products: " + request.getProducts() + ". (Location set to: "
                    + DEFAULT_ADDRESS + ")";
        }
    }

    private void handleLocation(Page page) {
        System.out.println("Checking Location settings...");
        try {
            // Check if we need to select location (Look for 'Select Location' button or
            // check if modal is open)
            // In a fresh session, it usually prompts or shows 'Select Location' in header
            Locator locationButton = page.locator("button[aria-label*='Select Location']");

            if (locationButton.isVisible()) {
                System.out.println("Clicking 'Select Location'...");
                locationButton.click();
            } else {
                System.out.println("Location button not found or location already set.");
                // Check if 'Type your location' input is already visible (Modal open)
            }

            Locator locationInput = page.locator("input[placeholder='Search a new address']");

            // If input is not visible, maybe we need to wait or it's a different flow
            if (!locationInput.isVisible()) {
                // Try generic placeholder if specific one fails
                locationInput = page.locator("input[placeholder*='Search']").first();
            }

            if (locationInput.isVisible()) {
                System.out.println("Setting location to: " + DEFAULT_ADDRESS);
                locationInput.fill(DEFAULT_ADDRESS);

                // Wait for suggestions
                page.waitForTimeout(2000);

                // Click first suggestion
                Locator firstSuggestion = page.locator("div[data-testid='address-search-suggestion']").first();
                if (!firstSuggestion.isVisible()) {
                    // Fallback to text match if testid missing
                    firstSuggestion = page.locator("span")
                            .filter(new Locator.FilterOptions().setHasText("Vijaya Nagar")).first();
                }

                if (firstSuggestion.isVisible()) {
                    firstSuggestion.click();
                    System.out.println("Location selected.");
                    page.waitForTimeout(2000); // Wait for location to apply
                } else {
                    System.out.println("No address suggestions found.");
                }
            }

            // Check for Blocking Login Drawer (Enter Phone Number)
            Locator phoneInput = page.locator("input[placeholder='Enter Phone Number']");
            if (phoneInput.isVisible()) {
                System.out.println("Login drawer detected. Attempting to dismiss or ignore...");
                // Try clicking outside or verifying if we can navigate away
                // Usually direct navigation to /search works if location cookie is set
            }

        } catch (Exception e) {
            System.out.println("Error handling location: " + e.getMessage());
            // Continue assuming it might work or we can search anyway
        }
    }
}
