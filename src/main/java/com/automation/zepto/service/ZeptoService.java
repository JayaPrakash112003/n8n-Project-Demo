package com.automation.zepto.service;

import com.automation.zepto.dto.OrderRequest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ZeptoService {

    private static final String DEFAULT_ADDRESS = "Vijaya Nagar - 1st Main Road, Vijaya Nagar, Chennai, Tamil Nadu";

    @Async
    public void executeOrder(OrderRequest request) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // Headless is true for Render

            // Create context with geolocation permissions if possible, though strict
            // address selection is better
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")); // Use
                                                                                                                                                 // standard
                                                                                                                                                 // User
                                                                                                                                                 // Agent

            Page page = context.newPage();
            // increase default timeout to 60s for Render network/cpu lag
            page.setDefaultTimeout(60000);

            System.out.println("Navigating to Zepto Home...");
            page.navigate("https://www.zepto.com/", new Page.NavigateOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // 1. Handle Location Explicitly
            handleLocation(page);

            System.out.println("Navigating to Zepto Search...");
            page.navigate("https://www.zepto.com/search", new Page.NavigateOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // 2. Search for Products
            for (String productName : request.getProducts()) {
                System.out.println("Searching for: " + productName);

                // Try multiple selectors for the search bar
                Locator searchInput = page.locator("input[placeholder*='Search for']").first();
                if (!searchInput.isVisible()) {
                    System.out.println("Standard search input not visible. Checking for fallback...");
                    // Sometimes it's a 'combobox' without exact placeholder
                    searchInput = page.getByRole(AriaRole.COMBOBOX).first();
                }

                // Explicit wait
                searchInput.waitFor();
                searchInput.fill(productName);
                searchInput.press("Enter");

                // Wait for results grid
                try {
                    page.locator("button:has-text('ADD')").first()
                            .waitFor(new Locator.WaitForOptions().setTimeout(10000));
                } catch (TimeoutError e) {
                    System.out.println("Warning: No 'ADD' buttons found immediately. Results might be empty.");
                }

                System.out.println("Adding " + productName + " to cart...");

                // Robust selector for ADD button
                Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD")).first();

                if (addButton.isVisible()) {
                    addButton.click();
                    System.out.println("Clicked ADD for " + productName);
                } else {
                    System.out.println("Could not find ADD button for " + productName);
                }

                // Return to search for next item
                if (request.getProducts().indexOf(productName) < request.getProducts().size() - 1) {
                    page.navigate("https://www.zepto.com/search");
                }
            }

            System.out.println("Order process completed for products: " + request.getProducts());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during automation: " + e.getMessage());
        }
    }

    private void handleLocation(Page page) {
        System.out.println("Checking Location settings...");
        try {
            // Check if location input is already there (Modal Open)
            Locator locationInput = page.locator("input[placeholder*='Search a new address']").first();

            if (!locationInput.isVisible()) {
                System.out.println("Location modal not open. Attempting to open...");
                // Click header location button.
                // Selector strategy: Button in header that is likely the location picker.
                // Usually has 'Select Location' or current location text.
                Locator locationBtn = page.locator("button[data-testid='user-address-container']").first(); // Try
                                                                                                            // testid
                                                                                                            // first if
                                                                                                            // exists
                                                                                                            // (guess)
                if (!locationBtn.isVisible()) {
                    locationBtn = page.locator("button")
                            .filter(new Locator.FilterOptions().setHasText("Select Location")).first();
                }
                if (!locationBtn.isVisible()) {
                    // Fallback: any button in header area?
                    // Let's try the subagent's finding: aria-label
                    locationBtn = page.locator("button[aria-label*='Select Location']").first();
                }

                if (locationBtn.isVisible()) {
                    locationBtn.click();
                    System.out.println("Clicked Location Header Button.");
                }
            }

            // Now wait for input
            locationInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            System.out.println("Setting address: " + DEFAULT_ADDRESS);
            locationInput.fill(DEFAULT_ADDRESS);

            // Wait for suggestions
            System.out.println("Waiting for suggestions...");
            Locator suggestion = page.locator("div[data-testid='address-search-suggestion']").first();
            suggestion.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            System.out.println("Clicking suggestion: " + suggestion.innerText());
            suggestion.click();

            // Wait for modal to close or Page to update
            // We can wait for the location input to disappear
            locationInput.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(10000));
            System.out.println("Location set successfully.");

            page.waitForTimeout(2000); // Breathe

        } catch (Exception e) {
            System.out.println("Location Setup Warning: " + e.getMessage());
            // Don't throw, try to proceed in case it was already set
        }
    }
}
