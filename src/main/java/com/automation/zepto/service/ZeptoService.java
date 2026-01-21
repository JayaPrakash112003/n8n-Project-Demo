package com.automation.zepto.service;

import com.automation.zepto.dto.OrderRequest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class ZeptoService {

    public String executeOrder(OrderRequest request) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // Headless for Render using .setHeadless(true)

            // Context might need storage state for login persistence
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("Navigating to Zepto Search...");
            // Navigate directly to search to avoid homepage trigger issues
            page.navigate("https://www.zepto.com/search");

            // Handle multiple products
            for (String productName : request.getProducts()) {
                System.out.println("Searching for: " + productName);

                // Use robust selector matching the placeholder "Search for over 5000 products"
                // Using a partial match locator for safety against number changes
                Locator searchInput = page.locator("input[placeholder*='Search for']");

                // Ensure it's visible
                searchInput.waitFor();
                searchInput.fill(productName);
                searchInput.press("Enter");

                page.waitForTimeout(3000); // Wait for results

                // Add first item if available
                System.out.println("Adding " + productName + " to cart...");

                // Robust selector for ADD button
                // Use "ADD" text which is standard on their buttons
                Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD")).first();

                if (addButton.isVisible()) {
                    addButton.click();
                    System.out.println("Added " + productName);
                } else {
                    System.out.println("Could not find ADD button for " + productName);
                }

                // Re-navigating to search page for next item
                if (request.getProducts().indexOf(productName) < request.getProducts().size() - 1) {
                    page.navigate("https://www.zepto.com/search");
                }
            }

            return "Order process initiated for products: " + request.getProducts()
                    + ". (Note: Checkout flow pending login)";
        }
    }
}
